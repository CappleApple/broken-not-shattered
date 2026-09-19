package com.cappleapple.brokennotshattered.gametest;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
@EventBusSubscriber(modid="broken_not_shattered")
public final class SmokeEvents {
    @SubscribeEvent public static void started(ServerStartedEvent event) {
        if (Boolean.getBoolean("broken_not_shattered.runtimeSmoke")) event.getServer().execute(() -> RuntimeSmoke.run(event.getServer()));
    }
}
