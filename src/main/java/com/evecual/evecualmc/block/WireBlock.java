package com.evecual.evecualmc.block;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.block.entity.WireBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public class WireBlock extends BlockWithEntity {
    public static final BooleanProperty NORTH = Properties.NORTH;
    public static final BooleanProperty SOUTH = Properties.SOUTH;
    public static final BooleanProperty EAST = Properties.EAST;
    public static final BooleanProperty WEST = Properties.WEST;
    public static final BooleanProperty UP = Properties.UP;
    public static final BooleanProperty DOWN = Properties.DOWN;

    private static final VoxelShape CORE = Block.createCuboidShape(6.0, 6.0, 6.0, 10.0, 10.0, 10.0);
    private static final VoxelShape UP_SHAPE = Block.createCuboidShape(6.0, 10.0, 6.0, 10.0, 16.0, 10.0);
    private static final VoxelShape DOWN_SHAPE = Block.createCuboidShape(6.0, 0.0, 6.0, 10.0, 6.0, 10.0);
    private static final VoxelShape NORTH_SHAPE = Block.createCuboidShape(6.0, 6.0, 0.0, 10.0, 10.0, 6.0);
    private static final VoxelShape SOUTH_SHAPE = Block.createCuboidShape(6.0, 6.0, 10.0, 10.0, 10.0, 16.0);
    private static final VoxelShape WEST_SHAPE = Block.createCuboidShape(0.0, 6.0, 6.0, 6.0, 10.0, 10.0);
    private static final VoxelShape EAST_SHAPE = Block.createCuboidShape(10.0, 6.0, 6.0, 16.0, 10.0, 10.0);

    public WireBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState()
                .with(NORTH, false)
                .with(SOUTH, false)
                .with(EAST, false)
                .with(WEST, false)
                .with(UP, false)
                .with(DOWN, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(NORTH, SOUTH, EAST, WEST, UP, DOWN);
    }

    private boolean canConnectTo(BlockState state) {
        return state.isOf(this)
                || state.isOf(EvecualMC.SOLAR_PANEL_BLOCK)
                || state.isOf(EvecualMC.WIND_TURBINE_BLOCK)
                || state.isOf(EvecualMC.BATTERY_BLOCK)
                || state.isOf(EvecualMC.ELECTRONIC_COMBINER_BLOCK)
                || state.isOf(EvecualMC.ELECTRONIC_DUPER_BLOCK)
                || state.isOf(EvecualMC.CHARGER_BLOCK)
                || state.isOf(EvecualMC.RC_CHARGER_BLOCK);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockView world = ctx.getWorld();
        BlockPos pos = ctx.getBlockPos();

        return getDefaultState()
                .with(NORTH, canConnectTo(world.getBlockState(pos.north())))
                .with(SOUTH, canConnectTo(world.getBlockState(pos.south())))
                .with(EAST, canConnectTo(world.getBlockState(pos.east())))
                .with(WEST, canConnectTo(world.getBlockState(pos.west())))
                .with(UP, canConnectTo(world.getBlockState(pos.up())))
                .with(DOWN, canConnectTo(world.getBlockState(pos.down())));
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        BooleanProperty property = getPropertyForDirection(direction);
        return state.with(property, canConnectTo(neighborState));
    }

    private BooleanProperty getPropertyForDirection(Direction direction) {
        return switch (direction) {
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case EAST -> EAST;
            case WEST -> WEST;
            case UP -> UP;
            case DOWN -> DOWN;
        };
    }

    @Override
    public float getAmbientOcclusionLightLevel(BlockState state, BlockView world, BlockPos pos) {
        return 1.0F; // Fulllight ambient occlusion: prevents shaders from casting pitch black shadows on wires
    }

    @Override
    public boolean isTransparent(BlockState state, BlockView world, BlockPos pos) {
        return true;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new WireBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, EvecualMC.WIRE_BLOCK_ENTITY, WireBlockEntity::tick);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        VoxelShape shape = CORE;
        if (state.get(UP)) shape = VoxelShapes.union(shape, UP_SHAPE);
        if (state.get(DOWN)) shape = VoxelShapes.union(shape, DOWN_SHAPE);
        if (state.get(NORTH)) shape = VoxelShapes.union(shape, NORTH_SHAPE);
        if (state.get(SOUTH)) shape = VoxelShapes.union(shape, SOUTH_SHAPE);
        if (state.get(WEST)) shape = VoxelShapes.union(shape, WEST_SHAPE);
        if (state.get(EAST)) shape = VoxelShapes.union(shape, EAST_SHAPE);
        return shape;
    }

    @Override
    public net.minecraft.util.ActionResult onUse(BlockState state, World world, BlockPos pos, net.minecraft.entity.player.PlayerEntity player, net.minecraft.util.Hand hand, net.minecraft.util.hit.BlockHitResult hit) {
        if (!world.isClient && player instanceof net.minecraft.server.network.ServerPlayerEntity serverPlayer) {
            BlockEntity be = world.getBlockEntity(pos);
            int rate = be instanceof WireBlockEntity wbe ? wbe.getTransferRate() : 0;
            String status = rate > 0 ? "⚡ Transferring: " + rate + " EU/t" : "🔌 Standby (0 EU/t throughput)";
            EvecualMC.sendOpenTipScreen(serverPlayer, "wire", rate, 100, status);
        }
        return net.minecraft.util.ActionResult.SUCCESS;
    }
}
