package com.evecual.evecualmc.block.entity;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.energy.EnergyStorage;
import com.evecual.evecualmc.entity.RcCarEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RcChargerBlockEntity extends BlockEntity implements EnergyStorage {
    public static final int MAX_ENERGY = 2000;
    private int energy = 2000;

    public RcChargerBlockEntity(BlockPos pos, BlockState state) {
        super(EvecualMC.RC_CHARGER_BLOCK_ENTITY, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, RcChargerBlockEntity be) {
        // Passive inductive solar trickle charge
        if (be.energy < MAX_ENERGY) {
            be.energy = Math.min(MAX_ENERGY, be.energy + 2);
            be.markDirty();
        }

        // Check for RC Cars parked squarely on the charger pad
        Box area = new Box(pos.getX() + 0.05, pos.getY(), pos.getZ() + 0.05, pos.getX() + 0.95, pos.getY() + 0.6, pos.getZ() + 0.95);
        List<RcCarEntity> cars = world.getEntitiesByClass(RcCarEntity.class, area, RcCarEntity::isAlive);

        for (RcCarEntity car : cars) {
            if (car.getEnergy() < RcCarEntity.MAX_ENERGY) {
                int needed = RcCarEntity.MAX_ENERGY - car.getEnergy();
                int transfer = Math.min(needed, 10);
                car.setEnergy(car.getEnergy() + transfer);

                // Stop auto-returning once docked
                if (car.isAutoReturning()) {
                    car.onReachedCharger();
                }

                // Visual spark particles
                if (world.isClient && world.random.nextFloat() < 0.45f) {
                    world.addParticle(ParticleTypes.ELECTRIC_SPARK,
                            car.getX() + (world.random.nextDouble() - 0.5) * 0.35,
                            car.getY() + 0.15,
                            car.getZ() + (world.random.nextDouble() - 0.5) * 0.35,
                            0, 0.08, 0);
                }

                // Sound feedback occasionally
                if (car.age % 25 == 0) {
                    world.playSound(null, pos, SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE, SoundCategory.BLOCKS, 0.35f, 2.0f);
                }
            }
        }
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
        long accepted = Math.min(amount, MAX_ENERGY - this.energy);
        if (!simulate) {
            this.energy += (int) accepted;
            markDirty();
        }
        return accepted;
    }

    @Override
    public long extractEnergy(long amount, boolean simulate) {
        long extracted = Math.min(amount, this.energy);
        if (!simulate) {
            this.energy -= (int) extracted;
            markDirty();
        }
        return extracted;
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putInt("Energy", this.energy);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        if (nbt.contains("Energy")) {
            this.energy = nbt.getInt("Energy");
        }
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        NbtCompound nbt = new NbtCompound();
        writeNbt(nbt);
        return nbt;
    }
}
