package com.evecual.evecualmc.screen;

import com.evecual.evecualmc.EvecualMC;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class ElectronicCombinerScreenHandler extends ScreenHandler {
    public static final int INVENTORY_SIZE = 7;
    private final Inventory inventory;
    private final PropertyDelegate propertyDelegate;

    public ElectronicCombinerScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(INVENTORY_SIZE), new ArrayPropertyDelegate(4));
    }

    public ElectronicCombinerScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, PropertyDelegate delegate) {
        super(EvecualMC.ELECTRONIC_COMBINER_SCREEN_HANDLER, syncId);
        checkSize(inventory, INVENTORY_SIZE);
        this.inventory = inventory;
        this.propertyDelegate = delegate;
        inventory.onOpen(playerInventory.player);
        this.addProperties(delegate);

        // 6 Input Slots: Row 1 (y=21), Row 2 (y=43)
        this.addSlot(new Slot(inventory, 0, 24, 21)); // 0: Engine
        this.addSlot(new Slot(inventory, 1, 44, 21)); // 1: Hull (Steel / Iron)
        this.addSlot(new Slot(inventory, 2, 64, 21)); // 2: Glass (Clear or Stained)
        this.addSlot(new Slot(inventory, 3, 24, 43)); // 3: Leather
        this.addSlot(new Slot(inventory, 4, 44, 43)); // 4: Colour (Dye - optional, default Red)
        this.addSlot(new Slot(inventory, 5, 64, 43)); // 5: Trunk Upgrade (optional, default Standard)

        // Output Slot (Slot 6)
        this.addSlot(new Slot(inventory, 6, 129, 34) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return false;
            }
        });

        // Player Inventory
        for (int m = 0; m < 3; ++m) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + m * 9 + 9, 8 + l * 18, 84 + m * 18));
            }
        }

        // Player Hotbar
        for (int m = 0; m < 9; ++m) {
            this.addSlot(new Slot(playerInventory, m, 8 + m * 18, 142));
        }
    }

    public int getProgress() {
        return this.propertyDelegate.get(0);
    }

    public int getMaxProgress() {
        return this.propertyDelegate.get(1);
    }

    public int getEnergy() {
        return this.propertyDelegate.get(2);
    }

    public int getMaxEnergy() {
        return this.propertyDelegate.get(3);
    }

    public boolean isCrafting() {
        return getProgress() > 0;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);
        if (slot != null && slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();
            if (invSlot < INVENTORY_SIZE) {
                if (!this.insertItem(originalStack, INVENTORY_SIZE, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(originalStack, 0, 6, false)) {
                return ItemStack.EMPTY;
            }

            if (originalStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }

        return newStack;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }
}
