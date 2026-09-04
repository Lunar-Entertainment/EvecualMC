package com.evecual.evecualmc.block;

import com.evecual.evecualmc.block.entity.BatteryBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class BatteryBlock extends BlockWithEntity {
    public static final IntProperty CHARGE_LEVEL = IntProperty.of("charge_level", 0, 8);

    public BatteryBlock(Settings settings) {
        super(settings);
        setDefaultState(this.stateManager.getDefaultState().with(CHARGE_LEVEL, 0));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(CHARGE_LEVEL);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new BatteryBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, com.evecual.evecualmc.EvecualMC.BATTERY_BLOCK_ENTITY, BatteryBlockEntity::tick);
    }

    @Override
    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return (int) Math.round((double) state.get(CHARGE_LEVEL) * 15.0 / 8.0);
    }

    @Override
    public net.minecraft.util.ActionResult onUse(BlockState state, World world, BlockPos pos, net.minecraft.entity.player.PlayerEntity player, net.minecraft.util.Hand hand, net.minecraft.util.hit.BlockHitResult hit) {
        if (!world.isClient && player instanceof net.minecraft.server.network.ServerPlayerEntity serverPlayer) {
            BlockEntity be = world.getBlockEntity(pos);
            int energy = be instanceof BatteryBlockEntity bbe ? (int) bbe.getEnergy() : 0;
            int max = be instanceof BatteryBlockEntity bbe ? (int) bbe.getMaxEnergy() : 600;
            String status = "🔋 Storing " + energy + " / " + max + " EU (" + (int)((energy / (double)max) * 100) + "%)";
            com.evecual.evecualmc.EvecualMC.sendOpenTipScreen(serverPlayer, "battery", energy, max, status);
        }
        return net.minecraft.util.ActionResult.SUCCESS;
    }
}
