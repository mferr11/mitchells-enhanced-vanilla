package mitchellenhancedvanilla.modid.client;

import mitchellenhancedvanilla.modid.BowStats;
import mitchellenhancedvanilla.modid.MitchellEnhancedVanilla;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;

// Bow tooltips: draw time and arrow speed, styled like the arrow damage section
public final class BowTooltip {
    private static final String KEY = "tooltip." + MitchellEnhancedVanilla.MOD_ID + ".bow.";
    private static final float TICKS_PER_SECOND = 20.0F;

    private BowTooltip() {}

    public static void register() {
        ItemTooltipCallback.EVENT.register((stack, context, flag, lines) -> {
            if (!(stack.getItem() instanceof BowItem)) return;
            BowStats stats = BowStats.of(stack);

            lines.add(Component.empty());
            lines.add(Component.translatable(KEY + "drawn").withStyle(ChatFormatting.GRAY));
            lines.add(stat("draw_time", stats.drawTicks() / TICKS_PER_SECOND));
            lines.add(stat("arrow_speed", stats.velocityMultiplier()));
        });
    }

    private static Component stat(String key, double value) {
        return Component.literal(" ").append(Component.translatable(KEY + key,
            ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(value))).withStyle(ChatFormatting.DARK_GREEN);
    }
}
