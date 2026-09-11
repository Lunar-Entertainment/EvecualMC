package com.evecual.evecualmc.turret;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class TurretTargetFilter {
    private boolean targetPlayers = false;   // Default: Players are SAFE
    private boolean targetMonsters = true;  // Default: Shoot hostile monsters
    private boolean targetAnimals = false;  // Default: Passive animals are SAFE
    private boolean targetBosses = true;    // Default: Shoot bosses (Wither, Warden, Dragon)
    private int radius = 64;                // Radius in blocks (Stationary max 256 = 512x512, Flying max 512 = 1024x1024)
    @Nullable
    private UUID ownerUuid = null;

    public TurretTargetFilter() {}

    public boolean isTargetPlayers() {
        return targetPlayers;
    }

    public void setTargetPlayers(boolean targetPlayers) {
        this.targetPlayers = targetPlayers;
    }

    public boolean isTargetMonsters() {
        return targetMonsters;
    }

    public void setTargetMonsters(boolean targetMonsters) {
        this.targetMonsters = targetMonsters;
    }

    public boolean isTargetAnimals() {
        return targetAnimals;
    }

    public void setTargetAnimals(boolean targetAnimals) {
        this.targetAnimals = targetAnimals;
    }

    public boolean isTargetBosses() {
        return targetBosses;
    }

    public void setTargetBosses(boolean targetBosses) {
        this.targetBosses = targetBosses;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    @Nullable
    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    public void setOwnerUuid(@Nullable UUID ownerUuid) {
        this.ownerUuid = ownerUuid;
    }

    public boolean isValidTarget(LivingEntity entity) {
        if (!entity.isAlive() || entity.isSpectator()) return false;

        // Player check
        if (entity instanceof PlayerEntity player) {
            if (player.isCreative()) return false;
            // Never shoot the owner unless explicitly targeted
            if (ownerUuid != null && ownerUuid.equals(player.getUuid())) {
                return false;
            }
            return targetPlayers;
        }

        // Boss check
        if (entity instanceof WitherEntity || entity instanceof EnderDragonEntity || entity instanceof WardenEntity) {
            return targetBosses;
        }

        // Hostile / Monster check
        if (entity instanceof Monster || entity instanceof HostileEntity) {
            return targetMonsters;
        }

        // Animal / Passive check
        if (entity instanceof AnimalEntity || entity instanceof PassiveEntity) {
            return targetAnimals;
        }

        // Other living entities default to monster rule
        return targetMonsters;
    }

    public void writeNbt(NbtCompound nbt) {
        nbt.putBoolean("TargetPlayers", targetPlayers);
        nbt.putBoolean("TargetMonsters", targetMonsters);
        nbt.putBoolean("TargetAnimals", targetAnimals);
        nbt.putBoolean("TargetBosses", targetBosses);
        nbt.putInt("Radius", radius);
        if (ownerUuid != null) {
            nbt.putUuid("OwnerUuid", ownerUuid);
        }
    }

    public void readNbt(NbtCompound nbt) {
        if (nbt.contains("TargetPlayers")) this.targetPlayers = nbt.getBoolean("TargetPlayers");
        if (nbt.contains("TargetMonsters")) this.targetMonsters = nbt.getBoolean("TargetMonsters");
        if (nbt.contains("TargetAnimals")) this.targetAnimals = nbt.getBoolean("TargetAnimals");
        if (nbt.contains("TargetBosses")) this.targetBosses = nbt.getBoolean("TargetBosses");
        if (nbt.contains("Radius")) this.radius = nbt.getInt("Radius");
        if (nbt.containsUuid("OwnerUuid")) this.ownerUuid = nbt.getUuid("OwnerUuid");
    }
}
