package com.evecual.evecualmc.item;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.energy.ItemEnergyHelper;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RailgunItem extends Item {
    public static final int MAX_ENERGY = 2000;
    public static final int ENERGY_PER_SHOT = 100;
    public static final float BASE_DAMAGE = 36.0F;
    public static final double MAX_RANGE = 80.0;
    public static final int COOLDOWN_TICKS = 25;

    public RailgunItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
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
