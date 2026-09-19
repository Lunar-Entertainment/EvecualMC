package com.evecual.evecualmc.item.crown;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;

public class MechanicalsCrownItem extends ArmorItem {
    private static final DustParticleEffect BRASS_SPARK = new DustParticleEffect(new Vector3f(0.85f, 0.65f, 0.20f), 0.8f);

    public MechanicalsCrownItem(Settings settings) {
        super(CrownArmorMaterial.MECHANICALS, Type.HELMET, settings);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);

        if (world.isClient() || !(entity instanceof PlayerEntity player)) {
            return;
        }

        // Active when equipped on the head or in the dedicated crown slot
        if (com.evecual.evecualmc.util.CrownHelper.isEquipped(player, stack)) {
            // Constant mechanical haste and speed
            if (!player.hasStatusEffect(StatusEffects.HASTE) || player.getStatusEffect(StatusEffects.HASTE).getDuration() <= 20) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 60, 1, true, false, true));
            }
            if (!player.hasStatusEffect(StatusEffects.SPEED) || player.getStatusEffect(StatusEffects.SPEED).getDuration() <= 20) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 60, 0, true, false, true));
            }

            // Every 10 seconds (200 ticks): Clockwork Auto-Repair
            if (player.age % 200 == 0) {
                ItemStack mainHand = player.getMainHandStack();
                if (!mainHand.isEmpty() && mainHand.isDamaged() && mainHand.getItem().canRepair(mainHand, mainHand)) {
                    mainHand.setDamage(Math.max(0, mainHand.getDamage() - 2));
                    world.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.PLAYERS, 0.6f, 1.8f);

                    if (world instanceof ServerWorld serverWorld) {
                        serverWorld.spawnParticles(BRASS_SPARK,
                                player.getX(), player.getY() + 1.8, player.getZ(),
                                6, 0.2, 0.15, 0.2, 0.02);
                    }
                }
            }

            // Periodic gear turning particles around head
            if (player.age % 20 == 0 && world instanceof ServerWorld serverWorld) {
                serverWorld.spawnParticles(ParticleTypes.CRIT,
                        player.getX(), player.getY() + 1.9, player.getZ(),
                        2, 0.25, 0.1, 0.25, 0.01);
            }
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.literal("⚙ Royal Crown of The Mechanicals").formatted(Formatting.GOLD, Formatting.BOLD));
        tooltip.add(Text.literal("Forged from interlocking brass cogs and clockwork springs.").formatted(Formatting.GRAY, Formatting.ITALIC));
        tooltip.add(Text.empty());
        tooltip.add(Text.literal("Perks when worn on Head:").formatted(Formatting.YELLOW));
        tooltip.add(Text.literal(" ⚙ Kinetic Momentum: ").formatted(Formatting.GOLD)
                .append(Text.literal("Permanent Haste II & Speed I").formatted(Formatting.WHITE)));
        tooltip.add(Text.literal(" ⚙ Clockwork Mending: ").formatted(Formatting.GOLD)
                .append(Text.literal("Automatically repairs held tools over time").formatted(Formatting.WHITE)));
        tooltip.add(Text.literal(" ⚙ Heavy Steampunk Armor: ").formatted(Formatting.GOLD)
                .append(Text.literal("+4 Armor, +2 Toughness").formatted(Formatting.WHITE)));
        tooltip.add(Text.empty());
        tooltip.add(Text.literal("✖ Non-Duplicable: ").formatted(Formatting.RED)
                .append(Text.literal("Protected Artifact").formatted(Formatting.GRAY)));
        tooltip.add(Text.literal("✨ Animated: ").formatted(Formatting.LIGHT_PURPLE)
                .append(Text.literal("Living Clockwork Mechanism").formatted(Formatting.GRAY)));
        super.appendTooltip(stack, world, tooltip, context);
    }
}
