package com.cappleapple.brokennotshattered;
import com.cappleapple.brokennotshattered.core.BreakPatternData;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public final class BrokenNotShattered implements ModInitializer {
    public static final String MOD_ID="broken_not_shattered";
    public static final Logger LOGGER=LoggerFactory.getLogger(MOD_ID);
    public void onInitialize() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> BreakPatternData.backfill(entity));
        ServerTickEvents.END_WORLD_TICK.register(level -> level.getAllEntities().forEach(BreakPatternData::backfill));
    }
}
