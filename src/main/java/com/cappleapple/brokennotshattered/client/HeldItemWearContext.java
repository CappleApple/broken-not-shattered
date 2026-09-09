package com.cappleapple.brokennotshattered.client;

import javax.annotation.Nullable;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Borrows the equipped item's identity for temporary render copies, without changing item data. */
public final class HeldItemWearContext {
    private static final ThreadLocal<Scope> CURRENT = new ThreadLocal<>();

    private HeldItemWearContext() {}

    public static Scope enter(@Nullable LivingEntity entity, ItemStack rendered, ItemDisplayContext context) {
        ItemStack owner = entity == null ? rendered : select(rendered, context, entity.getMainArm(),
            entity.getMainHandItem(), entity.getOffhandItem());
        return enter(rendered, owner);
    }

    static Scope enter(ItemStack rendered, ItemStack owner) {
        Scope scope = new Scope(CURRENT.get(), rendered, owner);
        CURRENT.set(scope);
        return scope;
    }

    public static ItemStack identity(ItemStack rendered) {
        Scope scope = CURRENT.get();
        return scope != null && scope.rendered == rendered ? scope.owner : rendered;
    }

    static ItemStack select(ItemStack rendered, ItemDisplayContext context, HumanoidArm mainArm,
                            ItemStack mainHand, ItemStack offHand) {
        boolean left;
        switch (context) {
            case FIRST_PERSON_LEFT_HAND, THIRD_PERSON_LEFT_HAND -> left = true;
            case FIRST_PERSON_RIGHT_HAND, THIRD_PERSON_RIGHT_HAND -> left = false;
            default -> { return rendered; }
        }
        ItemStack equipped = left == (mainArm == HumanoidArm.LEFT) ? mainHand : offHand;
        // An equip animation or cosmetic override can intentionally display a different item.
        return !equipped.isEmpty() && ItemStack.isSameItemSameComponents(rendered, equipped) ? equipped : rendered;
    }

    public static final class Scope implements AutoCloseable {
        private final Scope previous;
        private final ItemStack rendered;
        private final ItemStack owner;

        private Scope(Scope previous, ItemStack rendered, ItemStack owner) {
            this.previous = previous;
            this.rendered = rendered;
            this.owner = owner;
        }

        @Override
        public void close() {
            if (previous == null) CURRENT.remove();
            else CURRENT.set(previous);
        }
    }
}
