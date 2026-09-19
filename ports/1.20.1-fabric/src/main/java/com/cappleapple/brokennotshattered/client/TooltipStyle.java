package com.cappleapple.brokennotshattered.client;

import com.cappleapple.brokennotshattered.core.BrokenState;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class TooltipStyle {
    private static final String DEFAULT_TEXT = "[BROKEN]";

    private TooltipStyle() {
    }

    public static ChatFormatting parseColor(String configuredName) {
        if (configuredName == null) {
            return ChatFormatting.RED;
        }

        ChatFormatting formatting = ChatFormatting.getByName(configuredName.trim().toLowerCase(Locale.ROOT));
        return formatting != null && formatting.isColor() ? formatting : ChatFormatting.RED;
    }

    public static boolean shouldAppend(boolean enabled, ItemStack stack) {
        return enabled && BrokenState.isBroken(stack);
    }

    public static Component createLine(String configuredText, String configuredColor) {
        Component text = DEFAULT_TEXT.equals(configuredText)
            ? Component.translatable("tooltip.broken_not_shattered.broken")
            : Component.literal(configuredText);
        return text.copy().withStyle(parseColor(configuredColor));
    }
}
