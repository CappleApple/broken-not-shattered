package com.cappleapple.brokennotshattered.gametest;

import com.cappleapple.brokennotshattered.core.*;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class BrokenItemGameTests implements FabricGameTest {
    @GameTest(template=FabricGameTest.EMPTY_STRUCTURE)
    public void durabilityPreservationAndRepair(GameTestHelper helper) {
        for(Item item:List.of(Items.WOODEN_PICKAXE,Items.DIAMOND_PICKAXE,Items.NETHERITE_PICKAXE,
            Items.IRON_SWORD,Items.IRON_CHESTPLATE,Items.BOW,Items.CROSSBOW,Items.SHIELD,
            Items.FISHING_ROD,Items.SHEARS,Items.FLINT_AND_STEEL,Items.BRUSH,Items.TRIDENT,Items.ELYTRA)) {
            var stack=new ItemStack(item);stack.setDamageValue(stack.getMaxDamage()-1);
            stack.setHoverName(Component.literal("Kept name"));
            var calls=new AtomicInteger();
            stack.hurtAndBreak(1,helper.makeMockPlayer(),ignored->calls.incrementAndGet());
            helper.assertTrue(!stack.isEmpty()&&stack.getCount()==1,item+" disappeared");
            helper.assertTrue(stack.getDamageValue()==stack.getMaxDamage()&&BrokenState.isBroken(stack),item+" did not stop at zero");
            helper.assertTrue(calls.get()==1,item+" callback count was not one");
            helper.assertTrue(stack.getHoverName().getString().equals("Kept name"),"Custom name lost");
            helper.assertTrue(BreakPatternData.seed(stack)!=null,"Missing seed");
            stack.hurtAndBreak(50,helper.makeMockPlayer(),ignored->calls.incrementAndGet());
            helper.assertTrue(calls.get()==1&&stack.getDamageValue()==stack.getMaxDamage(),"Already broken item damaged again");
            stack.setDamageValue(stack.getMaxDamage()-1);helper.assertTrue(!BrokenState.isBroken(stack),"Repair did not restore item");
        }
        helper.succeed();
    }
    @GameTest(template=FabricGameTest.EMPTY_STRUCTURE)
    public void persistentSeedRoundTrip(GameTestHelper helper) {
        var stack=new ItemStack(Items.DIAMOND_AXE);stack.setDamageValue(stack.getMaxDamage()-1);
        stack.hurtAndBreak(1,helper.makeMockPlayer(),ignored->{});Long seed=BreakPatternData.seed(stack);
        helper.assertTrue(seed!=null&&seed.equals(BreakPatternData.seed(stack.copy())),"Copy lost seed");
        var restored=ItemStack.of(stack.save(new CompoundTag()));
        helper.assertTrue(seed.equals(BreakPatternData.seed(restored)),"Disk roundtrip lost seed");
        var buffer=new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
        try{buffer.writeItem(restored);helper.assertTrue(seed.equals(BreakPatternData.seed(buffer.readItem())),"Network lost seed");}finally{buffer.release();}
        restored.setDamageValue(0);restored.hurtAndBreak(restored.getMaxDamage(),helper.makeMockPlayer(),ignored->{});
        helper.assertTrue(seed.equals(BreakPatternData.seed(restored)),"Repair rerolled seed");helper.succeed();
    }
    @GameTest(template=FabricGameTest.EMPTY_STRUCTURE)
    public void toolAndCombatSuppression(GameTestHelper helper) {
        var pick=new ItemStack(Items.DIAMOND_PICKAXE);pick.setDamageValue(pick.getMaxDamage());
        helper.assertTrue(pick.getDestroySpeed(Blocks.DIAMOND_ORE.defaultBlockState())==1,"Mining speed not suppressed");
        helper.assertTrue(!pick.isCorrectToolForDrops(Blocks.DIAMOND_ORE.defaultBlockState()),"Drops not suppressed");
        pick.setDamageValue(pick.getMaxDamage()-1);
        helper.assertTrue(pick.getDestroySpeed(Blocks.DIAMOND_ORE.defaultBlockState())>1&&pick.isCorrectToolForDrops(Blocks.DIAMOND_ORE.defaultBlockState()),"Tool did not reactivate");
        var sword=new ItemStack(Items.DIAMOND_SWORD);sword.setDamageValue(sword.getMaxDamage());
        var attributes=new HashSet<>();sword.getAttributeModifiers(EquipmentSlot.MAINHAND).forEach((attribute,modifier)->attributes.add(attribute));
        helper.assertTrue(!attributes.contains(Attributes.ATTACK_DAMAGE)&&attributes.contains(Attributes.ATTACK_SPEED),"Combat filter affected wrong attributes");
        var armor=new ItemStack(Items.NETHERITE_CHESTPLATE);armor.setDamageValue(armor.getMaxDamage());
        attributes.clear();armor.getAttributeModifiers(EquipmentSlot.CHEST).forEach((attribute,modifier)->attributes.add(attribute));
        helper.assertTrue(!attributes.contains(Attributes.ARMOR)&&!attributes.contains(Attributes.ARMOR_TOUGHNESS)&&!attributes.contains(Attributes.KNOCKBACK_RESISTANCE),"Armor still protects");
        helper.succeed();
    }
    @GameTest(template=FabricGameTest.EMPTY_STRUCTURE)
    public void tagsAndBackfill(GameTestHelper helper) {
        var ignored=new ItemStack(Items.GOLDEN_SWORD);ignored.setDamageValue(ignored.getMaxDamage()-1);
        ignored.hurtAndBreak(1,helper.makeMockPlayer(),item->{});helper.assertTrue(ignored.isEmpty(),"Ignore tag did not permit destruction");
        var shatter=new ItemStack(Items.GOLDEN_AXE);shatter.setDamageValue(shatter.getMaxDamage()-1);
        shatter.hurtAndBreak(1,helper.makeMockPlayer(),item->{});helper.assertTrue(shatter.isEmpty(),"Shatters tag did not permit destruction");
        var legacy=new ItemStack(Items.DIAMOND_PICKAXE);legacy.setDamageValue(legacy.getMaxDamage());
        var player=helper.makeMockPlayer();
        legacy.inventoryTick(helper.getLevel(),player,0,false);helper.assertTrue(BreakPatternData.seed(legacy)!=null,"Inventory migration missing");helper.succeed();
    }
    @GameTest(template=FabricGameTest.EMPTY_STRUCTURE)
    public void useAndEnchantments(GameTestHelper helper) {
        var player=helper.makeMockPlayer();
        var sword=new ItemStack(Items.DIAMOND_SWORD);
        var sharpness=net.minecraft.world.item.enchantment.Enchantments.SHARPNESS;
        sword.enchant(sharpness,5);sword.setDamageValue(sword.getMaxDamage());
        float damage=net.minecraft.world.item.enchantment.EnchantmentHelper.getDamageBonus(sword,net.minecraft.world.entity.MobType.UNDEFINED);
        helper.assertTrue(damage==0,"Broken sword retained sharpness");helper.assertTrue(net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(sharpness,sword)==5,"Enchantment storage changed");
        var bow=new ItemStack(Items.BOW);bow.setDamageValue(bow.getMaxDamage());player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,bow);
        helper.assertTrue(bow.getUseDuration()==0,"Broken bow can charge");
        helper.assertTrue(bow.use(helper.getLevel(),player,net.minecraft.world.InteractionHand.MAIN_HAND).getResult()==net.minecraft.world.InteractionResult.PASS,"Broken bow can use");helper.succeed();
    }
}
