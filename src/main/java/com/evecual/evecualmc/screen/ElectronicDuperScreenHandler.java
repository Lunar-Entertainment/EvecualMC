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

public class ElectronicDuperScreenHandler extends ScreenHandler {
    private final Inventory inventory;
    private final PropertyDelegate propertyDelegate;

    public ElectronicDuperScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(2), new ArrayPropertyDelegate(4));
    }

    public ElectronicDuperScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, PropertyDelegate propertyDelegate) {
        super(EvecualMC.ELECTRONIC_DUPER_SCREEN_HANDLER, syncId);
        checkSize(inventory, 2);
        this.inventory = inventory;
        this.propertyDelegate = propertyDelegate;
        inventory.onOpen(playerInventory.player);
        addProperties(propertyDelegate);

        // Slot 0: Input Slot (x: 44, y: 35)
        this.addSlot(new Slot(inventory, 0, 44, 35));

        // Slot 1: Output Slot (x: 132, y: 35)
        this.addSlot(new Slot(inventory, 1, 132, 35) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return false;
            }
        });

        // Player Inventory (3 rows x 9 columns)
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        // Player Hotbar (1 row x 9 columns)
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    public int getEnergy() {
        return (this.propertyDelegate.get(0) & 0xFFFF) | ((this.propertyDelegate.get(1) & 0xFFFF) << 16);
    }

    public int getProgressTicks() {
        return this.propertyDelegate.get(2);
    }

    public boolean isDuplicating() {
        return this.propertyDelegate.get(3) == 1;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);

        if (slot != null && slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();

            if (invSlot < 2) {
                // From machine slots to player inventory
                if (!this.insertItem(originalStack, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // From player inventory to Input Slot (Slot 0)
                if (!this.insertItem(originalStack, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (originalStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }

            if (originalStack.getCount() == newStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTakeItem(player, originalStack);
        }

        return newStack;
    }
}
