package mitchellenhancedvanilla.modid.client.mixin;

import mitchellenhancedvanilla.modid.quiver.QuiverContainer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({InventoryScreen.class, CreativeModeInventoryScreen.class})
public abstract class InventoryScreensMixin {
    @Unique
    private static final Identifier SLOT_SPRITE = Identifier.withDefaultNamespace("container/slot");

    // The backgrounds have no slot baked in for the quiver
    @Inject(method = "extractBackground", at = @At("TAIL"))
    private void mev$quiverSlotFrame(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;
        AbstractContainerScreenAccessor pos = (AbstractContainerScreenAccessor) screen;
        for (Slot slot : screen.getMenu().slots) {
            if (slot.container instanceof QuiverContainer && slot.isActive()) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_SPRITE,
                    pos.mev$leftPos() + slot.x - 1, pos.mev$topPos() + slot.y - 1, 18, 18);
            }
        }
    }
}
