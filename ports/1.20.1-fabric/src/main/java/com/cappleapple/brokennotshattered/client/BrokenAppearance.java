package com.cappleapple.brokennotshattered.client;
import com.cappleapple.brokennotshattered.config.ClientConfig;
import com.cappleapple.brokennotshattered.core.BrokenState;
import com.cappleapple.brokennotshattered.core.BreakPatternData;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemStack;
public final class BrokenAppearance implements ClientModInitializer {
    private static final Cache<PatternKey,WearPattern> PATTERNS=CacheBuilder.newBuilder().maximumSize(4096).build();
    private static volatile WearSettings settings=WearSettings.DEFAULT;
    private static boolean invalidate; private static Object level;
    public static WearPattern pattern(ItemStack stack) {
        if(!settings.enabled()||!BrokenState.isBroken(stack))return null;
        Long seed=BreakPatternData.seed(stack);if(seed==null)return null;
        var key=new PatternKey(seed,BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
        return PATTERNS.asMap().computeIfAbsent(key,value->WearPattern.create(value.seed,settings,value.item));
    }
    public void onInitializeClient() {
        ClientConfig.load();settings=WearSettings.readConfig();
        ClientTickEvents.END_CLIENT_TICK.register(client->WornTextures.tick());
        ItemTooltipCallback.EVENT.register((stack,context,lines)->{
            if(TooltipStyle.shouldAppend(ClientConfig.TOOLTIP_ENABLED.get(),stack))lines.add(TooltipStyle.createLine(ClientConfig.TOOLTIP_TEXT.get(),ClientConfig.TOOLTIP_COLOR.get()));
        });
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener(){
            public ResourceLocation getFabricId(){return new ResourceLocation("broken_not_shattered","wear");}
            public void onResourceManagerReload(ResourceManager manager){ClientConfig.load();settings=WearSettings.readConfig();invalidate=true;}
        });
    }
    public static void onFrame() {
        Object current=Minecraft.getInstance().level;
        if(invalidate||level!=current){invalidate=false;level=current;WornTextures.clear();BrokenIconModel.clearCache();PATTERNS.invalidateAll();}
        WornTextures.beginFrame();
    }
    private record PatternKey(long seed,String item){}
}
