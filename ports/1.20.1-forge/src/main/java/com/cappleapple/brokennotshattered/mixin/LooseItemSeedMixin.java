package com.cappleapple.brokennotshattered.mixin;
import com.cappleapple.brokennotshattered.core.BreakPatternData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ItemEntity.class, HangingEntity.class})
abstract class LooseItemSeedMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void bns$migrateSeed(CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        if (!entity.level().isClientSide()) BreakPatternData.backfill(entity);
    }
}
