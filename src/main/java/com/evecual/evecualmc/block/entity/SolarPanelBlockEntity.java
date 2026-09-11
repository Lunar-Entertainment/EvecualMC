package com.evecual.evecualmc.block.entity;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.energy.EnergyStorage;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class SolarPanelBlockEntity extends BlockEntity implements EnergyStorage {
    public static final long MAX_CAPACITY = 100;
    private long energy = 0;
    private int generationRate = 0; // Current electricity generated per second (0 - 20)

    public SolarPanelBlockEntity(BlockPos pos, BlockState state) {
        super(EvecualMC.SOLAR_PANEL_BLOCK_ENTITY, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, SolarPanelBlockEntity be) {
        if (world.isClient) return;

        long worldTime = world.getTime();

        // Every 20 ticks (1 second), generate electricity based on sun angle
        if (worldTime % 20L == 0L) {
            boolean canSeeSky = world.isSkyVisible(pos.up());
            long dayTime = world.getTimeOfDay() % 24000L;

            if (canSeeSky && dayTime >= 0L && dayTime <= 12000L) {
                // Smooth sine wave: 0 at dawn (0), peak 1.0 at noon (6000), 0 at dusk (12000)
                double factor = Math.sin((dayTime / 12000.0) * Math.PI);

                if (world.isThundering()) {
                    factor *= 0.2;
                } else if (world.isRaining()) {
                    factor *= 0.5;
                }

                int generated = (int) Math.round(20.0 * factor);
                be.generationRate = Math.max(0, Math.min(20, generated));
                be.energy = Math.min(MAX_CAPACITY, be.energy + be.generationRate);
            } else {
                be.generationRate = 0;
            }

            be.markDirty();
            be.sync();
        }

        // Transfer stored electricity through wires to connected batteries
        if (be.energy > 0) {
            be.transferEnergyToNetwork(world, pos);
        }
    }

    public static final int MAX_WIRE_DISTANCE = 32;

    private record WireHop(BlockPos pos, int distance) {}

    private void transferEnergyToNetwork(World world, BlockPos startPos) {
        Queue<WireHop> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        List<EnergyStorage> targets = new ArrayList<>();
        visited.add(startPos);

        // Check direct neighbors of solar panel
        for (Direction dir : Direction.values()) {
            BlockPos neighbor = startPos.offset(dir);
            if (!visited.add(neighbor)) continue;

            BlockState neighborState = world.getBlockState(neighbor);
            if (neighborState.isOf(EvecualMC.WIRE_BLOCK)) {
                queue.add(new WireHop(neighbor, 1));
                BlockEntity wireBe = world.getBlockEntity(neighbor);
                if (wireBe instanceof WireBlockEntity wbe) {
                    wbe.recordEnergyTransfer(5);
                }
            } else {
                BlockEntity neighborBe = world.getBlockEntity(neighbor);
                if (neighborBe instanceof EnergyStorage storage && neighborBe != this) {
                    targets.add(storage);
                }
            }
        }

        // BFS through connected wire network limited to MAX_WIRE_DISTANCE (32 blocks)
        while (!queue.isEmpty()) {
            WireHop current = queue.poll();

            for (Direction dir : Direction.values()) {
                BlockPos next = current.pos().offset(dir);
                if (!visited.add(next)) continue;

                BlockState nextState = world.getBlockState(next);
                if (nextState.isOf(EvecualMC.WIRE_BLOCK)) {
                    if (current.distance() < MAX_WIRE_DISTANCE) {
                        queue.add(new WireHop(next, current.distance() + 1));
                        BlockEntity wireBe = world.getBlockEntity(next);
                        if (wireBe instanceof WireBlockEntity wbe) {
                            wbe.recordEnergyTransfer(5);
                        }
                    }
                } else {
                    BlockEntity nextBe = world.getBlockEntity(next);
                    if (nextBe instanceof EnergyStorage storage && nextBe != this) {
                        targets.add(storage);
                    }
                }
            }
        }

        // Push energy to connected consumers
        if (!targets.isEmpty()) {
            for (EnergyStorage storage : targets) {
                if (this.energy <= 0) break;

                long needed = storage.getMaxEnergy() - storage.getEnergy();
                if (needed > 0) {
                    long toSend = Math.min(this.energy, Math.min(needed, 5)); // Transfer speed per tick
                    long inserted = storage.insertEnergy(toSend, false);
                    this.energy -= inserted;
                    this.markDirty();
                    this.sync();
                }
            }
        }
    }

    public int getGenerationRate() {
        return generationRate;
    }

    @Override
    public long getEnergy() {
        return energy;
    }

    @Override
    public long getMaxEnergy() {
        return MAX_CAPACITY;
    }

    @Override
    public long insertEnergy(long amount, boolean simulate) {
        long canInsert = Math.min(amount, MAX_CAPACITY - energy);
        if (!simulate && canInsert > 0) {
            energy += canInsert;
            markDirty();
            sync();
        }
        return canInsert;
    }

    @Override
    public long extractEnergy(long amount, boolean simulate) {
        long canExtract = Math.min(amount, energy);
        if (!simulate && canExtract > 0) {
            energy -= canExtract;
            markDirty();
            sync();
        }
        return canExtract;
    }

    public void sync() {
        if (world != null && !world.isClient) {
            world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_LISTENERS);
        }
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.energy = nbt.getLong("Energy");
        this.generationRate = nbt.getInt("GenerationRate");
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putLong("Energy", this.energy);
        nbt.putInt("GenerationRate", this.generationRate);
    }

    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return createNbt();
    }
}
