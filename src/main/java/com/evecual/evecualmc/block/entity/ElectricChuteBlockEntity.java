package com.evecual.evecualmc.block.entity;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.block.ElectricChuteBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ElectricChuteBlockEntity extends BlockEntity implements com.evecual.evecualmc.energy.EnergyStorage {
    public static final int MAX_ENERGY = 2000;
    public static final int TRANSFER_COST = 5;

    private int energy = 0;
    private int transferCooldown = 0;

    public ElectricChuteBlockEntity(BlockPos pos, BlockState state) {
        super(EvecualMC.ELECTRIC_CHUTE_BLOCK_ENTITY, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, ElectricChuteBlockEntity be) {
        if (world.isClient) return;

        // 1. Draw energy from adjacent power sources & connected wires
        be.drawAdjacentEnergy(world, pos);

        if (be.transferCooldown > 0) {
            be.transferCooldown--;
            return;
        }

        // 2. Transfer items from Drone Pickup to Storage Unit using BFS (supports corners & turns)
        DronePickupBlockEntity pickup = be.findUpstreamPickup(world, pos);
        StorageUnitBlockEntity storage = be.findDownstreamStorage(world, pos, pickup);

        if (pickup != null && storage != null) {
            // Draw energy if needed from storage unit or parked drone
            if (be.energy < TRANSFER_COST && storage.getEnergy() >= TRANSFER_COST) {
                long pulled = storage.extractEnergy(TRANSFER_COST - be.energy, false);
                be.energy += (int) pulled;
                be.markDirty();
                be.sync();
            }
            if (be.energy < TRANSFER_COST && pickup.getParkedDrone() != null && pickup.getParkedDrone().getEnergy() >= 10) {
                pickup.getParkedDrone().setEnergy(pickup.getParkedDrone().getEnergy() - TRANSFER_COST);
                be.energy += TRANSFER_COST;
                be.markDirty();
                be.sync();
            }

            if (be.energy >= TRANSFER_COST && pickup.hasParkedDrone()) {
                ItemStack stack = pickup.extractItem(4);
                if (!stack.isEmpty()) {
                    ItemStack remainder = storage.insertItem(stack);
                    if (!remainder.isEmpty()) {
                        // Put remainder back into drone
                        if (pickup.getParkedDrone() != null) {
                            pickup.getParkedDrone().getTrunk().addStack(remainder);
                        }
                    }
                    storage.sync();
                    be.energy -= TRANSFER_COST;
                    be.transferCooldown = 8; // 8 ticks between transfers (~2.5 transfers/sec)
                    be.markDirty();
                    be.sync();

                    world.playSound(null, pos, SoundEvents.BLOCK_DISPENSER_DISPENSE, SoundCategory.BLOCKS, 0.4F, 1.8F);
                    if (world instanceof ServerWorld sw) {
                        sw.spawnParticles(ParticleTypes.ELECTRIC_SPARK,
                                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                                3, 0.2, 0.2, 0.2, 0.02);
                    }
                }
            }
        }
    }

    private void drawAdjacentEnergy(World world, BlockPos pos) {
        if (this.energy >= MAX_ENERGY) return;

        java.util.Set<BlockPos> visited = new java.util.HashSet<>();
        java.util.Queue<BlockPos> wireQueue = new java.util.LinkedList<>();
        visited.add(pos);

        // 1. Check direct adjacent blocks
        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = pos.offset(dir);
            if (!visited.add(neighborPos)) continue;

            BlockState neighborState = world.getBlockState(neighborPos);
            BlockEntity neighborBe = world.getBlockEntity(neighborPos);

            if (neighborState.isOf(EvecualMC.WIRE_BLOCK)) {
                wireQueue.add(neighborPos);
            } else if (neighborBe instanceof com.evecual.evecualmc.energy.EnergyStorage storage
                    && !(neighborBe instanceof ElectricChuteBlockEntity)) {
                int needed = MAX_ENERGY - this.energy;
                long extracted = storage.extractEnergy(Math.min(needed, 50), false);
                if (extracted > 0) {
                    this.energy += (int) extracted;
                    markDirty();
                    sync();
                    if (this.energy >= MAX_ENERGY) return;
                }
            }
        }

        // 2. Traverse connected wires via BFS to pull from batteries/generators across network
        while (!wireQueue.isEmpty() && visited.size() <= 48) {
            BlockPos wirePos = wireQueue.poll();
            for (Direction dir : Direction.values()) {
                BlockPos next = wirePos.offset(dir);
                if (!visited.add(next)) continue;

                BlockState nextState = world.getBlockState(next);
                BlockEntity nextBe = world.getBlockEntity(next);

                if (nextState.isOf(EvecualMC.WIRE_BLOCK)) {
                    wireQueue.add(next);
                } else if (nextBe instanceof com.evecual.evecualmc.energy.EnergyStorage storage
                        && !(nextBe instanceof ElectricChuteBlockEntity)) {
                    int needed = MAX_ENERGY - this.energy;
                    long extracted = storage.extractEnergy(Math.min(needed, 50), false);
                    if (extracted > 0) {
                        this.energy += (int) extracted;
                        BlockEntity wireBe = world.getBlockEntity(wirePos);
                        if (wireBe instanceof com.evecual.evecualmc.block.entity.WireBlockEntity wbe) {
                            wbe.recordEnergyTransfer(extracted);
                        }
                        markDirty();
                        sync();
                        if (this.energy >= MAX_ENERGY) return;
                    }
                }
            }
        }
    }

    @Nullable
    public DronePickupBlockEntity findUpstreamPickup(World world, BlockPos startPos) {
        java.util.Set<BlockPos> visited = new java.util.HashSet<>();
        java.util.Queue<BlockPos> queue = new java.util.LinkedList<>();

        visited.add(startPos);
        queue.add(startPos);

        while (!queue.isEmpty() && visited.size() <= 64) {
            BlockPos current = queue.poll();
            for (Direction dir : Direction.values()) {
                BlockPos neighbor = current.offset(dir);
                if (!visited.add(neighbor)) continue;

                BlockEntity be = world.getBlockEntity(neighbor);
                if (be instanceof DronePickupBlockEntity dp) {
                    return dp;
                }
                if (be instanceof ElectricChuteBlockEntity) {
                    queue.add(neighbor);
                }
            }
        }
        return null;
    }

    @Nullable
    public StorageUnitBlockEntity findDownstreamStorage(World world, BlockPos startPos, @Nullable DronePickupBlockEntity knownPickup) {
        BlockPos pickupPos = knownPickup != null ? knownPickup.getPos() : null;
        java.util.Set<BlockPos> visited = new java.util.HashSet<>();
        java.util.Queue<BlockPos> queue = new java.util.LinkedList<>();

        visited.add(startPos);
        queue.add(startPos);

        while (!queue.isEmpty() && visited.size() <= 64) {
            BlockPos current = queue.poll();
            for (Direction dir : Direction.values()) {
                BlockPos neighbor = current.offset(dir);
                if (neighbor.equals(pickupPos)) continue;
                if (!visited.add(neighbor)) continue;

                BlockEntity be = world.getBlockEntity(neighbor);
                if (be instanceof StorageUnitBlockEntity su) {
                    return su;
                }
                if (be instanceof ElectricChuteBlockEntity) {
                    queue.add(neighbor);
                }
            }
        }
        return null;
    }

    @Override
    public long getEnergy() {
        return this.energy;
    }

    @Override
    public long getMaxEnergy() {
        return MAX_ENERGY;
    }

    @Override
    public long insertEnergy(long amount, boolean simulate) {
        long room = MAX_ENERGY - this.energy;
        long canInsert = Math.min(amount, room);
        if (!simulate && canInsert > 0) {
            this.energy += (int) canInsert;
            markDirty();
            sync();
        }
        return canInsert;
    }

    @Override
    public long extractEnergy(long amount, boolean simulate) {
        long canExtract = Math.min(amount, this.energy);
        if (!simulate && canExtract > 0) {
            this.energy -= (int) canExtract;
            markDirty();
            sync();
        }
        return canExtract;
    }

    public int receiveEnergy(int amount) {
        int accepted = Math.min(amount, MAX_ENERGY - this.energy);
        this.energy += accepted;
        if (accepted > 0) {
            markDirty();
            sync();
        }
        return accepted;
    }

    public void setEnergy(int energy) {
        this.energy = Math.min(MAX_ENERGY, Math.max(0, energy));
        markDirty();
        sync();
    }

    public void sync() {
        if (this.world instanceof ServerWorld sw) {
            sw.getChunkManager().markForUpdate(this.pos);
        }
    }

    public String getStatusMessage() {
        if (this.world == null) return "⚡ Electric Chute: Offline";
        DronePickupBlockEntity pickup = findUpstreamPickup(this.world, this.pos);
        StorageUnitBlockEntity storage = findDownstreamStorage(this.world, this.pos, pickup);

        if (pickup == null) {
            return "⚠️ No Drone Pickup Station connected upstream.";
        }
        if (storage == null) {
            return "⚠️ No Storage Unit connected downstream.";
        }
        if (!pickup.hasParkedDrone()) {
            return "🟢 Standby: Awaiting Pickup Drone landing on station.";
        }
        if (this.energy < TRANSFER_COST && storage.getEnergy() < TRANSFER_COST && (pickup.getParkedDrone() == null || pickup.getParkedDrone().getEnergy() < 10)) {
            return "⚡ Unpowered: Connect electricity (Needs ≥ 5 EU).";
        }
        return "⚡ Pneumatic Transfer Active: Unloading Pickup Drone into Storage Unit!";
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putInt("Energy", this.energy);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.energy = nbt.getInt("Energy");
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
