package com.evecual.evecualmc.block;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.block.entity.SolarPanelBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class SolarPanelBlock extends BlockWithEntity {
    protected static final VoxelShape SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 6.0, 16.0);

    public SolarPanelBlock(Settings settings) {
        super(settings);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new SolarPanelBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, EvecualMC.SOLAR_PANEL_BLOCK_ENTITY, SolarPanelBlockEntity::tick);
    }

    @Override
    public net.minecraft.util.ActionResult onUse(BlockState state, World world, BlockPos pos, net.minecraft.entity.player.PlayerEntity player, net.minecraft.util.Hand hand, net.minecraft.util.hit.BlockHitResult hit) {
        if (!world.isClient && player instanceof net.minecraft.server.network.ServerPlayerEntity serverPlayer) {
            BlockEntity be = world.getBlockEntity(pos);
            int energy = be instanceof SolarPanelBlockEntity sp ? (int) sp.getEnergy() : 0;
            int max = be instanceof SolarPanelBlockEntity sp ? (int) sp.getMaxEnergy() : 1000;
            String status = (be instanceof SolarPanelBlockEntity sp && sp.getGenerationRate() > 0)
                    ? "⚡ Generating " + sp.getGenerationRate() + " EU/t (Daylight)"
                    : "🌙 Standby (No direct sunlight)";
            EvecualMC.sendOpenTipScreen(serverPlayer, "solar_panel", energy, max, status);
        }
        return net.minecraft.util.ActionResult.SUCCESS;
    }
}
