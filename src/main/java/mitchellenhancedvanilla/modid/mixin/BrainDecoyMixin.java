package mitchellenhancedvanilla.modid.mixin;

import mitchellenhancedvanilla.modid.DecoyBrain;
import mitchellenhancedvanilla.modid.DecoyTarget;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(Brain.class)
public abstract class BrainDecoyMixin implements DecoyBrain {
    @Unique private @Nullable DecoyTarget mev$decoyOwner;

    @Override
    public void mev$setDecoyOwner(DecoyTarget owner) {
        mev$decoyOwner = owner;
    }

    @Unique
    private boolean mev$blocks(MemoryModuleType<?> type) {
        DecoyTarget owner = mev$decoyOwner;
        return type == MemoryModuleType.ATTACK_TARGET && owner != null && owner.mev$isDistracted();
    }

    // Brain-driven mobs can't pick a fight while distracted
    @Inject(method = "setMemory(Lnet/minecraft/world/entity/ai/memory/MemoryModuleType;Ljava/lang/Object;)V",
        at = @At("HEAD"), cancellable = true)
    private void mev$blockSet(MemoryModuleType<?> type, Object value, CallbackInfo ci) {
        if (value != null && mev$blocks(type)) ci.cancel();
    }

    @Inject(method = "setMemory(Lnet/minecraft/world/entity/ai/memory/MemoryModuleType;Ljava/util/Optional;)V",
        at = @At("HEAD"), cancellable = true)
    private void mev$blockSetOptional(MemoryModuleType<?> type, Optional<?> value, CallbackInfo ci) {
        if (value.isPresent() && mev$blocks(type)) ci.cancel();
    }

    @Inject(method = "setMemoryWithExpiry", at = @At("HEAD"), cancellable = true)
    private void mev$blockSetExpiry(MemoryModuleType<?> type, Object value, long ttl, CallbackInfo ci) {
        if (mev$blocks(type)) ci.cancel();
    }
}
