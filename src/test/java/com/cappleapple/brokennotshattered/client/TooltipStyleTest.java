package com.cappleapple.brokennotshattered.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

class TooltipStyleTest {
    @Test
    void acceptsMinecraftNamedColorsCaseInsensitively() {
        assertEquals(ChatFormatting.DARK_RED, TooltipStyle.parseColor("dark_red"));
        assertEquals(ChatFormatting.AQUA, TooltipStyle.parseColor("AQUA"));
    }

    @Test
    void invalidAndNonColorValuesFallBackToRed() {
        assertEquals(ChatFormatting.RED, TooltipStyle.parseColor("not_a_color"));
        assertEquals(ChatFormatting.RED, TooltipStyle.parseColor("BOLD"));
        assertEquals(ChatFormatting.RED, TooltipStyle.parseColor(null));
    }

    @Test
    void defaultLineIsLocalizedAndCustomTextIsLiteral() {
        var defaultLine = TooltipStyle.createLine("[BROKEN]", "RED");
        assertTrue(defaultLine.getContents() instanceof TranslatableContents);
        assertEquals("tooltip.broken_not_shattered.broken", ((TranslatableContents) defaultLine.getContents()).getKey());
        assertEquals(ChatFormatting.RED.getColor(), defaultLine.getStyle().getColor().getValue());

        var customLine = TooltipStyle.createLine("Needs repair", "AQUA");
        assertEquals("Needs repair", customLine.getString());
        assertEquals(ChatFormatting.AQUA.getColor(), customLine.getStyle().getColor().getValue());
    }

    @Test
    void enabledAndBrokenAreBothRequired() {
        ItemStack broken = new ItemStack(Items.IRON_SWORD);
        broken.setDamageValue(broken.getMaxDamage());

        assertFalse(TooltipStyle.shouldAppend(false, broken));
        assertTrue(TooltipStyle.shouldAppend(true, broken));
        assertFalse(TooltipStyle.shouldAppend(true, new ItemStack(Items.IRON_SWORD)));
    }
}
