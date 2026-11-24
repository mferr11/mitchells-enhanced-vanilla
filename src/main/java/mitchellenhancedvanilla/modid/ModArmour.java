package mitchellenhancedvanilla.modid;

import java.util.Map;

import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorMaterials;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.resources.ResourceKey;

public final class ModArmour {
    // Starter armour, between leather (7 armour, x5 durability) and copper (10, x11)
    private static final ArmorMaterial VANILLA_CHAINMAIL = ArmorMaterials.CHAINMAIL;
    public static final ArmorMaterial CHAINMAIL = new ArmorMaterial(
        9,
        Map.of(
            ArmorType.HELMET, 1,
            ArmorType.CHESTPLATE, 3,
            ArmorType.LEGGINGS, 3,
            ArmorType.BOOTS, 1,
            ArmorType.BODY, 3),
        VANILLA_CHAINMAIL.enchantmentValue(),
        VANILLA_CHAINMAIL.equipSound(),
        VANILLA_CHAINMAIL.toughness(),
        VANILLA_CHAINMAIL.knockbackResistance(),
        VANILLA_CHAINMAIL.repairIngredient(),
        VANILLA_CHAINMAIL.assetId());

    // Worn textures: assets/<modid>/equipment/<name>.json
    public static final ResourceKey<EquipmentAsset> KINGS_CROWN_ASSET =
        ResourceKey.create(EquipmentAssets.ROOT_ID, MitchellEnhancedVanilla.id("kings_crown"));
    public static final ResourceKey<EquipmentAsset> TARNISHED_CROWN_ASSET =
        ResourceKey.create(EquipmentAssets.ROOT_ID, MitchellEnhancedVanilla.id("tarnished_crown"));

    // King's Crown: gold's armour and enchantability, with iron's durability
    private static final ArmorMaterial VANILLA_GOLD = ArmorMaterials.GOLD;
    private static final ArmorMaterial VANILLA_DIAMOND = ArmorMaterials.DIAMOND;
    public static final ArmorMaterial KINGS_CROWN = new ArmorMaterial(
        ArmorMaterials.IRON.durability(),
        VANILLA_DIAMOND.defense(),
        VANILLA_GOLD.enchantmentValue(),
        VANILLA_GOLD.equipSound(),
        VANILLA_GOLD.toughness(),
        VANILLA_GOLD.knockbackResistance(),
        VANILLA_GOLD.repairIngredient(),
        KINGS_CROWN_ASSET);

    private ModArmour() {}

    public static void register() {
        DefaultItemComponentEvents.MODIFY.register(ctx -> {
            setMaterial(ctx, Items.CHAINMAIL_HELMET, CHAINMAIL, ArmorType.HELMET);
            setMaterial(ctx, Items.CHAINMAIL_CHESTPLATE, CHAINMAIL, ArmorType.CHESTPLATE);
            setMaterial(ctx, Items.CHAINMAIL_LEGGINGS, CHAINMAIL, ArmorType.LEGGINGS);
            setMaterial(ctx, Items.CHAINMAIL_BOOTS, CHAINMAIL, ArmorType.BOOTS);
        });
    }

    // Armour points and durability; enchantability, sounds and repair are left as they are
    private static void setMaterial(DefaultItemComponentEvents.ModifyContext ctx, Item item, ArmorMaterial material, ArmorType type) {
        ctx.modify(item, builder -> builder
            .set(DataComponents.ATTRIBUTE_MODIFIERS, material.createAttributes(type))
            .set(DataComponents.MAX_DAMAGE, type.getDurability(material.durability())));
    }
}
