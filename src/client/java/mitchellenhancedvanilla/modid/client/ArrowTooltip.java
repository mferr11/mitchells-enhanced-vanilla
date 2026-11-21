package mitchellenhancedvanilla.modid.client;

import mitchellenhancedvanilla.modid.MitchellEnhancedVanilla;
import mitchellenhancedvanilla.modid.ModItems;
import mitchellenhancedvanilla.modid.entity.AmethystArrowEntity;
import mitchellenhancedvanilla.modid.item.ModArrowItem;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;

// Sword-style damage section on arrow tooltips
public final class ArrowTooltip {
    private static final String KEY = "tooltip." + MitchellEnhancedVanilla.MOD_ID + ".arrow.";

    private ArrowTooltip() {}

    public static void register() {
        ItemTooltipCallback.EVENT.register((stack, context, flag, lines) -> {
            if (!(stack.getItem() instanceof ArrowItem)) return;

            double baseDamage = stack.getItem() instanceof ModArrowItem arrow ? arrow.baseDamage() : ModArrowItem.VANILLA_BASE_DAMAGE;

            lines.add(Component.empty());
            lines.add(Component.translatable(KEY + "fired").withStyle(ChatFormatting.GRAY));
            lines.add(stat("damage", baseDamage));
            if (stack.is(ModItems.AMETHYST_ARROW)) {
                lines.add(stat("shrapnel", AmethystArrowEntity.SHRAPNEL_DAMAGE));
            }
        });
    }

    private static Component stat(String key, double value) {
        return Component.literal(" ").append(Component.translatable(KEY + key,
            ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(value))).withStyle(ChatFormatting.DARK_GREEN);
    }
}
