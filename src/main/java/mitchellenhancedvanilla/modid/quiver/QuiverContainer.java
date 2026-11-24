package mitchellenhancedvanilla.modid.quiver;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

// One-slot container view of the player's quiver attachment
public class QuiverContainer implements Container {
    private final Player player;

    public QuiverContainer(Player player) {
        this.player = player;
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return Quiver.get(player).isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return Quiver.get(player);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack stack = Quiver.get(player);
        ItemStack removed = stack.split(amount);
        Quiver.set(player, stack);
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack stack = Quiver.get(player);
        Quiver.set(player, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        Quiver.set(player, stack);
    }

    @Override
    public void setChanged() {}

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        Quiver.set(player, ItemStack.EMPTY);
    }
}
