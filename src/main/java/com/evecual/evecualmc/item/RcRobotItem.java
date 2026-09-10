package com.evecual.evecualmc.item;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.entity.RcRobotEntity;
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

public class RcRobotItem extends Item {
    public RcRobotItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        if (!world.isClient) {
            BlockPos pos = context.getBlockPos();
            Direction side = context.getSide();
            BlockPos spawnPos = pos.offset(side);

            RcRobotEntity robot = new RcRobotEntity(EvecualMC.RC_ROBOT_ENTITY, world);
            ItemStack stack = context.getStack();
            if (stack.hasNbt()) {
                NbtCompound nbt = stack.getNbt();
                if (nbt != null) {
                    if (nbt.contains("Energy")) robot.setEnergy(nbt.getInt("Energy"));
                    if (nbt.contains("EquippedTool")) {
                        robot.setEquippedTool(ItemStack.fromNbt(nbt.getCompound("EquippedTool")));
                    }
                    if (nbt.contains("EquippedLeftArm")) {
                        robot.setEquippedLeftArm(ItemStack.fromNbt(nbt.getCompound("EquippedLeftArm")));
                    }
                    if (nbt.contains("RobotInventory")) {
                        NbtCompound invNbt = nbt.getCompound("RobotInventory");
                        DefaultedList<ItemStack> list = DefaultedList.ofSize(robot.getInventory().size(), ItemStack.EMPTY);
                        Inventories.readNbt(invNbt, list);
                        for (int i = 0; i < robot.getInventory().size(); i++) {
                            robot.getInventory().setStack(i, list.get(i));
                        }
                    }
                }
            }
            robot.refreshPositionAndAngles(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, context.getPlayerYaw(), 0.0f);
            world.spawnEntity(robot);

            world.playSound(null, spawnPos, SoundEvents.BLOCK_NETHERITE_BLOCK_PLACE, SoundCategory.BLOCKS, 0.8f, 1.4f);

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
                EvecualMC.sendOpenTipScreen(serverPlayer, "rc_robot", 0, RcRobotEntity.MAX_ENERGY, "🤖 RC Excavator Robot: Autonomous mining and double chest cargo");
            }
            return net.minecraft.util.TypedActionResult.success(user.getStackInHand(hand));
        }
        return super.use(world, user, hand);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        int energy = RcRobotEntity.MAX_ENERGY;
        ItemStack tool = ItemStack.EMPTY;
        ItemStack leftArm = ItemStack.EMPTY;
        if (stack.hasNbt() && stack.getNbt() != null) {
            if (stack.getNbt().contains("Energy")) {
                energy = stack.getNbt().getInt("Energy");
            }
            if (stack.getNbt().contains("EquippedTool")) {
                tool = ItemStack.fromNbt(stack.getNbt().getCompound("EquippedTool"));
            }
            if (stack.getNbt().contains("EquippedLeftArm")) {
                leftArm = ItemStack.fromNbt(stack.getNbt().getCompound("EquippedLeftArm"));
            }
        }
        int percent = (int) ((energy / (float) RcRobotEntity.MAX_ENERGY) * 100.0f);
        tooltip.add(Text.literal("§7Battery: §a" + energy + " §7/ §2" + RcRobotEntity.MAX_ENERGY + " EU §8(" + percent + "%)"));
        if (!tool.isEmpty()) {
            tooltip.add(Text.literal("§7Right Arm (Weapon): §b" + tool.getName().getString() + " §7[LMB]"));
        } else {
            tooltip.add(Text.literal("§7Right Arm (Weapon): §8None (Right-click with tool)"));
        }
        if (!leftArm.isEmpty()) {
            tooltip.add(Text.literal("§7Left Arm (Place): §e" + leftArm.getName().getString() + " x" + leftArm.getCount() + " §7[RMB]"));
        } else {
            tooltip.add(Text.literal("§7Left Arm (Place): §8None (Right-click with block)"));
        }
        if (stack.hasNbt() && stack.getNbt() != null && stack.getNbt().contains("RobotInventory")) {
            NbtCompound invNbt = stack.getNbt().getCompound("RobotInventory");
            DefaultedList<ItemStack> list = DefaultedList.ofSize(RcRobotEntity.INVENTORY_SIZE, ItemStack.EMPTY);
            Inventories.readNbt(invNbt, list);
            int used = 0;
            for (ItemStack s : list) {
                if (!s.isEmpty()) used++;
            }
            tooltip.add(Text.literal("§7Cargo: §6" + used + " / " + RcRobotEntity.INVENTORY_SIZE + " slots used §7(Double Chest)"));
        } else {
            tooltip.add(Text.literal("§7Cargo: §8Empty (Double Chest - 54 Slots)"));
        }
        tooltip.add(Text.literal("§8LMB: Weapon/Tool | RMB: Place Blocks | <: FPV"));
        super.appendTooltip(stack, world, tooltip, context);
    }
}
