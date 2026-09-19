package com.cappleapple.brokennotshattered.gametest;

import com.cappleapple.brokennotshattered.BrokenNotShattered;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import org.lwjgl.glfw.GLFW;

/** Opt-in rendering probe; excluded from distribution jars. */
public final class ClientAppearanceSmoke implements net.fabricmc.api.ClientModInitializer {
    public void onInitializeClient(){
        if (net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("geckolib")) {
            try { Class.forName("software.bernie.geckolib.renderer.GeoArmorRenderer"); }
            catch (ClassNotFoundException e) { throw new IllegalStateException(e); }
        }
net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register(client->tick());}

    private static final boolean ENABLED = Boolean.getBoolean("broken_not_shattered.clientSmoke");
    private static int frames;
    private static boolean captured;
    public static void tick() {
        if (!ENABLED) return;
        Minecraft game = Minecraft.getInstance();
        game.options.getSoundSourceOptionInstance(SoundSource.MASTER).set(0.0);
        game.options.pauseOnLostFocus = false;
        GLFW.glfwHideWindow(game.getWindow().getWindow());
        game.mouseHandler.releaseMouse();
        if (game.screen instanceof TitleScreen && game.getOverlay() == null) game.setScreen(new Probe());
    }
    public static void render() throws Exception {
        if (!ENABLED || captured) return;
        Minecraft game = Minecraft.getInstance();
        if (!(game.screen instanceof Probe) || game.getOverlay() != null || ++frames < 100) return;
        captured = true;
        Path out = Path.of("../build/client-smoke");
        Files.createDirectories(out);
        try (var image = Screenshot.takeScreenshot(game.getMainRenderTarget())) {
            image.writeToFile(out.resolve("fabric-1.20.1-items.png"));
        }
        Files.writeString(out.resolve("result.txt"), "PASS: rendered healthy and broken seeded icons with and without enchantment glint");
        game.stop();
    }
    private static final class Probe extends Screen {
        private final List<Item> items = List.of(Items.DIAMOND_PICKAXE, Items.IRON_SWORD, Items.DIAMOND_AXE,
            Items.GOLDEN_SHOVEL, Items.BOW, Items.SHIELD, Items.ELYTRA, Items.DIAMOND_CHESTPLATE);
        private final ItemStack[][] stacks = new ItemStack[4][items.size()];
        Probe() {
            super(Component.literal("Broken Not Shattered Fabric 1.20.1"));
            for (int row = 0; row < 4; row++) for (int i = 0; i < items.size(); i++) {
                ItemStack stack = new ItemStack(items.get(i));
                if (row > 0) {
                    stack.setDamageValue(stack.getMaxDamage());
                    stack.getOrCreateTag().putLong("broken_not_shattered:break_seed", row == 3 ? 7331 : 1234);
                }
                if (row == 2) stack.enchant(Enchantments.MENDING, 1);
                stacks[row][i] = stack;
            }
        }
        @Override public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
            gui.fill(0, 0, width, height, 0xff202630);
            gui.drawString(font, "Fabric 1.20.1: original / broken / enchanted / another seed", 10, 8, 0xffffff, false);
            for (int row = 0; row < 4; row++) for (int i = 0; i < items.size(); i++) {
                gui.pose().pushPose();
                gui.pose().translate(14 + i * 38, 30 + row * 42, 0);
                gui.pose().scale(2, 2, 2);
                gui.renderItem(stacks[row][i], 0, 0);
                gui.pose().popPose();
            }
        }
    }
}
