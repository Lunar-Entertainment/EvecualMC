package com.evecual.evecualmc.block;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.block.entity.ChargerExtensionBlockEntity;
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
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ChargerExtensionBlock extends BlockWithEntity {
    public static final Map<UUID, BlockPos> PENDING_CABLES = new ConcurrentHashMap<>();

    public ChargerExtensionBlock(Settings settings) {
        super(settings);
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
                player.sendMessage(Text.literal("§6⚡ Note: Place the Extension directly on top of a Vehicle Charger to draw power!"), false);
            } else {
                player.sendMessage(Text.literal("§a⚡ Extension installed! Right-click with a Charger Cable to equip it."), true);
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

        // 1. Right-click with Charger Cable: Attach cable to extension
        if (held.isOf(EvecualMC.CHARGER_CABLE)) {
            if (!be.hasCable()) {
                if (!world.isClient) {
                    be.setHasCable(true);
                    if (!player.isCreative()) {
                        held.decrement(1);
                    }
                    world.playSound(null, pos, SoundEvents.ITEM_ARMOR_EQUIP_CHAIN, SoundCategory.BLOCKS, 1.0F, 1.2F);
                    player.sendMessage(Text.literal("§a⚡ Charger Cable attached to the Extension!"), true);
                }
                return ActionResult.SUCCESS;
            } else {
                if (!world.isClient) {
                    player.sendMessage(Text.literal("§e⚡ A Charger Cable is already installed on this station."), true);
                }
                return ActionResult.SUCCESS;
            }
        }

        // 2. Right-click with Empty Hand: Unholster / Disconnect cable
        if (held.isEmpty() && hand == Hand.MAIN_HAND) {
            if (!be.hasCable()) {
                if (!world.isClient) {
                    player.sendMessage(Text.literal("§c⚡ No cable installed! Right-click with a Charger Cable first."), true);
                }
                return ActionResult.SUCCESS;
            }

            if (be.isConnected()) {
                if (!world.isClient) {
                    be.disconnectCar();
                    world.playSound(null, pos, SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.BLOCKS, 1.0F, 0.8F);
                    player.sendMessage(Text.literal("§6⚡ Charging cable disconnected from vehicle."), true);
                }
                return ActionResult.SUCCESS;
            } else {
                if (!world.isClient) {
                    PENDING_CABLES.put(player.getUuid(), pos);
                    world.playSound(null, pos, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    player.sendMessage(Text.literal("§e⚡ Charging plug in hand! Right-click an Electric Car to plug in."), true);
                }
                return ActionResult.SUCCESS;
            }
        }

        return ActionResult.PASS;
    }

    @Override
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
