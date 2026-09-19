package com.cappleapple.brokennotshattered.client;

import com.cappleapple.brokennotshattered.BrokenNotShattered;
import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import org.jetbrains.annotations.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.SpriteTicker;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

/** Render-thread-owned, bounded GPU cache. Slots are recycled to bound RenderType's texture memoization too. */
public final class WornTextures {
    private static final int MAX_TEXTURES = 256;
    private static final long MAX_BYTES = 32L * 1024 * 1024;
    private static final long MAX_SOURCE_PIXELS = 1024L * 1024;
    private static final Map<Key, Entry> TEXTURES = new LinkedHashMap<>(32, 0.75F, true);
    private static final Map<Object, Boolean> UNAVAILABLE = new HashMap<>();
    private static final boolean[] SLOTS = new boolean[MAX_TEXTURES];
    private static long frame;
    private static long bytes;

    private WornTextures() {}

    public static void beginFrame() {
        frame++;
        // Reclaim inactive textures outside a submitted render batch.
        Iterator<Entry> iterator = TEXTURES.values().iterator();
        long cutoff = System.nanoTime() - 15_000_000_000L;
        while (iterator.hasNext()) {
            Entry entry = iterator.next();
            if (entry.lastUse < cutoff) {
                entry.close();
                iterator.remove();
            }
        }
    }

    public static void tick() {
        for (Entry entry : TEXTURES.values()) {
            if (entry.ticker != null) {
                entry.texture.bind();
                entry.ticker.tickAndUpload(0, 0);
            }
        }
    }

    public static void clear() {
        TEXTURES.values().forEach(Entry::close);
        TEXTURES.clear();
        UNAVAILABLE.clear();
    }

    @Nullable
    public static ResourceLocation sprite(TextureAtlasSprite sprite, WearPattern pattern) {
        return get(sprite.contents(), pattern);
    }

    public static ResourceLocation armor(ResourceLocation source, WearPattern pattern) {
        ResourceLocation generated = get(source, pattern);
        return generated == null ? source : generated;
    }

    @Nullable
    private static ResourceLocation get(Object source, WearPattern pattern) {
        Key key = new Key(pattern.seed(), source);
        Entry cached = TEXTURES.get(key);
        if (cached != null) {
            cached.touch();
            return cached.location;
        }
        if (UNAVAILABLE.containsKey(source)) return null;
        NativeImage input = null;
        boolean ownsInput = false;
        try {
            SpriteContents original = source instanceof SpriteContents sprite ? sprite : null;
            if (original != null) {
                input = ((com.cappleapple.brokennotshattered.mixin.SpriteContentsAccessor)(Object)original).bns$originalImage();
            } else {
                var resource = Minecraft.getInstance().getResourceManager().getResource((ResourceLocation) source);
                if (resource.isEmpty()) {
                    unavailable(source);
                    return null;
                }
                try (var stream = resource.get().open()) {
                    input = NativeImage.read(stream);
                    ownsInput = true;
                }
            }
            int width = input.getWidth();
            int height = input.getHeight();
            if ((long) width * height > MAX_SOURCE_PIXELS) {
                unavailable(source);
                return null;
            }
            int frameWidth = original == null ? width : original.width();
            int frameHeight = original == null ? height : original.height();
            long cost = ((long) width * height + (original == null ? 0 : (long) frameWidth * frameHeight)) * 4;
            int slot = allocate(cost);
            if (slot < 0) return null; // All candidates are still in this frame's buffers.
            NativeImage output = modify(input, pattern, frameWidth, frameHeight);
            DynamicTexture texture;
            SpriteContents generated = null;
            SpriteTicker ticker = null;
            if (original == null) {
                texture = new DynamicTexture(output);
            } else {
                generated = new SpriteContents(original.name(),
                    new net.minecraft.client.resources.metadata.animation.FrameSize(frameWidth, frameHeight),
                    output, original.metadata());
                texture = new DynamicTexture(frameWidth, frameHeight, true);
                texture.bind();
                generated.uploadFirstFrame(0, 0);
                ticker = generated.createTicker();
            }
            ResourceLocation location = ResourceLocation.fromNamespaceAndPath(BrokenNotShattered.MOD_ID, "generated/wear_" + slot);
            Minecraft.getInstance().getTextureManager().register(location, texture);
            Entry entry = new Entry(slot, location, texture, generated, ticker, cost);
            SLOTS[slot] = true;
            bytes += cost;
            TEXTURES.put(key, entry);
            return location;
        } catch (IOException | IllegalArgumentException exception) {
            unavailable(source);
            BrokenNotShattered.LOGGER.debug("Cannot generate worn texture for {}", source, exception);
            return null;
        } finally {
            if (ownsInput && input != null) input.close();
        }
    }

    static NativeImage modify(NativeImage input, WearPattern pattern, int frameWidth, int frameHeight) {
        NativeImage output = new NativeImage(input.getWidth(), input.getHeight(), false);
        for (int y = 0; y < input.getHeight(); y++) {
            for (int x = 0; x < input.getWidth(); x++) {
                output.setPixelRGBA(x, y, pattern.modify(input.getPixelRGBA(x, y),
                    x % frameWidth, y % frameHeight, frameWidth, frameHeight));
            }
        }
        return output;
    }

    private static void unavailable(Object source) {
        if (UNAVAILABLE.size() >= MAX_TEXTURES) UNAVAILABLE.clear();
        UNAVAILABLE.put(source, true);
    }

    private static int allocate(long cost) {
        Iterator<Entry> iterator = TEXTURES.values().iterator();
        while ((TEXTURES.size() >= MAX_TEXTURES || bytes + cost > MAX_BYTES) && iterator.hasNext()) {
            Entry entry = iterator.next();
            if (entry.lastFrame == frame) continue;
            entry.close();
            iterator.remove();
        }
        if (bytes + cost > MAX_BYTES) return -1;
        for (int i = 0; i < SLOTS.length; i++) if (!SLOTS[i]) return i;
        return -1;
    }

    private record Key(long seed, Object source) {}

    private static final class Entry {
        final int slot;
        final ResourceLocation location;
        final DynamicTexture texture;
        final SpriteContents contents;
        final SpriteTicker ticker;
        final long cost;
        long lastFrame;
        long lastUse;

        Entry(int slot, ResourceLocation location, DynamicTexture texture, SpriteContents contents,
              SpriteTicker ticker, long cost) {
            this.slot = slot;
            this.location = location;
            this.texture = texture;
            this.contents = contents;
            this.ticker = ticker;
            this.cost = cost;
            touch();
        }

        void touch() {
            lastFrame = frame;
            lastUse = System.nanoTime();
        }

        void close() {
            if (ticker != null) ticker.close();
            if (contents != null) contents.close();
            Minecraft.getInstance().getTextureManager().release(location);
            SLOTS[slot] = false;
            bytes -= cost;
        }
    }
}
