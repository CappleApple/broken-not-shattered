package com.cappleapple.brokennotshattered.client;

import com.cappleapple.brokennotshattered.config.ClientConfig;
import com.cappleapple.brokennotshattered.core.BrokenState;
import com.cappleapple.brokennotshattered.core.BreakPatternData;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

public final class BrokenAppearance {
    private static final Cache<PatternKey, WearPattern> PATTERNS = CacheBuilder.newBuilder().maximumSize(4096).build();
    private static volatile WearSettings settings = WearSettings.DEFAULT;
    private static volatile boolean invalidate;
    private static Object level;
    private BrokenAppearance() {}
    public static WearPattern pattern(ItemStack stack) {
        if (!settings.enabled() || !BrokenState.isBroken(stack)) return null;
        Long seed = BreakPatternData.seed(stack);
        if (seed == null) return null;
        PatternKey key = new PatternKey(seed, BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
        return PATTERNS.asMap().computeIfAbsent(key, value -> WearPattern.create(value.seed, settings, value.item));
    }
    public static void reload() { ClientConfig.load(); settings = WearSettings.readConfig(); invalidate = true; }
    public static void onFrame() {
        Object currentLevel = Minecraft.getInstance().level;
        if (invalidate || level != currentLevel) {
            invalidate = false; level = currentLevel;
            WornTextures.clear(); ModernItemWear.clear(); PATTERNS.invalidateAll();
        }
        WornTextures.beginFrame();
    }
    public static void onTick() { WornTextures.tick(); }
    private record PatternKey(long seed, String item) {}
}
