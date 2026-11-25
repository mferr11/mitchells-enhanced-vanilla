package mitchellenhancedvanilla.modid;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

// Tells a client to shake its camera: strength in degrees, fading out over the duration
public record ScreenShakePayload(float strength, int durationTicks) implements CustomPacketPayload {
    public static final Type<ScreenShakePayload> TYPE = new Type<>(MitchellEnhancedVanilla.id("screen_shake"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ScreenShakePayload> CODEC = StreamCodec.composite(
        ByteBufCodecs.FLOAT, ScreenShakePayload::strength,
        ByteBufCodecs.VAR_INT, ScreenShakePayload::durationTicks,
        ScreenShakePayload::new);

    // Fraction of the radius that gets full strength; it fades to nothing over the rest
    public static final double FULL_STRENGTH_FRACTION = 0.5;

    public static void register() {
        PayloadTypeRegistry.clientboundPlay().register(TYPE, CODEC);
    }

    // Shakes players within radius: full strength in the inner half, fading out towards the edge
    public static void shakeNearby(ServerLevel level, Vec3 centre, double radius, float strength, int durationTicks) {
        double fullRadius = radius * FULL_STRENGTH_FRACTION;
        for (ServerPlayer player : PlayerLookup.around(level, centre, radius)) {
            double distance = player.position().distanceTo(centre);
            double falloff = distance <= fullRadius ? 1.0 : 1.0 - (distance - fullRadius) / (radius - fullRadius);
            float scaled = strength * (float) falloff;
            ServerPlayNetworking.send(player, new ScreenShakePayload(scaled, durationTicks));
        }
    }

    @Override
    public Type<ScreenShakePayload> type() {
        return TYPE;
    }
}
