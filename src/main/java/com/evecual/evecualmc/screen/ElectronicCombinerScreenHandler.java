package com.evecual.evecualmc.screen;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.block.entity.ElectronicCombinerBlockEntity;
import com.evecual.evecualmc.recipe.CombinerRecipe;
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
        this(syncId, playerInventory, new SimpleInventory(INVENTORY_SIZE), new ArrayPropertyDelegate(5));
    }

    public ElectronicCombinerScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, PropertyDelegate delegate) {
        super(EvecualMC.ELECTRONIC_COMBINER_SCREEN_HANDLER, syncId);
        checkSize(inventory, INVENTORY_SIZE);
        this.inventory = inventory;
        this.propertyDelegate = delegate;
        inventory.onOpen(playerInventory.player);
        this.addProperties(delegate);

        // 6 Input Slots: Row 1 (y=21), Row 2 (y=43)
        // Strictly filtered per selected recipe
        for (int i = 0; i < 6; i++) {
            final int slotIndex = i;
            int slotX = 24 + (i % 3) * 20;
            int slotY = 21 + (i / 3) * 22;
            this.addSlot(new Slot(inventory, i, slotX, slotY) {
                @Override
                public boolean canInsert(ItemStack stack) {
                    CombinerRecipe recipe = getSelectedRecipe();
                    if (recipe == null) return false;
                    return recipe.isValidInput(slotIndex, stack);
                }

                @Override
                public boolean isEnabled() {
                    return getSelectedRecipeIndex() > 0;
                }
            });
        }

        // Output Slot (Slot 6)
        this.addSlot(new Slot(inventory, 6, 129, 34) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return false;
            }

            @Override
            public boolean isEnabled() {
                return getSelectedRecipeIndex() > 0;
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

    public int getSelectedRecipeIndex() {
        return this.propertyDelegate.get(4);
    }

    public CombinerRecipe getSelectedRecipe() {
        return CombinerRecipe.getRecipeByIndex(getSelectedRecipeIndex());
    }

    public boolean isCrafting() {
        return getProgress() > 0;
    }

    public void selectRecipe(int recipeIndex, PlayerEntity player) {
        if (this.inventory instanceof ElectronicCombinerBlockEntity be) {
            be.setSelectedRecipeIndex(recipeIndex);
        } else {
            this.propertyDelegate.set(4, recipeIndex);
        }

        CombinerRecipe newRecipe = CombinerRecipe.getRecipeByIndex(recipeIndex);
        // Refund any items that do not match the new recipe to player inventory
        for (int i = 0; i < 6; i++) {
            ItemStack current = this.inventory.getStack(i);
            if (!current.isEmpty()) {
                if (newRecipe == null || !newRecipe.isValidInput(i, current)) {
                    player.getInventory().offerOrDrop(current);
                    this.inventory.setStack(i, ItemStack.EMPTY);
                }
            }
        }
        this.inventory.markDirty();
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
            } else {
                CombinerRecipe recipe = getSelectedRecipe();
                if (recipe == null) {
                    return ItemStack.EMPTY;
                }
                boolean inserted = false;
                for (int i = 0; i < 6; i++) {
                    CombinerRecipe.SlotRequirement req = recipe.getSlotRequirement(i);
                    if (req != null && req.isValid(originalStack)) {
                        Slot targetSlot = this.slots.get(i);
                        if (targetSlot.canInsert(originalStack)) {
                            if (this.insertItem(originalStack, i, i + 1, false)) {
                                inserted = true;
                                if (originalStack.isEmpty()) break;
                            }
                        }
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

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }
}
