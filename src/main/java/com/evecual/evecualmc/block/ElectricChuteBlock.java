package com.evecual.evecualmc.block;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.block.entity.ElectricChuteBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.FacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ElectricChuteBlock extends BlockWithEntity {
    public static final DirectionProperty FACING = FacingBlock.FACING;
    public static final net.minecraft.state.property.BooleanProperty NORTH = net.minecraft.state.property.Properties.NORTH;
    public static final net.minecraft.state.property.BooleanProperty SOUTH = net.minecraft.state.property.Properties.SOUTH;
    public static final net.minecraft.state.property.BooleanProperty EAST = net.minecraft.state.property.Properties.EAST;
    public static final net.minecraft.state.property.BooleanProperty WEST = net.minecraft.state.property.Properties.WEST;
    public static final net.minecraft.state.property.BooleanProperty UP = net.minecraft.state.property.Properties.UP;
    public static final net.minecraft.state.property.BooleanProperty DOWN = net.minecraft.state.property.Properties.DOWN;

    protected static final VoxelShape CORE_SHAPE = Block.createCuboidShape(2.0, 2.0, 2.0, 14.0, 14.0, 14.0);
    protected static final VoxelShape NORTH_ARM = Block.createCuboidShape(2.0, 2.0, 0.0, 14.0, 14.0, 2.0);
    protected static final VoxelShape SOUTH_ARM = Block.createCuboidShape(2.0, 2.0, 14.0, 14.0, 14.0, 16.0);
    protected static final VoxelShape WEST_ARM  = Block.createCuboidShape(0.0, 2.0, 2.0, 2.0, 14.0, 14.0);
    protected static final VoxelShape EAST_ARM  = Block.createCuboidShape(14.0, 2.0, 2.0, 16.0, 14.0, 14.0);
    protected static final VoxelShape UP_ARM    = Block.createCuboidShape(2.0, 14.0, 2.0, 14.0, 16.0, 14.0);
    protected static final VoxelShape DOWN_ARM  = Block.createCuboidShape(2.0, 0.0, 2.0, 14.0, 2.0, 14.0);

    public ElectricChuteBlock(Settings settings) {
        super(settings);
        setDefaultState(this.stateManager.getDefaultState()
                .with(FACING, Direction.NORTH)
                .with(NORTH, false)
                .with(SOUTH, false)
                .with(EAST, false)
                .with(WEST, false)
                .with(UP, false)
                .with(DOWN, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, NORTH, SOUTH, EAST, WEST, UP, DOWN);
    }

    public static boolean canConnectTo(net.minecraft.world.WorldAccess world, BlockPos pos, Direction dir) {
        BlockPos targetPos = pos.offset(dir);
        BlockState targetState = world.getBlockState(targetPos);
        return targetState.isOf(EvecualMC.ELECTRIC_CHUTE_BLOCK)
                || targetState.isOf(EvecualMC.DRONE_PICKUP_BLOCK)
                || targetState.isOf(EvecualMC.STORAGE_UNIT_BLOCK);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        net.minecraft.world.WorldAccess world = ctx.getWorld();
        BlockPos pos = ctx.getBlockPos();

        boolean n = canConnectTo(world, pos, Direction.NORTH);
        boolean s = canConnectTo(world, pos, Direction.SOUTH);
        boolean e = canConnectTo(world, pos, Direction.EAST);
        boolean w = canConnectTo(world, pos, Direction.WEST);
        boolean u = canConnectTo(world, pos, Direction.UP);
        boolean d = canConnectTo(world, pos, Direction.DOWN);

        // If placed without connectable neighbors, default to straight line along clicked face
        if (!n && !s && !e && !w && !u && !d) {
            Direction facing = ctx.getSide().getOpposite();
            if (facing == Direction.NORTH || facing == Direction.SOUTH) {
                n = true; s = true;
            } else if (facing == Direction.EAST || facing == Direction.WEST) {
                e = true; w = true;
            } else {
                u = true; d = true;
            }
        }

        return this.getDefaultState()
                .with(FACING, ctx.getHorizontalPlayerFacing())
                .with(NORTH, n).with(SOUTH, s)
                .with(EAST, e).with(WEST, w)
                .with(UP, u).with(DOWN, d);
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, net.minecraft.world.WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        net.minecraft.state.property.BooleanProperty prop = switch (direction) {
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case EAST -> EAST;
            case WEST -> WEST;
            case UP -> UP;
            case DOWN -> DOWN;
        };
        boolean connects = canConnectTo(world, pos, direction);
        return state.with(prop, connects);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        VoxelShape shape = CORE_SHAPE;
        if (state.get(NORTH)) shape = net.minecraft.util.shape.VoxelShapes.union(shape, NORTH_ARM);
        if (state.get(SOUTH)) shape = net.minecraft.util.shape.VoxelShapes.union(shape, SOUTH_ARM);
        if (state.get(WEST))  shape = net.minecraft.util.shape.VoxelShapes.union(shape, WEST_ARM);
        if (state.get(EAST))  shape = net.minecraft.util.shape.VoxelShapes.union(shape, EAST_ARM);
        if (state.get(UP))    shape = net.minecraft.util.shape.VoxelShapes.union(shape, UP_ARM);
        if (state.get(DOWN))  shape = net.minecraft.util.shape.VoxelShapes.union(shape, DOWN_ARM);
        return shape;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ElectricChuteBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, EvecualMC.ELECTRIC_CHUTE_BLOCK_ENTITY, ElectricChuteBlockEntity::tick);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        return ActionResult.PASS;
    }
}
