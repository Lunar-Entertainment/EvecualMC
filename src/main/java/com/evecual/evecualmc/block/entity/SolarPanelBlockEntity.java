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

    private void transferEnergyToNetwork(World world, BlockPos startPos) {
        Queue<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        List<BatteryBlockEntity> targets = new ArrayList<>();

        // Check direct neighbors of solar panel
        for (Direction dir : Direction.values()) {
            BlockPos neighbor = startPos.offset(dir);
            BlockState neighborState = world.getBlockState(neighbor);

            if (neighborState.isOf(EvecualMC.WIRE_BLOCK)) {
                queue.add(neighbor);
                visited.add(neighbor);
            } else if (neighborState.isOf(EvecualMC.BATTERY_BLOCK)) {
                BlockEntity neighborBe = world.getBlockEntity(neighbor);
                if (neighborBe instanceof BatteryBlockEntity battery) {
                    targets.add(battery);
                }
            }
        }

        // BFS through connected wire network (up to 64 hops)
        int maxHops = 64;
        while (!queue.isEmpty() && visited.size() <= maxHops) {
            BlockPos current = queue.poll();

            for (Direction dir : Direction.values()) {
                BlockPos next = current.offset(dir);
                if (!visited.add(next)) continue;

                BlockState nextState = world.getBlockState(next);
                if (nextState.isOf(EvecualMC.WIRE_BLOCK)) {
                    queue.add(next);
                } else if (nextState.isOf(EvecualMC.BATTERY_BLOCK)) {
                    BlockEntity nextBe = world.getBlockEntity(next);
                    if (nextBe instanceof BatteryBlockEntity battery) {
                        targets.add(battery);
                    }
                }
            }
        }

        // Push energy to connected batteries
        if (!targets.isEmpty()) {
            for (BatteryBlockEntity battery : targets) {
                if (this.energy <= 0) break;

                long needed = battery.getMaxEnergy() - battery.getEnergy();
                if (needed > 0) {
                    long toSend = Math.min(this.energy, Math.min(needed, 5)); // Transfer speed per tick
                    long inserted = battery.insertEnergy(toSend, false);
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
