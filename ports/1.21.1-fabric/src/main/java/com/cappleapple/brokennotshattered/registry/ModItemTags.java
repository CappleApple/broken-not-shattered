package com.cappleapple.brokennotshattered.registry;

import com.cappleapple.brokennotshattered.BrokenNotShattered;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class ModItemTags {
    public static final TagKey<Item> IGNORE = tag("ignore");
    public static final TagKey<Item> SHATTERS = tag("shatters");
    public static final TagKey<Item> PROTECTED = tag("protected");

    private ModItemTags() {
    }

    private static TagKey<Item> tag(String path) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(BrokenNotShattered.MOD_ID, path));
    }
}
