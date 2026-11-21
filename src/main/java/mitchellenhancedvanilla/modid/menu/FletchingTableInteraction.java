package mitchellenhancedvanilla.modid.menu;

import mitchellenhancedvanilla.modid.MitchellEnhancedVanilla;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.block.Blocks;

// Right-click opens the fletching menu
public final class FletchingTableInteraction {
    private static final Component TITLE =
        Component.translatable("container." + MitchellEnhancedVanilla.MOD_ID + ".fletching_table");

    private FletchingTableInteraction() {}

    public static void register() {
        UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
            BlockPos pos = hit.getBlockPos();
            if (!level.getBlockState(pos).is(Blocks.FLETCHING_TABLE) || player.isSpectator()) {
                return InteractionResult.PASS;
            }
            // Sneak + item places it, like other workstations
            if (player.isSecondaryUseActive() && !player.getItemInHand(hand).isEmpty()) {
                return InteractionResult.PASS;
            }

            if (!level.isClientSide()) {
                player.openMenu(new SimpleMenuProvider(
                    (containerId, inventory, p) -> new FletchingTableMenu(containerId, inventory, ContainerLevelAccess.create(level, pos)),
                    TITLE));
            }
            return InteractionResult.SUCCESS;
        });
    }
}
