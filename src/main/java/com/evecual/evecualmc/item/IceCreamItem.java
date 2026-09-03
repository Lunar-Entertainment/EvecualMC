package com.evecual.evecualmc.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

public class IceCreamItem extends Item {
    public enum Flavor {
        VANILLA,
        CHOCOLATE,
        SWEET_BERRY,
        ELECTRIC
    }

    private final Flavor flavor;

    public IceCreamItem(Flavor flavor, Settings settings) {
        super(settings.maxCount(16).food(new FoodComponent.Builder()
                .hunger(6)
                .saturationModifier(0.8F)
                .alwaysEdible()
                .build()));
        this.flavor = flavor;
    }

    public Flavor getFlavor() {
        return this.flavor;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.EAT;
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        ItemStack result = super.finishUsing(stack, world, user);

        if (!world.isClient) {
            // Cool the player down if on fire
            user.extinguish();

            switch (flavor) {
                case VANILLA -> {
                    // Clears all harmful status effects (like milk)
                    user.getStatusEffects().stream()
                            .map(StatusEffectInstance::getEffectType)
                            .filter(type -> type.getCategory() == StatusEffectCategory.HARMFUL)
                            .toList()
                            .forEach(user::removeStatusEffect);
                    user.addStatusEffect(new StatusEffectInstance(StatusEffects.SATURATION, 60, 0));
                }
                case CHOCOLATE -> {
                    user.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 120, 1));
                    user.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 600, 0));
                }
                case SWEET_BERRY -> {
                    user.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 400, 1));
                    user.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 400, 0));
                }
                case ELECTRIC -> {
                    user.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 600, 1));
                    user.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 600, 1));
                    user.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 600, 0));
                    if (world instanceof ServerWorld serverWorld) {
                        serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK, user.getX(), user.getY() + 1.0, user.getZ(), 20, 0.4, 0.4, 0.4, 0.05);
                    }
                    world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.PLAYERS, 0.3F, 2.0F);
                }
            }
        }

        if (user instanceof PlayerEntity player && !player.getAbilities().creativeMode) {
            if (stack.isEmpty()) {
                return new ItemStack(Items.BOWL);
            }
            player.getInventory().offerOrDrop(new ItemStack(Items.BOWL));
        }

        return result;
    }
}
