package com.cappleapple.brokennotshattered.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;

public final class ArmorWear {
    private ArmorWear() {}

    /** Returns false when the caller should keep its normal trim rendering. */
    public static boolean renderTrim(ItemStack stack, ArmorMaterial material, PoseStack pose,
                                     MultiBufferSource buffers, int light, ArmorTrim trim, Model model, boolean inner) {
        WearPattern pattern = BrokenAppearance.pattern(stack);
        if (pattern == null) return false;
        TextureAtlasSprite sprite = Minecraft.getInstance().getModelManager().getAtlas(Sheets.ARMOR_TRIMS_SHEET)
            .getSprite(inner ? trim.innerTexture(material) : trim.outerTexture(material));
        ResourceLocation texture = WornTextures.sprite(sprite, pattern);
        if (texture == null) return false;
        model.renderToBuffer(pose, buffers.getBuffer(WearRenderType.wrap(
            Sheets.armorTrimsSheet(), texture)), light, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
        return true;
    }
}
