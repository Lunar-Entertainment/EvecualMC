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

        // 1. Draw energy from adjacent power sources
        be.drawAdjacentEnergy(world, pos);

        if (be.transferCooldown > 0) {
            be.transferCooldown--;
            return;
        }

        // 2. Transfer items from Drone Pickup to Storage Unit
        Direction facing = state.get(ElectricChuteBlock.FACING);
        DronePickupBlockEntity pickup = be.findUpstreamPickup(world, pos, facing);
        StorageUnitBlockEntity storage = be.findDownstreamStorage(world, pos, facing);

        if (pickup != null && storage != null) {
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
                    be.energy -= TRANSFER_COST;
                    be.transferCooldown = 8; // 8 ticks between transfers (~2.5 transfers/sec)
                    be.markDirty();

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

        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = pos.offset(dir);
            BlockEntity neighbor = world.getBlockEntity(neighborPos);

            if (neighbor instanceof com.evecual.evecualmc.energy.EnergyStorage storage
                    && !(neighbor instanceof ElectricChuteBlockEntity)) {
                int needed = MAX_ENERGY - this.energy;
                long extracted = storage.extractEnergy(Math.min(needed, 25), false);
                if (extracted > 0) {
                    this.energy += (int) extracted;
                    markDirty();
                }
            }
        }
    }

    @Nullable
    private DronePickupBlockEntity findUpstreamPickup(World world, BlockPos pos, Direction facing) {
        // Look directly backwards from facing first
        BlockEntity direct = world.getBlockEntity(pos.offset(facing.getOpposite()));
        if (direct instanceof DronePickupBlockEntity dp) return dp;

        // Check any adjacent block
        for (Direction d : Direction.values()) {
            if (d == facing) continue;
            BlockEntity be = world.getBlockEntity(pos.offset(d));
            if (be instanceof DronePickupBlockEntity dp) return dp;
        }

        // Trace backwards along chute chain
        BlockPos current = pos.offset(facing.getOpposite());
        for (int i = 0; i < 8; i++) {
            BlockEntity be = world.getBlockEntity(current);
            if (be instanceof DronePickupBlockEntity dp) return dp;
            if (!(be instanceof ElectricChuteBlockEntity)) break;
            current = current.offset(facing.getOpposite());
        }
        return null;
    }

    @Nullable
    private StorageUnitBlockEntity findDownstreamStorage(World world, BlockPos pos, Direction facing) {
        // Look directly in facing direction
        BlockEntity direct = world.getBlockEntity(pos.offset(facing));
        if (direct instanceof StorageUnitBlockEntity su) return su;

        // Trace forward along chute chain
        BlockPos current = pos.offset(facing);
        for (int i = 0; i < 8; i++) {
            BlockEntity be = world.getBlockEntity(current);
            if (be instanceof StorageUnitBlockEntity su) return su;
            if (!(be instanceof ElectricChuteBlockEntity)) break;
            current = current.offset(facing);
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
        }
        return canInsert;
    }

    @Override
    public long extractEnergy(long amount, boolean simulate) {
        return 0;
    }

    public void setEnergy(int energy) {
        this.energy = Math.min(MAX_ENERGY, Math.max(0, energy));
        markDirty();
    }

    public String getStatusMessage() {
        if (this.world == null) return "⚡ Electric Chute: Offline";
        Direction facing = this.getCachedState().get(ElectricChuteBlock.FACING);
        DronePickupBlockEntity pickup = findUpstreamPickup(this.world, this.pos, facing);
        StorageUnitBlockEntity storage = findDownstreamStorage(this.world, this.pos, facing);

        if (pickup == null) {
            return "⚠️ No Drone Pickup Station connected upstream.";
        }
        if (storage == null) {
            return "⚠️ No Storage Unit connected downstream.";
        }
        if (this.energy < TRANSFER_COST) {
            return "⚡ Unpowered: Connect electricity (Needs ≥ 5 EU).";
        }
        if (!pickup.hasParkedDrone()) {
            return "🟢 Active: Awaiting Pickup Drone landing on station.";
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
