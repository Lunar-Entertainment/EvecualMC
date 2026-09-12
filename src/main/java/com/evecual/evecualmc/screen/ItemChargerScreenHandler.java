package com.evecual.evecualmc.screen;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.energy.ItemEnergyHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class ItemChargerScreenHandler extends ScreenHandler {
    private final Inventory inventory;
    private final PropertyDelegate propertyDelegate;

    public ItemChargerScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(1), new ArrayPropertyDelegate(9));
    }

    public ItemChargerScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, PropertyDelegate propertyDelegate) {
        super(EvecualMC.ITEM_CHARGER_SCREEN_HANDLER, syncId);
        checkSize(inventory, 1);
        this.inventory = inventory;
        this.propertyDelegate = propertyDelegate;
        inventory.onOpen(playerInventory.player);
        addProperties(propertyDelegate);

        // Slot 0: Item Charging Cradle Slot (Center: x: 80, y: 35)
        this.addSlot(new Slot(inventory, 0, 80, 35) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return ItemEnergyHelper.isChargeable(stack);
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

    public int getMaxEnergy() {
        int val = (this.propertyDelegate.get(2) & 0xFFFF) | ((this.propertyDelegate.get(3) & 0xFFFF) << 16);
        return val > 0 ? val : 4000;
    }

    public int getItemEnergy() {
        return (this.propertyDelegate.get(4) & 0xFFFF) | ((this.propertyDelegate.get(5) & 0xFFFF) << 16);
    }

    public int getItemMaxEnergy() {
        int val = (this.propertyDelegate.get(6) & 0xFFFF) | ((this.propertyDelegate.get(7) & 0xFFFF) << 16);
        return val > 0 ? val : 1000;
    }

    public boolean isActive() {
        return this.propertyDelegate.get(8) == 1;
    }

    public ItemStack getChargingItem() {
        return this.inventory.getStack(0);
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

            if (invSlot == 0) {
                // From charging cradle to player inventory
                if (!this.insertItem(originalStack, 1, 37, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // From player inventory to charging cradle (0)
                if (ItemEnergyHelper.isChargeable(originalStack)) {
                    if (!this.insertItem(originalStack, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
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
