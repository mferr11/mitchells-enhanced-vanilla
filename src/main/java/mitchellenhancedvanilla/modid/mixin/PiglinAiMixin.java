package mitchellenhancedvanilla.modid.mixin;

import mitchellenhancedvanilla.modid.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PiglinAi.class)
public abstract class PiglinAiMixin {
    @Shadow
    private static void putInInventory(Piglin piglin, ItemStack stack) {
        throw new AssertionError();
    }

    // After admiring, pocket Gold Arrows instead of equipping them over the piglin's weapon
    @Inject(method = "stopHoldingOffHandItem", at = @At("HEAD"), cancellable = true)
    private static void mev$pocketGoldArrow(ServerLevel level, Piglin piglin, boolean barter, CallbackInfo ci) {
        ItemStack offhand = piglin.getItemInHand(InteractionHand.OFF_HAND);
        if (!offhand.is(ModItems.GOLD_ARROW)) return;
        piglin.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        putInInventory(piglin, offhand);
        ci.cancel();
    }
}
