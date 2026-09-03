package com.evecual.evecualmc.block;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.block.entity.ParkingLinesBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ParkingLinesBlock extends BlockWithEntity {
    public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;
    public static final EnumProperty<ParkingLinesPart> PART = EnumProperty.of("part", ParkingLinesPart.class);
    protected static final VoxelShape SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 1.0, 16.0);

    public ParkingLinesBlock(Settings settings) {
        super(settings);
        setDefaultState(this.stateManager.getDefaultState()
                .with(FACING, Direction.NORTH)
                .with(PART, ParkingLinesPart.FRONT_LEFT));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return VoxelShapes.empty(); // Vehicles and players can walk/drive seamlessly over painted lines
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        Direction facing = ctx.getHorizontalPlayerFacing();
        Direction right = facing.rotateYClockwise();
        World world = ctx.getWorld();
        BlockPos pos = ctx.getBlockPos();

        // Check if all 6 positions (3 long, 2 wide) are replaceable
        BlockPos[][] grid = getBayPositions(pos, facing, right);
        for (BlockPos[] row : grid) {
            for (BlockPos p : row) {
                if (!world.getBlockState(p).canReplace(ctx)) {
                    return null;
                }
            }
        }

        return this.getDefaultState().with(FACING, facing).with(PART, ParkingLinesPart.FRONT_LEFT);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        if (!world.isClient) {
            Direction facing = state.get(FACING);
            Direction right = facing.rotateYClockwise();
            BlockPos[][] grid = getBayPositions(pos, facing, right);

            // Row 0: Front
            world.setBlockState(grid[0][1], state.with(PART, ParkingLinesPart.FRONT_RIGHT), Block.NOTIFY_ALL);
            // Row 1: Mid
            world.setBlockState(grid[1][0], state.with(PART, ParkingLinesPart.MID_LEFT), Block.NOTIFY_ALL);
            world.setBlockState(grid[1][1], state.with(PART, ParkingLinesPart.MID_RIGHT), Block.NOTIFY_ALL);
            // Row 2: Back
            world.setBlockState(grid[2][0], state.with(PART, ParkingLinesPart.BACK_LEFT), Block.NOTIFY_ALL);
            world.setBlockState(grid[2][1], state.with(PART, ParkingLinesPart.BACK_RIGHT), Block.NOTIFY_ALL);
        }
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isClient) {
            Direction facing = state.get(FACING);
            Direction right = facing.rotateYClockwise();
            ParkingLinesPart part = state.get(PART);

            BlockPos origin = getOriginPos(pos, facing, right, part);
            BlockPos[][] grid = getBayPositions(origin, facing, right);

            for (BlockPos[] row : grid) {
                for (BlockPos p : row) {
                    if (!p.equals(pos) && world.getBlockState(p).isOf(this)) {
                        world.breakBlock(p, false);
                    }
                }
            }
        }
        super.onBreak(world, pos, state, player);
    }

    public static BlockPos[][] getBayPositions(BlockPos origin, Direction facing, Direction right) {
        return new BlockPos[][]{
                {origin, origin.offset(right)},
                {origin.offset(facing), origin.offset(facing).offset(right)},
                {origin.offset(facing, 2), origin.offset(facing, 2).offset(right)}
        };
    }

    public static BlockPos getOriginPos(BlockPos pos, Direction facing, Direction right, ParkingLinesPart part) {
        return switch (part) {
            case FRONT_LEFT -> pos;
            case FRONT_RIGHT -> pos.offset(right.getOpposite());
            case MID_LEFT -> pos.offset(facing.getOpposite());
            case MID_RIGHT -> pos.offset(facing.getOpposite()).offset(right.getOpposite());
            case BACK_LEFT -> pos.offset(facing.getOpposite(), 2);
            case BACK_RIGHT -> pos.offset(facing.getOpposite(), 2).offset(right.getOpposite());
        };
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        if (state.get(PART) == ParkingLinesPart.FRONT_LEFT) {
            return new ParkingLinesBlockEntity(pos, state);
        }
        return null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (state.get(PART) == ParkingLinesPart.FRONT_LEFT) {
            return checkType(type, EvecualMC.PARKING_LINES_BLOCK_ENTITY, ParkingLinesBlockEntity::tick);
        }
        return null;
    }
}
