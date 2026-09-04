package com.evecual.evecualmc.item;

import com.evecual.evecualmc.entity.RcCarEntity;
import com.evecual.evecualmc.entity.RcDroneEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class RcControllerItem extends Item {
    public RcControllerItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        return ActionResult.PASS;
    }

    public static boolean pairWithCar(ItemStack stack, PlayerEntity player, RcCarEntity car) {
        NbtCompound nbt = stack.getOrCreateNbt();
        nbt.putUuid("PairedCar", car.getUuid());
        nbt.remove("PairedDrone");
        nbt.putString("PairedType", "car");
        nbt.putBoolean("ActiveLink", true);
        car.setPairedPlayerUuid(player.getUuidAsString());

        player.sendMessage(Text.literal("§a📡 RC Controller paired to RC Car! §7(Range: 256m)"), true);
        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), SoundCategory.PLAYERS, 0.9f, 2.0f);
        return true;
    }

    public static boolean pairWithDrone(ItemStack stack, PlayerEntity player, RcDroneEntity drone) {
        NbtCompound nbt = stack.getOrCreateNbt();
        nbt.putUuid("PairedDrone", drone.getUuid());
        nbt.remove("PairedCar");
        nbt.putString("PairedType", "drone");
        nbt.putBoolean("ActiveLink", true);
        drone.setPairedPlayerUuid(player.getUuidAsString());

        player.sendMessage(Text.literal("§a📡 RC Controller paired to RC Drone! §7(Range: 512m)"), true);
        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), SoundCategory.PLAYERS, 0.9f, 2.0f);
        return true;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        NbtCompound nbt = stack.getOrCreateNbt();

        // Check for nearby RC Drone or RC Car to pair with (within 8 blocks)
        net.minecraft.util.math.Vec3d eyePos = user.getEyePos();
        net.minecraft.util.math.Vec3d lookVec = user.getRotationVec(1.0f);
        net.minecraft.util.math.Box searchBox = user.getBoundingBox().expand(8.0);

        Entity targetVehicle = null;
        double minDistance = Double.MAX_VALUE;

        // 1. Search for Drones in view
        for (RcDroneEntity drone : world.getEntitiesByClass(RcDroneEntity.class, searchBox, Entity::isAlive)) {
            net.minecraft.util.math.Vec3d toEntity = drone.getPos().add(0, 0.2, 0).subtract(eyePos).normalize();
            double dot = lookVec.dotProduct(toEntity);
            double dist = user.squaredDistanceTo(drone);
            if (dot > 0.4 && dist < minDistance) {
                minDistance = dist;
                targetVehicle = drone;
            }
        }

        // 2. Search for Cars in view
        for (RcCarEntity car : world.getEntitiesByClass(RcCarEntity.class, searchBox, Entity::isAlive)) {
            net.minecraft.util.math.Vec3d toEntity = car.getPos().add(0, 0.2, 0).subtract(eyePos).normalize();
            double dot = lookVec.dotProduct(toEntity);
            double dist = user.squaredDistanceTo(car);
            if (dot > 0.4 && dist < minDistance) {
                minDistance = dist;
                targetVehicle = car;
            }
        }

        // Fallback search in tight 4-block radius if not directly looked at
        if (targetVehicle == null) {
            for (RcDroneEntity drone : world.getEntitiesByClass(RcDroneEntity.class, user.getBoundingBox().expand(4.0), Entity::isAlive)) {
                targetVehicle = drone;
                break;
            }
            if (targetVehicle == null) {
                for (RcCarEntity car : world.getEntitiesByClass(RcCarEntity.class, user.getBoundingBox().expand(4.0), Entity::isAlive)) {
                    targetVehicle = car;
                    break;
                }
            }
        }

        boolean hasPairing = nbt.containsUuid("PairedCar") || nbt.containsUuid("PairedDrone");

        // If player sneaks or is unpaired and a vehicle is found, pair immediately!
        if (targetVehicle != null && (user.isSneaking() || !hasPairing)) {
            if (!world.isClient) {
                if (targetVehicle instanceof RcDroneEntity drone) {
                    pairWithDrone(stack, user, drone);
                } else if (targetVehicle instanceof RcCarEntity car) {
                    pairWithCar(stack, user, car);
                }
            }
            return TypedActionResult.success(stack, world.isClient);
        }

        if (!hasPairing) {
            if (!world.isClient) {
                user.sendMessage(Text.literal("§c📡 Not Paired! Right-click near an RC Car or Drone to pair."), true);
                world.playSound(null, user.getX(), user.getY(), user.getZ(),
                        SoundEvents.BLOCK_NOTE_BLOCK_BASS.value(), SoundCategory.PLAYERS, 0.8f, 0.8f);
            }
            return TypedActionResult.fail(stack);
        }

        boolean currentActive = nbt.getBoolean("ActiveLink");
        boolean newActive = !currentActive;
        nbt.putBoolean("ActiveLink", newActive);

        boolean isDrone = nbt.containsUuid("PairedDrone") || "drone".equals(nbt.getString("PairedType"));

        if (!world.isClient) {
            if (newActive) {
                if (isDrone) {
                    user.sendMessage(Text.literal("§b🚁 RC Drone Flight Link: §aENABLED §7[W/A/S/D Fly, Space Up, Shift Down, F Camera]"), true);
                } else {
                    user.sendMessage(Text.literal("§b🏎️ RC Car Driving Link: §aENABLED §7[W/A/S/D Drive, Space Hop, F Camera]"), true);
                }
                world.playSound(null, user.getX(), user.getY(), user.getZ(),
                        SoundEvents.BLOCK_NOTE_BLOCK_BIT.value(), SoundCategory.PLAYERS, 0.8f, 1.8f);
            } else {
                user.sendMessage(Text.literal("§7📡 RC Remote Link: §cDISABLED"), true);
                world.playSound(null, user.getX(), user.getY(), user.getZ(),
                        SoundEvents.BLOCK_NOTE_BLOCK_BIT.value(), SoundCategory.PLAYERS, 0.8f, 1.0f);
            }
        }

        return TypedActionResult.success(stack, world.isClient);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        NbtCompound nbt = stack.getNbt();
        if (nbt != null && nbt.containsUuid("PairedDrone")) {
            UUID droneUuid = nbt.getUuid("PairedDrone");
            boolean active = nbt.getBoolean("ActiveLink");
            String shortId = droneUuid.toString().substring(0, 8);
            tooltip.add(Text.literal("§a📡 Paired: §bRC Drone #" + shortId));
            tooltip.add(Text.literal("§e📶 Max Range: §f512 blocks"));
            tooltip.add(Text.literal(active ? "§b⚡ Link: §aACTIVE" : "§7⚡ Link: §cSTANDBY"));
            tooltip.add(Text.literal("§8• §7Right-click to toggle remote flight mode"));
            tooltip.add(Text.literal("§8• §7W/A/S/D Fly, Space Up, Shift Down, Ctrl Boost"));
            tooltip.add(Text.literal("§8• §7Press F for FPV Camera, Arrows to Orbit"));
        } else if (nbt != null && nbt.containsUuid("PairedCar")) {
            UUID carUuid = nbt.getUuid("PairedCar");
            boolean active = nbt.getBoolean("ActiveLink");
            String shortId = carUuid.toString().substring(0, 8);
            tooltip.add(Text.literal("§a📡 Paired: §fRC Car #" + shortId));
            tooltip.add(Text.literal("§e📶 Max Range: §f256 blocks"));
            tooltip.add(Text.literal(active ? "§b⚡ Link: §aACTIVE" : "§7⚡ Link: §cSTANDBY"));
            tooltip.add(Text.literal("§8• §7Right-click to toggle remote drive mode"));
            tooltip.add(Text.literal("§8• §7Use W/A/S/D to steer & drive, Space to Hop"));
            tooltip.add(Text.literal("§8• §7Press F for Chase Camera, Arrows to Orbit"));
        } else {
            tooltip.add(Text.literal("§c📡 Status: Unpaired"));
            tooltip.add(Text.literal("§8• §7Sneak + Right-click on an RC Car or Drone to pair"));
        }
        super.appendTooltip(stack, world, tooltip, context);
    }
}
