package com.evecual.evecualmc.item;

import com.evecual.evecualmc.util.CrownHolder;
import com.evecual.evecualmc.util.NoFireLightning;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LightningItem extends Item {
    public LightningItem(Settings settings) {
        super(settings);
    }

    private boolean isCrownEnhanced(Entity user) {
        return (user instanceof CrownHolder holder) && holder.evecual$hasMechanicalCrown();
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        boolean enhanced = isCrownEnhanced(user);

        // Sneak + Right Click: Toggle between Fire and No-Fire mode
        if (user.isSneaking()) {
            boolean currentNoFire = itemStack.hasNbt() && itemStack.getNbt().getBoolean("NoFire");
            boolean newNoFire = !currentNoFire;
            itemStack.getOrCreateNbt().putBoolean("NoFire", newNoFire);

            if (!world.isClient()) {
                if (newNoFire) {
                    if (enhanced) {
                        user.sendMessage(Text.literal("§e⚡ Lightning Mode: §b⚡ Overclocked Lightning §7(Pure Soul Damage, No Fire)"), true);
                    } else {
                        user.sendMessage(Text.literal("§e⚡ Lightning Mode: §b⚡ Pure Lightning §7(Damage Only, No Fire)"), true);
                    }
                    world.playSound(null, user.getX(), user.getY(), user.getZ(),
                            SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), SoundCategory.PLAYERS, 0.9F, 1.8F);
                } else {
                    if (enhanced) {
                        user.sendMessage(Text.literal("§e⚡ Lightning Mode: §b🔥 Soul Fire §7(Ignites Soul Fire & Massive Damage)"), true);
                    } else {
                        user.sendMessage(Text.literal("§e⚡ Lightning Mode: §c🔥 Fire §7(Ignites blocks & Deals Damage)"), true);
                    }
                    world.playSound(null, user.getX(), user.getY(), user.getZ(),
                            SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), SoundCategory.PLAYERS, 0.9F, 1.1F);
                }
            }
            return TypedActionResult.success(itemStack, world.isClient());
        }

        // Raycast up to 100 blocks to detect the targeted block
        HitResult hitResult = user.raycast(100.0, 0.0f, false);

        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult) hitResult;
            BlockPos targetPos = blockHit.getBlockPos().offset(blockHit.getSide());
            boolean noFire = itemStack.hasNbt() && itemStack.getNbt().getBoolean("NoFire");

            if (!world.isClient()) {
                // Durability: 1 per use if enhanced (150 total uses), 6 per use if normal (25 total uses)
                int durabilityCost = enhanced ? 1 : Math.min(6, itemStack.getMaxDamage() - itemStack.getDamage());
                itemStack.damage(durabilityCost, user, p -> p.sendToolBreakStatus(hand));

                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        BlockPos strikePos = targetPos.add(dx, 0, dz);
                        for (int strike = 0; strike < 3; strike++) {
                            LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(world);
                            if (lightning != null) {
                                lightning.refreshPositionAfterTeleport(Vec3d.ofBottomCenter(strikePos));
                                if (user instanceof ServerPlayerEntity serverPlayer) {
                                    lightning.setChanneler(serverPlayer);
                                }
                                if (lightning instanceof NoFireLightning noFireLightning) {
                                    if (noFire) {
                                        noFireLightning.evecual$setNoFire(true);
                                    } else if (enhanced) {
                                        noFireLightning.evecual$setSoulFire(true);
                                    }
                                    if (enhanced) {
                                        noFireLightning.evecual$setBonusDamage(15.0F);
                                    }
                                }
                                world.spawnEntity(lightning);
                            }
                        }
                    }
                }

                // Enhanced Crown Shockwave: massive AoE soul damage and particles
                if (enhanced && world instanceof ServerWorld serverWorld) {
                    Box aoeBox = new Box(targetPos).expand(8.0, 4.0, 8.0);
                    List<LivingEntity> targets = world.getEntitiesByClass(LivingEntity.class, aoeBox, e -> e.isAlive() && e != user);
                    for (LivingEntity target : targets) {
                        target.damage(serverWorld.getDamageSources().indirectMagic(user, user), 20.0F);
                        if (!noFire) {
                            target.setOnFireFor(8);
                        }
                    }

                    // Soul burst visuals & sound
                    serverWorld.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME,
                            targetPos.getX() + 0.5, targetPos.getY() + 1.0, targetPos.getZ() + 0.5,
                            40, 3.0, 1.5, 3.0, 0.08);
                    serverWorld.spawnParticles(ParticleTypes.SOUL,
                            targetPos.getX() + 0.5, targetPos.getY() + 1.5, targetPos.getZ() + 0.5,
                            25, 2.5, 2.0, 2.5, 0.05);
                    serverWorld.spawnParticles(ParticleTypes.FLASH,
                            targetPos.getX() + 0.5, targetPos.getY() + 1.0, targetPos.getZ() + 0.5,
                            1, 0, 0, 0, 0);

                    world.playSound(null, targetPos, SoundEvents.PARTICLE_SOUL_ESCAPE, SoundCategory.PLAYERS, 2.0F, 0.9F);
                    world.playSound(null, targetPos, SoundEvents.ENTITY_WARDEN_SONIC_BOOM, SoundCategory.PLAYERS, 1.2F, 1.4F);
                }
            }

            int cooldown = enhanced ? 50 : 80;
            user.getItemCooldownManager().set(this, cooldown);
            return TypedActionResult.success(itemStack, world.isClient());
        }

        return TypedActionResult.pass(itemStack);
    }

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        return stack.isDamaged();
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        Entity holder = stack.getHolder();
        if (holder != null && isCrownEnhanced(holder)) {
            return Math.round(13.0F - (float) stack.getDamage() * 13.0F / (float) stack.getMaxDamage());
        } else {
            float remainingUses = Math.max(0, (stack.getMaxDamage() - stack.getDamage()) / 6.0F);
            return Math.round(remainingUses * 13.0F / 25.0F);
        }
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        Entity holder = stack.getHolder();
        if (holder != null && isCrownEnhanced(holder)) {
            return 0x00E5FF; // Radiant cyan/electric soul color
        }
        float f = Math.max(0.0F, ((float) stack.getMaxDamage() - (float) stack.getDamage()) / (float) stack.getMaxDamage());
        return MathHelper.hsvToRgb(f / 3.0F, 1.0F, 1.0F);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        boolean noFire = stack.hasNbt() && stack.getNbt().getBoolean("NoFire");
        boolean enhanced = false;
        if (stack.getHolder() != null) {
            enhanced = isCrownEnhanced(stack.getHolder());
        } else if (net.minecraft.client.MinecraftClient.getInstance().player != null) {
            enhanced = isCrownEnhanced(net.minecraft.client.MinecraftClient.getInstance().player);
        }

        if (enhanced) {
            tooltip.add(Text.literal("§6👑 Crown Enhanced: §b⚡ Overclocked Soul Lightning"));
            tooltip.add(Text.literal("§c⚔ Damage: §fMassive §b(+Soul Shockwave)"));
            tooltip.add(Text.literal("§e⚡ Cooldown: §f2.5s"));
            int remainingUses = stack.getMaxDamage() - stack.getDamage();
            tooltip.add(Text.literal("§7Durability: §b" + remainingUses + " §7/ §3150 §7uses"));
            if (noFire) {
                tooltip.add(Text.literal("§7Mode: §b⚡ Overclocked Lightning (No Fire)"));
            } else {
                tooltip.add(Text.literal("§7Mode: §b🔥 Soul Fire (Ignites Soul Fire & Damage)"));
            }
        } else {
            tooltip.add(Text.literal("§e⚡ Cooldown: §f4.0s"));
            int remainingUses = (stack.getMaxDamage() - stack.getDamage()) / 6;
            tooltip.add(Text.literal("§7Durability: §a" + remainingUses + " §7/ §225 §7uses"));
            if (noFire) {
                tooltip.add(Text.literal("§7Mode: §b⚡ Pure Lightning (No Fire, Damage Only)"));
            } else {
                tooltip.add(Text.literal("§7Mode: §c🔥 Fire (Ignites blocks & Damage)"));
            }
            tooltip.add(Text.literal("§8[Equip Mechanical Crown for Soul Fire & 150 Uses!]"));
        }

        tooltip.add(Text.literal("§8[Sneak + Right-Click to toggle mode]"));
        super.appendTooltip(stack, world, tooltip, context);
    }
}
