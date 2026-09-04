package com.evecual.evecualmc.item;

import com.evecual.evecualmc.entity.RcCarEntity;
import com.evecual.evecualmc.entity.RcDroneEntity;
import com.evecual.evecualmc.entity.RcRobotEntity;
import net.minecraft.block.Block;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
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

public class StationaryRcControllerItem extends BlockItem {
    public StationaryRcControllerItem(Block block, Settings settings) {
        super(block, settings);
    }

    public static boolean isPaired(ItemStack stack) {
        if (!stack.hasNbt() || stack.getNbt() == null) return false;
        NbtCompound nbt = stack.getNbt();
        return nbt.containsUuid("PairedVehicle") || nbt.containsUuid("PairedCar")
                || nbt.containsUuid("PairedDrone") || nbt.containsUuid("PairedRobot");
    }

    public static boolean pairWithCar(ItemStack stack, PlayerEntity player, RcCarEntity car) {
        NbtCompound nbt = stack.getOrCreateNbt();
        nbt.putUuid("PairedVehicle", car.getUuid());
        nbt.putUuid("PairedCar", car.getUuid());
        nbt.remove("PairedDrone");
        nbt.remove("PairedRobot");
        nbt.putString("PairedType", "car");
        nbt.putString("VehicleName", "RC Car");
        nbt.putBoolean("ActiveLink", true);
        if (car.isInParkingSpot() || car.getParkingSpotPos() != null) {
            car.onPairFromParkingSpot();
        }

        player.sendMessage(Text.literal("§a📡 Stationary Controller linked to RC Car! Place down to install station."), true);
        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), SoundCategory.PLAYERS, 0.9f, 2.0f);
        return true;
    }

    public static boolean pairWithDrone(ItemStack stack, PlayerEntity player, RcDroneEntity drone) {
        NbtCompound nbt = stack.getOrCreateNbt();
        nbt.putUuid("PairedVehicle", drone.getUuid());
        nbt.putUuid("PairedDrone", drone.getUuid());
        nbt.remove("PairedCar");
        nbt.remove("PairedRobot");
        nbt.putString("PairedType", "drone");
        nbt.putString("VehicleName", "RC Drone");
        nbt.putBoolean("ActiveLink", true);
        if (drone.isInParkingSpot() || drone.getParkingSpotPos() != null) {
            drone.onPairFromParkingSpot();
        }

        player.sendMessage(Text.literal("§a📡 Stationary Controller linked to RC Drone! Place down to install station."), true);
        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), SoundCategory.PLAYERS, 0.9f, 2.0f);
        return true;
    }

    public static boolean pairWithRobot(ItemStack stack, PlayerEntity player, RcRobotEntity robot) {
        NbtCompound nbt = stack.getOrCreateNbt();
        nbt.putUuid("PairedVehicle", robot.getUuid());
        nbt.putUuid("PairedRobot", robot.getUuid());
        nbt.remove("PairedCar");
        nbt.remove("PairedDrone");
        nbt.putString("PairedType", "robot");
        nbt.putString("VehicleName", "RC Robot");
        nbt.putBoolean("ActiveLink", true);
        if (robot.isInParkingSpot() || robot.getParkingSpotPos() != null) {
            robot.onPairFromParkingSpot();
        }

        player.sendMessage(Text.literal("§a📡 Stationary Controller linked to RC Robot! Place down to install station."), true);
        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), SoundCategory.PLAYERS, 0.9f, 2.0f);
        return true;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        ItemStack stack = context.getStack();
        PlayerEntity player = context.getPlayer();

        if (!isPaired(stack)) {
            if (player != null) {
                player.sendMessage(Text.literal("§c⚠️ Right-click an RC Car, Drone, or Robot with this terminal to link it before placing!"), true);
                context.getWorld().playSound(null, context.getBlockPos(), SoundEvents.BLOCK_NOTE_BLOCK_BASS.value(), SoundCategory.BLOCKS, 0.8f, 0.8f);
            }
            return ActionResult.FAIL;
        }

        return super.useOnBlock(context);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        // Allow aiming at an RC vehicle up to 6 blocks away to pair
        Vec3d eyePos = user.getEyePos();
        Vec3d lookVec = user.getRotationVec(1.0f);
        Box searchBox = user.getBoundingBox().expand(6.0);

        Entity targetVehicle = null;
        double minDistance = Double.MAX_VALUE;

        for (RcRobotEntity robot : world.getEntitiesByClass(RcRobotEntity.class, searchBox, Entity::isAlive)) {
            Vec3d toEntity = robot.getPos().add(0, 0.4, 0).subtract(eyePos).normalize();
            double dot = lookVec.dotProduct(toEntity);
            double dist = user.squaredDistanceTo(robot);
            if (dot > 0.3 && dist < minDistance) {
                minDistance = dist;
                targetVehicle = robot;
            }
        }

        for (RcDroneEntity drone : world.getEntitiesByClass(RcDroneEntity.class, searchBox, Entity::isAlive)) {
            Vec3d toEntity = drone.getPos().add(0, 0.2, 0).subtract(eyePos).normalize();
            double dot = lookVec.dotProduct(toEntity);
            double dist = user.squaredDistanceTo(drone);
            if (dot > 0.3 && dist < minDistance) {
                minDistance = dist;
                targetVehicle = drone;
            }
        }

        for (RcCarEntity car : world.getEntitiesByClass(RcCarEntity.class, searchBox, Entity::isAlive)) {
            Vec3d toEntity = car.getPos().add(0, 0.2, 0).subtract(eyePos).normalize();
            double dot = lookVec.dotProduct(toEntity);
            double dist = user.squaredDistanceTo(car);
            if (dot > 0.3 && dist < minDistance) {
                minDistance = dist;
                targetVehicle = car;
            }
        }

        if (targetVehicle != null) {
            if (!world.isClient) {
                if (targetVehicle instanceof RcRobotEntity robot) {
                    pairWithRobot(stack, user, robot);
                } else if (targetVehicle instanceof RcDroneEntity drone) {
                    pairWithDrone(stack, user, drone);
                } else if (targetVehicle instanceof RcCarEntity car) {
                    pairWithCar(stack, user, car);
                }
            }
            return TypedActionResult.success(stack, world.isClient);
        }

        return super.use(world, user, hand);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        if (isPaired(stack)) {
            NbtCompound nbt = stack.getNbt();
            String type = nbt != null ? nbt.getString("PairedType") : "";
            String name = "car".equals(type) ? "RC Car" : "drone".equals(type) ? "RC Drone" : "robot".equals(type) ? "RC Robot" : "RC Vehicle";
            tooltip.add(Text.literal("§7Status: §aLinked to " + name));
            tooltip.add(Text.literal("§bReady to install! Place on ground to set up terminal."));
        } else {
            tooltip.add(Text.literal("§7Status: §eUnlinked"));
            tooltip.add(Text.literal("§eRight-click an RC Car, Drone, or Robot to link first."));
            tooltip.add(Text.literal("§cCannot be placed down until linked to a vehicle."));
        }
        tooltip.add(Text.literal("§8Stationary terminal: locks player in place with vehicle view."));
        super.appendTooltip(stack, world, tooltip, context);
    }
}
