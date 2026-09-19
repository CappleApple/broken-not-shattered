package com.cappleapple.brokennotshattered.client;

import com.cappleapple.brokennotshattered.config.ClientConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
public final class BrokenNotShatteredClient implements ClientModInitializer {
    @Override public void onInitializeClient() {
        BrokenAppearance.reload();
        ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
            if (TooltipStyle.shouldAppend(ClientConfig.TOOLTIP_ENABLED.get(), stack)) {
                lines.add(TooltipStyle.createLine(ClientConfig.TOOLTIP_TEXT.get(), ClientConfig.TOOLTIP_COLOR.get()));
            }
        });
    }
}
