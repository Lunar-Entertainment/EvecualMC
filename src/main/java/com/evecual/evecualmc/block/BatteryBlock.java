package com.evecual.evecualmc.block;

import com.evecual.evecualmc.block.entity.BatteryBlockEntity;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class BatteryBlock extends BlockWithEntity {
    public BatteryBlock(Settings settings) {
        super(settings);
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
    public <T extends BlockEntity> net.minecraft.block.entity.BlockEntityTicker<T> getTicker(net.minecraft.world.World world, BlockState state, net.minecraft.block.entity.BlockEntityType<T> type) {
        return checkType(type, com.evecual.evecualmc.EvecualMC.BATTERY_BLOCK_ENTITY, BatteryBlockEntity::tick);
    }
}
