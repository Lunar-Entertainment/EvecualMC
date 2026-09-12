package com.evecual.evecualmc.block;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.block.entity.StationaryTurretBlockEntity;
import com.evecual.evecualmc.block.entity.TurretAmmoContainerBlockEntity;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
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

public class StationaryTurretBlock extends BlockWithEntity {
    private static final VoxelShape SHAPE = VoxelShapes.union(
            createCuboidShape(0, 0, 0, 16, 16, 16),
            createCuboidShape(2, 16, 2, 14, 25, 14)
    );

    public StationaryTurretBlock(Settings settings) {
        super(settings);
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
        return new StationaryTurretBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, EvecualMC.STATIONARY_TURRET_BLOCK_ENTITY, StationaryTurretBlockEntity::tick);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        if (placer instanceof PlayerEntity player) {
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof StationaryTurretBlockEntity turret) {
                turret.getTargetFilter().setOwnerUuid(player.getUuid());
                turret.markDirty();
            }
        }
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack held = player.getStackInHand(hand);


        // Check if using Turret Linker item
        if (held.isOf(EvecualMC.TURRET_LINKER)) {
            if (held.hasNbt() && held.getNbt().contains("ContainerPos")) {
                BlockPos containerPos = net.minecraft.nbt.NbtHelper.toBlockPos(held.getNbt().getCompound("ContainerPos"));
                if (!world.isClient) {
                    BlockEntity be = world.getBlockEntity(pos);
                    if (be instanceof StationaryTurretBlockEntity turret) {
                        turret.setLinkedAmmoContainerPos(containerPos);

                        BlockEntity cBe = world.getBlockEntity(containerPos);
                        if (cBe instanceof TurretAmmoContainerBlockEntity c) {
                            c.linkStationaryTurret(pos);
                        }

                        player.sendMessage(Text.literal("§a🔗 Turret successfully linked to Ammo Container at §f[" + containerPos.getX() + ", " + containerPos.getY() + ", " + containerPos.getZ() + "]!"), true);
                    }
                }
                return ActionResult.SUCCESS;
            } else {
                if (!world.isClient) {
                    player.sendMessage(Text.literal("§c⚠️ Right-click an Ammo Container first to establish a link coordinate!"), true);
                }
                return ActionResult.SUCCESS;
            }
        }

        // Normal interaction: Open Turret configuration GUI
        if (!world.isClient) {
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof StationaryTurretBlockEntity turret) {
                player.openHandledScreen(turret);
            }
        }
        return ActionResult.SUCCESS;
    }
}
