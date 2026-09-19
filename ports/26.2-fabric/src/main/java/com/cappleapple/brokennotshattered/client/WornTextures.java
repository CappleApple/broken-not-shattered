package com.cappleapple.brokennotshattered.client;

import com.cappleapple.brokennotshattered.BrokenNotShattered;
import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.resources.Identifier;

/** Render-thread cache; texture slots are not recycled during a submitted frame. */
public final class WornTextures {
    private static final int MAX_TEXTURES = 256;
    private static final long MAX_BYTES = 32L * 1024 * 1024;
    private static final Map<Key,Entry> TEXTURES = new LinkedHashMap<>(32, .75f, true);
    private static final Set<Object> UNAVAILABLE = new HashSet<>();
    private static final boolean[] SLOTS = new boolean[MAX_TEXTURES];
    private static long frame, bytes;
    public interface Source {
        Optional<AnimationMetadataSection> bns$animation();
        NativeImage bns$image();
    }
    private WornTextures() {}
    public static void beginFrame() {
        frame++;
        var it = TEXTURES.values().iterator();
        long cutoff = System.nanoTime() - 15_000_000_000L;
        while(it.hasNext()) { Entry e = it.next(); if(e.lastUse < cutoff) {e.close(); it.remove();} }
    }
    public static void tick() { TEXTURES.values().forEach(Entry::tick); }
    public static void clear() { TEXTURES.values().forEach(Entry::close); TEXTURES.clear(); UNAVAILABLE.clear(); }
    public static Identifier sprite(TextureAtlasSprite sprite, WearPattern pattern) { return get(sprite.contents(), pattern); }
    public static Identifier armor(Identifier source, WearPattern pattern) {
        Identifier worn=get(source,pattern); return worn==null ? source : worn;
    }
    private static Identifier get(Object source, WearPattern pattern) {
        Key key=new Key(pattern, source);
        Entry existing=TEXTURES.get(key);
        if(existing!=null) {existing.touch(); return existing.location;}
        if(UNAVAILABLE.contains(source)) return null;
        NativeImage input=null; boolean owns=false;
        try {
            SpriteContents sprite=source instanceof SpriteContents s ? s : null;
            Optional<AnimationMetadataSection> animation=Optional.empty();
            if(sprite!=null) {
                Source access=(Source)(Object)sprite;
                input=access.bns$image(); animation=access.bns$animation();
            } else {
                var resource=Minecraft.getInstance().getResourceManager().getResource((Identifier)source);
                if(resource.isEmpty()) {unavailable(source); return null;}
                try(var stream=resource.get().open()) {input=NativeImage.read(stream); owns=true;}
            }
            int width=input.getWidth(), height=input.getHeight();
            if((long)width*height>1_048_576) {unavailable(source); return null;}
            int fw=sprite==null?width:sprite.width(), fh=sprite==null?height:sprite.height();
            long cost=((long)width*height+(long)fw*fh)*4;
            int slot=allocate(cost); if(slot<0) return null;
            NativeImage sheet=modify(input,pattern,fw,fh);
            Identifier location=Identifier.fromNamespaceAndPath(BrokenNotShattered.MOD_ID,"generated/wear_"+slot);
            DynamicTexture texture=new DynamicTexture(location.toString(),fw,fh,true);
            Entry entry=new Entry(slot,location,texture,sheet,animation,fw,fh,cost);
            entry.upload();
            Minecraft.getInstance().getTextureManager().register(location,texture);
            SLOTS[slot]=true; bytes+=cost; TEXTURES.put(key,entry);
            return location;
        } catch(IOException|IllegalArgumentException ex) {unavailable(source); BrokenNotShattered.LOGGER.debug("Cannot generate worn texture {}",source,ex); return null;}
        finally {if(owns && input!=null) input.close();}
    }
    static NativeImage modify(NativeImage input,WearPattern pattern,int fw,int fh) {
        NativeImage output=new NativeImage(input.getWidth(),input.getHeight(),false);
        for(int y=0;y<input.getHeight();y++) for(int x=0;x<input.getWidth();x++)
            output.setPixelABGR(x,y,pattern.modify(net.minecraft.util.ARGB.toABGR(input.getPixel(x,y)),x%fw,y%fh,fw,fh));
        return output;
    }
    private static void unavailable(Object source) {if(UNAVAILABLE.size()>=MAX_TEXTURES)UNAVAILABLE.clear(); UNAVAILABLE.add(source);}
    private static int allocate(long cost) {
        var it=TEXTURES.values().iterator();
        while((TEXTURES.size()>=MAX_TEXTURES || bytes+cost>MAX_BYTES)&&it.hasNext()) {
            Entry e=it.next(); if(e.lastFrame==frame)continue; e.close(); it.remove();
        }
        if(bytes+cost>MAX_BYTES)return -1;
        for(int i=0;i<SLOTS.length;i++)if(!SLOTS[i])return i;
        return -1;
    }
    private record Key(WearPattern pattern,Object source) {}
    private record Frame(int index,int ticks) {}
    private static final class Entry {
        final int slot,fw,fh; final Identifier location; final DynamicTexture texture; final NativeImage sheet;
        final List<Frame> frames=new ArrayList<>(); final boolean interpolate; final long cost;
        long lastUse,lastFrame; int index,ticks;
        Entry(int slot,Identifier location,DynamicTexture texture,NativeImage sheet,Optional<AnimationMetadataSection> metadata,int fw,int fh,long cost) {
            this.slot=slot;this.location=location;this.texture=texture;this.sheet=sheet;this.fw=fw;this.fh=fh;this.cost=cost;
            int count=(sheet.getWidth()/fw)*(sheet.getHeight()/fh);
            interpolate=metadata.map(AnimationMetadataSection::interpolatedFrames).orElse(false);
            if(metadata.isPresent()) {
                var m=metadata.get();
                if(m.frames().isPresent()) for(var f:m.frames().get()) {
                    int time=f.timeOr(m.defaultFrameTime());
                    if(f.index()>=0&&f.index()<count&&time>0) frames.add(new Frame(f.index(),time));
                }
                else for(int i=0;i<count;i++)frames.add(new Frame(i,Math.max(1,m.defaultFrameTime())));
            }
            if(frames.isEmpty())frames.add(new Frame(0,1));
            touch();
        }
        void touch(){lastFrame=frame;lastUse=System.nanoTime();}
        void tick(){
            if(frames.size()<2)return;
            if(++ticks>=frames.get(index).ticks()){ticks=0;index=(index+1)%frames.size();upload();}
            else if(interpolate)upload();
        }
        void upload(){
            Frame current=frames.get(index), next=frames.get((index+1)%frames.size());
            int columns=sheet.getWidth()/fw, x0=current.index()%columns*fw, y0=current.index()/columns*fh;
            int x1=next.index()%columns*fw,y1=next.index()/columns*fh;
            float blend=interpolate?(float)ticks/current.ticks():0;
            NativeImage pixels=texture.getPixels();
            for(int y=0;y<fh;y++)for(int x=0;x<fw;x++){
                int a=net.minecraft.util.ARGB.toABGR(sheet.getPixel(x0+x,y0+y));
                if(blend>0){int b=net.minecraft.util.ARGB.toABGR(sheet.getPixel(x1+x,y1+y)), c=a&0xff000000;for(int shift=0;shift<24;shift+=8)c|=(int)(((a>>>shift)&255)*(1-blend)+((b>>>shift)&255)*blend)<<shift;a=c;}
                pixels.setPixelABGR(x,y,a);
            }
            texture.upload();
        }
        void close(){sheet.close();Minecraft.getInstance().getTextureManager().release(location);SLOTS[slot]=false;bytes-=cost;}
    }
}
