package com.evecual.evecualmc.item;

import com.evecual.evecualmc.entity.RcCarEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class RcControllerItem extends Item {
    public RcControllerItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        return ActionResult.PASS;
    }

    public static boolean pairWithCar(ItemStack stack, PlayerEntity player, RcCarEntity car) {
        NbtCompound nbt = stack.getOrCreateNbt();
        nbt.putUuid("PairedCar", car.getUuid());
        nbt.putBoolean("ActiveLink", true);
        car.setPairedPlayerUuid(player.getUuidAsString());

        player.sendMessage(Text.literal("§a📡 RC Controller paired to RC Car!"), true);
        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), SoundCategory.PLAYERS, 0.9f, 2.0f);
        return true;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        NbtCompound nbt = stack.getOrCreateNbt();

        if (!nbt.containsUuid("PairedCar")) {
            if (!world.isClient) {
                user.sendMessage(Text.literal("§c📡 Not Paired! Sneak + Right-Click an RC Car to pair."), true);
                world.playSound(null, user.getX(), user.getY(), user.getZ(),
                        SoundEvents.BLOCK_NOTE_BLOCK_BASS.value(), SoundCategory.PLAYERS, 0.8f, 0.8f);
            }
            return TypedActionResult.fail(stack);
        }

        boolean currentActive = nbt.getBoolean("ActiveLink");
        boolean newActive = !currentActive;
        nbt.putBoolean("ActiveLink", newActive);

        if (!world.isClient) {
            if (newActive) {
                user.sendMessage(Text.literal("§b📡 RC Driving Link: §aENABLED §7[W/A/S/D to Drive, Space to Hop]"), true);
                world.playSound(null, user.getX(), user.getY(), user.getZ(),
                        SoundEvents.BLOCK_NOTE_BLOCK_BIT.value(), SoundCategory.PLAYERS, 0.8f, 1.8f);
            } else {
                user.sendMessage(Text.literal("§7📡 RC Driving Link: §cDISABLED"), true);
                world.playSound(null, user.getX(), user.getY(), user.getZ(),
                        SoundEvents.BLOCK_NOTE_BLOCK_BIT.value(), SoundCategory.PLAYERS, 0.8f, 1.0f);
            }
        }

        return TypedActionResult.success(stack, world.isClient);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        NbtCompound nbt = stack.getNbt();
        if (nbt != null && nbt.containsUuid("PairedCar")) {
            UUID carUuid = nbt.getUuid("PairedCar");
            boolean active = nbt.getBoolean("ActiveLink");
            String shortId = carUuid.toString().substring(0, 8);
            tooltip.add(Text.literal("§a📡 Paired: §fCar #" + shortId));
            tooltip.add(Text.literal(active ? "§b⚡ Link: §aACTIVE" : "§7⚡ Link: §cSTANDBY"));
            tooltip.add(Text.literal("§8• §7Right-click to toggle remote drive mode"));
            tooltip.add(Text.literal("§8• §7Use W/A/S/D to steer & drive, Space to Hop"));
        } else {
            tooltip.add(Text.literal("§c📡 Status: Unpaired"));
            tooltip.add(Text.literal("§8• §7Sneak + Right-click on an RC Car to pair"));
        }
        super.appendTooltip(stack, world, tooltip, context);
    }
}
