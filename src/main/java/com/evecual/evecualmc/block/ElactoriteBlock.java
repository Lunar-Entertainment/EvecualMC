package com.evecual.evecualmc.block;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.item.RailgunItem;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ElactoriteBlock extends Block {
    public ElactoriteBlock(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack stack = player.getStackInHand(hand);
        if (stack.isOf(EvecualMC.RAILGUN_ITEM) && RailgunItem.isIgniteMode(stack)) {
            if (!world.isClient() && player instanceof ServerPlayerEntity serverPlayer) {
                RailgunItem.teleportToEvecualDimension(serverPlayer, pos);
            }
            return ActionResult.success(world.isClient());
        }
        return ActionResult.PASS;
    }
}
