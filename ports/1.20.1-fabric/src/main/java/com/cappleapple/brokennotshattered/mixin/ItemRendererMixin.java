package com.cappleapple.brokennotshattered.mixin;
import com.cappleapple.brokennotshattered.client.*;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.*;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
@Mixin(ItemRenderer.class)
abstract class ItemRendererMixin {
    @Shadow public abstract void renderQuadList(PoseStack pose,VertexConsumer buffer,List<BakedQuad> quads,ItemStack stack,int light,int overlay);
    private static final ThreadLocal<RenderContext> BNS_CONTEXT=new ThreadLocal<>();
    @WrapMethod(method="render")
    private void bns$wear(ItemStack stack,ItemDisplayContext context,boolean left,PoseStack pose,MultiBufferSource buffers,int light,int overlay,BakedModel model,Operation<Void> original){
        WearPattern pattern=BrokenAppearance.pattern(HeldItemWearContext.identity(stack));
        if(pattern==null||!BrokenIconRenderPolicy.canWrap(model)){original.call(stack,context,left,pose,buffers,light,overlay,model);return;}
        var previous=BNS_CONTEXT.get();BNS_CONTEXT.set(new RenderContext(buffers,pattern));
        try{original.call(stack,context,left,pose,buffers,light,overlay,BrokenIconModel.wrap(model,stack,pattern,BrokenIconRenderPolicy.shouldSplit(context,model),context!=ItemDisplayContext.GUI));}
        finally{if(previous==null)BNS_CONTEXT.remove();else BNS_CONTEXT.set(previous);}
    }
    @WrapMethod(method="renderModelLists")
    private void bns$renderWornQuads(BakedModel model,ItemStack stack,int light,int overlay,PoseStack pose,VertexConsumer originalBuffer,Operation<Void> original){
        var ctx=BNS_CONTEXT.get();if(ctx==null||!(model instanceof BrokenIconModel)){original.call(model,stack,light,overlay,pose,originalBuffer);return;}
        var quads=model.getQuads(null,null,net.minecraft.util.RandomSource.create(42));
        for(var quad:quads){
            var texture=WornTextures.sprite(quad.getSprite(),ctx.pattern);
            if(texture==null){renderQuadList(pose,originalBuffer,List.of(quad),stack,light,overlay);continue;}
            var type=WearRenderType.wrap(ctx.type,texture,quad.getSprite());
            var buffer=ctx.direct?ItemRenderer.getFoilBufferDirect(WearRenderType.textureBuffers(ctx.buffers),type,true,stack.hasFoil()):ItemRenderer.getFoilBuffer(WearRenderType.textureBuffers(ctx.buffers),type,true,stack.hasFoil());
            renderQuadList(pose,buffer,List.of(quad),stack,light,overlay);
        }
    }
    @com.llamalad7.mixinextras.injector.ModifyExpressionValue(method="render",at=@org.spongepowered.asm.mixin.injection.At(value="INVOKE",target="Lnet/minecraft/client/renderer/ItemBlockRenderTypes;getRenderType(Lnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/client/renderer/RenderType;"))
    private RenderType bns$rememberLayer(RenderType type){var ctx=BNS_CONTEXT.get();if(ctx!=null)ctx.type=type;return type;}
    @com.llamalad7.mixinextras.injector.ModifyExpressionValue(method="render",at=@org.spongepowered.asm.mixin.injection.At(value="INVOKE",target="Lnet/minecraft/client/renderer/entity/ItemRenderer;getFoilBuffer(Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/renderer/RenderType;ZZ)Lcom/mojang/blaze3d/vertex/VertexConsumer;"))
    private VertexConsumer bns$rememberGlint(VertexConsumer buffer){var ctx=BNS_CONTEXT.get();if(ctx!=null)ctx.direct=false;return buffer;}
    @WrapMethod(method="renderStatic(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/level/Level;III)V")
    private void bns$holdIdentity(net.minecraft.world.entity.LivingEntity entity,ItemStack stack,ItemDisplayContext context,boolean left,PoseStack pose,MultiBufferSource buffers,net.minecraft.world.level.Level level,int light,int overlay,int seed,Operation<Void> original){
        try(var ignored=HeldItemWearContext.enter(entity,stack,context)){original.call(entity,stack,context,left,pose,buffers,level,light,overlay,seed);}
    }
    @org.spongepowered.asm.mixin.injection.ModifyVariable(method="render",at=@org.spongepowered.asm.mixin.injection.At(value="INVOKE",target="Lnet/minecraft/client/resources/model/BakedModel;getTransforms()Lnet/minecraft/client/renderer/block/model/ItemTransforms;"),argsOnly=true)
    private BakedModel bns$wrapResolvedModel(BakedModel model,ItemStack stack,ItemDisplayContext context){
        var ctx=BNS_CONTEXT.get();return ctx==null||model instanceof BrokenIconModel||!BrokenIconRenderPolicy.canWrap(model)?model:BrokenIconModel.wrap(model,stack,ctx.pattern,BrokenIconRenderPolicy.shouldSplit(context,model),context!=ItemDisplayContext.GUI);
    }
    private static final class RenderContext {
        final MultiBufferSource buffers;final WearPattern pattern;RenderType type;boolean direct=true;
        RenderContext(MultiBufferSource buffers,WearPattern pattern){this.buffers=buffers;this.pattern=pattern;}
    }
}
