package com.cappleapple.brokennotshattered.client;

import com.cappleapple.brokennotshattered.BrokenNotShattered;
import com.cappleapple.brokennotshattered.config.ClientConfig;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(modid = BrokenNotShattered.MOD_ID, value = Dist.CLIENT)
public final class ClientTooltipHandler {
    private ClientTooltipHandler() {
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        if (!TooltipStyle.shouldAppend(ClientConfig.TOOLTIP_ENABLED.get(), event.getItemStack())) {
            return;
        }

        event.getToolTip().add(TooltipStyle.createLine(ClientConfig.TOOLTIP_TEXT.get(), ClientConfig.TOOLTIP_COLOR.get()));
    }
}
