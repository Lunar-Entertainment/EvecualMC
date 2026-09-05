package com.evecual.evecualmc.screen;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.entity.HeliEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class HeliUpgradeScreenHandler extends ScreenHandler {
    public static final int UPGRADE_SLOTS_COUNT = 5;
    private final Inventory upgradeInventory;
    private final HeliEntity heli;

    public HeliUpgradeScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(UPGRADE_SLOTS_COUNT), null);
    }

    public HeliUpgradeScreenHandler(int syncId, PlayerInventory playerInventory, Inventory upgradeInventory, HeliEntity heli) {
        super(EvecualMC.HELI_UPGRADE_SCREEN_HANDLER, syncId);
        checkSize(upgradeInventory, UPGRADE_SLOTS_COUNT);
        this.upgradeInventory = upgradeInventory;
        this.heli = heli;
        upgradeInventory.onOpen(playerInventory.player);

        // Slot 0: Speed Mod (Turbo Engine) - Top Left
        this.addSlot(new Slot(upgradeInventory, 0, 30, 26) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.isOf(EvecualMC.UPGRADED_ENGINE) || stack.isOf(EvecualMC.ENGINE);
            }
            @Override
            public int getMaxItemCount() {
                return 1;
            }
        });

        // Slot 1: Cargo Mod (Trunk Upgrade) - Mid Left
        this.addSlot(new Slot(upgradeInventory, 1, 30, 50) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.isOf(EvecualMC.TRUNK_UPGRADE);
            }
            @Override
            public int getMaxItemCount() {
                return 1;
            }
        });

        // Slot 2: Energy Storage Mod (Battery Block / Cell) - Bottom Left
        this.addSlot(new Slot(upgradeInventory, 2, 30, 74) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.isOf(EvecualMC.BATTERY_ITEM);
            }
            @Override
            public int getMaxItemCount() {
                return 4;
            }
        });

        // Slot 3: Left Wing Arm Hardpoint (Mining Drill or Weapon) - Mid Right Top
        this.addSlot(new Slot(upgradeInventory, 3, 130, 36) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.isOf(EvecualMC.HELI_MINING_ARM) || stack.isOf(EvecualMC.HELI_WEAPON_ARM);
            }
            @Override
            public int getMaxItemCount() {
                return 1;
            }
        });

        // Slot 4: Right Wing Arm Hardpoint (Mining Drill or Weapon) - Mid Right Bottom
        this.addSlot(new Slot(upgradeInventory, 4, 130, 64) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.isOf(EvecualMC.HELI_MINING_ARM) || stack.isOf(EvecualMC.HELI_WEAPON_ARM);
            }
            @Override
            public int getMaxItemCount() {
                return 1;
            }
        });

        // Player Inventory (3 rows of 9)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 108 + row * 18));
            }
        }

        // Player Hotbar (1 row of 9)
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 166));
        }
    }

    public HeliEntity getHeli() {
        return this.heli;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.upgradeInventory.canPlayerUse(player);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();

            if (index < UPGRADE_SLOTS_COUNT) {
                // Moving from upgrade slots to player inventory
                if (!this.insertItem(originalStack, UPGRADE_SLOTS_COUNT, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Moving from player inventory to upgrade slots
                boolean moved = false;
                for (int u = 0; u < UPGRADE_SLOTS_COUNT; u++) {
                    Slot targetSlot = this.slots.get(u);
                    if (targetSlot.canInsert(originalStack)) {
                        if (this.insertItem(originalStack, u, u + 1, false)) {
                            moved = true;
                            break;
                        }
                    }
                }
                if (!moved) {
                    if (index < UPGRADE_SLOTS_COUNT + 27) {
                        if (!this.insertItem(originalStack, UPGRADE_SLOTS_COUNT + 27, this.slots.size(), false)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (!this.insertItem(originalStack, UPGRADE_SLOTS_COUNT, UPGRADE_SLOTS_COUNT + 27, false)) {
                        return ItemStack.EMPTY;
                    }
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
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        this.upgradeInventory.onClose(player);
        if (this.heli != null) {
            this.heli.syncUpgrades();
        }
    }
}
