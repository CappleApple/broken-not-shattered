package com.cappleapple.brokennotshattered.client;

import com.cappleapple.brokennotshattered.BrokenNotShattered;
import com.cappleapple.brokennotshattered.config.ClientConfig;
import com.cappleapple.brokennotshattered.core.BrokenState;
import com.cappleapple.brokennotshattered.core.BreakPatternData;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;


@EventBusSubscriber(modid = BrokenNotShattered.MOD_ID, value = Dist.CLIENT)
public final class BrokenAppearance {
    private static final Cache<PatternKey, WearPattern> PATTERNS = CacheBuilder.newBuilder().maximumSize(4096).build();
    private static volatile WearSettings settings = WearSettings.DEFAULT;
    private static volatile boolean invalidate;
    private static Object level;

    private BrokenAppearance() {}

    @Nullable
    public static WearPattern pattern(ItemStack stack) {
        if (!settings.enabled() || !BrokenState.isBroken(stack)) return null;
        Long seed = BreakPatternData.seed(stack);
        // Wait for authoritative migration instead of displaying a temporary random pattern.
        if (seed == null) return null;
        PatternKey key = new PatternKey(seed, BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
        return PATTERNS.asMap().computeIfAbsent(key, value -> WearPattern.create(value.seed, settings, value.item));
    }

    @SubscribeEvent
    public static void onFrame(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        Object currentLevel = Minecraft.getInstance().level;
        if (invalidate || level != currentLevel) {
            invalidate = false;
            level = currentLevel;
            WornTextures.clear();
            BrokenIconModel.clearCache();
            PATTERNS.invalidateAll();
        }
        WornTextures.beginFrame();
    }

    private record PatternKey(long seed, String item) {}

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) WornTextures.tick();
    }

    @EventBusSubscriber(modid = BrokenNotShattered.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
    public static final class Lifecycle {
        @SubscribeEvent
        public static void onConfig(ModConfigEvent event) {
            if (event.getConfig().getSpec() != ClientConfig.SPEC) return;
            if (event instanceof ModConfigEvent.Unloading) return;
            settings = WearSettings.readConfig();
            invalidate = true;
        }

        @SubscribeEvent
        public static void onReloadRegistration(RegisterClientReloadListenersEvent event) {
            event.registerReloadListener((net.minecraft.server.packs.resources.ResourceManagerReloadListener)
                manager -> invalidate = true);
        }
    }
}
