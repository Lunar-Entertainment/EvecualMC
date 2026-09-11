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
import net.minecraft.util.math.BlockPos;
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
            drone.setPosition(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);
            if (player != null) {
                drone.getTargetFilter().setOwnerUuid(player.getUuid());
                if (!player.isCreative()) {
                    stack.decrement(1);
                }
            }
            world.spawnEntity(drone);
            world.playSound(null, pos, SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE, SoundCategory.BLOCKS, 0.8F, 1.8F);
        }

        return ActionResult.SUCCESS;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.literal("§b⚡ Autonomous Aerial Defense Drone"));
        tooltip.add(Text.literal("§7Area Coverage: §aUp to 1024x1024"));
        tooltip.add(Text.literal("§7Firing Cooldown: §f2.0s"));
        tooltip.add(Text.literal("§e[Place on ground to launch drone]"));
        super.appendTooltip(stack, world, tooltip, context);
    }
}
