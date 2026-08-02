package red.jackf.chesttracker.impl.search;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

import java.util.Collection;
import java.util.function.Consumer;

/**
 * Initiates a search for a given request against ChestTracker's memory bank.
 *
 * <p>Adapted from Where Is It's {@code SearchInvoker}. Where Is It also shipped a handler that asked
 * the server over a network channel; that is gone, as ChestTracker searches only local memory.</p>
 */
public interface SearchInvoker {
    Event<SearchInvoker> EVENT = EventFactory.createArrayBacked(SearchInvoker.class, listeners -> (request, resultConsumer) -> {
        boolean hasAnySucceeded = false;
        for (SearchInvoker invoker : listeners) {
            hasAnySucceeded |= invoker.search(request, resultConsumer);
        }
        return hasAnySucceeded;
    });

    /**
     * Initiates a search request with the default handling - showing a fading overlay over the
     * resulting positions.
     *
     * @param request Request to search with
     * @return Whether any search method succeeded in starting. True even if no items were found, so
     * long as a request was started.
     */
    @SuppressWarnings("UnusedReturnValue")
    static boolean doSearch(SearchRequest request) {
        return SearchClient.doSearch(request);
    }

    /**
     * Process a search request.
     *
     * @param request        Request to search using
     * @param resultConsumer Callback for successful results
     * @return if the request was successfully started; not necessarily finished.
     */
    boolean search(SearchRequest request, Consumer<Collection<SearchResult>> resultConsumer);
}
