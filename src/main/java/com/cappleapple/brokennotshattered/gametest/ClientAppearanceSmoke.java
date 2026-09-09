package com.cappleapple.brokennotshattered.gametest;

import com.cappleapple.brokennotshattered.BrokenNotShattered;
import com.cappleapple.brokennotshattered.registry.ModDataComponents;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.client.player.RemotePlayer;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.armortrim.TrimMaterials;
import net.minecraft.world.item.armortrim.TrimPatterns;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/** Development-only real-client rendering gate; excluded from the published JAR with all GameTests. */
@EventBusSubscriber(modid = BrokenNotShattered.MOD_ID, value = Dist.CLIENT)
public final class ClientAppearanceSmoke {
    private static boolean started;
    private static boolean complete;
    private static int waitTicks;
    private static int frames;
    private static int phase;
    private static java.util.Map<?, ?> firstFrameTextures;
    private static final Path OUTPUT = Path.of("../build/client-smoke");

    private ClientAppearanceSmoke() {}

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        if (!Boolean.getBoolean("broken_not_shattered.clientSmoke") || complete) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof Gallery gallery && phase == 4) gallery.tickAttack();
        if (!started && mc.screen instanceof TitleScreen && mc.getOverlay() == null) {
            started = true;
            mc.options.guiScale().set(1);
            mc.options.renderDistance().set(2);
            mc.options.glintSpeed().set(0.0); // Freeze only the glint's animation for exact pixel comparisons.
            mc.resizeDisplay();
            mc.createWorldOpenFlows().createFreshLevel("wear-smoke-" + System.currentTimeMillis(),
                new LevelSettings("Wear smoke", GameType.CREATIVE, false, Difficulty.PEACEFUL, true,
                    new GameRules(), WorldDataConfiguration.DEFAULT),
                new WorldOptions(42, false, false),
                registries -> registries.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT)
                    .value().createWorldDimensions(), mc.screen);
        } else if (started && mc.level != null && mc.player != null && !(mc.screen instanceof Gallery)
            && mc.getOverlay() == null && ++waitTicks > 30 && phase == 0) {
            mc.setScreen(new Gallery());
        }
    }

    @SubscribeEvent
    public static void afterFrame(RenderFrameEvent.Post event) {
        if (!Boolean.getBoolean("broken_not_shattered.clientSmoke") || complete) return;
        Minecraft mc = Minecraft.getInstance();
        if (!(mc.screen instanceof Gallery gallery) || mc.getOverlay() != null || ++frames < 5) return;
        try {
            Files.createDirectories(OUTPUT);
            try (NativeImage screenshot = Screenshot.takeScreenshot(mc.getMainRenderTarget())) {
                screenshot.writeToFile(OUTPUT.resolve("appearance-" + phase + ".png"));
                if ((phase == 1 || phase == 4) && frames == 5) {
                    screenshot.writeToFile(OUTPUT.resolve("held-first-frame.png"));
                    firstFrameTextures = generatedTextures();
                    return;
                }
                if ((phase == 1 || phase == 4) && frames < 35) return;
                if (phase == 1 || phase == 4) {
                    if (!firstFrameTextures.equals(generatedTextures())) {
                        throw new AssertionError("Generated textures were replaced between unchanged renders");
                    }
                    if (phase == 1) try (var stream = Files.newInputStream(OUTPUT.resolve("held-first-frame.png"));
                         NativeImage first = NativeImage.read(stream)) {
                        gallery.verifyRegion(first, screenshot, 0, 640, widthFor(mc) / 4, 700, "Held item changed between frames");
                    }
                }
                if (phase == 1 || phase == 3) gallery.verifyBroken(screenshot);
                if (phase == 3) {
                    try (var stream = Files.newInputStream(OUTPUT.resolve("appearance-1.png"));
                         NativeImage beforeReload = NativeImage.read(stream)) {
                        gallery.verifyRegion(beforeReload, screenshot, 0, 50, widthFor(mc), 145,
                            "Saved pattern changed after item/resource reload");
                    }
                }
                if (phase == 2) {
                    try (var stream = Files.newInputStream(OUTPUT.resolve("appearance-0.png"));
                         NativeImage original = NativeImage.read(stream)) {
                        gallery.verifyRepair(original, screenshot);
                    }
                }
            }
            int lastPhase = Boolean.getBoolean("broken_not_shattered.smokeAnimationCompat") ? 5 : 3;
            if (phase < lastPhase) {
                phase++;
                frames = 0;
                if (phase == 3) {
                    gallery.reloadItems();
                    mc.reloadResourcePacks();
                }
                if (phase == 4) gallery.startAttack();
            } else {
                Files.writeString(OUTPUT.resolve("result.txt"),
                    "PASS: real client renders distinct broken tools, equipped player armor, and elytra; temporary held copies reuse texture objects and stay pixel-identical across 30 frames; repair restores original pixels; resource reload succeeds. Armor layer: "
                        + System.getProperty("broken_not_shattered.smokeArmorLayer") + "; armor set: "
                        + System.getProperty("broken_not_shattered.smokeArmorSet", "") + "; enchanted: "
                        + Boolean.getBoolean("broken_not_shattered.smokeEnchanted") + "; animation compatibility: "
                        + Boolean.getBoolean("broken_not_shattered.smokeAnimationCompat"));
                complete = true;
                mc.stop();
            }
        } catch (Throwable failure) {
            complete = true;
            BrokenNotShattered.LOGGER.error("Client appearance smoke failed", failure);
            try {
                Files.writeString(OUTPUT.resolve("result.txt"), "FAIL: " + failure);
            } catch (Exception ignored) {}
            mc.stop();
        }
    }

    private static int widthFor(Minecraft mc) {
        return mc.getWindow().getGuiScaledWidth();
    }

    private static java.util.Map<?, ?> generatedTextures() throws ReflectiveOperationException {
        var field = com.cappleapple.brokennotshattered.client.WornTextures.class.getDeclaredField("TEXTURES");
        field.setAccessible(true);
        return java.util.Map.copyOf((java.util.Map<?, ?>) field.get(null));
    }

    private static final class Gallery extends Screen {
        private final ItemStack[] picks = {testTool(), testTool()};
        private final ItemStack[][] armor = new ItemStack[2][4];
        private final ItemStack[] wings = {new ItemStack(Items.ELYTRA), new ItemStack(Items.ELYTRA)};
        private final RemotePlayer[] players = new RemotePlayer[9];
        private Object configuredRenderer;
        private java.lang.reflect.Method updateAttack;

        Gallery() {
            super(Component.literal("Broken Not Shattered appearance gate"));
            Minecraft mc = Minecraft.getInstance();
            if (Boolean.getBoolean("broken_not_shattered.smokeAnimationCompat")) {
                for (String id : new String[] {"entity_model_features", "entity_texture_features", "bettercombat",
                    "emf_compat_core", "emf_compat_better_combat"}) {
                    if (!net.neoforged.fml.ModList.get().isLoaded(id)) throw new IllegalStateException("Missing test mod " + id);
                }
                if (!mc.getResourcePackRepository().getSelectedIds().contains("file/FA+Player-v1.1.zip")) {
                    throw new IllegalStateException("Fresh Animations Player is not active");
                }
            }
            for (int i = 0; i < players.length; i++) {
                players[i] = new RemotePlayer(mc.level, new GameProfile(new UUID(0, i + 1), "WearTest" + i)) {
                    @Override public net.minecraft.client.resources.PlayerSkin getSkin() {
                        return net.minecraft.client.resources.DefaultPlayerSkin.get(new UUID(0, 1));
                    }
                };
            }
            useRequestedArmorLayer(mc);
            for (int i = 0; i < 2; i++) {
                armor[i] = new ItemStack[] {new ItemStack(Items.GOLDEN_HELMET), new ItemStack(Items.LEATHER_CHESTPLATE),
                    new ItemStack(Items.GOLDEN_LEGGINGS), new ItemStack(Items.GOLDEN_BOOTS)};
                String armorSet = System.getProperty("broken_not_shattered.smokeArmorSet", "");
                if (!armorSet.isEmpty()) {
                    String[] suffixes = {"helmet", "chestplate", "leggings", "boots"};
                    for (int slot = 0; slot < suffixes.length; slot++) {
                        var id = net.minecraft.resources.ResourceLocation.parse(armorSet + "_" + suffixes[slot]);
                        armor[i][slot] = new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM
                            .getOptional(id).orElseThrow(() -> new IllegalStateException("Missing test armor " + id)));
                        if (!armor[i][slot].isDamageableItem()) throw new IllegalStateException("Test armor has no durability: " + id);
                    }
                    continue;
                }
                armor[i][1].set(DataComponents.DYED_COLOR, new DyedItemColor(0x376ABE, true));
                armor[i][1].set(DataComponents.TRIM, new ArmorTrim(
                    mc.level.registryAccess().registryOrThrow(Registries.TRIM_MATERIAL).getHolderOrThrow(TrimMaterials.GOLD),
                    mc.level.registryAccess().registryOrThrow(Registries.TRIM_PATTERN).getHolderOrThrow(TrimPatterns.SENTRY)));
            }
            for (int i = 0; i < 2; i++) {
                prepareSavedFixture(picks[i], 100 + i);
                prepareSavedFixture(wings[i], 200 + i);
                for (int slot = 0; slot < 4; slot++) prepareSavedFixture(armor[i][slot], 300 + i * 10 + slot);
            }
        }

        private static ItemStack testTool() {
            return new ItemStack(Boolean.getBoolean("broken_not_shattered.smokeEnchanted") ? Items.DIAMOND_AXE : Items.GOLDEN_PICKAXE);
        }

        private void prepareSavedFixture(ItemStack stack, long seed) {
            // Explicit saved item data, like the component delivered in a server inventory packet.
            stack.set(ModDataComponents.BREAK_SEED, seed);
            if (Boolean.getBoolean("broken_not_shattered.smokeEnchanted")) {
                stack.enchant(Minecraft.getInstance().level.registryAccess().registryOrThrow(Registries.ENCHANTMENT)
                    .getHolderOrThrow(net.minecraft.world.item.enchantment.Enchantments.MENDING), 1);
            }
        }

        void reloadItems() {
            var registries = Minecraft.getInstance().level.registryAccess();
            for (int i = 0; i < 2; i++) {
                picks[i] = ItemStack.parseOptional(registries, (net.minecraft.nbt.CompoundTag) picks[i].save(registries));
                wings[i] = ItemStack.parseOptional(registries, (net.minecraft.nbt.CompoundTag) wings[i].save(registries));
                for (int slot = 0; slot < 4; slot++) {
                    armor[i][slot] = ItemStack.parseOptional(registries, (net.minecraft.nbt.CompoundTag) armor[i][slot].save(registries));
                }
            }
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        void startAttack() throws ReflectiveOperationException {
            Class<?> api = Class.forName("net.bettercombat.client.animation.PlayerAttackAnimatable");
            Class<? extends Enum> hand = (Class<? extends Enum>) Class.forName("net.bettercombat.logic.AnimatedHand");
            Object mainHand = Enum.valueOf(hand, "MAIN_HAND");
            var play = api.getMethod("playAttackAnimation", String.class, hand, float.class, float.class);
            updateAttack = api.getMethod("updateAnimationsOnTick");
            for (int i = 0; i < 4; i++) {
                play.invoke(players[i], "bettercombat:one_handed_slash_horizontal_right", mainHand, 12.0F, 0.35F);
            }
        }

        void tickAttack() {
            if (updateAttack == null) return;
            try {
                for (int i = 0; i < 4; i++) {
                    players[i].tick();
                    updateAttack.invoke(players[i]);
                }
            } catch (ReflectiveOperationException failure) {
                throw new IllegalStateException("Better Combat animation failed", failure);
            }
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        private void useRequestedArmorLayer(Minecraft mc) {
            try {
                // Exercise the actual optional replacement method without needing to start a combat ability.
                var renderer = mc.getEntityRenderDispatcher().getRenderer(players[0]);
                if (renderer == configuredRenderer) return;
                var field = net.minecraft.client.renderer.entity.LivingEntityRenderer.class.getDeclaredField("layers");
                field.setAccessible(true);
                var layers = (java.util.List) field.get(renderer);
                // Animated party hats are unrelated to wear and prevent deterministic pixel comparisons.
                layers.removeIf(existing -> existing.getClass().getName().contains("PartyHatLayer")
                    || existing.getClass().getName().contains("JarredHeadLayer"));
                configuredRenderer = renderer;
                if (!"mowzie".equals(System.getProperty("broken_not_shattered.smokeArmorLayer"))) return;
                var type = Class.forName("com.bobmowzie.mowziesmobs.client.render.entity.layer.GeckoArmorLayer");
                Object layer = type.getConstructors()[0].newInstance(renderer,
                    new net.minecraft.client.model.HumanoidModel<>(mc.getEntityModels().bakeLayer(net.minecraft.client.model.geom.ModelLayers.PLAYER_INNER_ARMOR)),
                    new net.minecraft.client.model.HumanoidModel<>(mc.getEntityModels().bakeLayer(net.minecraft.client.model.geom.ModelLayers.PLAYER_OUTER_ARMOR)),
                    mc.getModelManager());
                layers.removeIf(existing -> existing instanceof net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer);
                layers.add(layer);
            } catch (ReflectiveOperationException failure) {
                throw new IllegalStateException("Cannot configure smoke-test armor layer", failure);
            }
        }

        @Override
        public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
            useRequestedArmorLayer(Minecraft.getInstance());
            gui.fill(0, 0, width, height, 0xFF202630);
            if (phase == 5) {
                renderHeldCloseup(gui);
                return;
            }
            String[] labels = {"Original", "Broken A", "Broken B", "Repaired A"};
            int column = width / 4;
            for (int i = 0; i < 4; i++) {
                RemotePlayer stand = players[i];
                int index = i == 2 ? 1 : 0;
                boolean broken = (phase == 1 || phase >= 3) && (i == 1 || i == 2);
                int x = column * i;
                gui.drawString(font, labels[i], x + 20, 20, 0xFFFFFFFF, false);
                picks[index].setDamageValue(broken ? picks[index].getMaxDamage() : 0);
                stand.setItemSlot(EquipmentSlot.MAINHAND, picks[index]);
                if (Boolean.getBoolean("broken_not_shattered.smokeAnimationCompat")) {
                    picks[1 - index].setDamageValue(broken ? picks[1 - index].getMaxDamage() : 0);
                    stand.setItemSlot(EquipmentSlot.OFFHAND, picks[1 - index]);
                }
                gui.pose().pushPose();
                gui.pose().translate(x + 50, 60, 0);
                gui.pose().scale(5, 5, 1);
                gui.renderItem(picks[index], 0, 0);
                gui.pose().popPose();
                for (int slot = 0; slot < 4; slot++) {
                    ItemStack stack = armor[index][slot];
                    stack.setDamageValue(broken ? stack.getMaxDamage() : 0);
                    stand.setItemSlot(new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}[slot], stack);
                }
                InventoryScreen.renderEntityInInventory(gui, x + 100, 380, 95, new Vector3f(),
                    new Quaternionf().rotateZ((float) Math.PI).rotateY(-0.35F), null, stand);
                RemotePlayer wingPlayer = players[i + 4];
                wings[index].setDamageValue(broken ? wings[index].getMaxDamage() : 0);
                wingPlayer.setItemSlot(EquipmentSlot.CHEST, wings[index]);
                wingPlayer.elytraRotX = wingPlayer.elytraRotY = wingPlayer.elytraRotZ = 0;
                InventoryScreen.renderEntityInInventory(gui, x + 100, 625, 95, new Vector3f(),
                    new Quaternionf().rotateZ((float) Math.PI).rotateY((float) Math.PI - 0.35F), null, wingPlayer);
            }
            RemotePlayer stand = players[8];
            picks[0].setDamageValue((phase == 1 || phase >= 3) ? picks[0].getMaxDamage() : 0);
            stand.setItemSlot(EquipmentSlot.MAINHAND, picks[0]);
            gui.pose().pushPose();
            gui.pose().translate(110, 675, 100);
            gui.pose().scale(50, -50, 50);
            gui.pose().mulPose(new Quaternionf().rotateY(0.75F));
            Minecraft.getInstance().getEntityRenderDispatcher().getItemInHandRenderer().renderItem(
                stand, picks[0].copy(), ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, false,
                gui.pose(), gui.bufferSource(), 15728880);
            gui.pose().popPose();
            gui.flush();
        }

        private void renderHeldCloseup(GuiGraphics gui) {
            String[] labels = {"Original", "Broken A", "Broken B", "Repaired A"};
            for (int i = 0; i < 4; i++) {
                int x = width / 4 * i;
                int index = i == 2 ? 1 : 0;
                ItemStack stack = picks[index];
                stack.setDamageValue(i == 1 || i == 2 ? stack.getMaxDamage() : 0);
                players[i].setItemSlot(EquipmentSlot.MAINHAND, stack);
                gui.drawString(font, labels[i], x + 35, 100, 0xFFFFFFFF, false);
                gui.pose().pushPose();
                gui.pose().translate(x + 110, 475, 100);
                gui.pose().scale(400, -400, 400);
                gui.pose().mulPose(new Quaternionf().rotateY(0.75F));
                Minecraft.getInstance().getEntityRenderDispatcher().getItemInHandRenderer().renderItem(
                    players[i], stack.copy(), ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, false,
                    gui.pose(), gui.bufferSource(), 15728880);
                gui.pose().popPose();
                gui.flush();
            }
        }

        void verifyBroken(NativeImage image) {
            int column = width / 4;
            for (int[] region : new int[][] {{45, 55, 90, 90}, {78, 203, 45, 28},
                {75, 253, 45, 32}, {75, 300, 45, 65}, {25, 420, 155, 210}}) {
                int broken = difference(image, 0, column, region);
                int unique = difference(image, column, 2 * column, region);
                if (broken < 40 || unique < 20) {
                    throw new AssertionError("Region y=" + region[1] + ": broken=" + broken + ", unique=" + unique);
                }
            }
        }

        void verifyRepair(NativeImage original, NativeImage repaired) {
            // Compare at the SAME screen coordinates. Translating a rotated model can change
            // edge rasterization by a pixel, even when the model and texture are identical.
            verifyRegion(original, repaired, 0, 50, width, 635, "Repair did not restore original pixel");
        }

        void verifyRegion(NativeImage original, NativeImage repaired, int minX, int minY, int maxX, int maxY, String message) {
            for (int y = minY; y < maxY; y++) {
                for (int x = minX; x < maxX; x++) {
                    if (original.getPixelRGBA(x, y) != repaired.getPixelRGBA(x, y)) {
                        throw new AssertionError(message + " at " + x + ", " + y);
                    }
                }
            }
        }

        private int difference(NativeImage image, int firstX, int secondX, int[] region) {
            int changed = 0;
            for (int y = region[1]; y < region[1] + region[3]; y++) {
                for (int x = region[0]; x < region[0] + region[2]; x++) {
                    if (image.getPixelRGBA(firstX + x, y) != image.getPixelRGBA(secondX + x, y)) changed++;
                }
            }
            return changed;
        }

        @Override public boolean isPauseScreen() { return true; }
    }
}
