package com.cappleapple.brokennotshattered.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class ClientConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue TOOLTIP_ENABLED;
    public static final ModConfigSpec.ConfigValue<String> TOOLTIP_TEXT;
    public static final ModConfigSpec.ConfigValue<String> TOOLTIP_COLOR;
    public static final ModConfigSpec.BooleanValue WEAR_ENABLED;
    public static final ModConfigSpec.IntValue MIN_BREAK_LINES;
    public static final ModConfigSpec.IntValue MAX_BREAK_LINES;
    public static final ModConfigSpec.ConfigValue<java.util.List<? extends String>> BREAK_LINE_OVERRIDES;
    public static final ModConfigSpec.DoubleValue FADING;
    public static final ModConfigSpec.DoubleValue DARKENING;
    public static final ModConfigSpec.DoubleValue SCUFFING;
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
        BUILDER.push("appearance");
        WEAR_ENABLED = BUILDER.comment("Generate worn textures and varied breaks for broken items and equipped armor.")
            .define("enabled", true);
        MIN_BREAK_LINES = BUILDER.comment("Minimum main cracks per stack. Smaller forks do not count toward this range. Reversed minimum/maximum values are sorted.")
            .defineInRange("minBreakLines", 2, 0, 8);
        MAX_BREAK_LINES = BUILDER.comment("Maximum main cracks per stack (inclusive). Most stop inside the texture; occasional complete fractures separate pieces. Zero disables cracks and separation.")
            .defineInRange("maxBreakLines", 4, 0, 8);
        BREAK_LINE_OVERRIDES = BUILDER.comment("Optional item-specific ranges, for example minecraft:golden_pickaxe=2-5. Both ends must be 0..8.")
            .defineListAllowEmpty("breakLineOverrides", java.util.List.of(), ClientConfig::validBreakLineOverride);
        FADING = BUILDER.comment("Desaturation of the original texture (0..1).")
            .defineInRange("fading", 0.3, 0.0, 1.0);
        DARKENING = BUILDER.comment("Darkening of the original texture (0..1).")
            .defineInRange("darkening", 0.18, 0.0, 1.0);
        SCUFFING = BUILDER.comment("Density of scratches and worn patches (0..1).")
            .defineInRange("scuffing", 0.18, 0.0, 1.0);
        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    private ClientConfig() {
    }

    public static boolean validBreakLineOverride(Object value) {
        if (!(value instanceof String text)) return false;
        String[] parts = text.trim().split("=", -1);
        if (parts.length != 2 || net.minecraft.resources.ResourceLocation.tryParse(parts[0].trim()) == null) return false;
        return parts[1].trim().matches("[0-8]\\s*-\\s*[0-8]");
    }
}
