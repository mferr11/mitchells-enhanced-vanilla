package mitchellenhancedvanilla.modid.mixin;

import mitchellenhancedvanilla.modid.quiver.QuiverSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractCraftingMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InventoryMenu.class)
public abstract class InventoryMenuMixin extends AbstractCraftingMenu {
    private InventoryMenuMixin(MenuType<?> type, int containerId, int width, int height) {
        super(type, containerId, width, height);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void mev$addQuiverSlot(Inventory inventory, boolean active, Player owner, CallbackInfo ci) {
        this.addSlot(new QuiverSlot(owner));
    }

    // Shift-click a quiver from the inventory into the empty quiver slot
    @Inject(method = "quickMoveStack", at = @At("HEAD"), cancellable = true)
    private void mev$quickMoveQuiver(Player player, int index, CallbackInfoReturnable<ItemStack> cir) {
        if (index < InventoryMenu.INV_SLOT_START || index > InventoryMenu.SHIELD_SLOT) return;
        Slot from = this.slots.get(index);
        Slot quiver = this.slots.get(QuiverSlot.MENU_INDEX);
        ItemStack stack = from.getItem();
        if (quiver.hasItem() || !quiver.mayPlace(stack)) return;
        quiver.setByPlayer(stack.split(1));
        from.setChanged();
        cir.setReturnValue(ItemStack.EMPTY);
    }
}
