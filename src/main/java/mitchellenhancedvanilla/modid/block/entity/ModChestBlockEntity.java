package mitchellenhancedvanilla.modid.block.entity;

import mitchellenhancedvanilla.modid.ModBlocks;
import mitchellenhancedvanilla.modid.block.ModChestBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

// Vanilla chest storage; a locked one refuses to open (with its own message) until the King is beaten
public class ModChestBlockEntity extends ChestBlockEntity {
    public ModChestBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(ModBlocks.MOD_CHEST_ENTITY, worldPosition, blockState);
    }

    @Override
    public boolean canOpen(Player player) {
        return super.canOpen(player) && !(getBlockState().getBlock() instanceof ModChestBlock block && block.isLockedFor(player));
    }

    // Swaps vanilla's "X is locked" for the block's own message
    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        if (getBlockState().getBlock() instanceof ModChestBlock block && block.isLockedFor(player)) {
            block.sendLockedMessage(player, getBlockPos());
            return null;
        }
        return super.createMenu(containerId, inventory, player);
    }

    // Named after its block (Barrow Chest, Brood Chest)
    @Override
    protected Component getDefaultName() {
        return getBlockState().getBlock().getName();
    }

    // Hoppers can't drain a locked one from below
    @Override
    public boolean canTakeItem(Container into, int slot, ItemStack itemStack) {
        return !(getBlockState().getBlock() instanceof ModChestBlock block && block.locked());
    }
}
