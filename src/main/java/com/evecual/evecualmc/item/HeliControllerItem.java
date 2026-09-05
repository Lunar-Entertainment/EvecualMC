package com.evecual.evecualmc.item;

import com.evecual.evecualmc.entity.HeliEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class HeliControllerItem extends Item {
    public static final double MAX_RANGE = 1024.0;

    public HeliControllerItem(Settings settings) {
        super(settings);
    }

    public static boolean pairWithHeli(ItemStack stack, PlayerEntity player, HeliEntity heli) {
        NbtCompound nbt = stack.getOrCreateNbt();
        nbt.putUuid("PairedHeli", heli.getUuid());
        nbt.putBoolean("ActiveLink", true);
        heli.setPairedPlayerUuid(player.getUuidAsString());

        player.sendMessage(Text.literal("§a🚁 Heli Controller paired to EV Heli! §7(Range: 1024m)"), true);
        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), SoundCategory.PLAYERS, 0.9f, 2.0f);
        return true;
    }

    public static void unpair(ItemStack stack, @Nullable PlayerEntity player) {
        if (stack.hasNbt()) {
            NbtCompound nbt = stack.getNbt();
            if (nbt != null) {
                nbt.remove("PairedHeli");
                nbt.putBoolean("ActiveLink", false);
            }
        }
        if (player != null) {
            player.sendMessage(Text.literal("§e📡 Heli Controller unpaired."), true);
            player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BLOCK_NOTE_BLOCK_BASS.value(), SoundCategory.PLAYERS, 0.8f, 0.8f);
        }
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        NbtCompound nbt = stack.getOrCreateNbt();

        // Check for nearby EV Heli to pair with (within 12 blocks)
        net.minecraft.util.math.Vec3d eyePos = user.getEyePos();
        net.minecraft.util.math.Vec3d lookVec = user.getRotationVec(1.0f);
        net.minecraft.util.math.Box searchBox = user.getBoundingBox().expand(12.0);

        HeliEntity targetHeli = null;
        double minDistance = Double.MAX_VALUE;

        for (HeliEntity heli : world.getEntitiesByClass(HeliEntity.class, searchBox, Entity::isAlive)) {
            net.minecraft.util.math.Vec3d toEntity = heli.getPos().add(0, 0.5, 0).subtract(eyePos).normalize();
            double dot = lookVec.dotProduct(toEntity);
            double dist = user.squaredDistanceTo(heli);
            if (dot > 0.3 && dist < minDistance) {
                minDistance = dist;
                targetHeli = heli;
            }
        }

        if (targetHeli == null) {
            for (HeliEntity heli : world.getEntitiesByClass(HeliEntity.class, user.getBoundingBox().expand(6.0), Entity::isAlive)) {
                targetHeli = heli;
                break;
            }
        }

        boolean hasPairing = nbt.containsUuid("PairedHeli");

        // If shift-clicking or unpairing
        if (targetHeli != null && (user.isSneaking() || !hasPairing)) {
            if (!world.isClient) {
                pairWithHeli(stack, user, targetHeli);
            }
            return TypedActionResult.success(stack, world.isClient());
        }

        if (user.isSneaking() && hasPairing) {
            if (!world.isClient) {
                unpair(stack, user);
            }
            return TypedActionResult.success(stack, world.isClient());
        }

        // Toggle Active Telecommand Link
        if (hasPairing) {
            boolean currentLink = nbt.getBoolean("ActiveLink");
            nbt.putBoolean("ActiveLink", !currentLink);
            if (!world.isClient) {
                if (!currentLink) {
                    user.sendMessage(Text.literal("§a📡 Heli Remote Link Online §7(1024m Range)"), true);
                    world.playSound(null, user.getX(), user.getY(), user.getZ(),
                            SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.PLAYERS, 0.6f, 1.8f);
                } else {
                    user.sendMessage(Text.literal("§c📡 Heli Remote Link Standby"), true);
                    world.playSound(null, user.getX(), user.getY(), user.getZ(),
                            SoundEvents.BLOCK_BEACON_DEACTIVATE, SoundCategory.PLAYERS, 0.6f, 1.4f);
                }
            }
            return TypedActionResult.success(stack, world.isClient());
        }

        if (!world.isClient) {
            user.sendMessage(Text.literal("§e⚠️ Aim and Right-Click an EV Heli within 12 blocks to pair controller!"), true);
        }
        return TypedActionResult.pass(stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.literal("§7Aerospace Long-Range Flight Controller"));
        tooltip.add(Text.literal("§6📡 Operating Range: §a1024 Blocks"));

        if (stack.hasNbt() && stack.getNbt() != null && stack.getNbt().containsUuid("PairedHeli")) {
            boolean active = stack.getNbt().getBoolean("ActiveLink");
            tooltip.add(Text.literal("§b🚁 Paired: §fEV Heli"));
            tooltip.add(Text.literal("§aStatus: " + (active ? "§2● ACTIVE LINK" : "§7○ STANDBY")));
            tooltip.add(Text.literal("§7[Right-Click] Toggle Link"));
            tooltip.add(Text.literal("§7[Shift + Right-Click] Unpair / Re-pair"));
        } else {
            tooltip.add(Text.literal("§eStatus: §7Not Paired"));
            tooltip.add(Text.literal("§7Right-click on an EV Heli to pair"));
        }
    }
}
