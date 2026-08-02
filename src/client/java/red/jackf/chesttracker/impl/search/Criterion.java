package red.jackf.chesttracker.impl.search;

import net.minecraft.world.item.ItemStack;

/**
 * A test for an ItemStack.
 *
 * <p>Adapted from Where Is It's {@code red.jackf.whereisit.api.criteria.Criterion}. Where Is It needed
 * criteria to be registry-backed and codec-serialisable so they could be sent to a server for a
 * server-side search. ChestTracker only ever searches its own client-side memory bank, so the registry,
 * dispatch codecs and network plumbing have all been dropped - a criterion is just a predicate here.</p>
 */
public interface Criterion {
    /**
     * Test against a given ItemStack. Only called if {@link Criterion#valid()} returns true.
     *
     * @param stack Stack to test against
     * @return Whether this criterion matches the stack.
     */
    boolean test(ItemStack stack);

    /**
     * @return If this criterion has valid data.
     */
    default boolean valid() {
        return true;
    }

    /**
     * Returns a compacted version of this criterion. Used to flatten single-element groups.
     */
    default Criterion compact() {
        return this;
    }
}
