package com.cappleapple.brokennotshattered.core;

import com.cappleapple.brokennotshattered.registry.ModDataComponents;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Server-owned item identity. Repair, copying, networking and saving retain the seed. */
public final class BreakPatternData {
    private BreakPatternData() {}
    public static Long seed(ItemStack stack) { return stack.get(ModDataComponents.BREAK_SEED); }
    public static boolean ensureSeed(ItemStack stack) {
        if (seed(stack) != null || !BrokenState.isBroken(stack)) return false;
        stack.set(ModDataComponents.BREAK_SEED, ThreadLocalRandom.current().nextLong());
        return true;
    }
    public static void backfill(Entity entity) {
        if (entity instanceof LivingEntity living) {
            for (EquipmentSlot slot : EquipmentSlot.values()) ensureSeed(living.getItemBySlot(slot));
            if (living instanceof Player player) {
                for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
                    if (ensureSeed(player.getInventory().getItem(slot))) player.getInventory().setChanged();
                }
                player.containerMenu.slots.forEach(slot -> { if (ensureSeed(slot.getItem())) slot.setChanged(); });
            }
        } else if (entity instanceof ItemEntity dropped) {
            if (ensureSeed(dropped.getItem())) dropped.setItem(dropped.getItem().copy());
        } else if (entity instanceof ItemFrame frame) {
            if (ensureSeed(frame.getItem())) frame.setItem(frame.getItem().copy(), false);
        }
    }
}
