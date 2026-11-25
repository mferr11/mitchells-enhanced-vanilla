package mitchellenhancedvanilla.modid.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mitchellenhancedvanilla.modid.ScreenShakePayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

// Camera shake driven by ScreenShakePayload; applied after vanilla's hurt tilt
public final class ScreenShake {
    // Wobble speed (radians per tick) on each axis, ~3-4 Hz so it reads as a rumble rather than blurring out;
    // different per axis so it never looks like a loop
    private static final float PITCH_FREQ = 1.1F;
    private static final float YAW_FREQ = 1.35F;
    private static final float ROLL_FREQ = 0.85F;
    private static final double NANOS_PER_TICK = 50_000_000.0;

    private static float strength;
    private static long startNanos;
    private static double durationNanos;

    private ScreenShake() {}

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(ScreenShakePayload.TYPE, (payload, context) -> start(payload.strength(), payload.durationTicks()));
    }

    // A stronger shake replaces a weaker one already running
    public static void start(float newStrength, int durationTicks) {
        if (newStrength < currentStrength()) return;
        strength = newStrength;
        startNanos = System.nanoTime();
        durationNanos = durationTicks * NANOS_PER_TICK;
    }

    private static float currentStrength() {
        if (durationNanos <= 0.0) return 0.0F;
        double progress = (System.nanoTime() - startNanos) / durationNanos;
        return progress >= 1.0 ? 0.0F : strength * (float) (1.0 - progress);
    }

    public static void apply(PoseStack poseStack) {
        float current = currentStrength();
        if (current <= 0.0F) return;
        // Respects the Distortion Effects accessibility slider
        current *= Minecraft.getInstance().options.screenEffectScale().get().floatValue();
        if (current <= 0.0F) return;
        float t = (float) ((System.nanoTime() - startNanos) / NANOS_PER_TICK);
        poseStack.rotateDegrees(Axis.XP, Mth.sin(t * PITCH_FREQ) * current);
        poseStack.rotateDegrees(Axis.YP, Mth.sin(t * YAW_FREQ + 1.3F) * current);
        poseStack.rotateDegrees(Axis.ZP, Mth.sin(t * ROLL_FREQ + 2.6F) * current * 0.5F);
    }
}
