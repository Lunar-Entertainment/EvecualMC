package com.evecual.evecualmc.block.entity;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.screen.ElectronicCombinerScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.GlassBlock;
import net.minecraft.block.StainedGlassBlock;
import net.minecraft.block.StainedGlassPaneBlock;
import net.minecraft.block.TintedGlassBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ElectronicCombinerBlockEntity extends BlockEntity implements SidedInventory, ExtendedScreenHandlerFactory {
    public static final int INVENTORY_SIZE = 7;
    public static final int MAX_ENERGY = 500;
    public static final int MAX_PROGRESS = 100;

    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(INVENTORY_SIZE, ItemStack.EMPTY);
    private int energy = 0;
    private int progress = 0;

    protected final PropertyDelegate propertyDelegate = new PropertyDelegate() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> ElectronicCombinerBlockEntity.this.progress;
                case 1 -> MAX_PROGRESS;
                case 2 -> ElectronicCombinerBlockEntity.this.energy;
                case 3 -> MAX_ENERGY;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> ElectronicCombinerBlockEntity.this.progress = value;
                case 2 -> ElectronicCombinerBlockEntity.this.energy = value;
            }
        }

        @Override
        public int size() {
            return 4;
        }
    };

    public ElectronicCombinerBlockEntity(BlockPos pos, BlockState state) {
        super(EvecualMC.ELECTRONIC_COMBINER_BLOCK_ENTITY, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, ElectronicCombinerBlockEntity be) {
        if (world.isClient) return;

        if (be.hasValidRecipe() && be.energy >= 1) {
            be.progress++;
            if (be.progress % 2 == 0) {
                be.energy--; // Drains energy during assembly
            }

            if (be.progress >= MAX_PROGRESS) {
                be.craftItem();
                be.progress = 0;
            }
            be.markDirty();
            be.sync();
        } else {
            if (be.progress > 0) {
                be.progress = 0;
                be.markDirty();
                be.sync();
            }
        }
    }

    private boolean isGlassItem(Item item) {
        if (item == Items.GLASS || item == Items.GLASS_PANE || item == Items.TINTED_GLASS) return true;
        if (item instanceof BlockItem bi) {
            Block b = bi.getBlock();
            return b instanceof GlassBlock || b instanceof StainedGlassBlock || b instanceof StainedGlassPaneBlock || b instanceof TintedGlassBlock;
        }
        return false;
    }

    private boolean hasValidRecipe() {
        ItemStack engine = this.inventory.get(0);
        ItemStack hull = this.inventory.get(1);
        ItemStack glass = this.inventory.get(2);
        ItemStack leather = this.inventory.get(3);
        ItemStack output = this.inventory.get(6);

        boolean hasEngine = engine.isOf(EvecualMC.ENGINE) || engine.isOf(EvecualMC.UPGRADED_ENGINE);
        boolean hasHull = hull.isOf(EvecualMC.STEEL_INGOT) || hull.isOf(Items.IRON_INGOT);
        boolean hasGlass = isGlassItem(glass.getItem());
        boolean hasLeather = leather.isOf(Items.LEATHER);

        boolean canOutput = output.isEmpty() || (output.isOf(EvecualMC.CAR_ITEM) && output.getCount() < output.getMaxCount());

        return hasEngine && hasHull && hasGlass && hasLeather && canOutput;
    }

    public static int getGlassColorFromItem(Item item) {
        if (item == Items.TINTED_GLASS) return 1; // Smoked Tinted
        if (item == Items.WHITE_STAINED_GLASS || item == Items.WHITE_STAINED_GLASS_PANE) return 2;
        if (item == Items.LIGHT_GRAY_STAINED_GLASS || item == Items.GRAY_STAINED_GLASS) return 3;
        if (item == Items.BLACK_STAINED_GLASS || item == Items.BLACK_STAINED_GLASS_PANE) return 1;
        if (item == Items.RED_STAINED_GLASS || item == Items.RED_STAINED_GLASS_PANE) return 4;
        if (item == Items.ORANGE_STAINED_GLASS || item == Items.ORANGE_STAINED_GLASS_PANE) return 5;
        if (item == Items.YELLOW_STAINED_GLASS || item == Items.YELLOW_STAINED_GLASS_PANE) return 6;
        if (item == Items.LIME_STAINED_GLASS || item == Items.LIME_STAINED_GLASS_PANE || item == Items.GREEN_STAINED_GLASS) return 7;
        if (item == Items.CYAN_STAINED_GLASS || item == Items.LIGHT_BLUE_STAINED_GLASS) return 8;
        if (item == Items.BLUE_STAINED_GLASS || item == Items.BLUE_STAINED_GLASS_PANE) return 9;
        if (item == Items.PURPLE_STAINED_GLASS || item == Items.MAGENTA_STAINED_GLASS) return 10;
        if (item == Items.PINK_STAINED_GLASS || item == Items.PINK_STAINED_GLASS_PANE) return 11;
        return 0; // Clear Glass
    }

    public static int getCarColorFromDye(ItemStack stack) {
        if (stack.isEmpty()) return 0; // Default Red when empty!
        Item item = stack.getItem();
        if (item == Items.BLUE_DYE || item == Items.CYAN_DYE || item == Items.LIGHT_BLUE_DYE) return 1;
        if (item == Items.BLACK_DYE || item == Items.GRAY_DYE || item == Items.LIGHT_GRAY_DYE) return 2;
        if (item == Items.LIME_DYE || item == Items.GREEN_DYE) return 3;
        if (item == Items.WHITE_DYE) return 4;
        if (item == Items.YELLOW_DYE || item == Items.ORANGE_DYE) return 5;
        return 0; // Default Red
    }

    private void craftItem() {
        ItemStack engine = this.inventory.get(0);
        ItemStack hull = this.inventory.get(1);
        ItemStack glass = this.inventory.get(2);
        ItemStack leather = this.inventory.get(3);
        ItemStack color = this.inventory.get(4);
        ItemStack trunk = this.inventory.get(5);

        boolean isUpgradedEngine = engine.isOf(EvecualMC.UPGRADED_ENGINE);
        int glassColor = getGlassColorFromItem(glass.getItem());
        int carColor = getCarColorFromDye(color);
        int trunkTier = (!trunk.isEmpty() && (trunk.isOf(EvecualMC.TRUNK_UPGRADE) || trunk.isOf(Items.CHEST))) ? 1 : 0;

        // Decrement inputs
        this.removeStack(0, 1);
        this.removeStack(1, 1);
        this.removeStack(2, 1);
        this.removeStack(3, 1);
        if (!color.isEmpty()) {
            this.removeStack(4, 1);
        }
        if (!trunk.isEmpty()) {
            this.removeStack(5, 1);
        }

        ItemStack output = this.inventory.get(6);
        ItemStack car = new ItemStack(EvecualMC.CAR_ITEM, 1);
        NbtCompound nbt = car.getOrCreateNbt();
        nbt.putInt("ColorVariant", carColor);
        nbt.putInt("GlassColor", glassColor);
        nbt.putBoolean("UpgradedEngine", isUpgradedEngine);
        nbt.putInt("TrunkTier", trunkTier);

        if (output.isEmpty()) {
            this.setStack(6, car);
        } else if (output.isOf(EvecualMC.CAR_ITEM)) {
            output.increment(1);
        }
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable("container.evecualmc.electronic_combiner");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new ElectronicCombinerScreenHandler(syncId, playerInventory, this, this.propertyDelegate);
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        buf.writeBlockPos(this.pos);
    }

    public long getEnergy() {
        return energy;
    }

    public long getMaxEnergy() {
        return MAX_ENERGY;
    }

    public boolean isCrafting() {
        return progress > 0;
    }

    public void receiveEnergy(int amount) {
        this.energy = Math.min(this.energy + amount, MAX_ENERGY);
        markDirty();
        sync();
    }

    public void sync() {
        if (world != null && !world.isClient) {
            world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_ALL);
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, this.inventory);
        nbt.putInt("Energy", this.energy);
        nbt.putInt("Progress", this.progress);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        Inventories.readNbt(nbt, this.inventory);
        this.energy = nbt.getInt("Energy");
        this.progress = nbt.getInt("Progress");
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return createNbt();
    }

    @Override
    public int size() {
        return INVENTORY_SIZE;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : this.inventory) {
            if (!stack.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getStack(int slot) {
        return this.inventory.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        ItemStack result = Inventories.splitStack(this.inventory, slot, amount);
        if (!result.isEmpty()) {
            markDirty();
        }
        return result;
    }

    @Override
    public ItemStack removeStack(int slot) {
        return Inventories.removeStack(this.inventory, slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        this.inventory.set(slot, stack);
        if (stack.getCount() > getMaxCountPerStack()) {
            stack.setCount(getMaxCountPerStack());
        }
        markDirty();
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return this.world != null && this.world.getBlockEntity(this.pos) == this && player.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0;
    }

    @Override
    public void clear() {
        this.inventory.clear();
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        if (side == Direction.DOWN) {
            return new int[]{6}; // Output slot
        }
        return new int[]{0, 1, 2, 3, 4, 5}; // Input slots
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return slot != 6;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return slot == 6;
    }
}
