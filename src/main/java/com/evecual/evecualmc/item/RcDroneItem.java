package com.evecual.evecualmc.item;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.entity.RcDroneEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RcDroneItem extends Item {
    public RcDroneItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        if (!world.isClient) {
            BlockPos pos = context.getBlockPos();
            Direction side = context.getSide();
            BlockPos spawnPos = pos.offset(side);

            RcDroneEntity drone = new RcDroneEntity(EvecualMC.RC_DRONE_ENTITY, world);
            ItemStack stack = context.getStack();
            if (stack.hasNbt()) {
                NbtCompound nbt = stack.getNbt();
                if (nbt != null) {
                    if (nbt.contains("Energy")) drone.setEnergy(nbt.getInt("Energy"));
                    if (nbt.contains("ColorVariant")) drone.setColorVariant(nbt.getInt("ColorVariant"));
                    if (nbt.contains("PairedPlayer")) drone.setPairedPlayerUuid(nbt.getString("PairedPlayer"));
                    if (nbt.contains("TrunkItems")) {
                        DefaultedList<ItemStack> list = DefaultedList.ofSize(9, ItemStack.EMPTY);
                        Inventories.readNbt(nbt.getCompound("TrunkItems"), list);
                        for (int i = 0; i < list.size(); ++i) {
                            drone.getTrunk().setStack(i, list.get(i));
                        }
                    }
                }
            }
            drone.refreshPositionAndAngles(spawnPos.getX() + 0.5, spawnPos.getY() + 0.1, spawnPos.getZ() + 0.5, context.getPlayerYaw(), 0.0f);
            world.spawnEntity(drone);

            world.playSound(null, spawnPos, SoundEvents.ENTITY_ITEM_FRAME_ADD_ITEM, SoundCategory.BLOCKS, 0.8f, 1.6f);

            if (context.getPlayer() != null && !context.getPlayer().isCreative()) {
                stack.decrement(1);
            }
        }
        return ActionResult.success(world.isClient);
    }


    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        int energy = RcDroneEntity.MAX_ENERGY;
        int color = 1; // Default Cyber Blue
        NbtCompound nbt = stack.getNbt();
        if (nbt != null) {
            if (nbt.contains("Energy")) energy = nbt.getInt("Energy");
            if (nbt.contains("ColorVariant")) color = nbt.getInt("ColorVariant");
        }

        String colorName = switch (color) {
            case 0 -> "Crimson Red";
            case 2 -> "Stealth Black";
            case 3 -> "Neon Lime";
            case 4 -> "Pearl White";
            case 5 -> "Racing Yellow";
            default -> "Cyber Blue";
        };

        tooltip.add(Text.literal("§7Body Color: §f" + colorName));
        tooltip.add(Text.literal("§e⚡ Battery: §f" + energy + " / " + RcDroneEntity.MAX_ENERGY + " E"));

        if (nbt != null && nbt.contains("TrunkItems")) {
            DefaultedList<ItemStack> list = DefaultedList.ofSize(9, ItemStack.EMPTY);
            Inventories.readNbt(nbt.getCompound("TrunkItems"), list);
            int count = 0;
            for (ItemStack s : list) {
                if (!s.isEmpty()) count += s.getCount();
            }
            if (count > 0) {
                tooltip.add(Text.literal("§6📦 Cargo: §e" + count + " items"));
            }
        }

        tooltip.add(Text.literal("§8• §7Right-click on ground to deploy"));
        tooltip.add(Text.literal("§8• §7Right-click with empty hand to open cargo bay"));
        tooltip.add(Text.literal("§8• §7Sneak + Right-click to pick up"));
        tooltip.add(Text.literal("§b📡 Remote controllable up to 512 blocks with RC Controller"));
        super.appendTooltip(stack, world, tooltip, context);
    }
}
