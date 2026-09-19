package com.cappleapple.brokennotshattered.gametest;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

/** Development-only lifecycle wiring, excluded from the distributable mod. */
public final class SmokeInitializer implements ModInitializer {
    @Override public void onInitialize() {
        if (Boolean.getBoolean("broken_not_shattered.smoke")) {
            ServerLifecycleEvents.SERVER_STARTED.register(RuntimeSmoke::run);
        }
    }
}
