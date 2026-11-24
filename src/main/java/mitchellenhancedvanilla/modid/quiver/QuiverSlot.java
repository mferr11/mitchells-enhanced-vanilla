package mitchellenhancedvanilla.modid.quiver;

import mitchellenhancedvanilla.modid.MitchellEnhancedVanilla;
import mitchellenhancedvanilla.modid.ModItems;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

// Quiver slot in the player inventory, above the offhand
public class QuiverSlot extends Slot {
    public static final int MENU_INDEX = InventoryMenu.SHIELD_SLOT + 1; // appended after the offhand
    public static final int X = 77;
    public static final int Y = 44;
    private static final Identifier EMPTY_ICON = MitchellEnhancedVanilla.id("container/slot/quiver");

    public QuiverSlot(Player player) {
        super(new QuiverContainer(player), 0, X, Y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return stack.is(ModItems.QUIVER);
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public Identifier getNoItemIcon() {
        return EMPTY_ICON;
    }
}
