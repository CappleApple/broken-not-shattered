package com.cappleapple.brokennotshattered.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class ClientConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue TOOLTIP_ENABLED;
    public static final ModConfigSpec.ConfigValue<String> TOOLTIP_TEXT;
    public static final ModConfigSpec.ConfigValue<String> TOOLTIP_COLOR;
    public static final ModConfigSpec SPEC;

    static {
        BUILDER.push("tooltip");
        TOOLTIP_ENABLED = BUILDER
            .comment("Whether Broken Not Shattered appends its broken-state tooltip line.")
            .define("enabled", true);
        TOOLTIP_TEXT = BUILDER
            .comment("Visible broken-state text. The default uses the localization key tooltip.broken_not_shattered.broken.")
            .define("text", "[BROKEN]", value -> value instanceof String text && !text.isBlank());
        TOOLTIP_COLOR = BUILDER
            .comment("Minecraft named text color. Invalid values safely fall back to RED.")
            .define("color", "RED", value -> value instanceof String);
        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    private ClientConfig() {
    }
}
