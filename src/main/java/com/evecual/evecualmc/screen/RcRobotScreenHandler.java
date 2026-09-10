package com.evecual.evecualmc.screen;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.entity.RcRobotEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class RcRobotScreenHandler extends ScreenHandler {
    public static final int ARM_SLOTS_COUNT = 2;
    public static final int CARGO_SLOTS_COUNT = 54;
    public static final int TOTAL_ROBOT_SLOTS = ARM_SLOTS_COUNT + CARGO_SLOTS_COUNT; // 56

    private final Inventory armInventory;
    private final Inventory cargoInventory;
    private final RcRobotEntity robot;

    // Client Constructor
    public RcRobotScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(ARM_SLOTS_COUNT), new SimpleInventory(CARGO_SLOTS_COUNT), null);
    }

    // Server Constructor
    public RcRobotScreenHandler(int syncId, PlayerInventory playerInventory, Inventory armInventory, Inventory cargoInventory, RcRobotEntity robot) {
        super(EvecualMC.RC_ROBOT_SCREEN_HANDLER, syncId);
        checkSize(armInventory, ARM_SLOTS_COUNT);
        checkSize(cargoInventory, CARGO_SLOTS_COUNT);

        this.armInventory = armInventory;
        this.cargoInventory = cargoInventory;
        this.robot = robot;

        armInventory.onOpen(playerInventory.player);
        cargoInventory.onOpen(playerInventory.player);

        // --- Slot 0: Left Arm (ONLY Blocks for RMB placement) ---
        this.addSlot(new Slot(armInventory, 0, 190, 36) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.getItem() instanceof BlockItem;
            }

            @Override
            public void onTakeItem(PlayerEntity player, ItemStack stack) {
                super.onTakeItem(player, stack);
                if (RcRobotScreenHandler.this.robot != null) {
                    RcRobotScreenHandler.this.robot.setEquippedLeftArm(getStack());
                }
            }

            @Override
            public void setStack(ItemStack stack) {
                super.setStack(stack);
                if (RcRobotScreenHandler.this.robot != null) {
                    RcRobotScreenHandler.this.robot.setEquippedLeftArm(stack);
                }
            }

            @Override
            public void markDirty() {
                super.markDirty();
                if (RcRobotScreenHandler.this.robot != null) {
                    RcRobotScreenHandler.this.robot.setEquippedLeftArm(getStack());
                }
            }
        });

        // --- Slot 1: Right Arm (ONLY Tools / Weapons / Non-Block Items for LMB action) ---
        this.addSlot(new Slot(armInventory, 1, 190, 92) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return !(stack.getItem() instanceof BlockItem);
            }

            @Override
            public void onTakeItem(PlayerEntity player, ItemStack stack) {
                super.onTakeItem(player, stack);
                if (RcRobotScreenHandler.this.robot != null) {
                    RcRobotScreenHandler.this.robot.setEquippedTool(getStack());
                }
            }

            @Override
            public void setStack(ItemStack stack) {
                super.setStack(stack);
                if (RcRobotScreenHandler.this.robot != null) {
                    RcRobotScreenHandler.this.robot.setEquippedTool(stack);
                }
            }

            @Override
            public void markDirty() {
                super.markDirty();
                if (RcRobotScreenHandler.this.robot != null) {
                    RcRobotScreenHandler.this.robot.setEquippedTool(getStack());
                }
            }
        });

        // --- Slots 2 .. 55: Robot Cargo (54 Slots, 6 rows of 9) ---
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(cargoInventory, col + row * 9, 8 + col * 18, 18 + row * 18));
            }
        }

        // --- Slots 56 .. 82: Player Inventory (3 rows of 9) ---
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 140 + row * 18));
            }
        }

        // --- Slots 83 .. 91: Player Hotbar (1 row of 9) ---
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 198));
        }
    }

    public RcRobotEntity getRobot() {
        return this.robot;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.cargoInventory.canPlayerUse(player) && this.armInventory.canPlayerUse(player);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();

            if (index < ARM_SLOTS_COUNT) {
                // Moving from Arm slots (0 or 1) -> Try Cargo first, then Player Inventory
                if (!this.insertItem(originalStack, ARM_SLOTS_COUNT, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= ARM_SLOTS_COUNT && index < TOTAL_ROBOT_SLOTS) {
                // Moving from Cargo (2 .. 55)
                boolean movedToArm = false;
                if (originalStack.getItem() instanceof BlockItem && !this.slots.get(0).hasStack()) {
                    if (this.insertItem(originalStack, 0, 1, false)) {
                        movedToArm = true;
                    }
                } else if (!(originalStack.getItem() instanceof BlockItem) && !this.slots.get(1).hasStack()) {
                    if (this.insertItem(originalStack, 1, 2, false)) {
                        movedToArm = true;
                    }
                }

                if (!movedToArm) {
                    // Transfer to Player Inventory / Hotbar
                    if (!this.insertItem(originalStack, TOTAL_ROBOT_SLOTS, this.slots.size(), true)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else {
                // Moving from Player Inventory / Hotbar (56 .. 91)
                boolean movedToArm = false;
                if (originalStack.getItem() instanceof BlockItem && !this.slots.get(0).hasStack()) {
                    if (this.insertItem(originalStack, 0, 1, false)) {
                        movedToArm = true;
                    }
                } else if (!(originalStack.getItem() instanceof BlockItem) && !this.slots.get(1).hasStack()) {
                    if (this.insertItem(originalStack, 1, 2, false)) {
                        movedToArm = true;
                    }
                }

                if (!movedToArm) {
                    // Transfer to Robot Cargo
                    if (!this.insertItem(originalStack, ARM_SLOTS_COUNT, TOTAL_ROBOT_SLOTS, false)) {
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
        this.armInventory.onClose(player);
        this.cargoInventory.onClose(player);
        if (this.robot != null) {
            this.robot.setEquippedLeftArm(this.armInventory.getStack(0));
            this.robot.setEquippedTool(this.armInventory.getStack(1));
        }
    }
}
