package com.evecual.evecualmc.block;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.block.entity.AutoPickupBlockEntity;
import com.evecual.evecualmc.item.RcControllerItem;
import com.evecual.evecualmc.item.StationaryRcControllerItem;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class AutoPickupBlock extends BlockWithEntity {
    public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;
    public static final BooleanProperty ACTIVE = BooleanProperty.of("active");

    protected static final VoxelShape SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 16.0, 16.0);

    public AutoPickupBlock(Settings settings) {
        super(settings);
        setDefaultState(this.stateManager.getDefaultState()
                .with(FACING, Direction.NORTH)
                .with(ACTIVE, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, ACTIVE);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState()
                .with(FACING, ctx.getHorizontalPlayerFacing().getOpposite())
                .with(ACTIVE, false);
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
        return new AutoPickupBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, EvecualMC.AUTO_PICKUP_BLOCK_ENTITY, AutoPickupBlockEntity::tick);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient) {
            ItemStack held = player.getStackInHand(hand);
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof AutoPickupBlockEntity autoPickup) {
                // 1. Link from RC Controller
                if (held.getItem() instanceof RcControllerItem) {
                    UUID droneUuid = RcControllerItem.getPairedDroneUuid(held);
                    if (droneUuid != null) {
                        String name = held.hasNbt() && held.getNbt().contains("VehicleName")
                                ? held.getNbt().getString("VehicleName") : "Pickup Drone";
                        autoPickup.setLinkedDrone(droneUuid, name);
                        player.sendMessage(Text.literal("§a📡 Auto Pickup Station linked to " + name + "! Autonomous item detection active."), true);
                        world.playSound(null, pos, SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.BLOCKS, 0.8F, 1.8F);
                        return ActionResult.SUCCESS;
                    } else {
                        player.sendMessage(Text.literal("§c⚠️ RC Controller has no paired drone! Pair it to a Pickup Drone first."), true);
                        return ActionResult.SUCCESS;
                    }
                }
                // 2. Link from Stationary RC Controller
                else if (held.getItem() instanceof StationaryRcControllerItem) {
                    UUID droneUuid = StationaryRcControllerItem.getPairedDroneUuid(held);
                    if (droneUuid != null) {
                        autoPickup.setLinkedDrone(droneUuid, "Pickup Drone");
                        player.sendMessage(Text.literal("§a📡 Auto Pickup Station linked to Terminal's Pickup Drone!"), true);
                        world.playSound(null, pos, SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.BLOCKS, 0.8F, 1.8F);
                        return ActionResult.SUCCESS;
                    }
                }

                // 3. Inspect telemetry
                if (player instanceof ServerPlayerEntity serverPlayer) {
                    String status = autoPickup.getStatusMessage();
                    EvecualMC.sendOpenTipScreen(serverPlayer, "auto_pickup", autoPickup.isLinked() ? 100 : 0, 100, status);
                }
            }
        }
        return ActionResult.SUCCESS;
    }
}
