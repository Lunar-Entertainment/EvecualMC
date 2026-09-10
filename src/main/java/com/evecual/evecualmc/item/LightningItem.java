package com.evecual.evecualmc.item;

import com.evecual.evecualmc.util.NoFireLightning;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LightningItem extends Item {
    public LightningItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);

        // Sneak + Right Click: Toggle between Fire and No-Fire (Damage Only) mode
        if (user.isSneaking()) {
            boolean currentNoFire = itemStack.hasNbt() && itemStack.getNbt().getBoolean("NoFire");
            boolean newNoFire = !currentNoFire;
            itemStack.getOrCreateNbt().putBoolean("NoFire", newNoFire);

            if (!world.isClient()) {
                if (newNoFire) {
                    user.sendMessage(Text.literal("§e⚡ Lightning Mode: §b⚡ Pure Lightning §7(Damage Only, No Fire)"), true);
                    world.playSound(null, user.getX(), user.getY(), user.getZ(),
                            SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), SoundCategory.PLAYERS, 0.9F, 1.8F);
                } else {
                    user.sendMessage(Text.literal("§e⚡ Lightning Mode: §c🔥 Fire §7(Ignites blocks & Deals Damage)"), true);
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
                                if (noFire && lightning instanceof NoFireLightning noFireLightning) {
                                    noFireLightning.evecual$setNoFire(true);
                                }
                                world.spawnEntity(lightning);
                            }
                        }
                    }
                }
            }

            user.getItemCooldownManager().set(this, 10);
            return TypedActionResult.success(itemStack, world.isClient());
        }

        return TypedActionResult.pass(itemStack);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        boolean noFire = stack.hasNbt() && stack.getNbt().getBoolean("NoFire");
        if (noFire) {
            tooltip.add(Text.literal("§7Mode: §b⚡ Pure Lightning (No Fire, Damage Only)"));
        } else {
            tooltip.add(Text.literal("§7Mode: §c🔥 Fire (Ignites blocks & Damage)"));
        }
        tooltip.add(Text.literal("§8[Sneak + Right-Click to toggle mode]"));
        super.appendTooltip(stack, world, tooltip, context);
    }
}
