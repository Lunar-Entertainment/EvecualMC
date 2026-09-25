package com.evecual.evecualmc.item;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.energy.ItemEnergyHelper;
import net.fabricmc.fabric.api.dimension.v1.FabricDimensions;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RailgunItem extends Item {
    public static final String IGNITE_MODE_KEY = "IgniteMode";
    public static final int MAX_ENERGY = 2000;
    public static final int ENERGY_PER_SHOT = 100;
    public static final float BASE_DAMAGE = 36.0F;
    public static final double MAX_RANGE = 80.0;
    public static final int COOLDOWN_TICKS = 25;

    public RailgunItem(Settings settings) {
        super(settings);
    }

    public static boolean isIgniteMode(ItemStack stack) {
        return stack.hasNbt() && stack.getNbt().getBoolean(IGNITE_MODE_KEY);
    }

    public static void setIgniteMode(ItemStack stack, boolean ignite) {
        stack.getOrCreateNbt().putBoolean(IGNITE_MODE_KEY, ignite);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);
        ItemStack stack = context.getStack();
        PlayerEntity player = context.getPlayer();

        if (state.isOf(EvecualMC.ELACTORITE_BLOCK) && isIgniteMode(stack)) {
            if (!world.isClient() && player instanceof ServerPlayerEntity serverPlayer) {
                teleportToEvecualDimension(serverPlayer, pos);
            }
            return ActionResult.success(world.isClient());
        }
        return super.useOnBlock(context);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        // Ignite Mode handling: clicking on elactorite block triggers rift
        if (isIgniteMode(stack)) {
            HitResult hit = user.raycast(6.0, 0.0F, false);
            if (hit.getType() == HitResult.Type.BLOCK) {
                BlockPos hitPos = ((BlockHitResult) hit).getBlockPos();
                if (world.getBlockState(hitPos).isOf(EvecualMC.ELACTORITE_BLOCK)) {
                    if (!world.isClient() && user instanceof ServerPlayerEntity serverPlayer) {
                        teleportToEvecualDimension(serverPlayer, hitPos);
                    }
                    return TypedActionResult.success(stack, world.isClient());
                }
            }
            if (!world.isClient()) {
                user.sendMessage(Text.literal("§d🔥 Railgun in Ignite Mode: §eRight-click an Elactorite Block to ignite the rift."), true);
            }
            return TypedActionResult.fail(stack);
        }

        long energy = ItemEnergyHelper.getEnergy(stack);

        if (!user.isCreative() && energy < ENERGY_PER_SHOT) {
            if (!world.isClient()) {
                user.sendMessage(Text.literal("§c⚡ Railgun Discharged! (" + energy + "/" + MAX_ENERGY + " EU) Recharge in Item Charger."), true);
                world.playSound(null, user.getX(), user.getY(), user.getZ(),
                        SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.PLAYERS, 0.8F, 1.6F);
            }
            return TypedActionResult.fail(stack);
        }

        // Determine ammunition bonus and consume 1 unit
        float ammoBonus = 0.0F;
        String ammoName = "Pure Plasma Flux";
        boolean hasSpecialAmmo = false;

        if (!user.isCreative()) {
            ItemStack ammoStack = findAmmo(user);
            if (!ammoStack.isEmpty()) {
                if (ammoStack.isOf(EvecualMC.ELACTORITE_AMMO)) {
                    ammoBonus = 20.0F;
                    ammoName = "Elactorite Hyper-Slug";
                    hasSpecialAmmo = true;
                } else if (ammoStack.isOf(EvecualMC.STEEL_ROD)) {
                    ammoBonus = 16.0F;
                    ammoName = "Heavy Steel Kinetic Rod";
                    hasSpecialAmmo = true;
                } else if (ammoStack.isOf(EvecualMC.STEEL_AMMO)) {
                    ammoBonus = 12.0F;
                    ammoName = "Steel Armor-Piercing Slug";
                    hasSpecialAmmo = true;
                } else if (ammoStack.isOf(EvecualMC.IRON_AMMO)) {
                    ammoBonus = 8.0F;
                    ammoName = "Iron Slug";
                    hasSpecialAmmo = true;
                } else if (ammoStack.isOf(EvecualMC.COPPER_AMMO)) {
                    ammoBonus = 4.0F;
                    ammoName = "Copper Slug";
                    hasSpecialAmmo = true;
                }
                ammoStack.decrement(1);
            }
            ItemEnergyHelper.discharge(stack, ENERGY_PER_SHOT);
        } else {
            ammoBonus = 20.0F;
            ammoName = "Creative Hyper-Slug";
            hasSpecialAmmo = true;
        }

        float totalDamage = BASE_DAMAGE + ammoBonus;

        // Apply physical recoil to player
        Vec3d lookVec = user.getRotationVector();
        Vec3d recoil = lookVec.multiply(-0.32);
        user.addVelocity(recoil.x, Math.max(-0.08, recoil.y * 0.4), recoil.z);
        user.velocityModified = true;

        if (!world.isClient()) {
            ServerWorld serverWorld = (ServerWorld) world;
            Vec3d start = user.getEyePos().add(lookVec.multiply(0.4));
            Vec3d end = start.add(lookVec.multiply(MAX_RANGE));

            // Check block collision along the line
            BlockHitResult blockHit = world.raycast(new RaycastContext(
                    start, end, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, user));
            if (blockHit.getType() != HitResult.Type.MISS) {
                end = blockHit.getPos();
            }

            // Penetrating Lorentz Ray: find all living entities in beam trajectory
            List<LivingEntity> struckEntities = getEntitiesInBeam(world, user, start, end, 1.2);
            for (LivingEntity target : struckEntities) {
                target.damage(world.getDamageSources().playerAttack(user), totalDamage);
                Vec3d targetImpulse = lookVec.multiply(0.8).add(0, 0.25, 0);
                target.addVelocity(targetImpulse.x, targetImpulse.y, targetImpulse.z);
                target.velocityModified = true;

                // Impact sparks on each struck entity
                serverWorld.spawnParticles(ParticleTypes.FLASH, target.getX(), target.getY() + target.getHeight() * 0.5, target.getZ(), 1, 0, 0, 0, 0);
                serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY() + target.getHeight() * 0.5, target.getZ(), 20, 0.4, 0.4, 0.4, 0.08);
            }

            // Spawn continuous hypersonic particle beam
            double totalDist = start.distanceTo(end);
            int steps = (int) (totalDist * 3.0);
            Vec3d stepVec = end.subtract(start).multiply(1.0 / Math.max(1, steps));

            for (int i = 0; i <= steps; i++) {
                Vec3d point = start.add(stepVec.multiply(i));
                serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK, point.x, point.y, point.z, 1, 0.02, 0.02, 0.02, 0.01);
                if (i % 6 == 0) {
                    serverWorld.spawnParticles(ParticleTypes.END_ROD, point.x, point.y, point.z, 1, 0.01, 0.01, 0.01, 0.0);
                }
                if (hasSpecialAmmo && i % 10 == 0) {
                    serverWorld.spawnParticles(ParticleTypes.REVERSE_PORTAL, point.x, point.y, point.z, 1, 0.02, 0.02, 0.02, 0.0);
                }
            }

            // Impact shockwave at terminus
            serverWorld.spawnParticles(ParticleTypes.SONIC_BOOM, end.x, end.y, end.z, 1, 0, 0, 0, 0);
            serverWorld.spawnParticles(ParticleTypes.EXPLOSION, end.x, end.y, end.z, 2, 0.2, 0.2, 0.2, 0.02);
            serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK, end.x, end.y, end.z, 30, 0.5, 0.5, 0.5, 0.15);

            // Supersonic crack & Lorentz explosion audio
            world.playSound(null, user.getX(), user.getY(), user.getZ(),
                    SoundEvents.ENTITY_WARDEN_SONIC_BOOM, SoundCategory.PLAYERS, 1.4F, 1.4F);
            world.playSound(null, user.getX(), user.getY(), user.getZ(),
                    SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.PLAYERS, 1.0F, 1.8F);
            world.playSound(null, user.getX(), user.getY(), user.getZ(),
                    SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 0.7F, 1.9F);

            // Impact sound at terminus
            world.playSound(null, end.x, end.y, end.z,
                    SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT, SoundCategory.PLAYERS, 1.2F, 1.5F);

            user.getItemCooldownManager().set(this, COOLDOWN_TICKS);
            user.sendMessage(Text.literal("§b💥 RAILGUN DISCHARGED! §7(" + ammoName + " §e" + (int) totalDamage + " DMG§7)"), true);
        }

        return TypedActionResult.success(stack, world.isClient());
    }

    public static void teleportToEvecualDimension(ServerPlayerEntity player, BlockPos clickedPos) {
        MinecraftServer server = player.getServer();
        if (server == null) return;

        ServerWorld currentWorld = player.getServerWorld();
        RegistryKey<World> EVECUAL_WORLD_KEY = RegistryKey.of(RegistryKeys.WORLD, new Identifier(EvecualMC.MOD_ID, "evecual"));

        boolean inEvecual = currentWorld.getRegistryKey().equals(EVECUAL_WORLD_KEY);

        if (inEvecual) {
            // Return to Overworld
            ServerWorld overworld = server.getWorld(World.OVERWORLD);
            if (overworld == null) return;

            BlockPos destPos = clickedPos;
            int topY = overworld.getTopY(Heightmap.Type.MOTION_BLOCKING, destPos.getX(), destPos.getZ());
            if (topY <= overworld.getBottomY()) {
                topY = overworld.getSeaLevel() + 1;
            }

            // FX in departure dimension
            currentWorld.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BLOCK_PORTAL_TRAVEL, SoundCategory.PLAYERS, 0.8F, 1.3F);
            currentWorld.spawnParticles(ParticleTypes.REVERSE_PORTAL, player.getX(), player.getY() + 1.0, player.getZ(), 30, 0.5, 0.5, 0.5, 0.1);

            Vec3d targetVec = new Vec3d(destPos.getX() + 0.5, topY, destPos.getZ() + 0.5);
            FabricDimensions.teleport(player, overworld, new TeleportTarget(
                    targetVec,
                    Vec3d.ZERO,
                    player.getYaw(),
                    player.getPitch()
            ));

            // FX in arrival dimension
            overworld.playSound(null, targetVec.x, targetVec.y, targetVec.z,
                    SoundEvents.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, SoundCategory.PLAYERS, 1.0F, 1.5F);
            overworld.spawnParticles(ParticleTypes.ELECTRIC_SPARK, targetVec.x, targetVec.y + 1.0, targetVec.z, 40, 0.6, 0.6, 0.6, 0.15);
            overworld.spawnParticles(ParticleTypes.FLASH, targetVec.x, targetVec.y + 1.0, targetVec.z, 1, 0, 0, 0, 0);

            player.sendMessage(Text.literal("§b⚡ Dimensional Rift: §fReturned to the §aOverworld§f!"), true);
        } else {
            // Travel to Evecual Dimension
            ServerWorld evecualWorld = server.getWorld(EVECUAL_WORLD_KEY);
            if (evecualWorld == null) {
                player.sendMessage(Text.literal("§c⚠️ Dimension 'evecual' could not be found!"), true);
                return;
            }

            // FX in departure dimension
            currentWorld.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BLOCK_PORTAL_TRAVEL, SoundCategory.PLAYERS, 0.8F, 1.1F);
            currentWorld.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENTITY_WARDEN_SONIC_BOOM, SoundCategory.PLAYERS, 0.4F, 1.8F);
            currentWorld.spawnParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 1.0, player.getZ(), 40, 0.5, 0.5, 0.5, 0.1);
            currentWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getY() + 1.0, player.getZ(), 25, 0.5, 0.5, 0.5, 0.1);

            int targetX = clickedPos.getX();
            int targetZ = clickedPos.getZ();

            // Safe landing: superflat surface is y=4 (bedrock=0, dirt=1..2, grass=3).
            // Place an Elactorite block at surface (y=3) right under the player's feet so they have a rift return point!
            int surfaceY = 3;
            BlockPos basePlatformPos = new BlockPos(targetX, surfaceY, targetZ);
            evecualWorld.setBlockState(basePlatformPos, EvecualMC.ELACTORITE_BLOCK.getDefaultState(), Block.NOTIFY_ALL);
            evecualWorld.setBlockState(basePlatformPos.up(), Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
            evecualWorld.setBlockState(basePlatformPos.up(2), Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);

            Vec3d targetVec = new Vec3d(targetX + 0.5, surfaceY + 1.0, targetZ + 0.5);
            FabricDimensions.teleport(player, evecualWorld, new TeleportTarget(
                    targetVec,
                    Vec3d.ZERO,
                    player.getYaw(),
                    player.getPitch()
            ));

            // FX in arrival dimension
            evecualWorld.playSound(null, targetVec.x, targetVec.y, targetVec.z,
                    SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE, SoundCategory.PLAYERS, 1.0F, 1.4F);
            evecualWorld.spawnParticles(ParticleTypes.REVERSE_PORTAL, targetVec.x, targetVec.y + 1.0, targetVec.z, 50, 0.6, 0.6, 0.6, 0.1);
            evecualWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK, targetVec.x, targetVec.y + 1.0, targetVec.z, 30, 0.6, 0.6, 0.6, 0.1);
            evecualWorld.spawnParticles(ParticleTypes.FLASH, targetVec.x, targetVec.y + 1.0, targetVec.z, 1, 0, 0, 0, 0);

            player.sendMessage(Text.literal("§d🌌 Dimensional Rift: §fWelcome to the §5Evecual Dimension§f!"), true);
        }
    }

    private ItemStack findAmmo(PlayerEntity player) {
        // Priority order: Elactorite -> Steel Rod -> Steel Ammo -> Iron Ammo -> Copper Ammo
        Item[] ammoPriority = new Item[]{
                EvecualMC.ELACTORITE_AMMO,
                EvecualMC.STEEL_ROD,
                EvecualMC.STEEL_AMMO,
                EvecualMC.IRON_AMMO,
                EvecualMC.COPPER_AMMO
        };

        for (Item candidate : ammoPriority) {
            for (int i = 0; i < player.getInventory().size(); i++) {
                ItemStack st = player.getInventory().getStack(i);
                if (!st.isEmpty() && st.isOf(candidate)) {
                    return st;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    private List<LivingEntity> getEntitiesInBeam(World world, PlayerEntity shooter, Vec3d start, Vec3d end, double beamRadius) {
        Box searchBox = new Box(start, end).expand(beamRadius + 1.0);
        List<LivingEntity> candidates = world.getEntitiesByClass(LivingEntity.class, searchBox,
                e -> e != shooter && e.isAlive() && !e.isSpectator());

        List<LivingEntity> hitList = new ArrayList<>();
        Vec3d beamVec = end.subtract(start);
        double beamLenSq = beamVec.lengthSquared();

        for (LivingEntity entity : candidates) {
            Box entityBox = entity.getBoundingBox().expand(0.3);
            // Distance from entity center to segment
            Vec3d toEntity = entity.getEyePos().subtract(start);
            double t = Math.max(0.0, Math.min(1.0, toEntity.dotProduct(beamVec) / Math.max(0.0001, beamLenSq)));
            Vec3d projection = start.add(beamVec.multiply(t));
            if (entityBox.contains(projection) || projection.distanceTo(entity.getEyePos()) <= beamRadius) {
                hitList.add(entity);
            }
        }

        hitList.sort(Comparator.comparingDouble(e -> e.squaredDistanceTo(start)));
        return hitList;
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        long energy = ItemEnergyHelper.getEnergy(stack);
        return Math.round((float) energy * 13.0F / (float) MAX_ENERGY);
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        long energy = ItemEnergyHelper.getEnergy(stack);
        float ratio = (float) energy / (float) MAX_ENERGY;
        if (ratio > 0.5F) {
            return 0x00E5FF; // Electric Cyan
        } else if (ratio > 0.2F) {
            return 0xF59E0B; // Amber Warning
        } else {
            return 0xEF4444; // Red Critical
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        long energy = ItemEnergyHelper.getEnergy(stack);
        int pct = (int) (energy * 100 / MAX_ENERGY);

        tooltip.add(Text.literal("§6⚡ Hypervelocity Lorentz Accelerator"));
        if (isIgniteMode(stack)) {
            tooltip.add(Text.literal("§d🔥 Mode: §6IGNITE MODE §7(Dimensional Ignition)"));
            tooltip.add(Text.literal("§d• Right-Click Elactorite Block: §fIgnite Rift to Evecual Dimension"));
            tooltip.add(Text.literal("§e• Shift + 5: §fSwitch back to Kinetic Accelerator"));
        } else {
            tooltip.add(Text.literal("§b⚡ Mode: §fKinetic Accelerator"));
            tooltip.add(Text.literal("§e• Shift + 5: §fEnter Ignite Mode"));
        }
        tooltip.add(Text.literal("§e⚡ Energy: §f" + energy + " / " + MAX_ENERGY + " EU §7(" + pct + "%)"));
        tooltip.add(Text.literal("§e• Right-Click (RMB): §fFire Supersonic Kinetic Beam (-100 EU)"));
        tooltip.add(Text.literal("§e• Penetration: §fPierces through all targets in line of fire"));
        tooltip.add(Text.literal("§b• Ammo Compatibility:"));
        tooltip.add(Text.literal("  §7- §dElactorite Ammo: §f+20 DMG (56 total)"));
        tooltip.add(Text.literal("  §7- §fSteel Rod: §f+16 DMG (52 total)"));
        tooltip.add(Text.literal("  §7- §fSteel Ammo: §f+12 DMG (48 total)"));
        tooltip.add(Text.literal("  §7- §fIron/Copper Ammo: §f+4-8 DMG"));
        tooltip.add(Text.literal("  §7- §bPure Plasma Flux: §f36 DMG (No ammo required)"));
        tooltip.add(Text.literal("§8Recharge in an Item Charger station"));
        super.appendTooltip(stack, world, tooltip, context);
    }
}
