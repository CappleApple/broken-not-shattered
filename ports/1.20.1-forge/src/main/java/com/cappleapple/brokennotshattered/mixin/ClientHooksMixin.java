package com.cappleapple.brokennotshattered.mixin;
import com.cappleapple.brokennotshattered.client.BrokenAppearance;
import com.cappleapple.brokennotshattered.client.WearPattern;
import com.cappleapple.brokennotshattered.client.WornTextures;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.ForgeHooksClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ForgeHooksClient.class)
abstract class ClientHooksMixin {
    @ModifyReturnValue(method = "getArmorTexture", at = @At("RETURN"), remap = false)
    private static String bns$wearArmor(String source, Entity entity, ItemStack stack, String original,
                                       EquipmentSlot slot, String type) {
        WearPattern pattern = BrokenAppearance.pattern(stack);
        return pattern == null ? source : WornTextures.armor(new ResourceLocation(source), pattern).toString();
    }
}
