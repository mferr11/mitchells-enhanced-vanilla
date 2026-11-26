package mitchellenhancedvanilla.modid.client;

import com.mojang.blaze3d.platform.InputConstants;
import mitchellenhancedvanilla.modid.MitchellEnhancedVanilla;
import mitchellenhancedvanilla.modid.client.hud.QuiverHotbar;
import mitchellenhancedvanilla.modid.quiver.CycleQuiverPayload;
import mitchellenhancedvanilla.modid.quiver.Quiver;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

public final class QuiverKeys {
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(MitchellEnhancedVanilla.id("quiver"));
    public static final KeyMapping CYCLE = KeyMappingHelper.registerKeyMapping(new KeyMapping(
        "key.mitchells-enhanced-vanilla.cycle_quiver", InputConstants.Type.KEYBOARD, InputConstants.KEY_R, CATEGORY));

    private QuiverKeys() {}

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(QuiverKeys::tick);
    }

    private static void tick(Minecraft mc) {
        while (CYCLE.consumeClick()) {
            if (mc.player != null && Quiver.contents(mc.player).size() > 1) {
                QuiverHotbar.expectCycle(mc.player);
                ClientPlayNetworking.send(CycleQuiverPayload.INSTANCE);
            }
        }
    }
}
