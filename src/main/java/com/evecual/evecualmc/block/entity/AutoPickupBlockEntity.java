package com.evecual.evecualmc.block.entity;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.block.AutoPickupBlock;
import com.evecual.evecualmc.entity.PickupDroneEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class AutoPickupBlockEntity extends BlockEntity {
    public static final double SCAN_RADIUS = 256.0;

    private UUID linkedDroneUuid = null;
    private String linkedDroneName = "None";
    private int scanCooldown = 0;
    private int totalHarvests = 0;
    private String lastStatus = "Awaiting Drone Link";

    public AutoPickupBlockEntity(BlockPos pos, BlockState state) {
        super(EvecualMC.AUTO_PICKUP_BLOCK_ENTITY, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, AutoPickupBlockEntity be) {
        if (world.isClient) return;

        if (be.scanCooldown > 0) {
            be.scanCooldown--;
        }

        if (be.linkedDroneUuid == null) {
            if (state.get(AutoPickupBlock.ACTIVE)) {
                world.setBlockState(pos, state.with(AutoPickupBlock.ACTIVE, false), 3);
            }
            be.lastStatus = "⚠️ Standby: Right-click with paired RC Controller to link Pickup Drone";
            return;
        }

        if (!(world instanceof ServerWorld serverWorld)) return;

        Entity entity = serverWorld.getEntity(be.linkedDroneUuid);
        if (!(entity instanceof PickupDroneEntity drone) || !drone.isAlive()) {
            if (state.get(AutoPickupBlock.ACTIVE)) {
                world.setBlockState(pos, state.with(AutoPickupBlock.ACTIVE, false), 3);
            }
            be.lastStatus = "⚠️ Link Offline: Linked Pickup Drone not found or destroyed";
            return;
        }

        // Active link established
        if (!state.get(AutoPickupBlock.ACTIVE)) {
            world.setBlockState(pos, state.with(AutoPickupBlock.ACTIVE, true), 3);
        }

        if (be.scanCooldown <= 0) {
            be.scanCooldown = 20; // Check every second

            // If drone is dead or completely unpowered, do not dispatch
            if (drone.getEnergy() <= 10) {
                be.lastStatus = "⚡ Low Battery: Pickup Drone requires charging (≤ 10 EU)";
                return;
            }

            // If drone cargo is full, ensure it returns home
            if (drone.isCargoFull()) {
                be.lastStatus = "📦 Cargo Full: Drone returning to base to unload into Storage Unit";
                if (!drone.isAutoReturning() && !drone.isInParkingSpot()) {
                    drone.startAutoReturnToCharger();
                }
                return;
            }

            // Scan for any dropped item entities across the area
            Box scanArea = new Box(pos).expand(SCAN_RADIUS);
            List<ItemEntity> items = serverWorld.getEntitiesByClass(ItemEntity.class, scanArea,
                    item -> item.isAlive() && !item.cannotPickup() && !item.getStack().isEmpty());

            if (!items.isEmpty()) {
                // Find closest item to the drone
                Vec3d dronePos = drone.getPos();
                ItemEntity closest = items.stream()
                        .min(Comparator.comparingDouble(i -> i.squaredDistanceTo(dronePos)))
                        .orElse(null);

                if (closest != null) {
                    drone.dispatchAutoHarvest(closest.getPos(), closest.getUuid());
                    be.totalHarvests++;
                    be.lastStatus = "🎯 Mission Active: Dispatched drone to harvest dropped " + closest.getStack().getName().getString() + " (" + items.size() + " in radar)";
                    be.markDirty();
                    be.sync();

                    // Radar pulse particles & audio
                    serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK,
                            pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5,
                            5, 0.2, 0.1, 0.2, 0.05);
                    serverWorld.spawnParticles(ParticleTypes.ENCHANT,
                            pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5,
                            8, 0.3, 0.2, 0.3, 0.1);
                    world.playSound(null, pos, SoundEvents.BLOCK_RESPAWN_ANCHOR_DEPLETE.value(), SoundCategory.BLOCKS, 0.4F, 2.0F);
                }
            } else {
                if (drone.isAutoHarvesting()) {
                    // No items left in radar, order drone to return to base
                    drone.cancelAutoHarvest();
                    drone.startAutoReturnToCharger();
                    be.lastStatus = "🟢 Area Clear: Drone returning to dock on Drone Pickup Station";
                } else if (drone.isInParkingSpot()) {
                    be.lastStatus = "🟢 Standby: Radar scanning (256m radius | Drone docked & ready)";
                } else {
                    be.lastStatus = "🟢 Radar Active: Scanning for dropped items across " + (int)SCAN_RADIUS + "m radius";
                }
            }
        }
    }

    public void setLinkedDrone(UUID uuid, String name) {
        this.linkedDroneUuid = uuid;
        this.linkedDroneName = name != null ? name : "Pickup Drone";
        this.lastStatus = "🟢 Linked to " + this.linkedDroneName + " (Radar active)";
        markDirty();
        sync();
    }

    public boolean isLinked() {
        return this.linkedDroneUuid != null;
    }

    @Nullable
    public UUID getLinkedDroneUuid() {
        return this.linkedDroneUuid;
    }

    public String getLinkedDroneName() {
        return this.linkedDroneName;
    }

    public int getTotalHarvests() {
        return this.totalHarvests;
    }

    public String getStatusMessage() {
        return this.lastStatus;
    }

    public void sync() {
        if (this.world instanceof ServerWorld sw) {
            sw.getChunkManager().markForUpdate(this.pos);
        }
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        if (this.linkedDroneUuid != null) {
            nbt.putUuid("LinkedDrone", this.linkedDroneUuid);
        }
        nbt.putString("DroneName", this.linkedDroneName);
        nbt.putInt("TotalHarvests", this.totalHarvests);
        nbt.putString("LastStatus", this.lastStatus);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        if (nbt.contains("LinkedDrone")) {
            this.linkedDroneUuid = nbt.getUuid("LinkedDrone");
        } else {
            this.linkedDroneUuid = null;
        }
        if (nbt.contains("DroneName")) {
            this.linkedDroneName = nbt.getString("DroneName");
        }
        if (nbt.contains("TotalHarvests")) {
            this.totalHarvests = nbt.getInt("TotalHarvests");
        }
        if (nbt.contains("LastStatus")) {
            this.lastStatus = nbt.getString("LastStatus");
        }
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return createNbt();
    }
}
