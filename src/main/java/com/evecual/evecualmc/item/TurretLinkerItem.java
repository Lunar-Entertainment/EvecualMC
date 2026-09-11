package com.evecual.evecualmc.item;

import com.evecual.evecualmc.block.TurretAmmoContainerBlock;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TurretLinkerItem extends Item {
    public TurretLinkerItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        PlayerEntity player = context.getPlayer();
        ItemStack stack = context.getStack();

        if (world.getBlockState(pos).getBlock() instanceof TurretAmmoContainerBlock) {
            if (!world.isClient && player != null) {
                NbtCompound nbt = stack.getOrCreateNbt();
                nbt.put("ContainerPos", NbtHelper.fromBlockPos(pos));

                player.sendMessage(Text.literal("§a📡 Ammo Container coordinate stored: §f[" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + "]§a! Now right-click any Defense Turret to link."), true);
                world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), SoundCategory.PLAYERS, 1.0F, 1.8F);
            }
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        if (stack.hasNbt() && stack.getNbt().contains("ContainerPos")) {
            BlockPos pos = NbtHelper.toBlockPos(stack.getNbt().getCompound("ContainerPos"));
            tooltip.add(Text.literal("§a🔗 Locked Container: §f[" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + "]"));
            tooltip.add(Text.literal("§e[Right-Click Defense Turret to link]"));
        } else {
            tooltip.add(Text.literal("§7Status: §cNo Container Locked"));
            tooltip.add(Text.literal("§8[Right-Click Ammo Container to store position]"));
        }
        super.appendTooltip(stack, world, tooltip, context);
    }
}
