package red.jackf.chesttracker.impl.search;

import red.jackf.chesttracker.api.providers.ProviderUtils;
import red.jackf.chesttracker.impl.memory.MemoryBankAccessImpl;

/**
 * Serves search results out of the currently loaded memory bank.
 *
 * <p>This was previously {@code ChestTrackerWhereIsItPlugin}, registered through Where Is It's
 * {@code whereisit_client} entrypoint. With the search layer absorbed there is no external mod to
 * plug into, so it is now registered directly during client setup.</p>
 */
public final class MemoryBankSearchHandler {
    private MemoryBankSearchHandler() {}

    public static void setup() {
        SearchInvoker.EVENT.register((request, resultConsumer) -> {
            var currentKey = ProviderUtils.getPlayersCurrentKey();
            if (currentKey.isEmpty())
                return false;

            var bank = MemoryBankAccessImpl.INSTANCE.getLoadedInternal();
            if (bank.isEmpty())
                return false;

            var results = bank.get().doSearch(currentKey.get(), request);
            if (!results.isEmpty())
                resultConsumer.accept(results);
            return true;
        });
    }
}
