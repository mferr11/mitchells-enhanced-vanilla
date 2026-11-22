package mitchellenhancedvanilla.modid;

import net.minecraft.world.item.ItemStack;

// Per-bow draw time (ticks to full draw, vanilla 20), arrow speed multiplier (vanilla 1.0; damage scales with speed),
// arrow spread (vanilla 1.0) and full-draw zoom as a fraction of FOV removed (vanilla 0.15)
// Re-run scripts/generate_bow_arrows.py after changing a draw time
public record BowStats(float drawTicks, float velocityMultiplier, float inaccuracy, float zoom) {
    public static final BowStats WOODEN = new BowStats(30.0F, 0.5F, 1.0F, 0.15F);
    public static final BowStats IRON = new BowStats(20.0F, 0.75F, 1.0F, 0.3F);
    public static final BowStats GOLDEN = new BowStats(10.0F, 1.0F, 0.0F, 0.10F);
    public static final BowStats DIAMOND = new BowStats(20.0F, 1.25F, 0.5F, 0.6F);

    public static final int IRON_DURABILITY = 128;
    public static final int GOLDEN_DURABILITY = 64;
    public static final int DIAMOND_DURABILITY = 384;
    public static final int WOODEN_DURABILITY = 32;

    public static BowStats of(ItemStack bow) {
        if (bow.is(ModItems.IRON_BOW)) return IRON;
        if (bow.is(ModItems.GOLDEN_BOW)) return GOLDEN;
        if (bow.is(ModItems.DIAMOND_BOW)) return DIAMOND;
        return WOODEN;
    }

    // Vanilla's BowItem.getPowerForTime curve, stretched to this bow's draw time
    public float power(int ticks) {
        float f = ticks / drawTicks;
        f = (f * f + f * 2.0F) / 3.0F;
        return Math.min(f, 1.0F);
    }
}
