package com.cappleapple.brokennotshattered.gametest;

import com.cappleapple.brokennotshattered.BrokenNotShattered;
import com.cappleapple.brokennotshattered.core.BreakPatternData;
import com.cappleapple.brokennotshattered.core.BrokenState;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(BrokenNotShattered.MOD_ID)
@PrefixGameTestTemplate(false)
public final class BrokenItemGameTests {
    private static final String EMPTY = "bastion/mobs/empty";

    @GameTest(templateNamespace = "minecraft", template = EMPTY)
    public static void preservesDurabilityAndStoredState(GameTestHelper helper) {
        var player = helper.makeMockPlayer();
        for (Item item : List.of(Items.WOODEN_PICKAXE, Items.DIAMOND_PICKAXE, Items.NETHERITE_PICKAXE,
            Items.IRON_SWORD, Items.IRON_CHESTPLATE, Items.BOW, Items.CROSSBOW, Items.SHIELD,
            Items.FISHING_ROD, Items.SHEARS, Items.FLINT_AND_STEEL, Items.BRUSH, Items.TRIDENT, Items.ELYTRA)) {
            ItemStack stack = new ItemStack(item);
            stack.setDamageValue(stack.getMaxDamage() - 1);
            stack.setHoverName(Component.literal("Kept name"));
            stack.getOrCreateTag().putString("custom_test", "kept");
            stack.enchant(Enchantments.MENDING, 1);
            AtomicInteger callbacks = new AtomicInteger();
            stack.hurtAndBreak(1, player, ignored -> callbacks.incrementAndGet());
            helper.assertTrue(!stack.isEmpty() && stack.getCount() == 1, item + " disappeared");
            helper.assertTrue(stack.getDamageValue() == stack.getMaxDamage(), item + " damage did not clamp");
            helper.assertTrue(BrokenState.isBroken(stack), item + " not broken");
            helper.assertTrue(BreakPatternData.seed(stack) != null, item + " missing seed");
            helper.assertTrue(callbacks.get() == 1, item + " break callback missing/duplicated");
            helper.assertTrue(stack.getHoverName().getString().equals("Kept name"), "name lost");
            helper.assertTrue(stack.getTag().getString("custom_test").equals("kept"), "NBT lost");
            helper.assertTrue(EnchantmentHelper.getTagEnchantmentLevel(Enchantments.MENDING, stack) == 1, "Mending lost");
            stack.hurtAndBreak(50, player, ignored -> callbacks.incrementAndGet());
            helper.assertTrue(callbacks.get() == 1 && stack.getDamageValue() == stack.getMaxDamage(), "broken damage/callback repeated");
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY)
    public static void seedSurvivesCopyDiskNetworkRepair(GameTestHelper helper) {
        var player = helper.makeMockPlayer();
        ItemStack first = new ItemStack(Items.DIAMOND_AXE);
        ItemStack second = new ItemStack(Items.DIAMOND_AXE);
        first.hurtAndBreak(first.getMaxDamage(), player, ignored -> {});
        second.hurtAndBreak(second.getMaxDamage(), player, ignored -> {});
        Long seed = BreakPatternData.seed(first);
        helper.assertTrue(seed != null && !seed.equals(BreakPatternData.seed(second)), "independent seeds missing");
        helper.assertTrue(seed.equals(BreakPatternData.seed(first.copy())), "copy changed seed");
        ItemStack loaded = ItemStack.of(first.save(new CompoundTag()));
        helper.assertTrue(seed.equals(BreakPatternData.seed(loaded)), "disk changed seed");
        FriendlyByteBuf buffer = new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
        try {
            buffer.writeItem(loaded);
            helper.assertTrue(seed.equals(BreakPatternData.seed(buffer.readItem())), "network changed seed");
        } finally { buffer.release(); }
        loaded.setDamageValue(0);
        loaded.hurtAndBreak(loaded.getMaxDamage(), player, ignored -> {});
        helper.assertTrue(seed.equals(BreakPatternData.seed(loaded)), "repair/rebreak changed seed");
        ItemStack legacy = broken(Items.GOLDEN_PICKAXE);
        legacy.inventoryTick(helper.getLevel(), player, 0, false);
        helper.assertTrue(BreakPatternData.seed(legacy) != null, "legacy inventory did not migrate");
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY)
    public static void containerMigrationMarksDirtyOnce(GameTestHelper helper) {
        var player = helper.makeMockPlayer();
        AtomicInteger dirty = new AtomicInteger();
        var container = new net.minecraft.world.SimpleContainer(27) {
            @Override public void setChanged() { super.setChanged(); dirty.incrementAndGet(); }
        };
        ItemStack legacy = broken(Items.GOLDEN_CHESTPLATE);
        container.setItem(0, legacy);
        var menu = net.minecraft.world.inventory.ChestMenu.threeRows(1, player.getInventory(), container);
        dirty.set(0);
        MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.entity.player.PlayerContainerEvent.Open(player, menu));
        Long seed = BreakPatternData.seed(legacy);
        helper.assertTrue(seed != null && dirty.get() > 0, "migration did not save container");
        dirty.set(0);
        MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.entity.player.PlayerContainerEvent.Open(player, menu));
        helper.assertTrue(seed.equals(BreakPatternData.seed(legacy)) && dirty.get() == 0, "migration was not idempotent");
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY)
    public static void onePointRepairRestoresToolAndUse(GameTestHelper helper) {
        ItemStack pickaxe = broken(Items.DIAMOND_PICKAXE);
        helper.assertTrue(pickaxe.getDestroySpeed(Blocks.DIAMOND_ORE.defaultBlockState()) == 1, "broken speed");
        helper.assertTrue(!pickaxe.isCorrectToolForDrops(Blocks.DIAMOND_ORE.defaultBlockState()), "broken drops");
        helper.assertTrue(!pickaxe.canPerformAction(ToolActions.PICKAXE_DIG), "broken action");
        pickaxe.setDamageValue(pickaxe.getMaxDamage() - 1);
        helper.assertTrue(pickaxe.getDestroySpeed(Blocks.DIAMOND_ORE.defaultBlockState()) > 1, "repaired speed");
        helper.assertTrue(pickaxe.isCorrectToolForDrops(Blocks.DIAMOND_ORE.defaultBlockState()), "repaired drops");
        helper.assertTrue(pickaxe.canPerformAction(ToolActions.PICKAXE_DIG), "repaired action");
        for (Item item : List.of(Items.BOW, Items.CROSSBOW, Items.TRIDENT, Items.SHIELD, Items.BRUSH)) {
            ItemStack stack = broken(item);
            helper.assertTrue(stack.getUseDuration() == 0, item + " use active");
            stack.setDamageValue(stack.getMaxDamage() - 1);
            helper.assertTrue(stack.getUseDuration() > 0, item + " use did not restore");
        }
        ItemStack elytra = broken(Items.ELYTRA);
        helper.assertTrue(!elytra.canElytraFly(helper.makeMockPlayer()), "broken elytra active");
        elytra.setDamageValue(elytra.getMaxDamage() - 2);
        helper.assertTrue(elytra.canElytraFly(helper.makeMockPlayer()), "elytra did not restore");
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY)
    public static void combatAttributesAndEnchantmentsRestore(GameTestHelper helper) {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        var speed = List.copyOf(sword.getAttributeModifiers(EquipmentSlot.MAINHAND).get(Attributes.ATTACK_SPEED));
        sword.enchant(Enchantments.SHARPNESS, 5);
        sword.enchant(Enchantments.FIRE_ASPECT, 2);
        sword.enchant(Enchantments.KNOCKBACK, 2);
        sword.setDamageValue(sword.getMaxDamage());
        helper.assertTrue(!sword.getAttributeModifiers(EquipmentSlot.MAINHAND).containsKey(Attributes.ATTACK_DAMAGE), "broken damage attribute");
        helper.assertTrue(speed.equals(List.copyOf(sword.getAttributeModifiers(EquipmentSlot.MAINHAND).get(Attributes.ATTACK_SPEED))), "attack speed changed");
        helper.assertTrue(!sword.canPerformAction(ToolActions.SWORD_SWEEP), "broken sweep");
        var wearer = helper.makeMockPlayer();
        wearer.setItemSlot(EquipmentSlot.MAINHAND, sword);
        helper.assertTrue(EnchantmentHelper.getDamageBonus(sword, net.minecraft.world.entity.MobType.UNDEFINED) == 0, "broken Sharpness");
        helper.assertTrue(EnchantmentHelper.getFireAspect(wearer) == 0 && EnchantmentHelper.getKnockbackBonus(wearer) == 0, "broken weapon effects");
        sword.setDamageValue(sword.getMaxDamage() - 1);
        helper.assertTrue(sword.getAttributeModifiers(EquipmentSlot.MAINHAND).containsKey(Attributes.ATTACK_DAMAGE), "repaired damage");
        helper.assertTrue(EnchantmentHelper.getDamageBonus(sword, net.minecraft.world.entity.MobType.UNDEFINED) > 0, "Sharpness did not restore");
        helper.assertTrue(EnchantmentHelper.getFireAspect(wearer) == 2 && EnchantmentHelper.getKnockbackBonus(wearer) == 2, "weapon effects did not restore");
        ItemStack armor = broken(Items.DIAMOND_CHESTPLATE);
        armor.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 4);
        helper.assertTrue(!armor.getAttributeModifiers(EquipmentSlot.CHEST).containsKey(Attributes.ARMOR), "broken armor attribute");
        helper.assertTrue(EnchantmentHelper.getDamageProtection(List.of(armor), helper.getLevel().damageSources().generic()) == 0, "broken Protection");
        armor.setDamageValue(armor.getMaxDamage() - 1);
        helper.assertTrue(armor.getAttributeModifiers(EquipmentSlot.CHEST).containsKey(Attributes.ARMOR), "armor did not restore");
        helper.assertTrue(EnchantmentHelper.getDamageProtection(List.of(armor), helper.getLevel().damageSources().generic()) > 0, "Protection did not restore");
        helper.succeed();
    }
    private static ItemStack broken(Item item) {
        ItemStack stack = new ItemStack(item);
        stack.setDamageValue(stack.getMaxDamage());
        return stack;
    }
}
