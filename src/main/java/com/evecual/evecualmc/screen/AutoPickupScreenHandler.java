package com.evecual.evecualmc.screen;

import com.evecual.evecualmc.EvecualMC;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class AutoPickupScreenHandler extends ScreenHandler {
    private final Inventory inventory;

    public AutoPickupScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(9));
    }

    public AutoPickupScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory) {
        super(EvecualMC.AUTO_PICKUP_SCREEN_HANDLER, syncId);
        checkSize(inventory, 9);
        this.inventory = inventory;
        inventory.onOpen(playerInventory.player);

        // 9 Filter Slots (1 row x 9 columns)
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(inventory, i, 8 + i * 18, 36) {
                @Override
                public int getMaxItemCount() {
                    return 1;
                }
            });
        }

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

            if (invSlot < 9) {
                // Moving from Filter slots to Player inventory
                if (!this.insertItem(originalStack, 9, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Moving from Player inventory to Filter slots (limit to 1 item per slot)
                boolean inserted = false;
                for (int i = 0; i < 9; i++) {
                    Slot filterSlot = this.slots.get(i);
                    if (!filterSlot.hasStack()) {
                        ItemStack singleStack = originalStack.copy();
                        singleStack.setCount(1);
                        filterSlot.setStack(singleStack);
                        originalStack.decrement(1);
                        inserted = true;
                        break;
                    }
                }
                if (!inserted) {
                    return ItemStack.EMPTY;
                }
            }

            if (originalStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }

        return newStack;
    }
}
