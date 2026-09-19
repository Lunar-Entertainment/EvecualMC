package com.evecual.evecualmc.util;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

/**
 * Interface implemented by PlayerEntity to hold dedicated crown inventory data.
 */
public interface CrownHolder {
    Inventory evecualmc$getCrownInventory();
    ItemStack evecualmc$getCrown();
    void evecualmc$setCrown(ItemStack stack);
}
