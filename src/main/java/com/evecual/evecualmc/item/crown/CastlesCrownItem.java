package com.evecual.evecualmc.item.crown;

import com.evecual.evecualmc.entity.TurretBulletEntity;
import com.evecual.evecualmc.turret.AmmoType;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CastlesCrownItem extends ArmorItem {
    public static final int CANNON_COOLDOWN_TICKS = 40; // 2.0 seconds

    public CastlesCrownItem(Settings settings) {
        super(CrownArmorMaterial.CASTLES, Type.HELMET, settings);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);

        if (world.isClient() || !(entity instanceof PlayerEntity player)) {
            return;
        }

        // Only active when equipped on the head
        if (player.getEquippedStack(EquipmentSlot.HEAD) == stack) {
            // Permanent fortified resilience
            if (!player.hasStatusEffect(StatusEffects.RESISTANCE) || player.getStatusEffect(StatusEffects.RESISTANCE).getDuration() <= 20) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 60, 1, true, false, true));
            }

            // Sprinting ram impact against nearby hostile mobs
            if (player.isSprinting()) {
                Box impactBox = player.getBoundingBox().expand(0.5);
                List<LivingEntity> enemies = world.getEntitiesByClass(LivingEntity.class, impactBox,
                        e -> e != player && e.isAlive() && !e.isTeammate(player));

                for (LivingEntity enemy : enemies) {
                    Vec3d push = enemy.getPos().subtract(player.getPos()).normalize().multiply(0.8).add(0, 0.3, 0);
                    enemy.takeKnockback(0.8, -push.x, -push.z);
                    enemy.damage(world.getDamageSources().playerAttack(player), 6.0f);
                    world.playSound(null, enemy.getX(), enemy.getY(), enemy.getZ(),
                            SoundEvents.BLOCK_ANVIL_PLACE, SoundCategory.PLAYERS, 0.6f, 1.4f);
                }
            }

            // Fortress stone dust particles
            if (player.age % 25 == 0 && world instanceof ServerWorld serverWorld) {
                serverWorld.spawnParticles(ParticleTypes.ASH,
                        player.getX(), player.getY() + 1.9, player.getZ(),
                        3, 0.2, 0.1, 0.2, 0.01);
            }
        }
    }

    /**
     * Fires twin crown-mounted siege cannons forward.
     */
    public static boolean triggerArtilleryAbility(ServerPlayerEntity player, ItemStack stack) {
        World world = player.getWorld();

        if (player.getItemCooldownManager().isCoolingDown(stack.getItem())) {
            return false;
        }

        player.getItemCooldownManager().set(stack.getItem(), CANNON_COOLDOWN_TICKS);

        Vec3d lookVec = player.getRotationVector();
        Vec3d eyePos = player.getEyePos();
        // Calculate perpendicular horizontal vector for left/right cannon offset
        Vec3d up = new Vec3d(0, 1, 0);
        Vec3d rightVec = lookVec.crossProduct(up).normalize();

        double forwardOffset = 0.9;
        double sideOffset = 0.35;
        double heightOffset = 0.15;

        Vec3d leftCannonMuzzle = eyePos.add(lookVec.multiply(forwardOffset))
                .subtract(rightVec.multiply(sideOffset))
                .add(0, heightOffset, 0);

        Vec3d rightCannonMuzzle = eyePos.add(lookVec.multiply(forwardOffset))
                .add(rightVec.multiply(sideOffset))
                .add(0, heightOffset, 0);

        double bulletSpeed = 2.4;

        // Spawn left cannon shot
        TurretBulletEntity leftBullet = new TurretBulletEntity(world, leftCannonMuzzle.x, leftCannonMuzzle.y, leftCannonMuzzle.z, AmmoType.STEEL);
        leftBullet.setVelocity(lookVec.x * bulletSpeed, lookVec.y * bulletSpeed, lookVec.z * bulletSpeed);
        world.spawnEntity(leftBullet);

        // Spawn right cannon shot
        TurretBulletEntity rightBullet = new TurretBulletEntity(world, rightCannonMuzzle.x, rightCannonMuzzle.y, rightCannonMuzzle.z, AmmoType.STEEL);
        rightBullet.setVelocity(lookVec.x * bulletSpeed, lookVec.y * bulletSpeed, lookVec.z * bulletSpeed);
        world.spawnEntity(rightBullet);

        // Cannon Blast Audio
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 1.0f, 0.75f);
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ITEM_ARMOR_EQUIP_NETHERITE, SoundCategory.PLAYERS, 0.9f, 0.6f);

        // Thick black powder smoke and muzzle flash particles
        if (world instanceof ServerWorld serverWorld) {
            serverWorld.spawnParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    leftCannonMuzzle.x, leftCannonMuzzle.y, leftCannonMuzzle.z,
                    8, 0.1, 0.1, 0.1, 0.05);
            serverWorld.spawnParticles(ParticleTypes.FLAME,
                    leftCannonMuzzle.x, leftCannonMuzzle.y, leftCannonMuzzle.z,
                    4, 0.08, 0.08, 0.08, 0.04);

            serverWorld.spawnParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    rightCannonMuzzle.x, rightCannonMuzzle.y, rightCannonMuzzle.z,
                    8, 0.1, 0.1, 0.1, 0.05);
            serverWorld.spawnParticles(ParticleTypes.FLAME,
                    rightCannonMuzzle.x, rightCannonMuzzle.y, rightCannonMuzzle.z,
                    4, 0.08, 0.08, 0.08, 0.04);
        }

        // Damage the crown slightly on firing if not creative
        if (!player.isCreative()) {
            stack.damage(1, player, p -> p.sendEquipmentBreakStatus(EquipmentSlot.HEAD));
        }

        player.sendMessage(Text.literal("§c🏰 Crown Artillery Salvo Fired!").formatted(Formatting.GOLD), true);
        return true;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.literal("🏰 Royal Crown of The Castles").formatted(Formatting.DARK_RED, Formatting.BOLD));
        tooltip.add(Text.literal("Chiseled from weathered fortress stone with twin siege cannons.").formatted(Formatting.GRAY, Formatting.ITALIC));
        tooltip.add(Text.empty());
        tooltip.add(Text.literal("Perks when worn on Head:").formatted(Formatting.YELLOW));
        tooltip.add(Text.literal(" 🏰 Fortress Bulwark: ").formatted(Formatting.RED)
                .append(Text.literal("Permanent Resistance II & Heavy Armor (+6)").formatted(Formatting.WHITE)));
        tooltip.add(Text.literal(" 🏰 Siege Ram: ").formatted(Formatting.RED)
                .append(Text.literal("Heavy sprint charge shoves and damages foes").formatted(Formatting.WHITE)));
        tooltip.add(Text.literal(" 🏰 Twin Siege Cannons [Key V]: ").formatted(Formatting.GOLD)
                .append(Text.literal("Fires dual steel artillery cannonballs").formatted(Formatting.WHITE)));
        super.appendTooltip(stack, world, tooltip, context);
    }
}
