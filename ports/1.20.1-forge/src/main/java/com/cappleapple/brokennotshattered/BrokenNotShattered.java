package com.cappleapple.brokennotshattered;

import com.cappleapple.brokennotshattered.config.ClientConfig;
import com.cappleapple.brokennotshattered.core.FunctionalSuppression;
import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.common.MinecraftForge;
import org.slf4j.Logger;

@Mod(BrokenNotShattered.MOD_ID)
public final class BrokenNotShattered {
    public static final String MOD_ID = "broken_not_shattered";
    public static final Logger LOGGER = LogUtils.getLogger();
    public BrokenNotShattered() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
        MinecraftForge.EVENT_BUS.addListener(FunctionalSuppression::onAttributeModifiers);
    }
}
