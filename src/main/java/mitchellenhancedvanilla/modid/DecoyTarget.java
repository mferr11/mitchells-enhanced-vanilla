package mitchellenhancedvanilla.modid;

import net.minecraft.world.entity.Entity;

// Implemented on Mob by MobDecoyMixin: lets an entity pull a mob's attention away from its target
public interface DecoyTarget {
    // Distracts the mob for the given ticks
    void mev$distract(Entity decoy, int ticks);

    // True while a live decoy holds the mob's attention
    boolean mev$isDistracted();
}
