package com.evecual.evecualmc.block.entity;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.entity.HeliEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
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
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HeliChargerBlockEntity extends BlockEntity {
    private boolean activeCharging = false;

    public HeliChargerBlockEntity(BlockPos pos, BlockState state) {
        super(EvecualMC.HELI_CHARGER_BLOCK_ENTITY, pos, state);
    }

    public int getStoredEnergy() {
        if (this.world == null) return 0;
        ChargerBlockEntity charger = findConnectedBase();
        return charger != null ? (int) charger.getEnergy() : 0;
    }

    public int getMaxEnergy() {
        if (this.world == null) return 2000;
        ChargerBlockEntity charger = findConnectedBase();
        return charger != null ? (int) charger.getMaxEnergy() : 2000;
    }

    @Nullable
    public ChargerBlockEntity findConnectedBase() {
        if (this.world == null) return null;
        BlockEntity below = this.world.getBlockEntity(this.pos.down());
        if (below instanceof ChargerBlockEntity c) {
            return c;
        }
        for (Direction dir : Direction.values()) {
            if (dir == Direction.UP) continue;
            BlockEntity adj = this.world.getBlockEntity(this.pos.offset(dir));
            if (adj instanceof ChargerBlockEntity c) {
                return c;
            }
        }
        return null;
    }

    public static void tick(World world, BlockPos pos, BlockState state, HeliChargerBlockEntity be) {
        if (world.isClient) return;

        ServerWorld serverWorld = (ServerWorld) world;
        ChargerBlockEntity baseCharger = be.findConnectedBase();

        // Search for EV Heli positioned on or landed on the 3x3 Helipad (3x3 blocks footprint)
        Box searchBox = new Box(pos.getX() - 2.0, pos.getY(), pos.getZ() - 2.0,
                pos.getX() + 3.0, pos.getY() + 2.8, pos.getZ() + 3.0);
        List<HeliEntity> helis = world.getEntitiesByClass(HeliEntity.class, searchBox, Entity -> true);

        boolean chargingAny = false;
        for (HeliEntity heli : helis) {
            double dx = Math.abs(heli.getX() - (pos.getX() + 0.5));
            double dz = Math.abs(heli.getZ() - (pos.getZ() + 0.5));
            double dy = heli.getY() - (pos.getY() + 0.05);

            if (dx <= 2.2 && dz <= 2.2 && dy >= -0.2 && dy <= 1.8) {
                if (baseCharger != null && baseCharger.getEnergy() > 0 && heli.getEnergy() < heli.getMaxEnergy()) {
                    int needed = heli.getMaxEnergy() - heli.getEnergy();
                    int transfer = Math.min((int) baseCharger.getEnergy(), Math.min(needed, 15)); // 300 EU/s
                    if (transfer > 0) {
                        baseCharger.extractEnergy(transfer, false);
                        heli.charge(transfer);
                        heli.setCharging(true);
                        chargingAny = true;

                        // Visual electric sparks between charger pad and helicopter skids
                        if (world.getTime() % 2 == 0) {
                            // Center & Corner spark emissions
                            double sx = pos.getX() + 0.5 + (world.random.nextDouble() - 0.5) * 2.0;
                            double sz = pos.getZ() + 0.5 + (world.random.nextDouble() - 0.5) * 2.0;
                            serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK,
                                    sx, pos.getY() + 0.15, sz,
                                    2, 0.05, 0.05, 0.05, 0.01);
                            serverWorld.spawnParticles(ParticleTypes.COMPOSTER,
                                    heli.getX(), heli.getY() + 0.3, heli.getZ(),
                                    1, 0.1, 0.1, 0.1, 0.02);
                        }

                        if (world.getTime() % 20 == 0) {
                            world.playSound(null, pos, SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE,
                                    SoundCategory.BLOCKS, 0.4F, 1.8F);
                        }
                    }
                } else if (heli.getEnergy() >= heli.getMaxEnergy()) {
                    heli.setCharging(false);
                }
            }
        }

        be.activeCharging = chargingAny;
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
