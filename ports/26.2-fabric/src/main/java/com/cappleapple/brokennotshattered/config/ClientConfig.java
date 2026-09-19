package com.cappleapple.brokennotshattered.config;

import com.cappleapple.brokennotshattered.BrokenNotShattered;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;

/** Client-local JSON config; edits are picked up at resource reload. */
public final class ClientConfig {
    private static Data data = new Data();
    public static final Supplier<Boolean> TOOLTIP_ENABLED = () -> data.tooltip.enabled;
    public static final Supplier<String> TOOLTIP_TEXT = () -> data.tooltip.text;
    public static final Supplier<String> TOOLTIP_COLOR = () -> data.tooltip.color;
    public static final Supplier<Boolean> WEAR_ENABLED = () -> data.appearance.enabled;
    public static final Supplier<Integer> MIN_BREAK_LINES = () -> data.appearance.minBreakLines;
    public static final Supplier<Integer> MAX_BREAK_LINES = () -> data.appearance.maxBreakLines;
    public static final Supplier<List<String>> BREAK_LINE_OVERRIDES = () -> data.appearance.breakLineOverrides;
    public static final Supplier<Double> FADING = () -> data.appearance.fading;
    public static final Supplier<Double> DARKENING = () -> data.appearance.darkening;
    public static final Supplier<Double> SCUFFING = () -> data.appearance.scuffing;
    private ClientConfig() {}
    public static void load() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve("broken_not_shattered-client.json");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            if (Files.exists(path)) {
                Data parsed = gson.fromJson(Files.readString(path), Data.class);
                if (parsed != null && parsed.tooltip != null && parsed.appearance != null) data = parsed;
            } else { Files.createDirectories(path.getParent()); Files.writeString(path, gson.toJson(data)); }
            if (data.tooltip.text == null || data.tooltip.text.isBlank()) data.tooltip.text = "[BROKEN]";
            if (data.appearance.breakLineOverrides == null) data.appearance.breakLineOverrides = List.of();
            data.appearance.fading = Math.clamp(data.appearance.fading, 0, 1);
            data.appearance.darkening = Math.clamp(data.appearance.darkening, 0, 1);
            data.appearance.scuffing = Math.clamp(data.appearance.scuffing, 0, 1);
        } catch (Exception exception) { BrokenNotShattered.LOGGER.warn("Cannot load {}", path, exception); }
    }
    public static boolean validBreakLineOverride(Object value) {
        if (!(value instanceof String text)) return false;
        String[] parts = text.trim().split("=", -1);
        return parts.length == 2 && Identifier.tryParse(parts[0].trim()) != null && parts[1].trim().matches("[0-8]\\s*-\\s*[0-8]");
    }
    private static final class Data { Tooltip tooltip = new Tooltip(); Appearance appearance = new Appearance(); }
    private static final class Tooltip { boolean enabled = true; String text = "[BROKEN]"; String color = "RED"; }
    private static final class Appearance {
        boolean enabled = true; int minBreakLines = 2; int maxBreakLines = 4;
        List<String> breakLineOverrides = List.of(); double fading = 0.3; double darkening = 0.18; double scuffing = 0.18;
    }
}
