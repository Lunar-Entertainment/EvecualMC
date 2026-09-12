package com.evecual.evecualmc.screen;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.block.entity.StorageUnitBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

import java.util.ArrayList;
import java.util.List;

public class StorageUnitScreenHandler extends ScreenHandler {
    public static final int PAGE_SIZE = 54;
    private final Inventory inventory;
    private final PropertyDelegate propertyDelegate;
    private final List<StorageUnitBlockEntity> cluster = new ArrayList<>();
    private int currentPage = 0;

    // Client-side constructor
    public StorageUnitScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(PAGE_SIZE), new ArrayPropertyDelegate(4));
    }

    // Server-side constructor
    public StorageUnitScreenHandler(int syncId, PlayerInventory playerInventory, StorageUnitBlockEntity storage) {
        this(syncId, playerInventory, storage, new PropertyDelegate() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> (int) storage.getEnergy();
                    case 1 -> storage.findConnectedCluster().size();
                    case 2 -> 0; // Page managed server-side
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {}

            @Override
            public int size() {
                return 4;
            }
        });
    }

    public StorageUnitScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, PropertyDelegate propertyDelegate) {
        super(EvecualMC.STORAGE_UNIT_SCREEN_HANDLER, syncId);
        checkSize(inventory, PAGE_SIZE);
        this.inventory = inventory;
        this.propertyDelegate = propertyDelegate;
        this.addProperties(propertyDelegate);

        if (inventory instanceof StorageUnitBlockEntity storage) {
            this.cluster.addAll(storage.findConnectedCluster());
        }

        // 1. Storage Vault slots (6 rows of 9 slots = 54 slots)
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 9; col++) {
                int index = col + row * 9;
                this.addSlot(new DynamicVaultSlot(this, index, 8 + col * 18, 18 + row * 18));
            }
        }

        // 2. Player Inventory (3 rows of 9 slots)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 140 + row * 18));
            }
        }

        // 3. Player Hotbar (9 slots)
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 198));
        }
    }

    public int getEnergy() {
        return this.propertyDelegate.get(0);
    }

    public int getConnectedUnits() {
        return Math.max(1, this.propertyDelegate.get(1));
    }

    public int getTotalPages() {
        return getConnectedUnits() * 2;
    }

    public int getCurrentPage() {
        return this.currentPage;
    }

    public void setPage(int page) {
        int maxPages = getTotalPages();
        this.currentPage = Math.max(0, Math.min(maxPages - 1, page));
        this.sendContentUpdates();
    }

    @Override
    public boolean onButtonClick(PlayerEntity player, int id) {
        int max = getTotalPages();
        if (id == 0) {
            // Previous Page
            this.currentPage = (this.currentPage - 1 + max) % max;
            this.sendContentUpdates();
            return true;
        } else if (id == 1) {
            // Next Page
            this.currentPage = (this.currentPage + 1) % max;
            this.sendContentUpdates();
            return true;
        }
        return false;
    }

    public ItemStack getClusterStack(int slotOnPage) {
        if (this.cluster.isEmpty()) {
            return this.inventory.getStack(slotOnPage);
        }
        int unitIndex = this.currentPage / 2;
        if (unitIndex >= this.cluster.size()) unitIndex = 0;
        StorageUnitBlockEntity target = this.cluster.get(unitIndex);
        int internalSlot = (this.currentPage % 2) * PAGE_SIZE + slotOnPage;
        return target.getStack(internalSlot);
    }

    public void setClusterStack(int slotOnPage, ItemStack stack) {
        if (this.cluster.isEmpty()) {
            this.inventory.setStack(slotOnPage, stack);
            return;
        }
        int unitIndex = this.currentPage / 2;
        if (unitIndex >= this.cluster.size()) unitIndex = 0;
        StorageUnitBlockEntity target = this.cluster.get(unitIndex);
        int internalSlot = (this.currentPage % 2) * PAGE_SIZE + slotOnPage;
        target.setStack(internalSlot, stack);
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

            if (invSlot < PAGE_SIZE) {
                // Moving from Vault into Player Inventory
                if (!this.insertItem(originalStack, PAGE_SIZE, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Moving from Player Inventory into Vault
                if (!this.insertItem(originalStack, 0, PAGE_SIZE, false)) {
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

    private static class DynamicVaultSlot extends Slot {
        private final StorageUnitScreenHandler handler;
        private final int slotOnPage;

        public DynamicVaultSlot(StorageUnitScreenHandler handler, int slotOnPage, int x, int y) {
            super(handler.inventory, slotOnPage, x, y);
            this.handler = handler;
            this.slotOnPage = slotOnPage;
        }

        @Override
        public ItemStack getStack() {
            return this.handler.getClusterStack(this.slotOnPage);
        }

        @Override
        public void setStack(ItemStack stack) {
            this.handler.setClusterStack(this.slotOnPage, stack);
            this.markDirty();
        }

        @Override
        public void markDirty() {
            super.markDirty();
        }
    }
}
