package com.evecual.evecualmc.util;

import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;

public interface CrownHolder {
    SimpleInventory evecual$getCrownInventory();
    ItemStack evecual$getCrown();
    void evecual$setCrown(ItemStack crown);
    boolean evecual$hasMechanicalCrown();
    boolean evecual$hasBorgersCrown();
    void evecual$syncCrown();
}
