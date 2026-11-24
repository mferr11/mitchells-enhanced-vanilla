package mitchellenhancedvanilla.modid.mixin;

import mitchellenhancedvanilla.modid.quiver.QuiverSlot;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {
    // Let creative mode set the quiver slot (vanilla stops at the offhand, 45)
    @ModifyConstant(method = "handleSetCreativeModeSlot", constant = @Constant(intValue = 45))
    private int mev$allowQuiverSlot(int lastSlot) {
        return QuiverSlot.MENU_INDEX;
    }
}
