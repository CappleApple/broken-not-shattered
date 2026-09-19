package com.cappleapple.brokennotshattered.gametest;
import java.nio.file.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;
@EventBusSubscriber(modid="broken_not_shattered", value=Dist.CLIENT)
public final class ClientMixinSmokeEvents {
    @SubscribeEvent public static void construct(FMLConstructModEvent event) {
        if (!Boolean.getBoolean("broken_not_shattered.clientMixinSmoke")) return;
        int status=0;
        try {
            String[] targets={
                "net.minecraft.client.renderer.item.ItemModelResolver",
                "net.minecraft.client.renderer.item.ItemStackRenderState",
                "net.minecraft.client.renderer.item.ItemStackRenderState$LayerRenderState",
                "net.minecraft.client.renderer.feature.ItemFeatureRenderer",
                "net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer",
                "net.minecraft.client.renderer.rendertype.RenderSetup",
                "net.minecraft.client.renderer.rendertype.RenderSetup$TextureBinding",
                "net.minecraft.client.renderer.rendertype.RenderType",
                "net.minecraft.client.renderer.texture.SpriteContents"};
            for(String target:targets) Class.forName(target,false,ClientMixinSmokeEvents.class.getClassLoader());
            if(!com.cappleapple.brokennotshattered.mixin.ItemRenderStateAccessor.class.isAssignableFrom(net.minecraft.client.renderer.item.ItemStackRenderState.class))
                throw new AssertionError("Client mixins did not apply");
            Files.writeString(Path.of("client-mixin-result.txt"),"PASS: " + targets.length + " client target classes transformed; accessor applied. No window opened.\n");
        } catch(Throwable failure) {
            status=1;failure.printStackTrace();
            try {Files.writeString(Path.of("client-mixin-result.txt"),"FAIL: "+failure+"\n");}catch(Exception ignored){}
        }
        System.exit(status);
    }
}
