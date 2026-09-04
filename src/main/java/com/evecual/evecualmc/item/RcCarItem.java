package com.evecual.evecualmc.item;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.entity.RcCarEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RcCarItem extends Item {
    public RcCarItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        if (!world.isClient) {
            BlockPos pos = context.getBlockPos();
            Direction side = context.getSide();
            BlockPos spawnPos = pos.offset(side);

            RcCarEntity rcCar = new RcCarEntity(EvecualMC.RC_CAR_ENTITY, world);
            ItemStack stack = context.getStack();
            if (stack.hasNbt()) {
                NbtCompound nbt = stack.getNbt();
                if (nbt != null) {
                    if (nbt.contains("Energy")) rcCar.setEnergy(nbt.getInt("Energy"));
                    if (nbt.contains("ColorVariant")) rcCar.setColorVariant(nbt.getInt("ColorVariant"));
                    if (nbt.contains("TrunkItems")) {
                        net.minecraft.util.collection.DefaultedList<ItemStack> list = net.minecraft.util.collection.DefaultedList.ofSize(9, ItemStack.EMPTY);
                        net.minecraft.inventory.Inventories.readNbt(nbt.getCompound("TrunkItems"), list);
                        for (int i = 0; i < list.size(); ++i) {
                            rcCar.getTrunk().setStack(i, list.get(i));
                        }
                    }
                }
            }
            rcCar.refreshPositionAndAngles(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, context.getPlayerYaw(), 0.0f);
            world.spawnEntity(rcCar);

            world.playSound(null, spawnPos, SoundEvents.ENTITY_ITEM_FRAME_ADD_ITEM, SoundCategory.BLOCKS, 0.8f, 1.4f);

            if (context.getPlayer() != null && !context.getPlayer().isCreative()) {
                stack.decrement(1);
            }
        }
        return ActionResult.success(world.isClient);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        int energy = RcCarEntity.MAX_ENERGY;
        int color = 0;
        NbtCompound nbt = stack.getNbt();
        if (nbt != null) {
            if (nbt.contains("Energy")) energy = nbt.getInt("Energy");
            if (nbt.contains("ColorVariant")) color = nbt.getInt("ColorVariant");
        }

        String colorName = switch (color) {
            case 1 -> "Cyber Blue";
            case 2 -> "Stealth Black";
            case 3 -> "Neon Lime";
            case 4 -> "Pearl White";
            case 5 -> "Racing Yellow";
            default -> "Crimson Red";
        };

        tooltip.add(Text.literal("§7Body Color: §f" + colorName));
        tooltip.add(Text.literal("§e⚡ Battery: §f" + energy + " / " + RcCarEntity.MAX_ENERGY + " E"));

        if (nbt != null && nbt.contains("TrunkItems")) {
            net.minecraft.util.collection.DefaultedList<ItemStack> list = net.minecraft.util.collection.DefaultedList.ofSize(9, ItemStack.EMPTY);
            net.minecraft.inventory.Inventories.readNbt(nbt.getCompound("TrunkItems"), list);
            int count = 0;
            for (ItemStack s : list) {
                if (!s.isEmpty()) count += s.getCount();
            }
            if (count > 0) {
                tooltip.add(Text.literal("§6📦 Trunk: §e" + count + " items"));
            }
        }

        tooltip.add(Text.literal("§8• §7Right-click on ground to deploy"));
        tooltip.add(Text.literal("§8• §7Right-click with empty hand to open trunk"));
        tooltip.add(Text.literal("§8• §7Sneak + Right-click to pick up"));
        tooltip.add(Text.literal("§b📡 Remote controllable with RC Controller"));
        super.appendTooltip(stack, world, tooltip, context);
    }
}
