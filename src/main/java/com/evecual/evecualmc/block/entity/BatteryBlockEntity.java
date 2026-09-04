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

public class BatteryBlockEntity extends BlockEntity implements EnergyStorage {
    public static final long MAX_CAPACITY = 600;
    private long energy = 0;

    public BatteryBlockEntity(BlockPos pos, BlockState state) {
        super(EvecualMC.BATTERY_BLOCK_ENTITY, pos, state);
    }

    public static void tick(net.minecraft.world.World world, BlockPos pos, BlockState state, BatteryBlockEntity be) {
        if (world.isClient) return;

        if (be.energy > 0) {
            be.transferEnergyToConsumers(world, pos);
        }
        be.updateChargeLevel();
    }

    private void transferEnergyToConsumers(net.minecraft.world.World world, BlockPos startPos) {
        java.util.Queue<BlockPos> queue = new java.util.ArrayDeque<>();
        java.util.Set<BlockPos> visited = new java.util.HashSet<>();
        java.util.List<EnergyStorage> consumers = new java.util.ArrayList<>();

        for (net.minecraft.util.math.Direction dir : net.minecraft.util.math.Direction.values()) {
            BlockPos neighbor = startPos.offset(dir);
            BlockState neighborState = world.getBlockState(neighbor);

            if (neighborState.isOf(EvecualMC.WIRE_BLOCK)) {
                queue.add(neighbor);
                visited.add(neighbor);
            } else {
                BlockEntity neighborBe = world.getBlockEntity(neighbor);
                if ((neighborBe instanceof ElectronicCombinerBlockEntity || neighborBe instanceof ChargerBlockEntity || neighborBe instanceof RcChargerBlockEntity) && neighborBe != this) {
                    consumers.add((EnergyStorage) neighborBe);
                }
            }
        }

        int maxHops = 64;
        while (!queue.isEmpty() && visited.size() <= maxHops) {
            BlockPos current = queue.poll();

            for (net.minecraft.util.math.Direction dir : net.minecraft.util.math.Direction.values()) {
                BlockPos next = current.offset(dir);
                if (!visited.add(next)) continue;

                BlockState nextState = world.getBlockState(next);
                if (nextState.isOf(EvecualMC.WIRE_BLOCK)) {
                    queue.add(next);
                } else {
                    BlockEntity nextBe = world.getBlockEntity(next);
                    if ((nextBe instanceof ElectronicCombinerBlockEntity || nextBe instanceof ChargerBlockEntity || nextBe instanceof RcChargerBlockEntity) && nextBe != this) {
                        consumers.add((EnergyStorage) nextBe);
                    }
                }
            }
        }

        if (!consumers.isEmpty()) {
            for (EnergyStorage consumer : consumers) {
                if (this.energy <= 0) break;

                long needed = consumer.getMaxEnergy() - consumer.getEnergy();
                if (needed > 0) {
                    long toSend = Math.min(this.energy, Math.min(needed, 10));
                    long inserted = consumer.insertEnergy(toSend, false);
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
            updateChargeLevel();
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
            updateChargeLevel();
        }
        return canExtract;
    }

    public void updateChargeLevel() {
        if (world != null && !world.isClient) {
            BlockState state = getCachedState();
            if (state.isOf(EvecualMC.BATTERY_BLOCK)) {
                int level = (int) Math.round((double) this.energy * 8.0 / MAX_CAPACITY);
                level = net.minecraft.util.math.MathHelper.clamp(level, 0, 8);
                if (state.get(com.evecual.evecualmc.block.BatteryBlock.CHARGE_LEVEL) != level) {
                    world.setBlockState(pos, state.with(com.evecual.evecualmc.block.BatteryBlock.CHARGE_LEVEL, level), Block.NOTIFY_LISTENERS);
                }
            }
        }
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
