package mitchellenhancedvanilla.modid.quiver;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.component.BundleContents;

// Bundle tooltip data for a quiver, so the client can scale its fill bar
public record QuiverTooltip(BundleContents contents) implements TooltipComponent {}
