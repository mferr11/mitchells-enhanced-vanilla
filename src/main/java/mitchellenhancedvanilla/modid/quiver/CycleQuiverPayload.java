package mitchellenhancedvanilla.modid.quiver;

import mitchellenhancedvanilla.modid.MitchellEnhancedVanilla;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

// Client asks the server to cycle its quiver
public record CycleQuiverPayload() implements CustomPacketPayload {
    public static final CycleQuiverPayload INSTANCE = new CycleQuiverPayload();
    public static final Type<CycleQuiverPayload> TYPE = new Type<>(MitchellEnhancedVanilla.id("cycle_quiver"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CycleQuiverPayload> CODEC = StreamCodec.unit(INSTANCE);

    public static void register() {
        PayloadTypeRegistry.serverboundPlay().register(TYPE, CODEC);
        ServerPlayNetworking.registerGlobalReceiver(TYPE, (payload, context) -> Quiver.cycle(context.player()));
    }

    @Override
    public Type<CycleQuiverPayload> type() {
        return TYPE;
    }
}
