package com.evecual.evecualmc.item;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.entity.FlyingTurretEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FlyingTurretItem extends Item {
    public FlyingTurretItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos().offset(context.getSide());
        PlayerEntity player = context.getPlayer();
        ItemStack stack = context.getStack();

        if (!world.isClient) {
            FlyingTurretEntity drone = new FlyingTurretEntity(EvecualMC.FLYING_TURRET_ENTITY, world);
            drone.setPosition(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            drone.setStationPos(drone.getPos());
            if (player != null) {
                drone.getTargetFilter().setOwnerUuid(player.getUuid());
                if (!player.isCreative()) {
                    stack.decrement(1);
                }
            }
            world.spawnEntity(drone);
            world.playSound(null, pos, SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE, SoundCategory.BLOCKS, 0.8F, 1.8F);
            if (player != null) {
                player.sendMessage(Text.literal("§a🚁 Defense Drone stationed! §7(Requires Defense Controller to move)"), true);
            }
        }

        return ActionResult.SUCCESS;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!world.isClient) {
            Vec3d spawnPos = user.getEyePos().add(user.getRotationVec(1.0F).multiply(1.8));
            FlyingTurretEntity drone = new FlyingTurretEntity(EvecualMC.FLYING_TURRET_ENTITY, world);
            drone.setPosition(spawnPos.x, spawnPos.y, spawnPos.z);
            drone.setStationPos(drone.getPos());
            drone.getTargetFilter().setOwnerUuid(user.getUuid());
            if (!user.isCreative()) {
                stack.decrement(1);
            }
            world.spawnEntity(drone);
            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE, SoundCategory.PLAYERS, 0.8F, 1.8F);
            user.sendMessage(Text.literal("§a🚁 Defense Drone deployed in air! §7(Requires Defense Controller to move)"), true);
        }
        return TypedActionResult.success(stack, world.isClient());
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.literal("§b⚡ Aerial Defense Turret Drone"));
        tooltip.add(Text.literal("§7Stationary air defense unit."));
        tooltip.add(Text.literal("§e⚠️ Requires Defense Controller to fly & move"));
        tooltip.add(Text.literal("§7Area Coverage: §aUp to 1024x1024 (512m radius)"));
        tooltip.add(Text.literal("§7Kinetic Cannon: §f2.0s Cooldown"));
        tooltip.add(Text.literal("§7[Right-Click on ground or air to deploy]"));
        tooltip.add(Text.literal("§7[Shift + Right-Click with empty hand to pick up]"));
        super.appendTooltip(stack, world, tooltip, context);
    }
}
