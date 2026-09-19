package com.cappleapple.brokennotshattered.gametest;

import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

/** Validates client mixin targets without creating a window or taking mouse/audio focus. */
public final class ClientMixinSmoke implements PreLaunchEntrypoint {
    @Override public void onPreLaunch() {
        if (!Boolean.getBoolean("broken_not_shattered.clientMixinSmoke")) return;
        Path result = Path.of("client-mixin-result.txt");
        try {
            for (String name : new String[] {
                "net.minecraft.client.Minecraft",
                "net.minecraft.client.renderer.item.ItemStackRenderState",
                "net.minecraft.client.renderer.item.ItemModelResolver",
                "net.minecraft.client.renderer.feature.ItemFeatureRenderer",
                "net.minecraft.client.renderer.rendertype.RenderType",
                "net.minecraft.client.renderer.rendertype.RenderSetup",
                "net.minecraft.client.renderer.rendertype.RenderSetup$TextureBinding",
                "net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer",
                "net.minecraft.client.renderer.texture.SpriteContents",
                "net.minecraft.client.resources.palette.PalettedTextureManager",
                "net.minecraft.client.renderer.item.ItemStackRenderState$LayerRenderState"
            }) Class.forName(name, false, getClass().getClassLoader());
            if (!com.cappleapple.brokennotshattered.mixin.ItemRenderStateAccessor.class.isAssignableFrom(
                    Class.forName("net.minecraft.client.renderer.item.ItemStackRenderState", false, getClass().getClassLoader()))) {
                throw new AssertionError("Item render-state accessor was not applied");
            }
            if (!com.cappleapple.brokennotshattered.client.WornTextures.Source.class.isAssignableFrom(
                    Class.forName("net.minecraft.client.renderer.texture.SpriteContents", false, getClass().getClassLoader()))) {
                throw new AssertionError("Sprite image source accessor was not applied");
            }
            Files.writeString(result, "PASS: all eleven client renderer/lifecycle mixin targets transformed before opening a window.\n");
            System.exit(0);
        } catch (Throwable failure) {
            try { Files.writeString(result, "FAIL: " + failure + "\n"); } catch (Exception ignored) {}
            failure.printStackTrace();
            System.exit(1);
        }
    }
}
