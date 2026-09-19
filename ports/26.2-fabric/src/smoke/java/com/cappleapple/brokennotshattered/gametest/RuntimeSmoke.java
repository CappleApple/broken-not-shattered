package com.cappleapple.brokennotshattered.gametest;

import com.cappleapple.brokennotshattered.core.BreakPatternData;
import com.cappleapple.brokennotshattered.core.BrokenState;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;

/** Opt-in dedicated-server gate, enabled only by the Gradle smokeServer run. */
public final class RuntimeSmoke {
    private RuntimeSmoke() {}
    public static void run(MinecraftServer server) {
        Path result = Path.of("smoke-result.txt");
        try {
            var level = server.overworld();
            var mending = server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.MENDING);
            int items = 0;
            for (Item item : BuiltInRegistries.ITEM) {
                ItemStack stack = new ItemStack(item);
                if (!stack.isDamageableItem() || !BrokenState.isHandled(stack)) continue;
                items++;
                stack.set(DataComponents.CUSTOM_NAME, Component.literal("Saved name"));
                stack.enchant(mending, 1);
                stack.setDamageValue(stack.getMaxDamage() - 1);
                AtomicInteger callbacks = new AtomicInteger();
                stack.hurtAndBreak(1, level, null, ignored -> callbacks.incrementAndGet());
                require(!stack.isEmpty() && stack.getCount() == 1, item + " disappeared");
                require(BrokenState.isBroken(stack) && stack.getDamageValue() == stack.getMaxDamage(), item + " not broken");
                require(callbacks.get() == 1, item + " wrong callback count");
                require("Saved name".equals(stack.getHoverName().getString()), item + " lost name");
                require(EnchantmentHelper.getItemEnchantmentLevel(mending, stack) == 1, item + " lost mending");
                Long seed = BreakPatternData.seed(stack);
                require(seed != null && seed.equals(BreakPatternData.seed(stack.copy())), item + " lost seed on copy");
                stack.hurtAndBreak(3, level, null, ignored -> callbacks.incrementAndGet());
                require(callbacks.get() == 1, item + " repeated callback");
                require(stack.getDestroySpeed(Blocks.STONE.defaultBlockState()) == 1, item + " retained mining speed");
                require(!stack.isCorrectToolForDrops(Blocks.STONE.defaultBlockState()), item + " retained harvest ability");
                require(stack.getUseDuration(null) == 0, item + " retained use duration");
                for (EquipmentSlot slot : EquipmentSlot.values()) {
                    stack.forEachModifier(slot, (attribute, modifier) -> require(!List.of(Attributes.ATTACK_DAMAGE,
                        Attributes.ARMOR, Attributes.ARMOR_TOUGHNESS, Attributes.KNOCKBACK_RESISTANCE).contains(attribute),
                        item + " retained combat modifier " + attribute));
                }
                var ops = server.registryAccess().createSerializationContext(JsonOps.INSTANCE);
                JsonElement encoded = ItemStack.CODEC.encodeStart(ops, stack).getOrThrow();
                ItemStack restored = ItemStack.CODEC.parse(ops, encoded).getOrThrow();
                require(BrokenState.isBroken(restored) && seed.equals(BreakPatternData.seed(restored)), item + " failed persistence");
                stack.setDamageValue(stack.getMaxDamage() - 1);
                require(!BrokenState.isBroken(stack) && seed.equals(BreakPatternData.seed(stack)), item + " failed repair");
            }
            require(items > 50, "Too few durable items tested: " + items);
            ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
            List<Object> healthy = new ArrayList<>();
            sword.forEachModifier(EquipmentSlot.MAINHAND, (attribute, modifier) -> healthy.add(attribute));
            require(healthy.contains(Attributes.ATTACK_DAMAGE) && healthy.contains(Attributes.ATTACK_SPEED), "Healthy sword attributes absent");
            sword.setDamageValue(sword.getMaxDamage());
            List<Object> broken = new ArrayList<>();
            sword.forEachModifier(EquipmentSlot.MAINHAND, (attribute, modifier) -> broken.add(attribute));
            require(!broken.contains(Attributes.ATTACK_DAMAGE) && broken.contains(Attributes.ATTACK_SPEED), "Attack speed not preserved");
            sword.setDamageValue(0);
            List<Object> repaired = new ArrayList<>();
            sword.forEachModifier(EquipmentSlot.MAINHAND, (attribute, modifier) -> repaired.add(attribute));
            require(repaired.equals(healthy), "Repair did not restore attributes");
            var sharpness = server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SHARPNESS);
            sword.enchant(sharpness, 5);
            var target = EntityTypes.ARMOR_STAND.create(level, EntitySpawnReason.COMMAND);
            require(target != null, "No target");
            float healthyDamage = EnchantmentHelper.modifyDamage(level, sword, target, level.damageSources().generic(), 1);
            sword.setDamageValue(sword.getMaxDamage());
            require(healthyDamage > 1 && EnchantmentHelper.modifyDamage(level, sword, target, level.damageSources().generic(), 1) == 1, "Sharpness suppression failed");
            ItemStack chest = new ItemStack(Items.DIAMOND_CHESTPLATE);
            chest.enchant(server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.PROTECTION), 4);
            target.setItemSlot(EquipmentSlot.CHEST, chest);
            float protectedValue = EnchantmentHelper.getDamageProtection(level, target, level.damageSources().generic());
            chest.setDamageValue(chest.getMaxDamage());
            require(protectedValue > 0 && EnchantmentHelper.getDamageProtection(level, target, level.damageSources().generic()) == 0, "Protection suppression failed");
            ItemStack glider = new ItemStack(Items.ELYTRA);
            require(LivingEntity.canGlideUsing(glider, EquipmentSlot.CHEST), "Healthy glider unusable");
            glider.setDamageValue(glider.getMaxDamage());
            require(!LivingEntity.canGlideUsing(glider, EquipmentSlot.CHEST), "Broken glider usable");
            Files.writeString(result, "PASS: " + items + " durable items; preservation, callbacks, mining, use, combat attributes, copy/save/repair seeds, sharpness, protection, gliding.\n");
        } catch (Throwable failure) {
            try { Files.writeString(result, "FAIL: " + failure + "\n"); } catch (Exception ignored) {}
            failure.printStackTrace();
        } finally { server.halt(false); }
    }
    private static void require(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
