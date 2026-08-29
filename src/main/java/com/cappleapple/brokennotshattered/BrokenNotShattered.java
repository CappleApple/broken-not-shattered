package com.cappleapple.brokennotshattered;

import com.cappleapple.brokennotshattered.config.ClientConfig;
import com.cappleapple.brokennotshattered.core.FunctionalSuppression;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(BrokenNotShattered.MOD_ID)
public final class BrokenNotShattered {
    public static final String MOD_ID = "broken_not_shattered";
    public static final Logger LOGGER = LogUtils.getLogger();

    public BrokenNotShattered(IEventBus modEventBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);

        NeoForge.EVENT_BUS.addListener(FunctionalSuppression::onAttributeModifiers);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, FunctionalSuppression::onSweepAttack);
    }
}
