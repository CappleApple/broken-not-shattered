package com.cappleapple.brokennotshattered.mixin;
import com.cappleapple.brokennotshattered.client.BrokenAppearance;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(Minecraft.class)
abstract class MinecraftMixin {
    @Inject(method = "runTick", at = @At("HEAD"))
    private void bns$frame(CallbackInfo callback) { BrokenAppearance.onFrame(); }
    @Inject(method = "tick", at = @At("RETURN"))
    private void bns$tick(CallbackInfo callback) { BrokenAppearance.onTick(); }
    @Inject(method = "reloadResourcePacks()Ljava/util/concurrent/CompletableFuture;", at = @At("RETURN"))
    private void bns$reload(CallbackInfoReturnable<CompletableFuture<Void>> callback) { callback.getReturnValue().thenRun(BrokenAppearance::reload); }
}
