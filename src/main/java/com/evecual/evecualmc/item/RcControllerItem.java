package com.evecual.evecualmc.item;

import com.evecual.evecualmc.entity.RcCarEntity;
import com.evecual.evecualmc.entity.RcDroneEntity;
import com.evecual.evecualmc.entity.RcRobotEntity;
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
        nbt.remove("PairedRobot");
        nbt.putString("PairedType", "car");
        nbt.putBoolean("ActiveLink", true);
        car.setPairedPlayerUuid(player.getUuidAsString());
        car.setExplicitlyPairedInSpot(true);
        if (car.isInParkingSpot() || car.getParkingSpotPos() != null) {
            car.onPairFromParkingSpot();
        }

        player.sendMessage(Text.literal("§a📡 RC Controller paired to RC Car! §7(Range: 256m)"), true);
        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), SoundCategory.PLAYERS, 0.9f, 2.0f);
        return true;
    }

    public static boolean pairWithDrone(ItemStack stack, PlayerEntity player, RcDroneEntity drone) {
        NbtCompound nbt = stack.getOrCreateNbt();
        nbt.putUuid("PairedDrone", drone.getUuid());
        nbt.remove("PairedCar");
        nbt.remove("PairedRobot");
        nbt.putString("PairedType", "drone");
        nbt.putBoolean("ActiveLink", true);
        drone.setPairedPlayerUuid(player.getUuidAsString());
        drone.setExplicitlyPairedInSpot(true);
        if (drone.isInParkingSpot() || drone.getParkingSpotPos() != null) {
            drone.onPairFromParkingSpot();
        }

        player.sendMessage(Text.literal("§a📡 RC Controller paired to RC Drone! §7(Range: 512m)"), true);
        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), SoundCategory.PLAYERS, 0.9f, 2.0f);
        return true;
    }

    public static boolean pairWithRobot(ItemStack stack, PlayerEntity player, RcRobotEntity robot) {
        NbtCompound nbt = stack.getOrCreateNbt();
        nbt.putUuid("PairedRobot", robot.getUuid());
        nbt.remove("PairedCar");
        nbt.remove("PairedDrone");
        nbt.putString("PairedType", "robot");
        nbt.putBoolean("ActiveLink", true);
        robot.setPairedPlayerUuid(player.getUuidAsString());
        robot.setExplicitlyPairedInSpot(true);
        if (robot.isInParkingSpot() || robot.getParkingSpotPos() != null) {
            robot.onPairFromParkingSpot();
        }

        player.sendMessage(Text.literal("§a📡 RC Controller paired to RC Robot! §7(Range: 256m, LMB to use tool)"), true);
        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), SoundCategory.PLAYERS, 0.9f, 2.0f);
        return true;
    }

    public static void unpair(ItemStack stack, @Nullable PlayerEntity player) {
        if (stack.hasNbt()) {
            NbtCompound nbt = stack.getNbt();
            if (nbt != null) {
                nbt.remove("PairedCar");
                nbt.remove("PairedDrone");
                nbt.remove("PairedRobot");
                nbt.remove("PairedType");
                nbt.putBoolean("ActiveLink", false);
            }
        }
    }

    public static void unpairVehicleFromPlayer(World world, UUID vehicleUuid, String playerUuidStr) {
        if (playerUuidStr == null || playerUuidStr.isEmpty()) return;
        try {
            UUID pUuid = UUID.fromString(playerUuidStr);
            PlayerEntity player = world.getPlayerByUuid(pUuid);
            if (player != null) {
                for (ItemStack stack : player.getInventory().main) {
                    if (stack.isOf(com.evecual.evecualmc.EvecualMC.RC_CONTROLLER_ITEM) && stack.hasNbt()) {
                        NbtCompound nbt = stack.getNbt();
                        if (nbt != null) {
                            if ((nbt.containsUuid("PairedCar") && nbt.getUuid("PairedCar").equals(vehicleUuid)) ||
                                (nbt.containsUuid("PairedDrone") && nbt.getUuid("PairedDrone").equals(vehicleUuid)) ||
                                (nbt.containsUuid("PairedRobot") && nbt.getUuid("PairedRobot").equals(vehicleUuid))) {
                                unpair(stack, player);
                            }
                        }
                    }
                }
                for (ItemStack stack : player.getInventory().offHand) {
                    if (stack.isOf(com.evecual.evecualmc.EvecualMC.RC_CONTROLLER_ITEM) && stack.hasNbt()) {
                        NbtCompound nbt = stack.getNbt();
                        if (nbt != null) {
                            if ((nbt.containsUuid("PairedCar") && nbt.getUuid("PairedCar").equals(vehicleUuid)) ||
                                (nbt.containsUuid("PairedDrone") && nbt.getUuid("PairedDrone").equals(vehicleUuid)) ||
                                (nbt.containsUuid("PairedRobot") && nbt.getUuid("PairedRobot").equals(vehicleUuid))) {
                                unpair(stack, player);
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        NbtCompound nbt = stack.getOrCreateNbt();

        // Check for nearby RC Drone, RC Robot, or RC Car to pair with (within 8 blocks)
        net.minecraft.util.math.Vec3d eyePos = user.getEyePos();
        net.minecraft.util.math.Vec3d lookVec = user.getRotationVec(1.0f);
        net.minecraft.util.math.Box searchBox = user.getBoundingBox().expand(8.0);

        Entity targetVehicle = null;
        double minDistance = Double.MAX_VALUE;

        // 1. Search for Robots in view
        for (RcRobotEntity robot : world.getEntitiesByClass(RcRobotEntity.class, searchBox, Entity::isAlive)) {
            net.minecraft.util.math.Vec3d toEntity = robot.getPos().add(0, 0.4, 0).subtract(eyePos).normalize();
            double dot = lookVec.dotProduct(toEntity);
            double dist = user.squaredDistanceTo(robot);
            if (dot > 0.4 && dist < minDistance) {
                minDistance = dist;
                targetVehicle = robot;
            }
        }

        // 2. Search for Drones in view
        for (RcDroneEntity drone : world.getEntitiesByClass(RcDroneEntity.class, searchBox, Entity::isAlive)) {
            net.minecraft.util.math.Vec3d toEntity = drone.getPos().add(0, 0.2, 0).subtract(eyePos).normalize();
            double dot = lookVec.dotProduct(toEntity);
            double dist = user.squaredDistanceTo(drone);
            if (dot > 0.4 && dist < minDistance) {
                minDistance = dist;
                targetVehicle = drone;
            }
        }

        // 3. Search for Cars in view
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
            for (RcRobotEntity robot : world.getEntitiesByClass(RcRobotEntity.class, user.getBoundingBox().expand(4.0), Entity::isAlive)) {
                targetVehicle = robot;
                break;
            }
            if (targetVehicle == null) {
                for (RcDroneEntity drone : world.getEntitiesByClass(RcDroneEntity.class, user.getBoundingBox().expand(4.0), Entity::isAlive)) {
                    targetVehicle = drone;
                    break;
                }
            }
            if (targetVehicle == null) {
                for (RcCarEntity car : world.getEntitiesByClass(RcCarEntity.class, user.getBoundingBox().expand(4.0), Entity::isAlive)) {
                    targetVehicle = car;
                    break;
                }
            }
        }

        boolean hasPairing = nbt.containsUuid("PairedCar") || nbt.containsUuid("PairedDrone") || nbt.containsUuid("PairedRobot");

        // If player sneaks or is unpaired and a vehicle is found, pair immediately!
        if (targetVehicle != null && (user.isSneaking() || !hasPairing)) {
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

        if (!hasPairing) {
            if (!world.isClient) {
                user.sendMessage(Text.literal("§c📡 Not Paired! Right-click near an RC Car, Drone, or Robot to pair."), true);
                world.playSound(null, user.getX(), user.getY(), user.getZ(),
                        SoundEvents.BLOCK_NOTE_BLOCK_BASS.value(), SoundCategory.PLAYERS, 0.8f, 0.8f);
            }
            return TypedActionResult.fail(stack);
        }

        boolean currentActive = nbt.getBoolean("ActiveLink");
        boolean newActive = !currentActive;
        nbt.putBoolean("ActiveLink", newActive);

        boolean isDrone = nbt.containsUuid("PairedDrone") || "drone".equals(nbt.getString("PairedType"));
        boolean isRobot = nbt.containsUuid("PairedRobot") || "robot".equals(nbt.getString("PairedType"));

        if (!world.isClient) {
            if (newActive) {
                UUID carUuid = getPairedCarUuid(stack);
                UUID droneUuid = getPairedDroneUuid(stack);
                UUID robotUuid = getPairedRobotUuid(stack);

                if (isRobot) {
                    if (robotUuid != null) {
                        for (RcRobotEntity robot : world.getEntitiesByClass(RcRobotEntity.class, user.getBoundingBox().expand(256.0), r -> r.getUuid().equals(robotUuid))) {
                            if (robot.isInParkingSpot() || robot.getParkingSpotPos() != null) {
                                robot.onPairFromParkingSpot();
                            }
                        }
                    }
                    user.sendMessage(Text.literal("§6🤖 RC Robot Link: §aENABLED §7[W/A/S/D Move, LMB Use Tool, F Camera]"), true);
                } else if (isDrone) {
                    if (droneUuid != null) {
                        for (RcDroneEntity drone : world.getEntitiesByClass(RcDroneEntity.class, user.getBoundingBox().expand(512.0), d -> d.getUuid().equals(droneUuid))) {
                            if (drone.isInParkingSpot() || drone.getParkingSpotPos() != null) {
                                drone.onPairFromParkingSpot();
                            }
                        }
                    }
                    user.sendMessage(Text.literal("§b🚁 RC Drone Flight Link: §aENABLED §7[W/A/S/D Fly, Space Up, Shift Down, F Camera]"), true);
                } else {
                    if (carUuid != null) {
                        for (RcCarEntity car : world.getEntitiesByClass(RcCarEntity.class, user.getBoundingBox().expand(256.0), c -> c.getUuid().equals(carUuid))) {
                            if (car.isInParkingSpot() || car.getParkingSpotPos() != null) {
                                car.onPairFromParkingSpot();
                            }
                        }
                    }
                    user.sendMessage(Text.literal("§a📡 RC Car Remote Link: §aENABLED §7[W/A/S/D Drive, F Camera]"), true);
                }
                world.playSound(null, user.getX(), user.getY(), user.getZ(),
                        SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.PLAYERS, 0.6f, 1.8f);
            } else {
                user.sendMessage(Text.literal("§7📡 RC Remote Link: §cSTANDBY"), true);
                world.playSound(null, user.getX(), user.getY(), user.getZ(),
                        SoundEvents.BLOCK_BEACON_DEACTIVATE, SoundCategory.PLAYERS, 0.6f, 1.4f);
            }
        }

        return TypedActionResult.success(stack, world.isClient);
    }

    public static @Nullable UUID getPairedCarUuid(ItemStack stack) {
        if (stack.hasNbt() && stack.getNbt() != null && stack.getNbt().containsUuid("PairedCar")) {
            return stack.getNbt().getUuid("PairedCar");
        }
        return null;
    }

    public static @Nullable UUID getPairedDroneUuid(ItemStack stack) {
        if (stack.hasNbt() && stack.getNbt() != null && stack.getNbt().containsUuid("PairedDrone")) {
            return stack.getNbt().getUuid("PairedDrone");
        }
        return null;
    }

    public static @Nullable UUID getPairedRobotUuid(ItemStack stack) {
        if (stack.hasNbt() && stack.getNbt() != null && stack.getNbt().containsUuid("PairedRobot")) {
            return stack.getNbt().getUuid("PairedRobot");
        }
        return null;
    }

    public static boolean isLinkActive(ItemStack stack) {
        return stack.hasNbt() && stack.getNbt() != null && stack.getNbt().getBoolean("ActiveLink");
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        if (stack.hasNbt() && stack.getNbt() != null) {
            NbtCompound nbt = stack.getNbt();
            boolean active = nbt.getBoolean("ActiveLink");
            String type = nbt.getString("PairedType");

            if (nbt.containsUuid("PairedRobot") || "robot".equals(type)) {
                tooltip.add(Text.literal("§7Paired to: §6RC Robot"));
                tooltip.add(Text.literal("§7Link Status: " + (active ? "§aCONNECTED §7(Range: 256m)" : "§cSTANDBY")));
                tooltip.add(Text.literal("§8Controls: W/A/S/D Move | LMB Tool | Z Cargo | C Dock"));
            } else if (nbt.containsUuid("PairedDrone") || "drone".equals(type)) {
                tooltip.add(Text.literal("§7Paired to: §bRC Drone"));
                tooltip.add(Text.literal("§7Link Status: " + (active ? "§aCONNECTED §7(Range: 512m)" : "§cSTANDBY")));
                tooltip.add(Text.literal("§8Controls: W/S Pitch | A/D Roll | Space/Shift Alt | C Auto-Dock"));
            } else if (nbt.containsUuid("PairedCar")) {
                tooltip.add(Text.literal("§7Paired to: §aRC Car"));
                tooltip.add(Text.literal("§7Link Status: " + (active ? "§aCONNECTED §7(Range: 256m)" : "§cSTANDBY")));
                tooltip.add(Text.literal("§8Controls: W/S Throttle | A/D Steering | C Auto-Park"));
            } else {
                tooltip.add(Text.literal("§7Paired to: §cNone"));
                tooltip.add(Text.literal("§8Aim and Right-click near an RC Car, Drone, or Robot to pair."));
            }
        } else {
            tooltip.add(Text.literal("§7Paired to: §cNone"));
            tooltip.add(Text.literal("§8Aim and Right-click near an RC Car, Drone, or Robot to pair."));
        }
        super.appendTooltip(stack, world, tooltip, context);
    }
}
