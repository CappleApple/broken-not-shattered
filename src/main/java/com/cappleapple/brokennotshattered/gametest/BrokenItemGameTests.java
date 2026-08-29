package com.cappleapple.brokennotshattered.gametest;

import com.cappleapple.brokennotshattered.BrokenNotShattered;
import com.cappleapple.brokennotshattered.core.BrokenState;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(BrokenNotShattered.MOD_ID)
@PrefixGameTestTemplate(false)
public final class BrokenItemGameTests {
    private static final String EMPTY_TEMPLATE = "bastion/mobs/empty";

    private BrokenItemGameTests() {
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE)
    public static void durabilityPreservationAndStoredState(GameTestHelper helper) {
        List<Item> items = List.of(
            Items.WOODEN_PICKAXE,
            Items.DIAMOND_PICKAXE,
            Items.NETHERITE_PICKAXE,
            Items.IRON_SWORD,
            Items.IRON_CHESTPLATE,
            Items.BOW,
            Items.CROSSBOW,
            Items.SHIELD,
            Items.FISHING_ROD,
            Items.SHEARS,
            Items.FLINT_AND_STEEL,
            Items.BRUSH,
            Items.TRIDENT,
            Items.ELYTRA
        );

        Holder<?> mending = helper.getLevel()
            .registryAccess()
            .lookupOrThrow(Registries.ENCHANTMENT)
            .getOrThrow(Enchantments.MENDING);

        for (Item item : items) {
            ItemStack stack = new ItemStack(item);
            stack.setDamageValue(stack.getMaxDamage() - 1);
            stack.set(DataComponents.CUSTOM_NAME, Component.literal("Kept name"));
            CompoundTag storedData = new CompoundTag();
            storedData.putString("broken_not_shattered_test", item.toString());
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(storedData));

            @SuppressWarnings("unchecked")
            Holder<net.minecraft.world.item.enchantment.Enchantment> typedMending =
                (Holder<net.minecraft.world.item.enchantment.Enchantment>) mending;
            EnchantmentHelper.updateEnchantments(stack, mutable -> mutable.set(typedMending, 1));

            AtomicInteger breakCallbacks = new AtomicInteger();
            ItemStack sameReference = stack;
            stack.hurtAndBreak(
                1,
                helper.getLevel(),
                (LivingEntity) null,
                brokenItem -> breakCallbacks.incrementAndGet()
            );

            helper.assertTrue(stack == sameReference, item + " was replaced");
            helper.assertFalse(stack.isEmpty(), item + " disappeared at zero durability");
            helper.assertValueEqual(stack.getCount(), 1, item + " count changed");
            helper.assertValueEqual(stack.getDamageValue(), stack.getMaxDamage(), item + " did not reach zero remaining durability");
            helper.assertTrue(BrokenState.isBroken(stack), item + " was not recognized as broken");
            helper.assertValueEqual(breakCallbacks.get(), 1, item + " did not invoke its break callback exactly once");
            helper.assertValueEqual(stack.getHoverName().getString(), "Kept name", item + " lost its custom name");
            helper.assertValueEqual(stack.getEnchantmentLevel(typedMending), 1, item + " lost its enchantment");
            helper.assertTrue(
                stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).contains("broken_not_shattered_test"),
                item + " lost custom component data"
            );

            stack.hurtAndBreak(
                50,
                helper.getLevel(),
                (LivingEntity) null,
                brokenItem -> breakCallbacks.incrementAndGet()
            );
            helper.assertValueEqual(stack.getDamageValue(), stack.getMaxDamage(), item + " overflowed while already broken");
            helper.assertValueEqual(breakCallbacks.get(), 1, item + " replayed the break callback while already broken");
        }

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE)
    public static void onePointRepairRestoresToolBehavior(GameTestHelper helper) {
        ItemStack pickaxe = new ItemStack(Items.DIAMOND_PICKAXE);
        pickaxe.setDamageValue(pickaxe.getMaxDamage());

        helper.assertTrue(BrokenState.isBroken(pickaxe), "depleted pickaxe was not broken");
        helper.assertValueEqual(pickaxe.getDestroySpeed(Blocks.DIAMOND_ORE.defaultBlockState()), 1.0F, "broken mining speed was not hand speed");
        helper.assertFalse(pickaxe.isCorrectToolForDrops(Blocks.DIAMOND_ORE.defaultBlockState()), "broken pickaxe qualified for drops");
        helper.assertFalse(pickaxe.canPerformAction(ItemAbilities.PICKAXE_DIG), "broken pickaxe retained its ItemAbility");

        pickaxe.setDamageValue(pickaxe.getMaxDamage() - 1);

        helper.assertFalse(BrokenState.isBroken(pickaxe), "one-point repair did not reactivate the pickaxe");
        helper.assertTrue(pickaxe.getDestroySpeed(Blocks.DIAMOND_ORE.defaultBlockState()) > 1.0F, "repaired mining speed did not return");
        helper.assertTrue(pickaxe.isCorrectToolForDrops(Blocks.DIAMOND_ORE.defaultBlockState()), "repaired pickaxe did not qualify for drops");
        helper.assertTrue(pickaxe.canPerformAction(ItemAbilities.PICKAXE_DIG), "repaired pickaxe ability did not return");
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE)
    public static void brokenItemsSuppressGenericAbilities(GameTestHelper helper) {
        List<AbilityCase> cases = List.of(
            new AbilityCase(Items.IRON_AXE, ItemAbilities.AXE_STRIP),
            new AbilityCase(Items.IRON_HOE, ItemAbilities.HOE_TILL),
            new AbilityCase(Items.IRON_SHOVEL, ItemAbilities.SHOVEL_FLATTEN),
            new AbilityCase(Items.IRON_SWORD, ItemAbilities.SWORD_SWEEP),
            new AbilityCase(Items.SHEARS, ItemAbilities.SHEARS_HARVEST),
            new AbilityCase(Items.SHIELD, ItemAbilities.SHIELD_BLOCK),
            new AbilityCase(Items.FISHING_ROD, ItemAbilities.FISHING_ROD_CAST),
            new AbilityCase(Items.TRIDENT, ItemAbilities.TRIDENT_THROW),
            new AbilityCase(Items.BRUSH, ItemAbilities.BRUSH_BRUSH),
            new AbilityCase(Items.FLINT_AND_STEEL, ItemAbilities.FIRESTARTER_LIGHT)
        );

        for (AbilityCase abilityCase : cases) {
            ItemStack stack = new ItemStack(abilityCase.item());
            helper.assertTrue(stack.canPerformAction(abilityCase.ability()), abilityCase.item() + " lacked its expected baseline ability");
            stack.setDamageValue(stack.getMaxDamage());
            helper.assertFalse(stack.canPerformAction(abilityCase.ability()), abilityCase.item() + " retained ability while broken");
            stack.setDamageValue(stack.getMaxDamage() - 1);
            helper.assertTrue(stack.canPerformAction(abilityCase.ability()), abilityCase.item() + " ability did not return after repair");
        }

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE)
    public static void brokenCombatAttributesAreTemporary(GameTestHelper helper) {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        helper.assertTrue(hasAttribute(sword.getAttributeModifiers(), Attributes.ATTACK_DAMAGE), "baseline sword attack damage missing");
        helper.assertTrue(hasAttribute(sword.getAttributeModifiers(), Attributes.ATTACK_SPEED), "baseline sword attack speed missing");

        sword.setDamageValue(sword.getMaxDamage());
        helper.assertFalse(hasAttribute(sword.getAttributeModifiers(), Attributes.ATTACK_DAMAGE), "broken sword retained attack damage");
        helper.assertFalse(hasAttribute(sword.getAttributeModifiers(), Attributes.ATTACK_SPEED), "broken sword retained attack speed");

        sword.setDamageValue(sword.getMaxDamage() - 1);
        helper.assertTrue(hasAttribute(sword.getAttributeModifiers(), Attributes.ATTACK_DAMAGE), "repaired sword attack damage did not return");
        helper.assertTrue(hasAttribute(sword.getAttributeModifiers(), Attributes.ATTACK_SPEED), "repaired sword attack speed did not return");

        ItemStack chestplate = new ItemStack(Items.DIAMOND_CHESTPLATE);
        chestplate.setDamageValue(chestplate.getMaxDamage());
        helper.assertFalse(hasAttribute(chestplate.getAttributeModifiers(), Attributes.ARMOR), "broken armor retained armor points");
        helper.assertFalse(hasAttribute(chestplate.getAttributeModifiers(), Attributes.ARMOR_TOUGHNESS), "broken armor retained toughness");

        chestplate.setDamageValue(chestplate.getMaxDamage() - 1);
        helper.assertTrue(hasAttribute(chestplate.getAttributeModifiers(), Attributes.ARMOR), "repaired armor points did not return");
        helper.assertTrue(hasAttribute(chestplate.getAttributeModifiers(), Attributes.ARMOR_TOUGHNESS), "repaired armor toughness did not return");

        var protection = helper.getLevel()
            .registryAccess()
            .lookupOrThrow(Registries.ENCHANTMENT)
            .getOrThrow(Enchantments.PROTECTION);
        EnchantmentHelper.updateEnchantments(chestplate, mutable -> mutable.set(protection, 4));
        LivingEntity wearer = helper.makeMockPlayer(GameType.SURVIVAL);
        wearer.setItemSlot(EquipmentSlot.CHEST, chestplate);
        helper.assertTrue(
            EnchantmentHelper.getDamageProtection(helper.getLevel(), wearer, helper.getLevel().damageSources().generic()) > 0.0F,
            "repaired armor protection enchantment did not apply"
        );
        chestplate.setDamageValue(chestplate.getMaxDamage());
        helper.assertValueEqual(
            EnchantmentHelper.getDamageProtection(helper.getLevel(), wearer, helper.getLevel().damageSources().generic()),
            0.0F,
            "broken armor retained defensive enchantment protection"
        );
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE)
    public static void brokenUseItemsAndElytraAreInactive(GameTestHelper helper) {
        LivingEntity mockPlayer = helper.makeMockPlayer(GameType.SURVIVAL);
        for (Item item : List.of(Items.BOW, Items.CROSSBOW, Items.TRIDENT, Items.SHIELD, Items.BRUSH)) {
            ItemStack stack = new ItemStack(item);
            stack.setDamageValue(stack.getMaxDamage());
            helper.assertValueEqual(stack.getUseDuration(mockPlayer), 0, item + " retained use duration while broken");
            stack.setDamageValue(stack.getMaxDamage() - 1);
            helper.assertTrue(stack.getUseDuration(mockPlayer) > 0, item + " use duration did not return after repair");
        }

        ItemStack elytra = new ItemStack(Items.ELYTRA);
        elytra.setDamageValue(elytra.getMaxDamage());
        helper.assertFalse(elytra.canElytraFly(mockPlayer), "broken elytra allowed flight");
        elytra.setDamageValue(elytra.getMaxDamage() - 2);
        helper.assertTrue(elytra.canElytraFly(mockPlayer), "vanilla-safe repaired elytra did not allow flight");
        helper.succeed();
    }

    private static boolean hasAttribute(ItemAttributeModifiers modifiers, Holder<Attribute> attribute) {
        return modifiers.modifiers().stream().anyMatch(entry -> entry.attribute().equals(attribute));
    }

    private record AbilityCase(Item item, ItemAbility ability) {
    }
}
