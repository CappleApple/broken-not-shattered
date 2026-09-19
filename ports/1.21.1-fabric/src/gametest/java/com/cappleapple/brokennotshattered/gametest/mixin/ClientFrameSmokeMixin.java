package com.cappleapple.brokennotshattered.gametest.mixin;
import com.cappleapple.brokennotshattered.gametest.ClientAppearanceSmoke;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(net.minecraft.client.Minecraft.class)
abstract class ClientFrameSmokeMixin {
    @Inject(method="runTick",at=@At("RETURN"))
    private void bns$capture(boolean tick,CallbackInfo ci)throws Exception{ClientAppearanceSmoke.render();}
}
