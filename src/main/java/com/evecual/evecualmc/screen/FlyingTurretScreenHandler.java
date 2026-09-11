package com.evecual.evecualmc.screen;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.entity.FlyingTurretEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.BlockPos;

public class FlyingTurretScreenHandler extends ScreenHandler {
    private final Inventory inventory;
    private final PropertyDelegate propertyDelegate;
    private final FlyingTurretEntity drone;

    public FlyingTurretScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, null, new ArrayPropertyDelegate(12));
    }

    public FlyingTurretScreenHandler(int syncId, PlayerInventory playerInventory, FlyingTurretEntity drone, PropertyDelegate propertyDelegate) {
        super(EvecualMC.FLYING_TURRET_SCREEN_HANDLER, syncId);
        this.inventory = new SimpleInventory(0);
        this.propertyDelegate = propertyDelegate;
        this.drone = drone;
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
        return r > 0 ? r : 128;
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

    public int getPatrolAltitude() {
        int alt = this.propertyDelegate.get(11);
        return alt > 0 ? alt : 16;
    }

    @Override
    public boolean onButtonClick(PlayerEntity player, int id) {
        if (drone != null) {
            switch (id) {
                case 0 -> { // Toggle Target Players
                    boolean next = !drone.getTargetFilter().isTargetPlayers();
                    drone.getTargetFilter().setTargetPlayers(next);
                    this.propertyDelegate.set(2, next ? 1 : 0);
                    return true;
                }
                case 1 -> { // Toggle Target Monsters
                    boolean next = !drone.getTargetFilter().isTargetMonsters();
                    drone.getTargetFilter().setTargetMonsters(next);
                    this.propertyDelegate.set(3, next ? 1 : 0);
                    return true;
                }
                case 2 -> { // Toggle Target Animals
                    boolean next = !drone.getTargetFilter().isTargetAnimals();
                    drone.getTargetFilter().setTargetAnimals(next);
                    this.propertyDelegate.set(4, next ? 1 : 0);
                    return true;
                }
                case 3 -> { // Toggle Target Bosses
                    boolean next = !drone.getTargetFilter().isTargetBosses();
                    drone.getTargetFilter().setTargetBosses(next);
                    this.propertyDelegate.set(5, next ? 1 : 0);
                    return true;
                }
                case 4 -> { // Cycle Radius: 64 -> 128 -> 256 -> 512 -> 32 -> 64
                    int cur = drone.getTargetFilter().getRadius();
                    int next = switch (cur) {
                        case 32 -> 64;
                        case 64 -> 128;
                        case 128 -> 256;
                        case 256 -> 512;
                        default -> 32;
                    };
                    drone.getTargetFilter().setRadius(next);
                    this.propertyDelegate.set(1, next);
                    return true;
                }
                case 5 -> { // Cycle Patrol Altitude: 10 -> 16 -> 24 -> 32 -> 10
                    int cur = getPatrolAltitude();
                    int next = switch (cur) {
                        case 10 -> 16;
                        case 16 -> 24;
                        case 24 -> 32;
                        default -> 10;
                    };
                    this.propertyDelegate.set(11, next);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return drone != null && drone.isAlive() && player.squaredDistanceTo(drone) <= 64.0;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int invSlot) {
        return ItemStack.EMPTY;
    }
}
