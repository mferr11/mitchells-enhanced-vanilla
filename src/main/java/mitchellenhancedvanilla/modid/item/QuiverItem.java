package mitchellenhancedvanilla.modid.item;

import java.util.Optional;

import mitchellenhancedvanilla.modid.quiver.QuiverTooltip;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.component.BundleContents;
import org.apache.commons.lang3.math.Fraction;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.ItemStack;

// A bundle that only holds arrows
public class QuiverItem extends BundleItem {
    public static final int CAPACITY = 4; // in stacks
    private static final int BAR_COLOR = ARGB.colorFromFloat(1.0F, 0.44F, 0.53F, 1.0F);      // vanilla bundle
    private static final int FULL_BAR_COLOR = ARGB.colorFromFloat(1.0F, 1.0F, 0.33F, 0.33F);

    public QuiverItem(Properties properties) {
        super(properties);
    }

    public static boolean canHold(ItemStack stack) {
        return stack.is(ItemTags.ARROWS);
    }

    // 0 to 1 of the quiver's capacity
    private static Fraction fullness(ItemStack quiver) {
        BundleContents contents = quiver.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
        return contents.weight().result().orElse(Fraction.ZERO).divideBy(Fraction.getFraction(CAPACITY, 1));
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.min(1 + Mth.mulAndTruncate(fullness(stack), 12), 13);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return fullness(stack).compareTo(Fraction.ONE) >= 0 ? FULL_BAR_COLOR : BAR_COLOR;
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        return super.getTooltipImage(stack)
            .map(tooltip -> new QuiverTooltip(stack.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY)));
    }

    // Quiver carried, clicked onto a slot
    @Override
    public boolean overrideStackedOnOther(ItemStack quiver, Slot slot, ClickAction action, Player player) {
        ItemStack target = slot.getItem();
        if (action == ClickAction.PRIMARY && !target.isEmpty() && !canHold(target)) {
            return reject(player);
        }
        return super.overrideStackedOnOther(quiver, slot, action, player);
    }

    // Something carried, clicked onto the quiver
    @Override
    public boolean overrideOtherStackedOnMe(ItemStack quiver, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess carried) {
        if (action == ClickAction.PRIMARY && !other.isEmpty() && !canHold(other)) {
            return reject(player);
        }
        return super.overrideOtherStackedOnMe(quiver, other, slot, action, player, carried);
    }

    private static boolean reject(Player player) {
        player.playSound(SoundEvents.BUNDLE_INSERT_FAIL, 1.0F, 1.0F);
        return true;
    }
}
