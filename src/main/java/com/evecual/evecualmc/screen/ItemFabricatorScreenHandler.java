package com.evecual.evecualmc.screen;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.block.entity.ItemFabricatorBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class ItemFabricatorScreenHandler extends ScreenHandler {
    private final Inventory inventory;
    private final PropertyDelegate propertyDelegate;

    public ItemFabricatorScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(ItemFabricatorBlockEntity.INVENTORY_SIZE), new ArrayPropertyDelegate(10));
    }

    public ItemFabricatorScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, PropertyDelegate propertyDelegate) {
        super(EvecualMC.ITEM_FABRICATOR_SCREEN_HANDLER, syncId);
        checkSize(inventory, ItemFabricatorBlockEntity.INVENTORY_SIZE);
        this.inventory = inventory;
        this.propertyDelegate = propertyDelegate;
        inventory.onOpen(playerInventory.player);
        addProperties(propertyDelegate);

        // Input slots 0..5 (3 columns x 2 rows)
        // Row 0
        this.addSlot(new Slot(inventory, 0, 44, 34));
        this.addSlot(new Slot(inventory, 1, 62, 34));
        this.addSlot(new Slot(inventory, 2, 80, 34));
        // Row 1
        this.addSlot(new Slot(inventory, 3, 44, 52));
        this.addSlot(new Slot(inventory, 4, 62, 52));
        this.addSlot(new Slot(inventory, 5, 80, 52));

        // Output slot 6 (x: 134, y: 43)
        this.addSlot(new Slot(inventory, 6, 134, 43) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return false;
            }
        });

        // Player Inventory (3 rows x 9 columns) at y = 112
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 112 + i * 18));
            }
        }

        // Player Hotbar (1 row x 9 columns) at y = 170
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 170));
        }
    }

    public int getEnergy() {
        return (this.propertyDelegate.get(0) & 0xFFFF) | ((this.propertyDelegate.get(1) & 0xFFFF) << 16);
    }

    public int getMaxEnergy() {
        int val = (this.propertyDelegate.get(2) & 0xFFFF) | ((this.propertyDelegate.get(3) & 0xFFFF) << 16);
        return val > 0 ? val : ItemFabricatorBlockEntity.MAX_ENERGY;
    }

    public int getProgressTicks() {
        return (this.propertyDelegate.get(4) & 0xFFFF) | ((this.propertyDelegate.get(5) & 0xFFFF) << 16);
    }

    public int getTotalTicks() {
        int val = (this.propertyDelegate.get(6) & 0xFFFF) | ((this.propertyDelegate.get(7) & 0xFFFF) << 16);
        return val > 0 ? val : 100;
    }

    public boolean isActive() {
        return this.propertyDelegate.get(8) == 1;
    }

    public int getSelectedRecipe() {
        return this.propertyDelegate.get(9);
    }

    public ItemStack getStackInSlot(int slot) {
        return this.inventory.getStack(slot);
    }

    @Override
    public boolean onButtonClick(PlayerEntity player, int id) {
        if (id == ItemFabricatorBlockEntity.RECIPE_ZAPPER || id == ItemFabricatorBlockEntity.RECIPE_RAILGUN || id == ItemFabricatorBlockEntity.RECIPE_DUPER) {
            if (this.inventory instanceof ItemFabricatorBlockEntity be) {
                be.setSelectedRecipe(id);
            } else {
                this.propertyDelegate.set(9, id);
            }
            return true;
        }
        return false;
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

            if (invSlot < 7) {
                // From machine slots to player inventory
                if (!this.insertItem(originalStack, 7, 43, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // From player inventory to input slots (0..5)
                if (!this.insertItem(originalStack, 0, 6, false)) {
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
