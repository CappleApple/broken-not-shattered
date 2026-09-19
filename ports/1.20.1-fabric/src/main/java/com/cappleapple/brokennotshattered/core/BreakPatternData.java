package com.cappleapple.brokennotshattered.core;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
public final class BreakPatternData {
    public static Long seed(ItemStack stack) { return stack.hasTag() && stack.getTag().contains("broken_not_shattered:break_seed", 4) ? stack.getTag().getLong("broken_not_shattered:break_seed") : null; }
    public static boolean ensureSeed(ItemStack stack) {
        if (seed(stack)!=null || !BrokenState.isBroken(stack)) return false;
        stack.getOrCreateTag().putLong("broken_not_shattered:break_seed", ThreadLocalRandom.current().nextLong());
        return true;
    }
    public static void backfill(Entity entity) {
        if (entity instanceof LivingEntity living) for (EquipmentSlot slot:EquipmentSlot.values()) ensureSeed(living.getItemBySlot(slot));
        if (entity instanceof Player player) player.containerMenu.slots.forEach(slot -> {if(ensureSeed(slot.getItem())) slot.setChanged();});
        if (entity instanceof ItemEntity dropped && ensureSeed(dropped.getItem())) dropped.setItem(dropped.getItem().copy());
        if (entity instanceof ItemFrame frame && ensureSeed(frame.getItem())) frame.setItem(frame.getItem().copy(),false);
    }
}
