package mitchellenhancedvanilla.modid.block;

import mitchellenhancedvanilla.modid.MitchellEnhancedVanilla;
import mitchellenhancedvanilla.modid.ModBlocks;
import mitchellenhancedvanilla.modid.block.entity.ModChestBlockEntity;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

// Single-only chest with its own texture; a locked one (Brood) stays shut and unbreakable until the player has beaten the Barrow King
public class ModChestBlock extends ChestBlock {
    private static final Identifier DEFEAT_ADVANCEMENT = MitchellEnhancedVanilla.id("adventure/defeat_barrow_king");

    // Under textures/entity/chest/
    private final Identifier texture;
    private final boolean locked;

    public ModChestBlock(Identifier texture, boolean locked, Properties properties) {
        super(() -> ModBlocks.MOD_CHEST_ENTITY, SoundEvents.CHEST_OPEN, SoundEvents.CHEST_CLOSE, properties);
        this.texture = texture;
        this.locked = locked;
    }

    public Identifier texture() {
        return texture;
    }

    public boolean locked() {
        return locked;
    }

    // Whether this player is kept out of it
    public boolean isLockedFor(Player player) {
        return locked && !isUnlockedFor(player);
    }

    // Its own "<block key>.locked" lang line over the hotbar, with vanilla's locked-chest sound
    public void sendLockedMessage(Player player, BlockPos pos) {
        player.sendOverlayMessage(Component.translatable(getDescriptionId() + ".locked"));
        player.level().playSound(null, pos, SoundEvents.CHEST_LOCKED, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    // Never pairs into a double chest (the textures are single-only)
    @Override
    public boolean chestCanConnectTo(BlockState blockState) {
        return false;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos worldPosition, BlockState blockState) {
        return new ModChestBlockEntity(worldPosition, blockState);
    }

    // Server-side only; creative players always pass
    public static boolean isUnlockedFor(Player player) {
        if (player.isCreative()) return true;
        if (!(player instanceof ServerPlayer serverPlayer)) return false;
        AdvancementHolder advancement = serverPlayer.level().getServer().getAdvancements().get(DEFEAT_ADVANCEMENT);
        return advancement != null && serverPlayer.getAdvancements().getOrStartProgress(advancement).isDone();
    }

    // Locked chests can't be broken open either
    public static void registerBreakLock() {
        PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, blockEntity) -> {
            if (!(state.getBlock() instanceof ModChestBlock chestBlock) || !chestBlock.isLockedFor(player)) return true;
            chestBlock.sendLockedMessage(player, pos);
            return false;
        });
    }
}
