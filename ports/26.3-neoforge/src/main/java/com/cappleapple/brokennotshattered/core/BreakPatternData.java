package com.cappleapple.brokennotshattered.core;

import com.cappleapple.brokennotshattered.BrokenNotShattered;
import com.cappleapple.brokennotshattered.registry.ModDataComponents;
import java.util.concurrent.ThreadLocalRandom;
import org.jspecify.annotations.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/** Server-owned item identity. Repair, copying, networking and saving all retain the same seed. */
@EventBusSubscriber(modid = BrokenNotShattered.MOD_ID)
public final class BreakPatternData {
    private BreakPatternData() {}

    @Nullable
    public static Long seed(ItemStack stack) {
        return stack.get(ModDataComponents.BREAK_SEED);
    }

    /** Call only from an authoritative item lifecycle, never from a renderer. */
    public static boolean ensureSeed(ItemStack stack) {
        if (seed(stack) != null || !BrokenState.isBroken(stack)) return false;
        stack.set(ModDataComponents.BREAK_SEED, ThreadLocalRandom.current().nextLong());
        return true;
    }

    @SubscribeEvent
    public static void onJoin(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide()) backfill(event.getEntity());
    }

    @SubscribeEvent
    public static void onTick(EntityTickEvent.Post event) {
        if (!event.getEntity().level().isClientSide()) backfill(event.getEntity());
    }

    @SubscribeEvent
    public static void onContainerOpen(PlayerContainerEvent.Open event) {
        if (event.getEntity().level().isClientSide()) return;
        event.getContainer().slots.forEach(slot -> {
            if (ensureSeed(slot.getItem())) slot.setChanged();
        });
    }

    private static void backfill(Entity entity) {
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
