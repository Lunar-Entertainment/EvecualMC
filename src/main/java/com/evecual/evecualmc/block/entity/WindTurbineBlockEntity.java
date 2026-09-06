package com.evecual.evecualmc.block.entity;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.block.WindTurbineBlock;
import com.evecual.evecualmc.energy.EnergyStorage;
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

public class WindTurbineBlockEntity extends BlockEntity implements EnergyStorage {
    public static final long MAX_CAPACITY = 100;
    public static final long OUTPUT_RATE = 50; // 50 EU/t generated out into connected grid
    private long energy = 0;

    public WindTurbineBlockEntity(BlockPos pos, BlockState state) {
        super(EvecualMC.WIND_TURBINE_BLOCK_ENTITY, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, WindTurbineBlockEntity be) {
        if (world.isClient) return;

        // Generate 50 EU/t
        be.energy = Math.min(MAX_CAPACITY, be.energy + OUTPUT_RATE);
        be.markDirty();

        if (be.energy > 0) {
            be.outputEnergyToNetwork(world, pos);
        }
    }

    private void outputEnergyToNetwork(World world, BlockPos basePos) {
        Queue<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        List<EnergyStorage> targets = new ArrayList<>();

        // Check direct neighbors of all 4 vertical turbine blocks (Base, Shaft 1, Shaft 2, Head)
        for (int h = 0; h <= 3; h++) {
            BlockPos partPos = basePos.up(h);
            visited.add(partPos);

            for (Direction dir : Direction.values()) {
                BlockPos neighbor = partPos.offset(dir);
                if (!visited.add(neighbor)) continue;

                BlockState neighborState = world.getBlockState(neighbor);
                if (neighborState.isOf(EvecualMC.WIRE_BLOCK)) {
                    queue.add(neighbor);
                } else {
                    BlockEntity neighborBe = world.getBlockEntity(neighbor);
                    if (neighborBe instanceof EnergyStorage storage && neighborBe != this) {
                        targets.add(storage);
                    }
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
                    BlockEntity wireBe = world.getBlockEntity(next);
                    if (wireBe instanceof com.evecual.evecualmc.block.entity.WireBlockEntity wbe) {
                        wbe.recordEnergyTransfer(OUTPUT_RATE);
                    }
                } else {
                    BlockEntity nextBe = world.getBlockEntity(next);
                    if (nextBe instanceof EnergyStorage storage && nextBe != this) {
                        targets.add(storage);
                    }
                }
            }
        }

        // Distribute generated 50 EU/t to connected consumers/batteries
        if (!targets.isEmpty()) {
            for (EnergyStorage storage : targets) {
                if (this.energy <= 0) break;

                long needed = storage.getMaxEnergy() - storage.getEnergy();
                if (needed > 0) {
                    long toSend = Math.min(this.energy, Math.min(needed, OUTPUT_RATE));
                    long inserted = storage.insertEnergy(toSend, false);
                    this.energy -= inserted;
                    this.markDirty();
                    this.sync();
                }
            }
        }
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
            world.updateListeners(pos, getCachedState(), getCachedState(), net.minecraft.block.Block.NOTIFY_LISTENERS);
        }
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.energy = nbt.getLong("Energy");
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putLong("Energy", this.energy);
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
