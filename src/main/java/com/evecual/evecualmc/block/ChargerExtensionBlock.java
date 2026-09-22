package com.evecual.evecualmc.block;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.block.entity.ChargerExtensionBlockEntity;
import com.evecual.evecualmc.entity.CarEntity;
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
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;

public class ChargerExtensionBlock extends BlockWithEntity {
    public static final net.minecraft.state.property.DirectionProperty FACING = net.minecraft.state.property.Properties.HORIZONTAL_FACING;
    public static final BooleanProperty HAS_CABLE = BooleanProperty.of("has_cable");
    public static final BooleanProperty CONNECTED = BooleanProperty.of("connected");

    public ChargerExtensionBlock(Settings settings) {
        super(settings);
        setDefaultState(this.stateManager.getDefaultState().with(FACING, net.minecraft.util.math.Direction.NORTH).with(HAS_CABLE, false).with(CONNECTED, false));
    }

    @Override
    public BlockState getPlacementState(net.minecraft.item.ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, HAS_CABLE, CONNECTED);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ChargerExtensionBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, EvecualMC.CHARGER_EXTENSION_BLOCK_ENTITY, ChargerExtensionBlockEntity::tick);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        if (!world.isClient && placer instanceof PlayerEntity player) {
            BlockState below = world.getBlockState(pos.down());
            if (!below.isOf(EvecualMC.CHARGER_BLOCK)) {
                player.sendMessage(Text.literal("§6⚡ Note: Place the Extension on or next to a Vehicle Charger Base!"), false);
            } else {
                player.sendMessage(Text.literal("§a⚡ Extension installed! Right-click with a Charger Cable to equip."), true);
            }
        }
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!(blockEntity instanceof ChargerExtensionBlockEntity be)) {
            return ActionResult.PASS;
        }

        ItemStack held = player.getStackInHand(hand);

        // 1. Right-click with Charger Cable in hand
        if (held.isOf(EvecualMC.CHARGER_CABLE)) {
            if (!world.isClient) {
                be.setHasCable(true);
                if (!player.isCreative()) {
                    held.decrement(1);
                }
                world.playSound(null, pos, SoundEvents.ITEM_ARMOR_EQUIP_CHAIN, SoundCategory.BLOCKS, 1.0F, 1.2F);

                // Auto-connect to nearby car if parked within 16 blocks
                CarEntity nearbyCar = findNearbyCar(world, pos);
                if (nearbyCar != null) {
                    be.connectCar(nearbyCar);
                    world.playSound(null, pos, SoundEvents.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, SoundCategory.PLAYERS, 1.0F, 1.8F);
                    player.sendMessage(Text.literal("§a⚡ Cable attached and plugged into your car! Charging... (Press X to disconnect)"), true);
                } else {
                    player.sendMessage(Text.literal("§a⚡ Charger Cable installed! Park your car and press X to connect."), true);
                }
            }
            return ActionResult.SUCCESS;
        }

        // 2. Right-click with empty hand
        if (hand == Hand.MAIN_HAND && held.isEmpty()) {
            if (!world.isClient) {
                // If currently connected: disconnect!
                if (be.isConnected()) {
                    be.disconnectCar();
                    world.playSound(null, pos, SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.BLOCKS, 1.0F, 0.8F);
                    player.sendMessage(Text.literal("§6⚡ Charging cable disconnected from vehicle."), true);
                    return ActionResult.SUCCESS;
                }

                // If not connected:
                if (!be.hasCable()) {
                    // Check if player has cable in inventory
                    if (player.getInventory().contains(new ItemStack(EvecualMC.CHARGER_CABLE))) {
                        int slot = player.getInventory().indexOf(new ItemStack(EvecualMC.CHARGER_CABLE));
                        if (slot != -1 && !player.isCreative()) {
                            player.getInventory().getStack(slot).decrement(1);
                        }
                        be.setHasCable(true);
                        world.playSound(null, pos, SoundEvents.ITEM_ARMOR_EQUIP_CHAIN, SoundCategory.BLOCKS, 1.0F, 1.2F);
                    } else {
                        player.sendMessage(Text.literal("§c⚡ No cable installed! Right-click with a Charger Cable first."), true);
                        return ActionResult.SUCCESS;
                    }
                }

                // Now connect to nearest car
                CarEntity nearbyCar = findNearbyCar(world, pos);
                if (nearbyCar != null) {
                    be.connectCar(nearbyCar);
                    world.playSound(null, pos, SoundEvents.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, SoundCategory.PLAYERS, 1.0F, 1.8F);
                    player.sendMessage(Text.literal("§a⚡ Cable plugged into your car! Charging... (Press X or Right-click to disconnect)"), true);
                } else {
                    player.sendMessage(Text.literal("§e⚡ Station ready! Park your electric car within 16 blocks and press X to connect."), true);
                }
            }
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    @Nullable
    private CarEntity findNearbyCar(World world, BlockPos pos) {
        Box box = new Box(pos).expand(16.0);
        List<CarEntity> cars = world.getEntitiesByClass(CarEntity.class, box, c -> true);
        if (cars.isEmpty()) return null;
        cars.sort(Comparator.comparingDouble(c -> c.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)));
        return cars.get(0);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof ChargerExtensionBlockEntity extension) {
                if (extension.isConnected()) {
                    extension.disconnectCar();
                }
                if (extension.hasCable() && !world.isClient) {
                    ItemEntity item = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(EvecualMC.CHARGER_CABLE));
                    world.spawnEntity(item);
                }
            }
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }
}
