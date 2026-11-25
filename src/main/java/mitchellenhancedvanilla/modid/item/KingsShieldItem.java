package mitchellenhancedvanilla.modid.item;

import java.util.List;
import java.util.Optional;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.component.BlocksAttacks;

// The King's Shield, dropped by the Barrow King: tougher than a vanilla shield, and bashes back anything that hits it
public class KingsShieldItem extends ShieldItem {
    // Vanilla shield is 336
    public static final int DURABILITY = 672;
    // Shove given to a melee attacker that hits the raised shield (vanilla sword knockback is ~0.4)
    public static final double BASH_KNOCKBACK = 1.0;

    public KingsShieldItem(Properties properties) {
        super(properties);
    }

    // Same blocking behaviour as the vanilla shield, with more durability
    public static Item.Properties properties() {
        return new Item.Properties()
            .durability(DURABILITY)
            .rarity(Rarity.RARE)
            .repairable(Items.GOLD_INGOT)
            .equippableUnswappable(EquipmentSlot.OFFHAND)
            .delayedComponent(DataComponents.BLOCKS_ATTACKS, context -> new BlocksAttacks(
                0.25F,
                1.0F,
                List.of(new BlocksAttacks.DamageReduction(90.0F, Optional.empty(), 0.0F, 1.0F)),
                new BlocksAttacks.ItemDamageFunction(3.0F, 1.0F, 1.0F),
                Optional.of(context.getOrThrow(DamageTypeTags.BYPASSES_SHIELD)),
                Optional.of(SoundEvents.SHIELD_BLOCK),
                Optional.of(SoundEvents.SHIELD_BREAK)))
            .component(DataComponents.BREAK_SOUND, SoundEvents.SHIELD_BREAK);
    }

    // Called (via LivingEntityShieldBashMixin) when a melee hit lands on a raised King's Shield
    public static void bash(ServerLevel level, LivingEntity defender, LivingEntity attacker, DamageSource source) {
        shove(attacker, defender, BASH_KNOCKBACK, source);
        level.playSound(null, defender.getX(), defender.getY(), defender.getZ(), SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR, SoundSource.PLAYERS, 0.6F, 1.2F);
    }

    // Knocks `target` away from `from` (also used by the Barrow King's own shield)
    public static void shove(LivingEntity target, LivingEntity from, double strength, DamageSource source) {
        // knockback pushes away from the given direction, so point it from the target to the shover
        target.knockback(strength, from.getX() - target.getX(), from.getZ() - target.getZ(), source, 0.0F);
        // Players move client-side; this sends them their own motion too
        target.syncVelocity = true;
    }
}
