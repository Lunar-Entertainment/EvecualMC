package com.evecual.evecualmc.item.crown;

import com.evecual.evecualmc.energy.ItemEnergyHelper;
import com.evecual.evecualmc.util.NoFireLightning;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;

public class ElectriciansCrownItem extends ArmorItem {
    public static final int MAX_ENERGY = 5000;
    public static final int ABILITY_ENERGY_COST = 150;
    private static final DustParticleEffect CYAN_ELECTRIC = new DustParticleEffect(new Vector3f(0.15f, 0.85f, 1.0f), 0.9f);

    public ElectriciansCrownItem(Settings settings) {
        super(CrownArmorMaterial.ELECTRICIANS, Type.HELMET, settings);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);

        if (world.isClient() || !(entity instanceof ServerPlayerEntity player)) {
            return;
        }

        // Only active when equipped on the head
        if (player.getEquippedStack(EquipmentSlot.HEAD) == stack) {
            long currentEnergy = ItemEnergyHelper.getEnergy(stack);

            // Constant energetic speed and jump boost
            if (!player.hasStatusEffect(StatusEffects.SPEED) || player.getStatusEffect(StatusEffects.SPEED).getDuration() <= 20) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 60, 1, true, false, true));
            }

            // In thunderstorms or rain, harvest atmospheric static energy
            if (world.isThundering() && world.isSkyVisible(player.getBlockPos()) && player.age % 10 == 0) {
                if (currentEnergy < MAX_ENERGY) {
                    ItemEnergyHelper.setEnergy(stack, Math.min(MAX_ENERGY, currentEnergy + 25));
                }
            } else if (world.isRaining() && world.isSkyVisible(player.getBlockPos()) && player.age % 20 == 0) {
                if (currentEnergy < MAX_ENERGY) {
                    ItemEnergyHelper.setEnergy(stack, Math.min(MAX_ENERGY, currentEnergy + 10));
                }
            }

            // Inductive wireless recharge of held items
            if (currentEnergy > 5 && player.age % 10 == 0) {
                ItemStack main = player.getMainHandStack();
                if (!main.isEmpty() && ItemEnergyHelper.isChargeable(main)) {
                    long mainEnergy = ItemEnergyHelper.getEnergy(main);
                    long mainMax = ItemEnergyHelper.getMaxEnergy(main);
                    if (mainEnergy < mainMax) {
                        long transferred = Math.min(20, Math.min(currentEnergy, mainMax - mainEnergy));
                        ItemEnergyHelper.setEnergy(main, mainEnergy + transferred);
                        ItemEnergyHelper.setEnergy(stack, currentEnergy - transferred);
                    }
                }
            }

            // Electric spark particles hovering on head
            if (player.age % 12 == 0 && world instanceof ServerWorld serverWorld) {
                serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK,
                        player.getX(), player.getY() + 1.95, player.getZ(),
                        3, 0.22, 0.1, 0.22, 0.02);
            }
        }
    }

    /**
     * Executes the crown's targeted lightning / high-voltage discharge power.
     */
    public static boolean triggerLightningAbility(ServerPlayerEntity player, ItemStack stack) {
        World world = player.getWorld();
        long energy = ItemEnergyHelper.getEnergy(stack);

        if (!player.isCreative() && energy < ABILITY_ENERGY_COST) {
            player.sendMessage(Text.literal("§c⚡ Electricians Crown Discharged! (" + energy + "/" + MAX_ENERGY + " EU)").formatted(Formatting.RED), true);
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.PLAYERS, 0.8f, 1.8f);
            return false;
        }

        HitResult hit = player.raycast(64.0, 0.0f, false);
        BlockPos targetPos;
        if (hit.getType() == HitResult.Type.BLOCK) {
            BlockHitResult bHit = (BlockHitResult) hit;
            targetPos = bHit.getBlockPos().offset(bHit.getSide());
        } else {
            Vec3d targetVec = player.getEyePos().add(player.getRotationVector().multiply(15));
            targetPos = BlockPos.ofFloored(targetVec);
        }

        if (!player.isCreative()) {
            ItemEnergyHelper.setEnergy(stack, energy - ABILITY_ENERGY_COST);
        }

        // Spawn a focused pure lightning bolt
        LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(world);
        if (lightning != null) {
            lightning.refreshPositionAfterTeleport(Vec3d.ofBottomCenter(targetPos));
            lightning.setChanneler(player);
            if (lightning instanceof NoFireLightning noFireLightning) {
                noFireLightning.evecual$setNoFire(true);
            }
            world.spawnEntity(lightning);
        }

        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.PLAYERS, 0.9f, 1.3f);

        if (world instanceof ServerWorld serverWorld) {
            serverWorld.spawnParticles(CYAN_ELECTRIC,
                    player.getX(), player.getY() + 1.8, player.getZ(),
                    15, 0.4, 0.3, 0.4, 0.08);
        }

        player.sendMessage(Text.literal("§b⚡ Crown Lightning Discharge! §7(-" + ABILITY_ENERGY_COST + " EU)").formatted(Formatting.AQUA), true);
        return true;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        long energy = ItemEnergyHelper.getEnergy(stack);
        tooltip.add(Text.literal("⚡ Royal Crown of The Electricians").formatted(Formatting.AQUA, Formatting.BOLD));
        tooltip.add(Text.literal("Forged with pure Elactorite and energized superconductor coils.").formatted(Formatting.GRAY, Formatting.ITALIC));
        tooltip.add(Text.empty());

        // Energy Bar Display
        int percentage = (int) ((energy * 100) / MAX_ENERGY);
        Formatting barColor = percentage > 50 ? Formatting.GREEN : (percentage > 20 ? Formatting.YELLOW : Formatting.RED);
        tooltip.add(Text.literal("⚡ Energy: ").formatted(Formatting.AQUA)
                .append(Text.literal(String.format("%,d / %,d EU (%d%%)", energy, MAX_ENERGY, percentage)).formatted(barColor)));

        tooltip.add(Text.empty());
        tooltip.add(Text.literal("Perks when worn on Head:").formatted(Formatting.YELLOW));
        tooltip.add(Text.literal(" ⚡ Atmospheric Grid: ").formatted(Formatting.AQUA)
                .append(Text.literal("Gathers EU automatically in rain & storms").formatted(Formatting.WHITE)));
        tooltip.add(Text.literal(" ⚡ Inductive Charger: ").formatted(Formatting.AQUA)
                .append(Text.literal("Passively powers held electric equipment").formatted(Formatting.WHITE)));
        tooltip.add(Text.literal(" ⚡ Neural Accelerator: ").formatted(Formatting.AQUA)
                .append(Text.literal("Permanent Speed II & lightning immunity").formatted(Formatting.WHITE)));
        tooltip.add(Text.literal(" ⚡ Active Ability [Key V]: ").formatted(Formatting.GOLD)
                .append(Text.literal("Calls targeted lightning bolt (-150 EU)").formatted(Formatting.WHITE)));
        super.appendTooltip(stack, world, tooltip, context);
    }
}
