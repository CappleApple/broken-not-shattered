package com.cappleapple.brokennotshattered.gametest.mixin;
import com.cappleapple.brokennotshattered.gametest.ClientAppearanceSmoke;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.lwjgl.glfw.GLFW;
@Mixin(com.mojang.blaze3d.platform.Window.class)
abstract class WindowSmokeMixin {
    @Inject(method="<init>",at=@At(value="INVOKE",target="Lorg/lwjgl/glfw/GLFW;glfwCreateWindow(IILjava/lang/CharSequence;JJ)J",remap=false))
    private void bns$background(CallbackInfo ci){if(Boolean.getBoolean("broken_not_shattered.clientSmoke")){GLFW.glfwWindowHint(GLFW.GLFW_FOCUSED,GLFW.GLFW_FALSE);GLFW.glfwWindowHint(GLFW.GLFW_FOCUS_ON_SHOW,GLFW.GLFW_FALSE);GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE,GLFW.GLFW_FALSE);}}
}
