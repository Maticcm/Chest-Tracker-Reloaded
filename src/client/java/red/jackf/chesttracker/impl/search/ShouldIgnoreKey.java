package red.jackf.chesttracker.impl.search;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;

import java.util.Arrays;

/**
 * Checks whether a key press should be ignored because a text field has focus.
 *
 * <p>Adapted from Where Is It's {@code ShouldIgnoreKey}, with its vanilla defaults folded in.</p>
 */
public interface ShouldIgnoreKey {
    Event<ShouldIgnoreKey> EVENT = EventFactory.createArrayBacked(ShouldIgnoreKey.class, listeners -> () ->
            Arrays.stream(listeners).anyMatch(ShouldIgnoreKey::shouldIgnoreKey)
    );

    /**
     * Registers the built-in handlers. Called once during client setup.
     */
    static void setupDefaults() {
        EVENT.register(() -> {
            var screen = Minecraft.getInstance().gui.screen();
            if (screen == null) return false;
            if (screen instanceof CreativeModeInventoryScreen creativeScreen) {
                return creativeScreen.searchBox.canConsumeInput();
            } else if (screen.getFocused() instanceof EditBox editBox) {
                return editBox.canConsumeInput();
            }
            return false;
        });
    }

    /**
     * @return If a key press should be ignored at this moment.
     */
    boolean shouldIgnoreKey();
}
