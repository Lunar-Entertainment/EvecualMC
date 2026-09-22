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
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public class TurretTargetFilter {
    public enum FilterMode {
        WHITELIST, // Listed gametags are SAFE (friends)
        BLACKLIST  // Listed gametags are HUNTED (enemies)
    }

    private boolean targetPlayers = false;   // Default: Players are SAFE
    private boolean targetMonsters = true;  // Default: Shoot hostile monsters
    private boolean targetAnimals = false;  // Default: Passive animals are SAFE
    private boolean targetBosses = true;    // Default: Shoot bosses (Wither, Warden, Dragon)
    private int radius = 64;                // Radius in blocks (Stationary max 256 = 512x512, Flying max 512 = 1024x1024)
    private FilterMode filterMode = FilterMode.WHITELIST;
    private final Set<String> playerList = new LinkedHashSet<>();
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

    public FilterMode getFilterMode() {
        return filterMode;
    }

    public void setFilterMode(FilterMode filterMode) {
        this.filterMode = filterMode != null ? filterMode : FilterMode.WHITELIST;
    }

    public Set<String> getPlayerList() {
        return Collections.unmodifiableSet(playerList);
    }

    public boolean addPlayer(String gametag) {
        if (gametag == null || gametag.trim().isEmpty()) return false;
        return playerList.add(gametag.trim().toLowerCase());
    }

    public boolean removePlayer(String gametag) {
        if (gametag == null) return false;
        return playerList.remove(gametag.trim().toLowerCase());
    }

    public void clearPlayers() {
        playerList.clear();
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
            // Never shoot the owner
            if (ownerUuid != null && ownerUuid.equals(player.getUuid())) {
                return false;
            }

            String gametag = player.getGameProfile().getName().toLowerCase();
            boolean inList = playerList.contains(gametag);

            if (filterMode == FilterMode.WHITELIST) {
                // Whitelist: Listed players are safe (friends). All others targetable if targetPlayers is on.
                if (inList) return false;
                return targetPlayers;
            } else {
                // Blacklist: Only listed players are shot (hostile targets).
                return inList;
            }
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
        nbt.putString("FilterMode", filterMode.name());
        if (ownerUuid != null) {
            nbt.putUuid("OwnerUuid", ownerUuid);
        }

        NbtList list = new NbtList();
        for (String p : playerList) {
            list.add(NbtString.of(p));
        }
        nbt.put("PlayerList", list);
    }

    public void readNbt(NbtCompound nbt) {
        if (nbt.contains("TargetPlayers")) this.targetPlayers = nbt.getBoolean("TargetPlayers");
        if (nbt.contains("TargetMonsters")) this.targetMonsters = nbt.getBoolean("TargetMonsters");
        if (nbt.contains("TargetAnimals")) this.targetAnimals = nbt.getBoolean("TargetAnimals");
        if (nbt.contains("TargetBosses")) this.targetBosses = nbt.getBoolean("TargetBosses");
        if (nbt.contains("Radius")) this.radius = nbt.getInt("Radius");
        if (nbt.contains("FilterMode")) {
            try {
                this.filterMode = FilterMode.valueOf(nbt.getString("FilterMode"));
            } catch (Exception ignored) {
                this.filterMode = FilterMode.WHITELIST;
            }
        }
        if (nbt.containsUuid("OwnerUuid")) this.ownerUuid = nbt.getUuid("OwnerUuid");

        if (nbt.contains("PlayerList", NbtElement.LIST_TYPE)) {
            NbtList list = nbt.getList("PlayerList", NbtElement.STRING_TYPE);
            this.playerList.clear();
            for (int i = 0; i < list.size(); i++) {
                this.playerList.add(list.getString(i).trim().toLowerCase());
            }
        }
    }
}
