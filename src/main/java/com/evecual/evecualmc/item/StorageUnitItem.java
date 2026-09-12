package com.evecual.evecualmc.item;

import com.evecual.evecualmc.block.entity.StorageUnitBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class StorageUnitItem extends BlockItem {

    public StorageUnitItem(Block block, Settings settings) {
        super(block, settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        if (stack.hasNbt() && stack.getSubNbt("BlockEntityTag") != null) {
            NbtCompound tag = stack.getSubNbt("BlockEntityTag");
            int energy = tag.getInt("Energy");
            DefaultedList<ItemStack> items = DefaultedList.ofSize(StorageUnitBlockEntity.SLOTS_PER_UNIT, ItemStack.EMPTY);
            Inventories.readNbt(tag, items);

            int totalCount = 0;
            int distinctTypes = 0;
            for (ItemStack s : items) {
                if (!s.isEmpty()) {
                    totalCount += s.getCount();
                    distinctTypes++;
                }
            }

            if (totalCount > 0) {
                tooltip.add(Text.literal("§a⚡ Quantum Shulker Containment Active"));
                tooltip.add(Text.literal("§e⚡ Stored Energy: §f" + energy + " / " + StorageUnitBlockEntity.MAX_ENERGY + " EU"));
                tooltip.add(Text.literal("§b📦 Contains §f" + totalCount + "§b items stored:"));

                int shown = 0;
                for (ItemStack s : items) {
                    if (!s.isEmpty() && shown < 4) {
                        tooltip.add(Text.literal("  §7- §f" + s.getCount() + "x §7" + s.getName().getString()));
                        shown++;
                    }
                }
                if (distinctTypes > 4) {
                    tooltip.add(Text.literal("  §8... and " + (distinctTypes - 4) + " more items"));
                }
                tooltip.add(Text.literal("§6⚡ Requires 200 EU initialization power when placed."));
                return;
            }
        }

        tooltip.add(Text.literal("§7Colossal multi-block expandable electric storage vault."));
        tooltip.add(Text.literal("§e⚡ Place adjacent units to combine storage into a single bank."));
        tooltip.add(Text.literal("§6⚡ Charged Shulker: Retains all items when mined while powered (≥ 200 EU)."));
        tooltip.add(Text.literal("§c⚠️ Uncharged: Drops all stored items on the ground when broken."));
    }
}
