package com.cappleapple.brokennotshattered.mixin;
import com.cappleapple.brokennotshattered.client.BrokenAppearance;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Minecraft.class)
abstract class MinecraftMixin {
    @Inject(method="runTick",at=@At("HEAD"))
    private void bns$beginFrame(boolean tick,CallbackInfo ci){BrokenAppearance.onFrame();}
}
