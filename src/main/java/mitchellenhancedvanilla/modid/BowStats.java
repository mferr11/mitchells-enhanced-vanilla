package mitchellenhancedvanilla.modid;

public final class BowStats {
    /** Ticks to reach full draw. Vanilla is 20.
     *  If you change this, re-run scripts/generate_bow_arrows.py so the bow's pull animation in
     *  assets/minecraft/items/bow.json matches (the script reads this value). */
    public static final float DRAW_TICKS = 30.0F;

    private BowStats() {}
}
