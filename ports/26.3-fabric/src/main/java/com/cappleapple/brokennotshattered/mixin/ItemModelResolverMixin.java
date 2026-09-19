package com.cappleapple.brokennotshattered.mixin;
import com.cappleapple.brokennotshattered.client.ModernItemWear;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ItemModelResolver.class)
abstract class ItemModelResolverMixin {
    @Inject(method="updateForTopItem", at=@At("RETURN"))
    private void bns$wear(ItemStackRenderState state, ItemStack stack, ItemDisplayContext context, Level level, ItemOwner owner, int seed, CallbackInfo ci) {
        ModernItemWear.apply(state, stack, context);
    }
}
