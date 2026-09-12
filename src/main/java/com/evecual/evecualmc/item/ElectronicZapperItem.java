package com.evecual.evecualmc.item;

import com.evecual.evecualmc.energy.ItemEnergyHelper;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public class ElectronicZapperItem extends Item {
    public static final String MODE_KEY = "ZapperMode";
    public static final int MODE_ATTACK = 0;
    public static final int MODE_ZAP = 1;

    public static final int MAX_ENERGY = ItemEnergyHelper.ZAPPER_MAX_ENERGY; // 1000 EU
    public static final int COST_ZAP_BLOCK = 5;
    public static final int COST_ATTACK_MELEE = 10;
    public static final int COST_ATTACK_RANGED = 20;

    public ElectronicZapperItem(Settings settings) {
        super(settings);
    }

    public static int getMode(ItemStack stack) {
        if (!stack.hasNbt() || !stack.getNbt().contains(MODE_KEY)) {
            return MODE_ATTACK;
        }
        return stack.getNbt().getInt(MODE_KEY);
    }

    public static void setMode(ItemStack stack, int mode) {
        stack.getOrCreateNbt().putInt(MODE_KEY, mode);
    }

    public static boolean isAttackMode(ItemStack stack) {
        return getMode(stack) == MODE_ATTACK;
    }

    public static boolean isZapMode(ItemStack stack) {
        return getMode(stack) == MODE_ZAP;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        // Shift + Right-Click: Toggle Mode
        if (user.isSneaking()) {
            int currentMode = getMode(stack);
            int newMode = (currentMode == MODE_ATTACK) ? MODE_ZAP : MODE_ATTACK;
            setMode(stack, newMode);

            if (!world.isClient()) {
                if (newMode == MODE_ZAP) {
                    user.sendMessage(Text.literal("§e⚡ Zapper Mode: §b🌀 ZAP MODE §7(Instant Block Disintegration)"), true);
                    world.playSound(null, user.getX(), user.getY(), user.getZ(),
                            SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE, SoundCategory.PLAYERS, 0.85F, 1.9F);
                } else {
                    user.sendMessage(Text.literal("§e⚡ Zapper Mode: §c⚔ ATTACK MODE §7(Enhanced Combat & Shock Arcs)"), true);
                    world.playSound(null, user.getX(), user.getY(), user.getZ(),
                            SoundEvents.ITEM_TRIDENT_THUNDER, SoundCategory.PLAYERS, 0.75F, 1.7F);
                }
            }
            return TypedActionResult.success(stack, world.isClient());
        }

        long currentEnergy = ItemEnergyHelper.getEnergy(stack);

        // Zap Mode: Raycast up to 12 blocks to disintegrate blocks at range
        if (isZapMode(stack)) {
            HitResult hit = user.raycast(12.0, 0.0F, false);
            if (hit.getType() == HitResult.Type.BLOCK) {
                BlockHitResult blockHit = (BlockHitResult) hit;
                BlockPos targetPos = blockHit.getBlockPos();
                BlockState state = world.getBlockState(targetPos);

                if (state.getHardness(world, targetPos) >= 0 && !state.isAir()) {
                    if (currentEnergy < COST_ZAP_BLOCK) {
                        if (!world.isClient()) {
                            user.sendMessage(Text.literal("§c⚡ Zapper Discharged! (" + currentEnergy + "/" + MAX_ENERGY + " EU) Charge in Item Charger."), true);
                            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.PLAYERS, 0.6F, 1.6F);
                        }
                        return TypedActionResult.fail(stack);
                    }

                    if (!world.isClient()) {
                        ServerWorld serverWorld = (ServerWorld) world;
                        ItemEnergyHelper.discharge(stack, COST_ZAP_BLOCK);
                        world.breakBlock(targetPos, true, user);

                        // Beam particle line from player to block
                        Vec3d start = user.getEyePos().add(user.getRotationVector().multiply(0.3));
                        Vec3d end = Vec3d.ofCenter(targetPos);
                        spawnElectricBeam(serverWorld, start, end, 16);

                        // Block impact effects
                        serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK, targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5, 14, 0.25, 0.25, 0.25, 0.05);
                        serverWorld.spawnParticles(ParticleTypes.SONIC_BOOM, targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5, 1, 0, 0, 0, 0);

                        world.playSound(null, targetPos, SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT, SoundCategory.BLOCKS, 0.6F, 1.8F);
                        world.playSound(null, targetPos, SoundEvents.BLOCK_AMETHYST_BLOCK_BREAK, SoundCategory.BLOCKS, 0.8F, 1.6F);

                        user.getItemCooldownManager().set(this, 3);
                    }
                    return TypedActionResult.success(stack, world.isClient());
                }
            }
        }

        // Attack Mode: Right-Click discharges a concentrated plasma shock arc at target creature
        if (isAttackMode(stack)) {
            LivingEntity target = getTargetEntity(user, 16.0);
            if (target != null) {
                if (currentEnergy < COST_ATTACK_RANGED) {
                    if (!world.isClient()) {
                        user.sendMessage(Text.literal("§c⚡ Low Power! Need " + COST_ATTACK_RANGED + " EU for Plasma Arc (Have " + currentEnergy + " EU)."), true);
                        world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.PLAYERS, 0.6F, 1.6F);
                    }
                    return TypedActionResult.fail(stack);
                }

                if (!world.isClient()) {
                    ServerWorld serverWorld = (ServerWorld) world;
                    ItemEnergyHelper.discharge(stack, COST_ATTACK_RANGED);
                    target.damage(world.getDamageSources().playerAttack(user), 16.0F);

                    // Electric beam from player to target
                    Vec3d start = user.getEyePos().add(user.getRotationVector().multiply(0.3));
                    Vec3d end = target.getEyePos();
                    spawnElectricBeam(serverWorld, start, end, 20);

                    // Lightning impact FX
                    serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY() + target.getHeight() * 0.5, target.getZ(), 24, 0.35, 0.35, 0.35, 0.08);
                    serverWorld.spawnParticles(ParticleTypes.FLASH, target.getX(), target.getY() + target.getHeight() * 0.5, target.getZ(), 1, 0, 0, 0, 0);

                    world.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT, SoundCategory.PLAYERS, 0.8F, 1.6F);
                    world.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.PLAYERS, 0.6F, 1.8F);

                    user.getItemCooldownManager().set(this, 10);
                }
                return TypedActionResult.success(stack, world.isClient());
            } else {
                // If no entity aimed at, fire a forward spark discharge
                if (currentEnergy < 5) {
                    if (!world.isClient()) {
                        user.sendMessage(Text.literal("§c⚡ Zapper Discharged! Charge in Item Charger."), true);
                        world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.PLAYERS, 0.5F, 1.8F);
                    }
                    return TypedActionResult.fail(stack);
                }

                if (!world.isClient()) {
                    ServerWorld serverWorld = (ServerWorld) world;
                    ItemEnergyHelper.discharge(stack, 5);
                    Vec3d start = user.getEyePos().add(user.getRotationVector().multiply(0.3));
                    Vec3d end = start.add(user.getRotationVector().multiply(12.0));
                    spawnElectricBeam(serverWorld, start, end, 14);
                    world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT, SoundCategory.PLAYERS, 0.4F, 2.0F);
                    user.getItemCooldownManager().set(this, 8);
                }
                return TypedActionResult.success(stack, world.isClient());
            }
        }

        return super.use(world, user, hand);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        PlayerEntity user = context.getPlayer();
        if (user != null && user.isSneaking()) {
            return ActionResult.PASS; // Defer to use() for shift+click mode toggling
        }

        ItemStack stack = context.getStack();
        if (isZapMode(stack)) {
            World world = context.getWorld();
            BlockPos pos = context.getBlockPos();
            BlockState state = world.getBlockState(pos);

            if (state.getHardness(world, pos) >= 0 && !state.isAir()) {
                long energy = ItemEnergyHelper.getEnergy(stack);
                if (energy < COST_ZAP_BLOCK) {
                    if (!world.isClient() && user != null) {
                        user.sendMessage(Text.literal("§c⚡ Zapper Discharged! (" + energy + "/" + MAX_ENERGY + " EU) Charge in Item Charger."), true);
                        world.playSound(null, pos, SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.PLAYERS, 0.6F, 1.6F);
                    }
                    return ActionResult.FAIL;
                }

                if (!world.isClient()) {
                    ServerWorld serverWorld = (ServerWorld) world;
                    ItemEnergyHelper.discharge(stack, COST_ZAP_BLOCK);
                    world.breakBlock(pos, true, user);

                    serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 12, 0.2, 0.2, 0.2, 0.05);
                    serverWorld.spawnParticles(ParticleTypes.SONIC_BOOM, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1, 0, 0, 0, 0);

                    world.playSound(null, pos, SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT, SoundCategory.BLOCKS, 0.6F, 1.8F);
                    world.playSound(null, pos, SoundEvents.BLOCK_AMETHYST_BLOCK_BREAK, SoundCategory.BLOCKS, 0.8F, 1.6F);

                    if (user != null) {
                        user.getItemCooldownManager().set(this, 3);
                    }
                }
                return ActionResult.SUCCESS;
            }
        }

        return super.useOnBlock(context);
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        World world = attacker.getWorld();
        long energy = ItemEnergyHelper.getEnergy(stack);

        if (isAttackMode(stack)) {
            // Enhanced Attack Mode: High electric strike + chain shock
            if (energy >= COST_ATTACK_MELEE) {
                if (!world.isClient() && attacker instanceof PlayerEntity player) {
                    ServerWorld serverWorld = (ServerWorld) world;
                    ItemEnergyHelper.discharge(stack, COST_ATTACK_MELEE);

                    // Extra bonus electric shock damage
                    target.damage(world.getDamageSources().playerAttack(player), 14.0F);

                    // Particle explosion
                    serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY() + 0.8, target.getZ(), 20, 0.3, 0.3, 0.3, 0.08);
                    serverWorld.spawnParticles(ParticleTypes.FLASH, target.getX(), target.getY() + 1.0, target.getZ(), 1, 0, 0, 0, 0);

                    world.playSound(null, target.getX(), target.getY(), target.getZ(),
                            SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT, SoundCategory.PLAYERS, 0.85F, 1.5F);

                    // Chain electric arcs to up to 2 nearby hostile mobs within 6 blocks
                    Box nearbyBox = target.getBoundingBox().expand(6.0);
                    List<LivingEntity> nearby = world.getEntitiesByClass(LivingEntity.class, nearbyBox,
                            e -> e != attacker && e != target && e.isAlive() && (e instanceof HostileEntity || e instanceof MobEntity));

                    int chained = 0;
                    for (LivingEntity chainTarget : nearby) {
                        if (chained >= 2) break;
                        chainTarget.damage(world.getDamageSources().playerAttack(player), 8.0F);
                        spawnElectricBeam(serverWorld, target.getEyePos(), chainTarget.getEyePos(), 10);
                        serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK, chainTarget.getX(), chainTarget.getY() + 0.5, chainTarget.getZ(), 10, 0.2, 0.2, 0.2, 0.04);
                        chained++;
                    }
                }
            } else {
                // Out of energy in attack mode
                if (!world.isClient() && attacker instanceof PlayerEntity player) {
                    player.sendMessage(Text.literal("§c⚡ Zapper Discharged! (" + energy + "/" + MAX_ENERGY + " EU) Charge in Item Charger."), true);
                    world.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), SoundEvents.BLOCK_NOTE_BLOCK_SNARE.value(), SoundCategory.PLAYERS, 0.5F, 1.9F);
                }
            }
        } else {
            // Zap Mode: Hits with lower/minor damage
            if (!world.isClient()) {
                ServerWorld serverWorld = (ServerWorld) world;
                serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY() + 0.5, target.getZ(), 5, 0.15, 0.15, 0.15, 0.02);
                world.playSound(null, target.getX(), target.getY(), target.getZ(),
                        SoundEvents.BLOCK_NOTE_BLOCK_SNARE.value(), SoundCategory.PLAYERS, 0.5F, 1.9F);
            }
        }

        return super.postHit(stack, target, attacker);
    }

    private void spawnElectricBeam(ServerWorld world, Vec3d start, Vec3d end, int segments) {
        Vec3d diff = end.subtract(start);
        for (int i = 0; i <= segments; i++) {
            double progress = (double) i / segments;
            Vec3d pos = start.add(diff.multiply(progress));
            world.spawnParticles(ParticleTypes.ELECTRIC_SPARK, pos.x, pos.y, pos.z, 1, 0.02, 0.02, 0.02, 0.01);
            if (i % 4 == 0) {
                world.spawnParticles(ParticleTypes.REVERSE_PORTAL, pos.x, pos.y, pos.z, 1, 0.01, 0.01, 0.01, 0.0);
            }
        }
    }

    @Nullable
    private LivingEntity getTargetEntity(PlayerEntity player, double maxDistance) {
        World world = player.getWorld();
        Vec3d eyePos = player.getEyePos();
        Vec3d rot = player.getRotationVector();
        Vec3d reach = eyePos.add(rot.multiply(maxDistance));

        Box searchBox = player.getBoundingBox().stretch(rot.multiply(maxDistance)).expand(1.5);
        Predicate<Entity> predicate = e -> !e.isSpectator() && e.isAlive() && e != player && e instanceof LivingEntity;

        double closestDist = maxDistance * maxDistance;
        LivingEntity closestTarget = null;

        for (Entity entity : world.getOtherEntities(player, searchBox, predicate)) {
            Box entityBox = entity.getBoundingBox().expand(0.3);
            var optHit = entityBox.raycast(eyePos, reach);
            if (optHit.isPresent()) {
                double dist = eyePos.squaredDistanceTo(optHit.get());
                if (dist < closestDist) {
                    closestDist = dist;
                    closestTarget = (LivingEntity) entity;
                }
            }
        }

        return closestTarget;
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
            return 0x00E5FF; // Bright Cyan Electric
        } else if (ratio > 0.2F) {
            return 0xF59E0B; // Amber Warning
        } else {
            return 0xEF4444; // Red Critical
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        boolean attack = isAttackMode(stack);
        long energy = ItemEnergyHelper.getEnergy(stack);
        int pct = (int) (energy * 100 / MAX_ENERGY);

        if (attack) {
            tooltip.add(Text.literal("§6⚡ Mode: §c⚔ ATTACK MODE §7(Enhanced Combat)"));
        } else {
            tooltip.add(Text.literal("§6⚡ Mode: §b🌀 ZAP MODE §7(Block Disintegration)"));
        }

        tooltip.add(Text.literal("§e⚡ Energy: §f" + energy + " / " + MAX_ENERGY + " EU §7(" + pct + "%)"));
        tooltip.add(Text.literal("§e• Shift + Right-Click: §fSwitch Active Mode"));
        tooltip.add(Text.literal("§e• Zap Mode (RMB): §fDisintegrate blocks (-5 EU)"));
        tooltip.add(Text.literal("§e• Attack Mode: §f+14 Melee Shock (-10 EU) / Plasma Bolt (-20 EU)"));
        tooltip.add(Text.literal("§8Recharge in an Item Charger station"));
        super.appendTooltip(stack, world, tooltip, context);
    }
}
