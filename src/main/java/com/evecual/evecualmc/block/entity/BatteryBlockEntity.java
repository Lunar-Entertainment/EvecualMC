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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class BatteryBlockEntity extends BlockEntity implements EnergyStorage {
    public static final long MAX_CAPACITY = 600;
    private long energy = 0;

    public record BatteryCluster(List<BatteryBlockEntity> batteries, long totalEnergy, long maxCapacity, int size) {}

    public BatteryBlockEntity(BlockPos pos, BlockState state) {
        super(EvecualMC.BATTERY_BLOCK_ENTITY, pos, state);
    }

    public static final int MAX_WIRE_DISTANCE = 1024;

    public static void tick(net.minecraft.world.World world, BlockPos pos, BlockState state, BatteryBlockEntity be) {
        if (world.isClient) return;

        // Equalize cluster energy periodically (every 10 ticks) or when active
        if (world.getTime() % 10L == 0L) {
            be.equalizeCluster();
        }

        BatteryCluster cluster = be.getCluster();
        // Only the deterministic first battery in the sorted cluster drives the network transfer
        if (!cluster.batteries().isEmpty() && cluster.batteries().get(0) == be && cluster.totalEnergy() > 0) {
            be.transferEnergyToConsumers(world, pos, cluster);
        }
        be.updateChargeLevel();
    }

    public BatteryCluster getCluster() {
        if (world == null) return new BatteryCluster(List.of(this), this.energy, MAX_CAPACITY, 1);

        Queue<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        List<BatteryBlockEntity> clusterBatteries = new ArrayList<>();

        queue.add(pos);
        visited.add(pos);

        while (!queue.isEmpty() && visited.size() <= 256) {
            BlockPos current = queue.poll();
            BlockEntity currentBe = world.getBlockEntity(current);

            if (currentBe instanceof BatteryBlockEntity bbe) {
                clusterBatteries.add(bbe);

                for (Direction dir : Direction.values()) {
                    BlockPos next = current.offset(dir);
                    if (!visited.contains(next) && world.getBlockState(next).isOf(EvecualMC.BATTERY_BLOCK)) {
                        visited.add(next);
                        queue.add(next);
                    }
                }
            }
        }

        // Sort deterministically by block coordinates so ALL batteries in the cluster agree on the exact same master
        clusterBatteries.sort(java.util.Comparator.comparing(BlockEntity::getPos));

        long totalStored = 0;
        for (BatteryBlockEntity bbe : clusterBatteries) {
            totalStored += bbe.energy;
        }

        long totalCap = (long) clusterBatteries.size() * MAX_CAPACITY;
        return new BatteryCluster(clusterBatteries, totalStored, totalCap, clusterBatteries.size());
    }

    public void equalizeCluster() {
        BatteryCluster cluster = getCluster();
        if (cluster.size() <= 1) return;

        long perBattery = cluster.totalEnergy() / cluster.size();
        long remainder = cluster.totalEnergy() % cluster.size();

        for (int i = 0; i < cluster.batteries().size(); i++) {
            BatteryBlockEntity bbe = cluster.batteries().get(i);
            long targetEnergy = perBattery + (i < remainder ? 1 : 0);
            if (bbe.energy != targetEnergy) {
                bbe.energy = targetEnergy;
                bbe.markDirty();
                bbe.sync();
                bbe.updateChargeLevel();
            }
        }
    }

    private record WireHop(BlockPos pos, int distance) {}

    private void transferEnergyToConsumers(net.minecraft.world.World world, BlockPos startPos, BatteryCluster cluster) {
        Queue<WireHop> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        List<EnergyStorage> consumers = new ArrayList<>();

        // Add all battery positions in the cluster to visited so the cluster doesn't feed into itself
        for (BatteryBlockEntity bbe : cluster.batteries()) {
            visited.add(bbe.getPos());
        }

        for (BatteryBlockEntity bbe : cluster.batteries()) {
            for (Direction dir : Direction.values()) {
                BlockPos neighbor = bbe.getPos().offset(dir);
                if (!visited.add(neighbor)) continue;

                BlockState neighborState = world.getBlockState(neighbor);
                if (neighborState.isOf(EvecualMC.WIRE_BLOCK)) {
                    queue.add(new WireHop(neighbor, 1));
                    BlockEntity wireBe = world.getBlockEntity(neighbor);
                    if (wireBe instanceof WireBlockEntity wbe) {
                        wbe.recordEnergyTransfer(20);
                    }
                } else {
                    BlockEntity neighborBe = world.getBlockEntity(neighbor);
                    if (neighborBe instanceof EnergyStorage storage
                            && !(neighborBe instanceof BatteryBlockEntity)
                            && !(neighborBe instanceof SolarPanelBlockEntity)
                            && !(neighborBe instanceof WindTurbineBlockEntity)) {
                        consumers.add(storage);
                    }
                }
            }
        }

        // BFS through connected wire network limited to MAX_WIRE_DISTANCE (1024 blocks)
        while (!queue.isEmpty() && visited.size() <= 1024) {
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
                            wbe.recordEnergyTransfer(20);
                        }
                    }
                } else {
                    BlockEntity nextBe = world.getBlockEntity(next);
                    if (nextBe instanceof EnergyStorage storage
                            && !(nextBe instanceof BatteryBlockEntity)
                            && !(nextBe instanceof SolarPanelBlockEntity)
                            && !(nextBe instanceof WindTurbineBlockEntity)) {
                        consumers.add(storage);
                    }
                }
            }
        }

        if (!consumers.isEmpty() && cluster.totalEnergy() > 0) {
            for (EnergyStorage consumer : consumers) {
                if (cluster.totalEnergy() <= 0) break;

                long needed = consumer.getMaxEnergy() - consumer.getEnergy();
                if (needed > 0) {
                    long toSend = Math.min(cluster.totalEnergy(), Math.min(needed, 20));
                    long extracted = this.extractEnergy(toSend, false);
                    if (extracted > 0) {
                        consumer.insertEnergy(extracted, false);
                    }
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
        BatteryCluster cluster = getCluster();
        long roomInCluster = cluster.maxCapacity() - cluster.totalEnergy();
        long canInsert = Math.min(amount, roomInCluster);

        if (!simulate && canInsert > 0) {
            long newTotal = cluster.totalEnergy() + canInsert;
            int count = cluster.size();
            long perBattery = newTotal / count;
            long remainder = newTotal % count;

            for (int i = 0; i < cluster.batteries().size(); i++) {
                BatteryBlockEntity bbe = cluster.batteries().get(i);
                bbe.energy = perBattery + (i < remainder ? 1 : 0);
                bbe.markDirty();
                bbe.sync();
                bbe.updateChargeLevel();
            }
        }
        return canInsert;
    }

    @Override
    public long extractEnergy(long amount, boolean simulate) {
        BatteryCluster cluster = getCluster();
        long canExtract = Math.min(amount, cluster.totalEnergy());

        if (!simulate && canExtract > 0) {
            long newTotal = cluster.totalEnergy() - canExtract;
            int count = cluster.size();
            long perBattery = newTotal / count;
            long remainder = newTotal % count;

            for (int i = 0; i < cluster.batteries().size(); i++) {
                BatteryBlockEntity bbe = cluster.batteries().get(i);
                bbe.energy = perBattery + (i < remainder ? 1 : 0);
                bbe.markDirty();
                bbe.sync();
                bbe.updateChargeLevel();
            }
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
