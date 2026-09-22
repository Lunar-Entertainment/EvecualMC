package com.evecual.evecualmc.block;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.block.entity.WindTurbineBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class WindTurbineBlock extends BlockWithEntity {
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    public static final IntProperty SEGMENT = IntProperty.of("segment", 0, 3); // 0=Base, 1=Shaft Lower, 2=Shaft Upper, 3=Head

    public WindTurbineBlock(Settings settings) {
        super(settings);
        setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH).with(SEGMENT, 0));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, SEGMENT);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        World world = ctx.getWorld();
        BlockPos basePos = ctx.getBlockPos();

        // Check if all 3 vertical blocks above the base are clear/replaceable
        for (int h = 1; h <= 3; h++) {
            BlockPos p = basePos.up(h);
            if (!world.getBlockState(p).canReplace(ctx)) {
                return null; // Block placement if vertical space is obstructed
            }
        }
        return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite()).with(SEGMENT, 0);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        if (!world.isClient && state.get(SEGMENT) == 0) {
            Direction facing = state.get(FACING);
            // Place Shaft Lower (1), Shaft Upper (2), and Turbine Head (3)
            world.setBlockState(pos.up(1), state.with(SEGMENT, 1).with(FACING, facing), Block.NOTIFY_ALL);
            world.setBlockState(pos.up(2), state.with(SEGMENT, 2).with(FACING, facing), Block.NOTIFY_ALL);
            world.setBlockState(pos.up(3), state.with(SEGMENT, 3).with(FACING, facing), Block.NOTIFY_ALL);

            if (placer instanceof PlayerEntity player) {
                player.sendMessage(Text.literal("§a⚡ 4-Block High Wind Turbine constructed! Connect Wires to the base/shaft to draw 50 EU/t."), true);
            }
        }
    }

    public static BlockPos getBasePos(BlockPos pos, int segment) {
        return pos.down(segment);
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isClient) {
            int segment = state.get(SEGMENT);
            BlockPos basePos = getBasePos(pos, segment);

            // Clean up all 4 blocks of the turbine tower
            for (int h = 0; h <= 3; h++) {
                BlockPos p = basePos.up(h);
                BlockState s = world.getBlockState(p);
                if (s.isOf(this)) {
                    world.breakBlock(p, false, player);
                }
            }

            if (!player.isCreative()) {
                ItemEntity drop = new ItemEntity(world, basePos.getX() + 0.5, basePos.getY() + 0.5, basePos.getZ() + 0.5,
                        new ItemStack(EvecualMC.WIND_TURBINE_ITEM));
                world.spawnEntity(drop);
            }
        }
        super.onBreak(world, pos, state, player);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return state.get(SEGMENT) == 0 ? new WindTurbineBlockEntity(pos, state) : null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return state.get(SEGMENT) == 0 ?
                checkType(type, EvecualMC.WIND_TURBINE_BLOCK_ENTITY, WindTurbineBlockEntity::tick) : null;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        return ActionResult.PASS;
    }
}
