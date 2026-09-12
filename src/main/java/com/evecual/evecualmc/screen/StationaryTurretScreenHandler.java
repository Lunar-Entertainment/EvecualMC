package com.evecual.evecualmc.screen;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.block.entity.StationaryTurretBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.BlockPos;

public class StationaryTurretScreenHandler extends ScreenHandler {
    private final PropertyDelegate propertyDelegate;
    private final StationaryTurretBlockEntity blockEntity;

    public StationaryTurretScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, null, new ArrayPropertyDelegate(11));
    }

    public StationaryTurretScreenHandler(int syncId, PlayerInventory playerInventory, StationaryTurretBlockEntity blockEntity, PropertyDelegate propertyDelegate) {
        super(EvecualMC.STATIONARY_TURRET_SCREEN_HANDLER, syncId);
        this.propertyDelegate = propertyDelegate;
        this.blockEntity = blockEntity;
        addProperties(propertyDelegate);

        // Player Inventory (3 rows x 9 columns)
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 102 + i * 18));
            }
        }

        // Player Hotbar (1 row x 9 columns)
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 160));
        }
    }

    public int getCooldown() {
        return this.propertyDelegate.get(0);
    }

    public int getRadius() {
        int r = this.propertyDelegate.get(1);
        return r > 0 ? r : 64;
    }

    public boolean isTargetPlayers() {
        return this.propertyDelegate.get(2) == 1;
    }

    public boolean isTargetMonsters() {
        return this.propertyDelegate.get(3) == 1;
    }

    public boolean isTargetAnimals() {
        return this.propertyDelegate.get(4) == 1;
    }

    public boolean isTargetBosses() {
        return this.propertyDelegate.get(5) == 1;
    }

    public boolean hasLinkedContainer() {
        return this.propertyDelegate.get(6) == 1;
    }

    public BlockPos getLinkedContainerPos() {
        return new BlockPos(this.propertyDelegate.get(7), this.propertyDelegate.get(8), this.propertyDelegate.get(9));
    }

    public boolean hasTarget() {
        return this.propertyDelegate.get(10) == 1;
    }

    @Override
    public boolean onButtonClick(PlayerEntity player, int id) {
        if (blockEntity != null) {
            switch (id) {
                case 0 -> { // Toggle Target Players
                    boolean next = !blockEntity.getTargetFilter().isTargetPlayers();
                    blockEntity.getTargetFilter().setTargetPlayers(next);
                    this.propertyDelegate.set(2, next ? 1 : 0);
                    blockEntity.markDirty();
                    blockEntity.sync();
                    return true;
                }
                case 1 -> { // Toggle Target Monsters
                    boolean next = !blockEntity.getTargetFilter().isTargetMonsters();
                    blockEntity.getTargetFilter().setTargetMonsters(next);
                    this.propertyDelegate.set(3, next ? 1 : 0);
                    blockEntity.markDirty();
                    blockEntity.sync();
                    return true;
                }
                case 2 -> { // Toggle Target Animals
                    boolean next = !blockEntity.getTargetFilter().isTargetAnimals();
                    blockEntity.getTargetFilter().setTargetAnimals(next);
                    this.propertyDelegate.set(4, next ? 1 : 0);
                    blockEntity.markDirty();
                    blockEntity.sync();
                    return true;
                }
                case 3 -> { // Toggle Target Bosses
                    boolean next = !blockEntity.getTargetFilter().isTargetBosses();
                    blockEntity.getTargetFilter().setTargetBosses(next);
                    this.propertyDelegate.set(5, next ? 1 : 0);
                    blockEntity.markDirty();
                    blockEntity.sync();
                    return true;
                }
                case 4 -> { // Cycle Radius: 16 -> 32 -> 64 -> 128 -> 256 -> 16
                    int cur = blockEntity.getTargetFilter().getRadius();
                    int next = switch (cur) {
                        case 16 -> 32;
                        case 32 -> 64;
                        case 64 -> 128;
                        case 128 -> 256;
                        default -> 16;
                    };
                    blockEntity.getTargetFilter().setRadius(next);
                    this.propertyDelegate.set(1, next);
                    blockEntity.markDirty();
                    blockEntity.sync();
                    return true;
                }
                case 5 -> { // Unlink Container
                    blockEntity.setLinkedAmmoContainerPos(null);
                    this.propertyDelegate.set(6, 0);
                    blockEntity.markDirty();
                    blockEntity.sync();
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int invSlot) {
        return ItemStack.EMPTY;
    }
}
