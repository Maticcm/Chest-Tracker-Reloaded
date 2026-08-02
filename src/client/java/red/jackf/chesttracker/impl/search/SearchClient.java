package red.jackf.chesttracker.impl.search;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import red.jackf.chesttracker.impl.ChestTracker;
import red.jackf.chesttracker.impl.search.render.HighlightRenderer;

import java.util.Collection;

/**
 * Client-side entry point for the absorbed search/highlight layer.
 *
 * <p>Adapted from Where Is It's {@code red.jackf.whereisit.client.WhereIsItClient}, reduced to the
 * parts ChestTracker uses. Where Is It's own keybind handling, config screen, recipe-viewer plugins
 * and server networking are not carried over.</p>
 */
public class SearchClient {
    private SearchClient() {}

    /** How long after a highlight has fully faded before its results are discarded. */
    private static final int POST_FADEOUT_GRACE_TICKS = 20 * 20;

    /**
     * Whether the current search closed a screen when it began. Read by the memory bank screen so a
     * search triggered from the GUI does not immediately reopen it.
     */
    public static boolean closedScreenThisSearch = false;

    /**
     * Registers the built-in handlers for the search layer. Called once during client init.
     */
    public static void setup() {
        ConnectedBlocksGrabber.setupDefaults();
        NestedItemsGrabber.setupDefaults();
        ShouldIgnoreKey.setupDefaults();
        SearchRequestPopulator.setupDefaults();
        MemoryBankSearchHandler.setup();
        HighlightRenderer.setup();

        // Drives the highlight fade. Without this the tick counter never advances, so the fade
        // progress stays at 0 - and the box easing function returns a scale of exactly 0 at
        // progress 0, meaning results render at zero size and are invisible.
        ClientTickEvents.START_LEVEL_TICK.register(level -> {
            HighlightRenderer.incrementTicksSinceSearch();

            // Drop stale results a while after they have finished fading out.
            if (HighlightRenderer.getTicksSinceSearch()
                    > SearchConfig.fadeoutTimeTicks() + POST_FADEOUT_GRACE_TICKS) {
                HighlightRenderer.clearResults();
            }
        });
    }

    /**
     * Runs a search request through all registered invokers and shows the results.
     *
     * @return whether any invoker started a search.
     */
    public static boolean doSearch(SearchRequest request) {
        if (!request.hasCriteria()) return false;

        HighlightRenderer.clearResults();
        HighlightRenderer.setLastRequest(request);
        HighlightRenderer.resetSearchTime();

        closedScreenThisSearch = false;

        boolean started = SearchInvoker.EVENT.invoker().search(request, SearchClient::receiveResults);

        if (!started) {
            ChestTracker.LOGGER.debug("No search handler accepted request {}", request);
        }

        return started;
    }

    /**
     * Accept a batch of results and start showing them.
     *
     * <p>Closes the open screen on the first batch, so the highlighted containers are actually
     * visible. Matches Where Is It's {@code closeGuiOnFoundResults} behaviour.</p>
     */
    public static void receiveResults(Collection<SearchResult> results) {
        if (results.isEmpty()) return;

        if (SearchConfig.closeGuiOnFoundResults() && !closedScreenThisSearch) {
            closeScreenForSearch();
        }

        HighlightRenderer.addResults(results);
    }

    /**
     * Closes the current screen, if one is open, and records that it was closed by a search.
     */
    public static void closeScreenForSearch() {
        Minecraft client = Minecraft.getInstance();
        if (client.gui.screen() != null && client.player != null) {
            closedScreenThisSearch = true;
            // closeContainer() rather than setScreen(null): it also tells the server the container
            // was closed, so the server does not think the player still has it open.
            client.player.closeContainer();
        }
    }
}
