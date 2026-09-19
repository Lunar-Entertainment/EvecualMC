package com.evecual.evecualmc.item.crown;

import com.evecual.evecualmc.EvecualMC;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

import java.util.function.Supplier;

public enum CrownArmorMaterial implements ArmorMaterial {
    MECHANICALS(
            "evecualmc:crown_mechanicals",
            30,
            new int[]{3, 6, 7, 4}, // boots, leggings, chestplate, helmet
            20,
            SoundEvents.ITEM_ARMOR_EQUIP_IRON,
            2.0f,
            0.1f,
            () -> Ingredient.ofItems(EvecualMC.STEEL_INGOT)
    ),
    ELECTRICIANS(
            "evecualmc:crown_electricians",
            35,
            new int[]{4, 7, 8, 5},
            25,
            SoundEvents.ITEM_ARMOR_EQUIP_GOLD,
            2.5f,
            0.1f,
            () -> Ingredient.ofItems(EvecualMC.ELACTORITE)
    ),
    CASTLES(
            "evecualmc:crown_castles",
            45,
            new int[]{4, 7, 9, 6},
            15,
            SoundEvents.ITEM_ARMOR_EQUIP_NETHERITE,
            3.5f,
            0.3f,
            () -> Ingredient.ofItems(Items.STONE_BRICKS, EvecualMC.STEEL_INGOT)
    );

    private static final int[] BASE_DURABILITY = new int[]{13, 15, 16, 11};
    private final String name;
    private final int durabilityMultiplier;
    private final int[] protectionAmounts;
    private final int enchantability;
    private final SoundEvent equipSound;
    private final float toughness;
    private final float knockbackResistance;
    private final Supplier<Ingredient> repairIngredientSupplier;

    CrownArmorMaterial(String name, int durabilityMultiplier, int[] protectionAmounts, int enchantability,
                       SoundEvent equipSound, float toughness, float knockbackResistance,
                       Supplier<Ingredient> repairIngredientSupplier) {
        this.name = name;
        this.durabilityMultiplier = durabilityMultiplier;
        this.protectionAmounts = protectionAmounts;
        this.enchantability = enchantability;
        this.equipSound = equipSound;
        this.toughness = toughness;
        this.knockbackResistance = knockbackResistance;
        this.repairIngredientSupplier = repairIngredientSupplier;
    }

    @Override
    public int getDurability(ArmorItem.Type type) {
        return BASE_DURABILITY[type.getEquipmentSlot().getEntitySlotId()] * this.durabilityMultiplier;
    }

    @Override
    public int getProtection(ArmorItem.Type type) {
        return this.protectionAmounts[type.getEquipmentSlot().getEntitySlotId()];
    }

    @Override
    public int getEnchantability() {
        return this.enchantability;
    }

    @Override
    public SoundEvent getEquipSound() {
        return this.equipSound;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return this.repairIngredientSupplier.get();
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public float getToughness() {
        return this.toughness;
    }

    @Override
    public float getKnockbackResistance() {
        return this.knockbackResistance;
    }
}
