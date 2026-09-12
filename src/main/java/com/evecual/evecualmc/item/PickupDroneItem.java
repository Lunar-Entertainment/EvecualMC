package com.evecual.evecualmc.item;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.entity.PickupDroneEntity;
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

public class PickupDroneItem extends Item {
    public PickupDroneItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        if (!world.isClient) {
            BlockPos pos = context.getBlockPos();
            Direction side = context.getSide();
            BlockPos spawnPos = pos.offset(side);

            PickupDroneEntity drone = new PickupDroneEntity(EvecualMC.PICKUP_DRONE_ENTITY, world);
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
    public net.minecraft.util.TypedActionResult<ItemStack> use(World world, net.minecraft.entity.player.PlayerEntity user, net.minecraft.util.Hand hand) {
        if (user.isSneaking()) {
            if (!world.isClient && user instanceof net.minecraft.server.network.ServerPlayerEntity serverPlayer) {
                EvecualMC.sendOpenTipScreen(serverPlayer, "pickup_drone", 0, PickupDroneEntity.MAX_ENERGY, "🛡️ Pickup Drone: Tactical defense harvester with automated item vacuum pickup");
            }
            return net.minecraft.util.TypedActionResult.success(user.getStackInHand(hand));
        }
        return super.use(world, user, hand);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        int energy = PickupDroneEntity.MAX_ENERGY;
        int color = 6; // Default Tactical Defense Gunmetal
        NbtCompound nbt = stack.getNbt();
        if (nbt != null) {
            if (nbt.contains("Energy")) energy = nbt.getInt("Energy");
            if (nbt.contains("ColorVariant")) color = nbt.getInt("ColorVariant");
        }

        String colorName = switch (color) {
            case 0 -> "Crimson Red";
            case 1 -> "Cyber Blue";
            case 2 -> "Stealth Black";
            case 3 -> "Neon Lime";
            case 4 -> "Pearl White";
            case 5 -> "Hazard Yellow";
            default -> "Tactical Defense Gunmetal";
        };

        tooltip.add(Text.literal("§7Chassis Coating: §f" + colorName));
        tooltip.add(Text.literal("§e⚡ Battery: §f" + energy + " / " + PickupDroneEntity.MAX_ENERGY + " E"));

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

        tooltip.add(Text.literal("§a🧲 Item Vacuum: §7Automatically collects nearby items into cargo"));
        tooltip.add(Text.literal("§8• §7Right-click on ground to deploy"));
        tooltip.add(Text.literal("§8• §7Shift + Right-Click in air for Help & Field Guide"));
        tooltip.add(Text.literal("§8• §7Pair using handheld RC Controller or Ground Station"));

        super.appendTooltip(stack, world, tooltip, context);
    }
}
