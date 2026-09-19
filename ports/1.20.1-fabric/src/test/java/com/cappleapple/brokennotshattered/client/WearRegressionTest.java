package com.cappleapple.brokennotshattered.client;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import org.junit.jupiter.api.Test;
class WearRegressionTest {
    @Test void seedsAreDeterministicAndDistinct(){
        var a=WearPattern.create(42,WearSettings.DEFAULT,"minecraft:diamond_pickaxe");
        var b=WearPattern.create(42,WearSettings.DEFAULT,"minecraft:diamond_pickaxe");
        var c=WearPattern.create(43,WearSettings.DEFAULT,"minecraft:diamond_pickaxe");
        assertEquals(a.cracks(),b.cracks());assertNotEquals(a.cracks(),c.cracks());
        for(int y=0;y<16;y++)for(int x=0;x<16;x++)assertEquals(a.modify(0xffccbbaa,x,y,16,16),b.modify(0xffccbbaa,x,y,16,16));
    }
    @Test void configRangesClampAndSort(){assertEquals(new WearSettings.Range(0,8),new WearSettings.Range(12,-3));}
    @Test void transparentPixelsRemainTransparent(){
        var a=WearPattern.create(1,WearSettings.DEFAULT,"minecraft:diamond_pickaxe");
        for(int y=0;y<16;y++)for(int x=0;x<16;x++)assertEquals(0,a.modify(0x00ccbbaa,x,y,16,16)>>>24);
    }
    @Test void zeroCracksPreservesGeometry(){
        var settings=new WearSettings(true,new WearSettings.Range(0,0),Map.of(),0.3,0.18,0.18);
        var pattern=WearPattern.create(1,settings,"minecraft:diamond_pickaxe");
        assertTrue(pattern.seams().isEmpty());assertTrue(pattern.cracks().isEmpty());
        int[] data=new int[32];assertSame(data,BrokenIconGeometry.splitVertexData(data,pattern).get(0));
    }
}
