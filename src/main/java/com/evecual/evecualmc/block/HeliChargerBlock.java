package com.evecual.evecualmc.block;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.block.entity.HeliChargerBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
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
import net.minecraft.state.property.EnumProperty;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class HeliChargerBlock extends BlockWithEntity {
    public static final EnumProperty<HeliChargerPart> PART = EnumProperty.of("part", HeliChargerPart.class);
    protected static final VoxelShape SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 1.0, 16.0);

    public HeliChargerBlock(Settings settings) {
        super(settings);
        setDefaultState(this.stateManager.getDefaultState().with(PART, HeliChargerPart.CENTER));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(PART);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        World world = ctx.getWorld();
        BlockPos centerPos = ctx.getBlockPos();

        // Check if all 3x3 positions are replaceable
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos p = centerPos.add(dx, 0, dz);
                if (p.equals(centerPos)) continue;
                if (!world.getBlockState(p).canReplace(ctx)) {
                    return null; // Cannot place if area is blocked
                }
            }
        }

        return this.getDefaultState().with(PART, HeliChargerPart.CENTER);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return VoxelShapes.empty(); // Seamless landing for helicopters
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return state.get(PART) == HeliChargerPart.CENTER ? new HeliChargerBlockEntity(pos, state) : null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return state.get(PART) == HeliChargerPart.CENTER ?
                checkType(type, EvecualMC.HELI_CHARGER_BLOCK_ENTITY, HeliChargerBlockEntity::tick) : null;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        if (!world.isClient) {
            // Set up the full 3x3 Helipad Grid
            world.setBlockState(pos.add(0, 0, -1), state.with(PART, HeliChargerPart.NORTH), Block.NOTIFY_ALL);
            world.setBlockState(pos.add(0, 0, 1), state.with(PART, HeliChargerPart.SOUTH), Block.NOTIFY_ALL);
            world.setBlockState(pos.add(-1, 0, 0), state.with(PART, HeliChargerPart.WEST), Block.NOTIFY_ALL);
            world.setBlockState(pos.add(1, 0, 0), state.with(PART, HeliChargerPart.EAST), Block.NOTIFY_ALL);

            world.setBlockState(pos.add(-1, 0, -1), state.with(PART, HeliChargerPart.NORTH_WEST), Block.NOTIFY_ALL);
            world.setBlockState(pos.add(1, 0, -1), state.with(PART, HeliChargerPart.NORTH_EAST), Block.NOTIFY_ALL);
            world.setBlockState(pos.add(-1, 0, 1), state.with(PART, HeliChargerPart.SOUTH_WEST), Block.NOTIFY_ALL);
            world.setBlockState(pos.add(1, 0, 1), state.with(PART, HeliChargerPart.SOUTH_EAST), Block.NOTIFY_ALL);

            if (placer instanceof PlayerEntity player) {
                BlockState below = world.getBlockState(pos.down());
                if (!below.isOf(EvecualMC.CHARGER_BLOCK)) {
                    player.sendMessage(Text.literal("§6⚡ Note: Place the center of the Helipad on top of a Vehicle Charger Base to power it!"), false);
                } else {
                    player.sendMessage(Text.literal("§a⚡ 3x3 Helipad installed! Land your EV Heli on this pad to charge."), true);
                }
            }
        }
    }

    public static BlockPos getCenterPos(BlockPos pos, HeliChargerPart part) {
        return switch (part) {
            case CENTER -> pos;
            case NORTH -> pos.add(0, 0, 1);
            case SOUTH -> pos.add(0, 0, -1);
            case WEST -> pos.add(1, 0, 0);
            case EAST -> pos.add(-1, 0, 0);
            case NORTH_WEST -> pos.add(1, 0, 1);
            case NORTH_EAST -> pos.add(-1, 0, 1);
            case SOUTH_WEST -> pos.add(1, 0, -1);
            case SOUTH_EAST -> pos.add(-1, 0, -1);
        };
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isClient) {
            HeliChargerPart part = state.get(PART);
            BlockPos center = getCenterPos(pos, part);

            // Clean up all 9 tiles of the helipad
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos p = center.add(dx, 0, dz);
                    BlockState s = world.getBlockState(p);
                    if (s.isOf(this)) {
                        world.breakBlock(p, false, player);
                    }
                }
            }

            if (!player.isCreative()) {
                ItemEntity drop = new ItemEntity(world, center.getX() + 0.5, center.getY() + 0.5, center.getZ() + 0.5,
                        new ItemStack(EvecualMC.HELI_CHARGER_ITEM));
                world.spawnEntity(drop);
            }
        }
        super.onBreak(world, pos, state, player);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient && player instanceof ServerPlayerEntity serverPlayer) {
            HeliChargerPart part = state.get(PART);
            BlockPos center = getCenterPos(pos, part);
            BlockEntity be = world.getBlockEntity(center);
            int energy = be instanceof HeliChargerBlockEntity hcbe ? hcbe.getStoredEnergy() : 0;
            int max = be instanceof HeliChargerBlockEntity hcbe ? hcbe.getMaxEnergy() : 2000;
            String status = "🚁 3x3 Helipad Ready: Land EV Heli to recharge";
            EvecualMC.sendOpenTipScreen(serverPlayer, "heli_charger", energy, max, status);
        }
        return ActionResult.SUCCESS;
    }
}
