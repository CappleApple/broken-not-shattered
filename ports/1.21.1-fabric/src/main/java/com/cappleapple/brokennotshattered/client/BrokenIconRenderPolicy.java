package com.cappleapple.brokennotshattered.client;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;

/** Selects icon/model contexts without splitting true 3D held or dropped item models. */
public final class BrokenIconRenderPolicy {
    private BrokenIconRenderPolicy() {
    }

    public static boolean canWrap(BakedModel model) {
        return !model.isCustomRenderer() && (!((Object) model instanceof net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel fabric) || fabric.isVanillaAdapter());
    }

    public static boolean shouldSplit(ItemDisplayContext displayContext, BakedModel resolvedModel) {
        return shouldSplit(displayContext, resolvedModel.isGui3d());
    }

    static boolean shouldSplit(ItemDisplayContext displayContext, boolean gui3d) {
        if (displayContext == ItemDisplayContext.GUI) {
            return true;
        }
        if (gui3d) {
            return false;
        }

        return switch (displayContext) {
            case GROUND,
                FIRST_PERSON_LEFT_HAND,
                FIRST_PERSON_RIGHT_HAND,
                THIRD_PERSON_LEFT_HAND,
                THIRD_PERSON_RIGHT_HAND -> true;
            default -> false;
        };
    }
}
