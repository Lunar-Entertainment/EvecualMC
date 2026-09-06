package com.evecual.evecualmc.block;

import com.evecual.evecualmc.block.entity.BatteryBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public class BatteryBlock extends BlockWithEntity {
    public static final IntProperty CHARGE_LEVEL = IntProperty.of("charge_level", 0, 8);
    public static final BooleanProperty NORTH = Properties.NORTH;
    public static final BooleanProperty SOUTH = Properties.SOUTH;
    public static final BooleanProperty EAST = Properties.EAST;
    public static final BooleanProperty WEST = Properties.WEST;
    public static final BooleanProperty UP = Properties.UP;
    public static final BooleanProperty DOWN = Properties.DOWN;

    public BatteryBlock(Settings settings) {
        super(settings);
        setDefaultState(this.stateManager.getDefaultState()
                .with(CHARGE_LEVEL, 0)
                .with(NORTH, false)
                .with(SOUTH, false)
                .with(EAST, false)
                .with(WEST, false)
                .with(UP, false)
                .with(DOWN, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(CHARGE_LEVEL, NORTH, SOUTH, EAST, WEST, UP, DOWN);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        World world = ctx.getWorld();
        BlockPos pos = ctx.getBlockPos();
        return this.getDefaultState()
                .with(NORTH, isBattery(world, pos.north()))
                .with(SOUTH, isBattery(world, pos.south()))
                .with(EAST, isBattery(world, pos.east()))
                .with(WEST, isBattery(world, pos.west()))
                .with(UP, isBattery(world, pos.up()))
                .with(DOWN, isBattery(world, pos.down()));
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        boolean isConnected = neighborState.getBlock() instanceof BatteryBlock;
        return switch (direction) {
            case NORTH -> state.with(NORTH, isConnected);
            case SOUTH -> state.with(SOUTH, isConnected);
            case EAST -> state.with(EAST, isConnected);
            case WEST -> state.with(WEST, isConnected);
            case UP -> state.with(UP, isConnected);
            case DOWN -> state.with(DOWN, isConnected);
        };
    }

    private boolean isBattery(World world, BlockPos pos) {
        return world.getBlockState(pos).getBlock() instanceof BatteryBlock;
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
            if (be instanceof BatteryBlockEntity bbe) {
                BatteryBlockEntity.BatteryCluster cluster = bbe.getCluster();
                int energy = (int) cluster.totalEnergy();
                int max = (int) cluster.maxCapacity();
                int count = cluster.size();
                String status = count > 1
                        ? "🔋 Seamless Multi-Block Battery Cluster: " + energy + " / " + max + " EU (" + count + " Blocks)"
                        : "🔋 Storing " + energy + " / " + max + " EU (" + (int) ((energy / (double) max) * 100) + "%)";
                com.evecual.evecualmc.EvecualMC.sendOpenTipScreen(serverPlayer, "battery", energy, max, status);
            }
        }
        return net.minecraft.util.ActionResult.SUCCESS;
    }
}
