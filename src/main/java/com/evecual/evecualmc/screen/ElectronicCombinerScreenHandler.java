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

import com.evecual.evecualmc.mixin.SlotAccessor;

public class ElectronicCombinerScreenHandler extends ScreenHandler {
    public static final int INVENTORY_SIZE = 7;
    private final Inventory inventory;
    private final PropertyDelegate propertyDelegate;

    public static void setSlotPos(Slot slot, int x, int y) {
        if (slot instanceof SlotAccessor sa) {
            sa.setX(x);
            sa.setY(y);
        }
    }

    public void updateSlotPositions(int recipeIndex) {
        CombinerRecipe recipe = CombinerRecipe.getRecipeByIndex(recipeIndex);
        if (recipe == null) {
            for (int i = 0; i < 6; i++) {
                setSlotPos(this.slots.get(i), -999, -999);
            }
            setSlotPos(this.slots.get(6), -999, -999);
            return;
        }
        int[][] coords = recipe.getSlotCoordinates();
        for (int i = 0; i < 6; i++) {
            if (i < coords.length && coords[i] != null) {
                setSlotPos(this.slots.get(i), coords[i][0], coords[i][1]);
            } else {
                setSlotPos(this.slots.get(i), -999, -999);
            }
        }
        // Output slot (Slot 6) on the right
        setSlotPos(this.slots.get(6), 210, 37);
    }

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

        // 6 Input Slots: Dynamically positioned on the machine per blueprint
        for (int i = 0; i < 6; i++) {
            final int slotIndex = i;
            this.addSlot(new Slot(inventory, i, 40 + (i % 3) * 20, 25 + (i / 3) * 22) {
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
        this.addSlot(new Slot(inventory, 6, 210, 37) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return false;
            }

            @Override
            public boolean isEnabled() {
                return getSelectedRecipeIndex() > 0;
            }
        });

        // Player Inventory: Centered horizontally in 240px wide GUI (offset 39px)
        for (int m = 0; m < 3; ++m) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + m * 9 + 9, 39 + l * 18, 86 + m * 18));
            }
        }

        // Player Hotbar: Centered horizontally in 240px wide GUI (offset 39px)
        for (int m = 0; m < 9; ++m) {
            this.addSlot(new Slot(playerInventory, m, 39 + m * 18, 146));
        }

        // Initialize slots according to selected recipe
        updateSlotPositions(getSelectedRecipeIndex());
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
        updateSlotPositions(recipeIndex);

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
