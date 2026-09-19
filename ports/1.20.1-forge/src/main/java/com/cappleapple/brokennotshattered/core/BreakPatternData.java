package com.cappleapple.brokennotshattered.core;

import com.cappleapple.brokennotshattered.BrokenNotShattered;
import net.minecraft.nbt.Tag;
import java.util.concurrent.ThreadLocalRandom;
import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.living.LivingEvent;

/** Server-owned item identity. Repair, copying, networking and saving all retain the same seed. */
@EventBusSubscriber(modid = BrokenNotShattered.MOD_ID)
public final class BreakPatternData {
    private BreakPatternData() {}

    @Nullable
    public static Long seed(ItemStack stack) {
        return stack.hasTag() && stack.getTag().contains("broken_not_shattered:break_seed", Tag.TAG_LONG)
            ? stack.getTag().getLong("broken_not_shattered:break_seed") : null;
    }

    /** Call only from an authoritative item lifecycle, never from a renderer. */
    public static boolean ensureSeed(ItemStack stack) {
        if (seed(stack) != null || !BrokenState.isBroken(stack)) return false;
        stack.getOrCreateTag().putLong("broken_not_shattered:break_seed", ThreadLocalRandom.current().nextLong());
        return true;
    }

    @SubscribeEvent
    public static void onJoin(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide()) backfill(event.getEntity());
    }

    @SubscribeEvent
    public static void onTick(LivingEvent.LivingTickEvent event) {
        if (!event.getEntity().level().isClientSide()) backfill(event.getEntity());
    }

    @SubscribeEvent
    public static void onContainerOpen(PlayerContainerEvent.Open event) {
        if (event.getEntity().level().isClientSide()) return;
        event.getContainer().slots.forEach(slot -> {
            if (ensureSeed(slot.getItem())) slot.setChanged();
        });
    }

    public static void backfill(Entity entity) {
        if (entity instanceof LivingEntity living) {
            for (EquipmentSlot slot : EquipmentSlot.values()) ensureSeed(living.getItemBySlot(slot));
        } else if (entity instanceof ItemEntity dropped) {
            ItemStack stack = dropped.getItem();
            if (ensureSeed(stack)) dropped.setItem(stack.copy());
        } else if (entity instanceof ItemFrame frame) {
            ItemStack stack = frame.getItem();
            if (ensureSeed(stack)) frame.setItem(stack.copy(), false);
        }
    }
}
