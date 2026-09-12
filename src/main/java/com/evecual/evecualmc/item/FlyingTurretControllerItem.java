package com.evecual.evecualmc.item;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.entity.FlyingTurretEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class FlyingTurretControllerItem extends Item {
    public static final double MAX_RANGE = 1024.0;

    public FlyingTurretControllerItem(Settings settings) {
        super(settings);
    }

    public static boolean pairWithTurret(ItemStack stack, PlayerEntity player, FlyingTurretEntity turret) {
        NbtCompound nbt = stack.getOrCreateNbt();
        nbt.putUuid("PairedTurret", turret.getUuid());
        nbt.putBoolean("ActiveLink", true);
        turret.setPairedPlayerUuid(player.getUuidAsString());

        player.sendMessage(Text.literal("§a🚁 Defense Controller paired to Defense Drone! §7(Range: 1024m)"), true);
        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), SoundCategory.PLAYERS, 0.9f, 2.0f);
        return true;
    }

    public static void unpair(ItemStack stack, @Nullable PlayerEntity player) {
        if (stack.hasNbt()) {
            NbtCompound nbt = stack.getNbt();
            if (nbt != null) {
                nbt.remove("PairedTurret");
                nbt.putBoolean("ActiveLink", false);
            }
        }
        if (player != null) {
            player.sendMessage(Text.literal("§e📡 Defense Controller unpaired."), true);
            player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BLOCK_NOTE_BLOCK_BASS.value(), SoundCategory.PLAYERS, 0.8f, 0.8f);
        }
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        NbtCompound nbt = stack.getOrCreateNbt();

        // Search for nearby Defense Drone to pair with (within 16 blocks)
        Vec3d eyePos = user.getEyePos();
        Vec3d lookVec = user.getRotationVec(1.0f);
        Box searchBox = user.getBoundingBox().expand(16.0);

        FlyingTurretEntity targetedTurret = null;
        double minDistance = Double.MAX_VALUE;
        for (FlyingTurretEntity turret : world.getEntitiesByClass(FlyingTurretEntity.class, searchBox, Entity::isAlive)) {
            Vec3d toEntity = turret.getPos().add(0, 0.4, 0).subtract(eyePos).normalize();
            double dot = lookVec.dotProduct(toEntity);
            double dist = user.squaredDistanceTo(turret);
            if (dot > 0.4 && dist < minDistance) {
                minDistance = dist;
                targetedTurret = turret;
            }
        }

        if (targetedTurret == null) {
            for (FlyingTurretEntity turret : world.getEntitiesByClass(FlyingTurretEntity.class, user.getBoundingBox().expand(8.0), Entity::isAlive)) {
                targetedTurret = turret;
                break;
            }
        }

        boolean hasPairing = nbt.containsUuid("PairedTurret");

        // Pairing interaction
        if (targetedTurret != null && (user.isSneaking() || !hasPairing)) {
            if (!world.isClient) {
                pairWithTurret(stack, user, targetedTurret);
            }
            return TypedActionResult.success(stack, world.isClient());
        }

        // Shift-click in the air: Open Remote Terminal GUI
        if (user.isSneaking() && hasPairing) {
            if (!world.isClient && user instanceof ServerPlayerEntity serverPlayer) {
                UUID pairedUuid = nbt.getUuid("PairedTurret");
                Entity e = serverPlayer.getServerWorld().getEntity(pairedUuid);
                if (e instanceof FlyingTurretEntity turret && turret.isAlive()) {
                    if (user.squaredDistanceTo(turret) <= MAX_RANGE * MAX_RANGE) {
                        serverPlayer.openHandledScreen(turret);
                        user.sendMessage(Text.literal("§b📡 Remote Defense Drone Terminal opened!"), true);
                    } else {
                        user.sendMessage(Text.literal("§c⚠️ Defense Drone is out of communication range (> 1024m)!"), true);
                    }
                } else {
                    user.sendMessage(Text.literal("§c⚠️ Paired Defense Drone not found in this dimension!"), true);
                }
            }
            return TypedActionResult.success(stack, world.isClient());
        }

        // Normal Right-Click: Toggle Active Telecommand Link
        if (hasPairing) {
            boolean currentLink = nbt.getBoolean("ActiveLink");
            nbt.putBoolean("ActiveLink", !currentLink);
            if (!world.isClient) {
                if (!currentLink) {
                    user.sendMessage(Text.literal("§a📡 Defense Drone Remote Link Online §7(1024m Range)"), true);
                    world.playSound(null, user.getX(), user.getY(), user.getZ(),
                            SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.PLAYERS, 0.6f, 1.8f);
                } else {
                    user.sendMessage(Text.literal("§c📡 Defense Drone Remote Link Standby"), true);
                    world.playSound(null, user.getX(), user.getY(), user.getZ(),
                            SoundEvents.BLOCK_BEACON_DEACTIVATE, SoundCategory.PLAYERS, 0.6f, 1.4f);
                }
            }
            return TypedActionResult.success(stack, world.isClient());
        }

        if (!world.isClient) {
            user.sendMessage(Text.literal("§e⚠️ Aim and Right-Click a Defense Drone within 16 blocks to pair!"), true);
        }
        return TypedActionResult.pass(stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.literal("§7Defense Drone Remote Piloting & Targeting Device"));
        tooltip.add(Text.literal("§6📡 Operating Range: §a1024 Blocks"));
        tooltip.add(Text.literal("§e⚠️ Required to fly & reposition Defense Drone"));

        if (stack.hasNbt() && stack.getNbt() != null && stack.getNbt().containsUuid("PairedTurret")) {
            boolean active = stack.getNbt().getBoolean("ActiveLink");
            tooltip.add(Text.literal("§b🚁 Paired: §fDefense Drone"));
            tooltip.add(Text.literal("§aStatus: " + (active ? "§2● ACTIVE LINK" : "§7○ STANDBY")));
            tooltip.add(Text.literal("§7[WASD / Space / Sneak] Pilot & move drone"));
            tooltip.add(Text.literal("§7[Right-Click] Toggle Link"));
            tooltip.add(Text.literal("§7[Shift + Right-Click] Open Remote Terminal"));
            tooltip.add(Text.literal("§7[F Key] Toggle FPV Drone Camera"));
            tooltip.add(Text.literal("§7[LMB / Attack] Manual Kinetic Cannon Fire"));
            tooltip.add(Text.literal("§7[C Key] Recall Drone to Player"));
        } else {
            tooltip.add(Text.literal("§eStatus: §7Not Paired"));
            tooltip.add(Text.literal("§7Right-click on a Defense Drone to pair"));
        }
    }
}
