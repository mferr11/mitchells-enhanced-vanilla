package mitchellenhancedvanilla.modid.client;

import mitchellenhancedvanilla.modid.ModItems;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

// Barrow King items: an effect line for the shield; lore plus a usage hint for the crown ("<item key>.desc" / ".hint")
public final class BarrowTooltip {
    private BarrowTooltip() {}

    public static void register() {
        ItemTooltipCallback.EVENT.register((stack, context, flag, lines) -> {
            if (lines.isEmpty()) return;
            // Keys built only for these two items, since this runs for every tooltip
            if (stack.is(ModItems.KINGS_SHIELD)) {
                String key = stack.getItem().getDescriptionId();
                lines.add(1, Component.translatable(key + ".desc").withStyle(ChatFormatting.GRAY));
            } else if (stack.is(ModItems.TARNISHED_CROWN)) {
                String key = stack.getItem().getDescriptionId();
                lines.add(1, Component.translatable(key + ".desc").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
                // How to use it, quieter than the lore
                lines.add(2, Component.translatable(key + ".hint").withStyle(ChatFormatting.GRAY));
            }
        });
    }
}
