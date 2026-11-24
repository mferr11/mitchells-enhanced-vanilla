package mitchellenhancedvanilla.modid.client.mixin;

import mitchellenhancedvanilla.modid.quiver.QuiverContainer;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeModeInventoryScreenMixin {
    // Left of the creative offhand slot (35, 20)
    @Unique private static final int QUIVER_X = 17;
    @Unique private static final int QUIVER_Y = 20;

    // Vanilla lays extra slots out in the main grid, on top of the hotbar
    @Inject(method = "selectTab", at = @At("TAIL"))
    private void mev$placeQuiverSlot(CreativeModeTab tab, CallbackInfo ci) {
        CreativeModeInventoryScreen screen = (CreativeModeInventoryScreen) (Object) this;
        if (!screen.isInventoryOpen()) return;
        for (Slot slot : screen.getMenu().slots) {
            if (slot.container instanceof QuiverContainer) {
                ((SlotAccessor) slot).mev$setX(QUIVER_X);
                ((SlotAccessor) slot).mev$setY(QUIVER_Y);
            }
        }
    }
}
