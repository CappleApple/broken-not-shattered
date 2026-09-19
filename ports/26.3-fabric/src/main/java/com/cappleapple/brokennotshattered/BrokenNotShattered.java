package com.cappleapple.brokennotshattered;

import com.cappleapple.brokennotshattered.core.BreakPatternData;
import com.cappleapple.brokennotshattered.registry.ModDataComponents;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BrokenNotShattered implements ModInitializer {
    public static final String MOD_ID = "broken_not_shattered";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    @Override public void onInitialize() {
        ModDataComponents.init();
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> BreakPatternData.backfill(entity));
        ServerTickEvents.END_LEVEL_TICK.register(world -> world.getAllEntities().forEach(BreakPatternData::backfill));
    }
}
