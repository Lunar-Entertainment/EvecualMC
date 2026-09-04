package com.evecual.evecualmc.block;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.block.entity.StationaryRcControllerBlockEntity;
import com.evecual.evecualmc.entity.RcCarEntity;
import com.evecual.evecualmc.entity.RcDroneEntity;
import com.evecual.evecualmc.entity.RcRobotEntity;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class StationaryRcControllerBlock extends BlockWithEntity {
    public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;

    protected static final VoxelShape BASE = Block.createCuboidShape(2.0, 0.0, 2.0, 14.0, 2.0, 14.0);
    protected static final VoxelShape COLUMN = Block.createCuboidShape(5.0, 2.0, 5.0, 11.0, 8.0, 11.0);
    protected static final VoxelShape HEAD = Block.createCuboidShape(1.0, 8.0, 1.0, 15.0, 16.0, 15.0);
    protected static final VoxelShape SHAPE = VoxelShapes.union(BASE, COLUMN, HEAD);

    public StationaryRcControllerBlock(Settings settings) {
        super(settings);
        setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
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
        return new StationaryRcControllerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, EvecualMC.STATIONARY_RC_CONTROLLER_BLOCK_ENTITY, StationaryRcControllerBlockEntity::tick);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        if (!world.isClient && world.getBlockEntity(pos) instanceof StationaryRcControllerBlockEntity be) {
            if (itemStack.hasNbt()) {
                NbtCompound nbt = itemStack.getNbt();
                if (nbt != null) {
                    UUID vehicleUuid = null;
                    if (nbt.containsUuid("PairedVehicle")) vehicleUuid = nbt.getUuid("PairedVehicle");
                    else if (nbt.containsUuid("PairedCar")) vehicleUuid = nbt.getUuid("PairedCar");
                    else if (nbt.containsUuid("PairedDrone")) vehicleUuid = nbt.getUuid("PairedDrone");
                    else if (nbt.containsUuid("PairedRobot")) vehicleUuid = nbt.getUuid("PairedRobot");

                    String type = nbt.getString("PairedType");
                    String name = nbt.getString("VehicleName");
                    if (vehicleUuid != null) {
                        be.setPairedVehicle(vehicleUuid, type, name);
                        if (placer instanceof PlayerEntity player) {
                            player.sendMessage(Text.literal("§a📡 Stationary RC Controller installed and linked to §f" + (name.isEmpty() ? "RC Vehicle" : name) + "§a!"), true);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            if (!world.isClient && world.getBlockEntity(pos) instanceof StationaryRcControllerBlockEntity be) {
                // If a player was currently operating the station, disconnect them
                if (be.getCurrentUserUuid() != null && world instanceof ServerWorld serverWorld) {
                    ServerPlayerEntity user = serverWorld.getServer().getPlayerManager().getPlayer(be.getCurrentUserUuid());
                    if (user != null) {
                        ServerPlayNetworking.send(user, EvecualMC.EXIT_RC_STATION_PACKET_ID, PacketByteBufs.empty());
                    }
                }

                // Drop item preserving paired vehicle NBT
                ItemStack drop = new ItemStack(EvecualMC.STATIONARY_RC_CONTROLLER_ITEM);
                if (be.isPaired()) {
                    NbtCompound nbt = drop.getOrCreateNbt();
                    nbt.putUuid("PairedVehicle", be.getPairedVehicleUuid());
                    if ("car".equals(be.getPairedType())) nbt.putUuid("PairedCar", be.getPairedVehicleUuid());
                    else if ("drone".equals(be.getPairedType())) nbt.putUuid("PairedDrone", be.getPairedVehicleUuid());
                    else if ("robot".equals(be.getPairedType())) nbt.putUuid("PairedRobot", be.getPairedVehicleUuid());
                    nbt.putString("PairedType", be.getPairedType());
                    nbt.putString("VehicleName", be.getVehicleName());
                    nbt.putBoolean("ActiveLink", true);
                }
                world.spawnEntity(new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, drop));
            }
            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient) {
            return ActionResult.SUCCESS;
        }

        if (world.getBlockEntity(pos) instanceof StationaryRcControllerBlockEntity be) {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
            ServerWorld serverWorld = (ServerWorld) world;

            if (!be.isPaired()) {
                serverPlayer.sendMessage(Text.literal("§c📡 No RC vehicle linked! Right-click an RC Car, Drone, or Robot with this controller in hand before placing it."), true);
                world.playSound(null, pos, SoundEvents.BLOCK_NOTE_BLOCK_BASS.value(), SoundCategory.BLOCKS, 0.8f, 0.8f);
                return ActionResult.SUCCESS;
            }

            Entity vehicle = serverWorld.getEntity(be.getPairedVehicleUuid());
            if (vehicle == null || !vehicle.isAlive()) {
                serverPlayer.sendMessage(Text.literal("§c📡 Linked RC vehicle not found or out of range!"), true);
                world.playSound(null, pos, SoundEvents.BLOCK_NOTE_BLOCK_BASS.value(), SoundCategory.BLOCKS, 0.8f, 0.8f);
                return ActionResult.SUCCESS;
            }

            // If terminal is already used by someone else
            if (be.getCurrentUserUuid() != null && !be.getCurrentUserUuid().equals(player.getUuid())) {
                serverPlayer.sendMessage(Text.literal("§c📡 Terminal is currently in use by another player!"), true);
                return ActionResult.SUCCESS;
            }

            // If current player is already using it, disconnect
            if (player.getUuid().equals(be.getCurrentUserUuid())) {
                be.setCurrentUserUuid(null);
                ServerPlayNetworking.send(serverPlayer, EvecualMC.EXIT_RC_STATION_PACKET_ID, PacketByteBufs.empty());
                serverPlayer.sendMessage(Text.literal("§7📡 Disconnected from RC Control Station."), true);
                world.playSound(null, pos, SoundEvents.BLOCK_BEACON_DEACTIVATE, SoundCategory.BLOCKS, 0.6f, 1.4f);
                return ActionResult.SUCCESS;
            }

            // Connect player to terminal
            be.setCurrentUserUuid(player.getUuid());

            // Link vehicle to player
            if (vehicle instanceof RcCarEntity car) {
                car.setPairedPlayerUuid(player.getUuidAsString());
                car.setExplicitlyPairedInSpot(true);
            } else if (vehicle instanceof RcDroneEntity drone) {
                drone.setPairedPlayerUuid(player.getUuidAsString());
                drone.setExplicitlyPairedInSpot(true);
            } else if (vehicle instanceof RcRobotEntity robot) {
                robot.setPairedPlayerUuid(player.getUuidAsString());
                robot.setExplicitlyPairedInSpot(true);
            }

            // Send S2C packet to lock player and engage RC camera
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeBlockPos(pos);
            buf.writeUuid(be.getPairedVehicleUuid());
            buf.writeString(be.getPairedType());
            ServerPlayNetworking.send(serverPlayer, EvecualMC.ENTER_RC_STATION_PACKET_ID, buf);

            String vehicleName = vehicle.getName().getString();
            serverPlayer.sendMessage(Text.literal("§b🖥️ RC Control Station: §aLINKED TO " + vehicleName.toUpperCase() + " §7[Shift / Sneak to Exit]"), true);
            world.playSound(null, pos, SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.BLOCKS, 0.8f, 1.8f);
        }

        return ActionResult.SUCCESS;
    }
}
