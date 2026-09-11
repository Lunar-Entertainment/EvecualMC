package com.evecual.evecualmc.turret;

import com.evecual.evecualmc.EvecualMC;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public enum AmmoType {
    COPPER(6.0f, 0.0f, "Copper Ammo", 0xFFD97706, 0.8f),
    IRON(14.0f, 0.2f, "Iron Ammo", 0xFFE2E8F0, 1.2f),
    DIAMOND(30.0f, 0.6f, "Diamond Ammo", 0xFF00E5FF, 1.8f);

    private final float damage;
    private final float armorPenetration;
    private final String displayName;
    private final int color;
    private final float velocityMultiplier;

    AmmoType(float damage, float armorPenetration, String displayName, int color, float velocityMultiplier) {
        this.damage = damage;
        this.armorPenetration = armorPenetration;
        this.displayName = displayName;
        this.color = color;
        this.velocityMultiplier = velocityMultiplier;
    }

    public float getDamage() {
        return damage;
    }

    public float getArmorPenetration() {
        return armorPenetration;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getColor() {
        return color;
    }

    public float getVelocityMultiplier() {
        return velocityMultiplier;
    }

    public static AmmoType fromStack(ItemStack stack) {
        if (stack.isEmpty()) return null;
        if (stack.isOf(EvecualMC.DIAMOND_AMMO) || stack.isOf(Items.DIAMOND)) return DIAMOND;
        if (stack.isOf(EvecualMC.IRON_AMMO) || stack.isOf(Items.IRON_INGOT)) return IRON;
        if (stack.isOf(EvecualMC.COPPER_AMMO) || stack.isOf(Items.COPPER_INGOT)) return COPPER;
        return null;
    }
}
