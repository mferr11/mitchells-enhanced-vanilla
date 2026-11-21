package mitchellenhancedvanilla.modid.client.model;

import com.mojang.serialization.MapCodec;
import mitchellenhancedvanilla.modid.MitchellEnhancedVanilla;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperties;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

// Item model property: ID of the ammo the bow would fire
public record BowAmmoProperty() implements SelectItemModelProperty<Identifier> {
    public static final Identifier ID = MitchellEnhancedVanilla.id("bow_ammo");
    public static final SelectItemModelProperty.Type<BowAmmoProperty, Identifier> TYPE =
        SelectItemModelProperty.Type.create(MapCodec.unit(new BowAmmoProperty()), Identifier.CODEC);

    private static final Identifier NONE = BuiltInRegistries.ITEM.getKey(Items.AIR);

    public static void register() {
        SelectItemModelProperties.ID_MAPPER.put(ID, TYPE);
    }

    @Override
    public Identifier get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed, ItemDisplayContext context) {
        // Other players' inventories aren't synced, so they get the default
        if (!(entity instanceof Player player)) return NONE;
        ItemStack ammo = player.getProjectile(stack);
        return ammo.isEmpty() ? NONE : BuiltInRegistries.ITEM.getKey(ammo.getItem());
    }

    @Override
    public com.mojang.serialization.Codec<Identifier> valueCodec() {
        return Identifier.CODEC;
    }

    @Override
    public SelectItemModelProperty.Type<BowAmmoProperty, Identifier> type() {
        return TYPE;
    }
}
