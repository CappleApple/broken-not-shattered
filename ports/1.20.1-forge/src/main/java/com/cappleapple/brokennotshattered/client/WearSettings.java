package com.cappleapple.brokennotshattered.client;

import com.cappleapple.brokennotshattered.config.ClientConfig;
import java.util.HashMap;
import java.util.Map;

/** Immutable snapshots also serve as cache keys when a client config is reloaded. */
public record WearSettings(boolean enabled, Range lines, Map<String, Range> overrides,
                           double fading, double darkening, double scuffing) {
    public static final WearSettings DEFAULT = new WearSettings(true, new Range(2, 4), Map.of(), 0.3, 0.18, 0.18);

    public WearSettings {
        overrides = Map.copyOf(overrides);
    }

    public static WearSettings readConfig() {
        Map<String, Range> overrides = new HashMap<>();
        for (String entry : ClientConfig.BREAK_LINE_OVERRIDES.get()) {
            if (!ClientConfig.validBreakLineOverride(entry)) continue;
            String[] parts = entry.trim().split("=");
            String[] ends = parts[1].trim().split("-");
            overrides.put(new net.minecraft.resources.ResourceLocation(parts[0].trim()).toString(),
                new Range(Integer.parseInt(ends[0].trim()), Integer.parseInt(ends[1].trim())));
        }
        return new WearSettings(ClientConfig.WEAR_ENABLED.get(),
            new Range(ClientConfig.MIN_BREAK_LINES.get(), ClientConfig.MAX_BREAK_LINES.get()), overrides,
            ClientConfig.FADING.get(), ClientConfig.DARKENING.get(), ClientConfig.SCUFFING.get());
    }

    public Range rangeFor(String itemId) {
        return overrides.getOrDefault(itemId, lines);
    }

    public record Range(int min, int max) {
        public Range {
            int lower = com.cappleapple.brokennotshattered.core.Numbers.clamp(Math.min(min, max), 0, 8);
            max = com.cappleapple.brokennotshattered.core.Numbers.clamp(Math.max(min, max), 0, 8);
            min = lower;
        }
    }
}
