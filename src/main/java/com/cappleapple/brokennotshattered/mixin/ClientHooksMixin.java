package com.cappleapple.brokennotshattered.mixin;

import com.cappleapple.brokennotshattered.client.BrokenAppearance;
import com.cappleapple.brokennotshattered.client.WearPattern;
import com.cappleapple.brokennotshattered.client.WornTextures;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.ClientHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Runs after modded armor texture selection, including replacement player armor layers. */
@Mixin(ClientHooks.class)
abstract class ClientHooksMixin {
    @ModifyReturnValue(method = "getArmorTexture", at = @At("RETURN"))
    private static ResourceLocation bns$wearArmor(ResourceLocation source, Entity entity, ItemStack stack,
                                                  ArmorMaterial.Layer layer, boolean inner, EquipmentSlot slot) {
        ItemStack owner = stack;
        if (entity instanceof LivingEntity living) {
            ItemStack equipped = living.getItemBySlot(slot);
            if (!equipped.isEmpty() && ItemStack.isSameItemSameComponents(stack, equipped)) owner = equipped;
        }
        WearPattern pattern = BrokenAppearance.pattern(owner);
        return pattern == null ? source : WornTextures.armor(source, pattern);
    }
}
