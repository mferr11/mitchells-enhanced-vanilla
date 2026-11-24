package mitchellenhancedvanilla.modid.quiver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import mitchellenhancedvanilla.modid.MitchellEnhancedVanilla;
import mitchellenhancedvanilla.modid.ModItems;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.BundleContents;

// The player's equipped quiver, stored outside the vanilla inventory
public final class Quiver {
    public static final AttachmentType<ItemStack> SLOT = AttachmentRegistry.create(
        MitchellEnhancedVanilla.id("quiver"), builder -> builder
            .persistent(ItemStack.OPTIONAL_CODEC)
            .syncWith(ItemStack.OPTIONAL_STREAM_CODEC, AttachmentSyncPredicate.targetOnly())
            .copyOnDeath()); // emptied on death unless keepInventory, see InventoryMixin

    // Ammo handed out by getProjectile, so draw() knows to take it from the quiver
    private static final ThreadLocal<ItemStack> LAST_AMMO = new ThreadLocal<>();

    private Quiver() {}

    public static void initialize() {}

    public static ItemStack get(Player player) {
        return player.getAttachedOrElse(SLOT, ItemStack.EMPTY);
    }

    public static void set(Player player, ItemStack stack) {
        if (stack.isEmpty()) {
            player.removeAttached(SLOT);
        } else {
            player.setAttached(SLOT, stack);
        }
    }

    public static BundleContents contents(Player player) {
        ItemStack quiver = get(player);
        if (!quiver.is(ModItems.QUIVER)) return BundleContents.EMPTY;
        return quiver.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
    }

    // Arrow stack at this position in the quiver, or empty
    public static ItemStack peek(Player player, int index) {
        List<ItemStackTemplate> items = contents(player).items();
        return index < items.size() ? items.get(index).create() : ItemStack.EMPTY;
    }

    public static int count(Player player, ItemStack match) {
        int total = 0;
        for (ItemStackTemplate item : contents(player).items()) {
            ItemStack stack = item.create();
            if (ItemStack.isSameItemSameComponents(stack, match)) total += stack.getCount();
        }
        return total;
    }

    // First arrow the weapon accepts, as a tracked copy
    public static ItemStack takeAmmo(Player player, Predicate<ItemStack> supported) {
        for (ItemStackTemplate item : contents(player).items()) {
            ItemStack stack = item.create();
            if (supported.test(stack)) {
                LAST_AMMO.set(stack);
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    public static boolean isQuiverAmmo(ItemStack stack) {
        return LAST_AMMO.get() == stack;
    }

    public static void consume(Player player, ItemStack ammo, int amount) {
        List<ItemStackTemplate> items = new ArrayList<>(contents(player).items());
        for (int i = 0; i < items.size(); i++) {
            ItemStackTemplate item = items.get(i);
            if (!ItemStack.isSameItemSameComponents(item.create(), ammo)) continue;
            int left = item.count() - amount;
            if (left > 0) {
                items.set(i, item.withCount(left));
            } else {
                items.remove(i);
            }
            setContents(player, items);
            return;
        }
    }

    // Front arrow stack moves to the back
    public static void cycle(Player player) {
        List<ItemStackTemplate> items = new ArrayList<>(contents(player).items());
        if (items.size() < 2) return;
        Collections.rotate(items, -1);
        setContents(player, items);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.BUNDLE_INSERT, SoundSource.PLAYERS, 0.6F, 1.4F);
    }

    public static void dropOnDeath(Player player) {
        ItemStack quiver = get(player);
        if (quiver.isEmpty()) return;
        ItemEntity entity = player.createItemStackToDrop(quiver, true, false);
        if (entity != null) player.level().addFreshEntity(entity);
        set(player, ItemStack.EMPTY);
    }

    // Replaced rather than mutated so the attachment re-syncs
    private static void setContents(Player player, List<ItemStackTemplate> items) {
        ItemStack quiver = get(player).copy();
        quiver.set(DataComponents.BUNDLE_CONTENTS, new BundleContents(items));
        set(player, quiver);
    }
}
