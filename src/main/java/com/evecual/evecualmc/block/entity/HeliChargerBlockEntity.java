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
import net.minecraft.util.math.MathHelper;
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
        ChargerBlockEntity charger = findNearbyChargerBase();
        return charger != null ? (int) charger.getEnergy() : 0;
    }

    public int getMaxEnergy() {
        if (this.world == null) return 2000;
        ChargerBlockEntity charger = findNearbyChargerBase();
        return charger != null ? (int) charger.getMaxEnergy() : 2000;
    }

    @Nullable
    public ChargerBlockEntity findNearbyChargerBase() {
        if (this.world == null) return null;

        // 1. Direct neighbor check (down & 4 cardinal directions)
        BlockEntity below = this.world.getBlockEntity(this.pos.down());
        if (below instanceof ChargerBlockEntity c && c.getEnergy() > 0) {
            return c;
        }
        for (Direction dir : Direction.values()) {
            if (dir == Direction.UP) continue;
            BlockEntity adj = this.world.getBlockEntity(this.pos.offset(dir));
            if (adj instanceof ChargerBlockEntity c && c.getEnergy() > 0) {
                return c;
            }
        }

        // 2. Wireless 16-Block Radius Scan for Vehicle Charger Base
        ChargerBlockEntity best = null;
        double bestDistSq = Double.MAX_VALUE;

        for (BlockPos p : BlockPos.iterate(this.pos.add(-16, -6, -16), this.pos.add(16, 6, 16))) {
            BlockEntity be = this.world.getBlockEntity(p);
            if (be instanceof ChargerBlockEntity charger && charger.getEnergy() > 0) {
                double distSq = this.pos.getSquaredDistance(p);
                if (distSq <= 256.0 && distSq < bestDistSq) { // 16 blocks radius
                    bestDistSq = distSq;
                    best = charger;
                }
            }
        }
        return best;
    }

    public static void tick(World world, BlockPos pos, BlockState state, HeliChargerBlockEntity be) {
        if (world.isClient) return;

        ServerWorld serverWorld = (ServerWorld) world;
        ChargerBlockEntity baseCharger = be.findNearbyChargerBase();

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

                        // Visual electric arc spark beams between charger base and helipad
                        if (world.getTime() % 2 == 0) {
                            if (baseCharger.getPos().getSquaredDistance(pos) > 4.0) {
                                BlockPos basePos = baseCharger.getPos();
                                double bx = basePos.getX() + 0.5;
                                double by = basePos.getY() + 0.5;
                                double bz = basePos.getZ() + 0.5;
                                double hx = pos.getX() + 0.5;
                                double hy = pos.getY() + 0.2;
                                double hz = pos.getZ() + 0.5;

                                for (double t = 0.25; t <= 1.0; t += 0.25) {
                                    double px = MathHelper.lerp(t, bx, hx);
                                    double py = MathHelper.lerp(t, by, hy);
                                    double pz = MathHelper.lerp(t, bz, hz);
                                    serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK, px, py, pz, 1, 0.02, 0.02, 0.02, 0.01);
                                }
                            }

                            // Center & Skid spark emissions
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
