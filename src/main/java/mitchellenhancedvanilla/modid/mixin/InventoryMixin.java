package mitchellenhancedvanilla.modid.mixin;

import mitchellenhancedvanilla.modid.quiver.Quiver;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Inventory.class)
public abstract class InventoryMixin {
    @Shadow @Final public Player player;

    // Death drops (skipped by vanilla under keepInventory)
    @Inject(method = "dropAll", at = @At("HEAD"))
    private void mev$dropQuiver(CallbackInfo ci) {
        Quiver.dropOnDeath(player);
    }
}
