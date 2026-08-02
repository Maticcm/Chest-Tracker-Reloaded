package red.jackf.chesttracker.impl;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.api.ChestTrackerPlugin;
import red.jackf.chesttracker.api.gui.ScreenBlacklist;
import red.jackf.chesttracker.impl.config.ChestTrackerConfig;
import red.jackf.chesttracker.impl.gui.DeveloperOverlay;
import red.jackf.chesttracker.impl.gui.invbutton.ButtonPositionMap;
import red.jackf.chesttracker.impl.gui.invbutton.CTButtonScreenDuck;
import red.jackf.chesttracker.impl.gui.invbutton.InventoryButtonFeature;
import red.jackf.chesttracker.impl.gui.screen.ChestTrackerScreen;
import red.jackf.chesttracker.impl.gui.util.CTTitleOverrideDuck;
import red.jackf.chesttracker.impl.gui.util.ImagePixelReader;
import red.jackf.chesttracker.impl.memory.MemoryBankAccessImpl;
import red.jackf.chesttracker.impl.memory.MemoryIntegrity;
import red.jackf.chesttracker.impl.memory.MemoryKeyImpl;
import red.jackf.chesttracker.impl.memory.key.OverrideInfo;
import red.jackf.chesttracker.impl.providers.InteractionTrackerImpl;
import red.jackf.chesttracker.impl.providers.ProviderHandler;
import red.jackf.chesttracker.impl.providers.ScreenCloseContextImpl;
import red.jackf.chesttracker.impl.providers.ScreenOpenContextImpl;
import red.jackf.chesttracker.impl.rendering.NameRenderer;
import red.jackf.chesttracker.impl.storage.ConnectionSettings;
import red.jackf.chesttracker.impl.storage.Storage;
import red.jackf.chesttracker.impl.search.ShouldIgnoreKey;

import java.util.Optional;

public class ChestTracker implements ClientModInitializer {
    public static final String ID = "chesttracker";

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(ID, path);
    }

    public static final Logger LOGGER = LogManager.getLogger();

    private static boolean shouldSkipProviderForNextGuiClose = false;

    public static Logger getLogger(String suffix) {
        return LogManager.getLogger(ChestTracker.class.getCanonicalName() + "/" + suffix);
    }

    /**
     * 26.x replaced the plain string category with a registered {@link KeyMapping.Category}.
     * The translation key for the label is derived from this id.
     */
    public static final KeyMapping.Category KEY_CATEGORY = KeyMapping.Category.register(id("main"));

    public static final KeyMapping OPEN_GUI = KeyMappingHelper.registerKeyMapping(
            new KeyMapping("key.chesttracker.open_gui", InputConstants.Type.KEYSYM, InputConstants.KEY_GRAVE, KEY_CATEGORY)
    );

    public static void openInGame(Minecraft client, @Nullable Screen parent) {
        client.gui.setScreen(new ChestTrackerScreen(parent));
    }

    public static void skipProviderForNextGuiClose() {
        shouldSkipProviderForNextGuiClose = true;
    }

    @Override
    public void onInitializeClient() {
        ChestTrackerConfig.init();
        LOGGER.debug("Loading ChestTracker");

        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            // opening Chest Tracker GUI with no screen open
            if (client.gui.screen() == null && client.gui.overlay() == null)
                while (OPEN_GUI.consumeClick())
                    openInGame(client, null);
        });

        ClientTickEvents.START_LEVEL_TICK.register(ignored -> MemoryBankAccessImpl.INSTANCE.getLoadedInternal().ifPresent(bank -> {
            bank.getMetadata().incrementLoadedTime();
        }));

        ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof AbstractContainerScreen<?> containerScreen) {
                ProviderHandler.INSTANCE.getCurrentProvider().ifPresent(provider -> {
                    ScreenOpenContextImpl openContext = ScreenOpenContextImpl.createFor(containerScreen);

                    provider.onScreenOpen(openContext);

                    ((CTButtonScreenDuck) containerScreen).chesttracker$setContext(openContext);

                    if (ChestTrackerConfig.INSTANCE.instance().gui.useCustomNameInGUIs) {
                        MemoryBankAccessImpl.INSTANCE.getLoadedInternal().ifPresent(bank -> {
                            if (openContext.getTarget() != null) {
                                Optional<MemoryKeyImpl> key = bank.getKeyInternal(openContext.getTarget().memoryKey());
                                if (key.isPresent()) {
                                    OverrideInfo override = key.get().overrides().get(openContext.getTarget().position());
                                    if (override != null) {
                                        if (override.getCustomName() != null) {
                                            ((CTTitleOverrideDuck) containerScreen).chesttracker$setTitleOverride(Component.literal(override.getCustomName()));
                                        } else {
                                            ((CTTitleOverrideDuck) containerScreen).chesttracker$clearTitleOverride();
                                        }
                                    } else {
                                        ((CTTitleOverrideDuck) containerScreen).chesttracker$clearTitleOverride();
                                    }
                                }
                            }
                        });
                    }
                });
            }
        });

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (Minecraft.getInstance().level == null) return;
            if (screen instanceof AbstractContainerScreen<?>) {
                // opening Chest Tracker GUI with a screen open
                ScreenKeyboardEvents.afterKeyPress(screen).register((parent, keyEvent) -> {
                    // don't search in search bars, etc
                    if (ShouldIgnoreKey.EVENT.invoker().shouldIgnoreKey()) {
                        return;
                    }

                    if (OPEN_GUI.matches(keyEvent)) {
                        openInGame(client, parent);
                    }
                });

                InventoryButtonFeature.onScreenOpen(client, screen, scaledWidth, scaledHeight);

                // counting items after screen close
                if (!ScreenBlacklist.isBlacklisted(screen.getClass()))
                    ScreenEvents.remove(screen).register(screen1 -> {
                        if (!shouldSkipProviderForNextGuiClose) {
                            ProviderHandler.INSTANCE.getCurrentProvider().ifPresent(provider -> {
                                provider.onScreenClose(ScreenCloseContextImpl.createFor((AbstractContainerScreen<?>) screen1));
                            });
                            InteractionTrackerImpl.INSTANCE.clear();
                        } else {
                            shouldSkipProviderForNextGuiClose = false;
                        }
                    });
                else
                    LOGGER.debug("Blacklisted screen class, ignoring");
            }
        });

        red.jackf.chesttracker.impl.search.SearchClient.setup();
        InventoryButtonFeature.setup();

        // auto add placed blocks with data, such as shulker boxes
        ProviderHandler.INSTANCE.setupEvents();
        NameRenderer.setup();
        InteractionTrackerImpl.setup();
        MemoryIntegrity.setup();
        ImagePixelReader.setup();
        Storage.setup();
        DeveloperOverlay.setup();
        ConnectionSettings.load();
        ButtonPositionMap.loadUserPositions();

        for (EntrypointContainer<ChestTrackerPlugin> container : FabricLoader.getInstance().getEntrypointContainers("chesttracker", ChestTrackerPlugin.class)) {
            LOGGER.debug("Loading entrypoint from mod {}", container.getProvider().getMetadata().getId());
            container.getEntrypoint().load();
        }
    }
}
