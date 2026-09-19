package com.cappleapple.brokennotshattered.config;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.Files;
import java.util.List;
public final class ClientConfig {
    public static final Value<Boolean> TOOLTIP_ENABLED=new Value<>(true), WEAR_ENABLED=new Value<>(true);
    public static final Value<String> TOOLTIP_TEXT=new Value<>("[BROKEN]"), TOOLTIP_COLOR=new Value<>("RED");
    public static final Value<Integer> MIN_BREAK_LINES=new Value<>(2), MAX_BREAK_LINES=new Value<>(4);
    public static final Value<List<String>> BREAK_LINE_OVERRIDES=new Value<>(List.of());
    public static final Value<Double> FADING=new Value<>(0.3), DARKENING=new Value<>(0.18), SCUFFING=new Value<>(0.18);
    public static final class Value<T> { private T value; Value(T value){this.value=value;} public T get(){return value;} }
    public static boolean validBreakLineOverride(Object value) {
        if(!(value instanceof String text))return false;
        String[] parts=text.trim().split("=",-1);
        return parts.length==2 && net.minecraft.resources.ResourceLocation.tryParse(parts[0].trim())!=null && parts[1].trim().matches("[0-8]\\s*-\\s*[0-8]");
    }
    public static void load() {
        var file=FabricLoader.getInstance().getConfigDir().resolve("broken_not_shattered-client.json");
        var gson=new GsonBuilder().setPrettyPrinting().create();
        try {
            if(Files.exists(file)) {
                var root=gson.fromJson(Files.readString(file),JsonObject.class);
                var tooltip=root.getAsJsonObject("tooltip"); var appearance=root.getAsJsonObject("appearance");
                if(tooltip!=null) {
                    if(tooltip.has("enabled"))TOOLTIP_ENABLED.value=tooltip.get("enabled").getAsBoolean();
                    if(tooltip.has("text")&&!tooltip.get("text").getAsString().isBlank())TOOLTIP_TEXT.value=tooltip.get("text").getAsString();
                    if(tooltip.has("color"))TOOLTIP_COLOR.value=tooltip.get("color").getAsString();
                }
                if(appearance!=null) {
                    if(appearance.has("enabled"))WEAR_ENABLED.value=appearance.get("enabled").getAsBoolean();
                    if(appearance.has("minBreakLines"))MIN_BREAK_LINES.value=Math.max(0,Math.min(8,appearance.get("minBreakLines").getAsInt()));
                    if(appearance.has("maxBreakLines"))MAX_BREAK_LINES.value=Math.max(0,Math.min(8,appearance.get("maxBreakLines").getAsInt()));
                    if(appearance.has("breakLineOverrides"))BREAK_LINE_OVERRIDES.value=java.util.stream.StreamSupport.stream(appearance.getAsJsonArray("breakLineOverrides").spliterator(),false).map(e->e.getAsString()).filter(ClientConfig::validBreakLineOverride).toList();
                    if(appearance.has("fading"))FADING.value=clamp(appearance.get("fading").getAsDouble());
                    if(appearance.has("darkening"))DARKENING.value=clamp(appearance.get("darkening").getAsDouble());
                    if(appearance.has("scuffing"))SCUFFING.value=clamp(appearance.get("scuffing").getAsDouble());
                }
            } else {
                var tooltip=new JsonObject(); tooltip.addProperty("enabled",true);tooltip.addProperty("text","[BROKEN]");tooltip.addProperty("color","RED");
                var appearance=new JsonObject();appearance.addProperty("enabled",true);appearance.addProperty("minBreakLines",2);appearance.addProperty("maxBreakLines",4);appearance.add("breakLineOverrides",new com.google.gson.JsonArray());appearance.addProperty("fading",0.3);appearance.addProperty("darkening",0.18);appearance.addProperty("scuffing",0.18);
                var root=new JsonObject();root.add("tooltip",tooltip);root.add("appearance",appearance);Files.createDirectories(file.getParent());Files.writeString(file,gson.toJson(root));
            }
        }catch(Exception exception){com.cappleapple.brokennotshattered.BrokenNotShattered.LOGGER.warn("Cannot load client appearance config",exception);}
    }
    private static double clamp(double value){return Double.isFinite(value)?Math.max(0,Math.min(1,value)):0;}
}
