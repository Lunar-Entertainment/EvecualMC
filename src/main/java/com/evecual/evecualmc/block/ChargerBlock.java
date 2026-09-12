package com.evecual.evecualmc.block;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.block.entity.ChargerBlockEntity;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ChargerBlock extends BlockWithEntity {
    public ChargerBlock(Settings settings) {
        super(settings);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ChargerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, EvecualMC.CHARGER_BLOCK_ENTITY, ChargerBlockEntity::tick);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        if (!world.isClient && world instanceof ServerWorld serverWorld) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeBlockPos(pos);
            buf.writeBoolean(true); // add waypoint

            for (ServerPlayerEntity player : PlayerLookup.world(serverWorld)) {
                ServerPlayNetworking.send(player, EvecualMC.CHARGER_WAYPOINT_PACKET_ID, buf);
            }

            if (placer instanceof PlayerEntity player) {
                player.sendMessage(Text.literal("§e⚡ Vehicle Charger waypoint established at [" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + "]"), true);
            }
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            if (!world.isClient && world instanceof ServerWorld serverWorld) {
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeBlockPos(pos);
                buf.writeBoolean(false); // remove waypoint

                for (ServerPlayerEntity player : PlayerLookup.world(serverWorld)) {
                    ServerPlayNetworking.send(player, EvecualMC.CHARGER_WAYPOINT_PACKET_ID, buf);
                }
            }
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    public net.minecraft.util.ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, net.minecraft.util.Hand hand, net.minecraft.util.hit.BlockHitResult hit) {
        if (!world.isClient && player instanceof ServerPlayerEntity serverPlayer) {
            BlockEntity be = world.getBlockEntity(pos);
            int energy = be instanceof ChargerBlockEntity cbe ? (int) cbe.getEnergy() : 0;
            int max = be instanceof ChargerBlockEntity cbe ? (int) cbe.getMaxEnergy() : 1000;
            String status = "⚡ Storing " + energy + " / " + max + " EU (Connect extension above)";
            EvecualMC.sendOpenTipScreen(serverPlayer, "charger", energy, max, status);
        }
        return net.minecraft.util.ActionResult.SUCCESS;
    }
}
