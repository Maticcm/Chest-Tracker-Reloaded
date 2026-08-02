package red.jackf.chesttracker.impl.search;

import com.google.common.collect.Lists;
import net.minecraft.world.item.ItemStack;
import red.jackf.chesttracker.impl.ChestTracker;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Represents a request to search for an item.
 *
 * <p>Adapted from Where Is It's {@code red.jackf.whereisit.api.SearchRequest}, with the codec/NBT
 * serialisation dropped - that only existed so requests could be sent to a server, which ChestTracker
 * never does.</p>
 */
public class SearchRequest implements Consumer<Criterion> {
    private final List<Criterion> criteria;

    public SearchRequest() {
        this.criteria = new ArrayList<>();
    }

    public SearchRequest(List<Criterion> criteria) {
        this.criteria = Lists.newArrayList(criteria);
    }

    /**
     * Perform a check on an ItemStack with the given request. Use this method rather than testing
     * directly, as it correctly handles nested items (shulker boxes, bundles).
     *
     * @param stack   ItemStack to test against
     * @param request Search request to test with
     * @return Whether this ItemStack, or any sub-item if applicable, matches the request
     */
    public static boolean check(ItemStack stack, SearchRequest request) {
        if (request.test(stack)) return true;

        if (SearchConfig.nestedSearch()) {
            return NestedItemsGrabber.get(stack).anyMatch(request::test);
        }

        return false;
    }

    /**
     * @return Whether this request has any criteria.
     */
    public boolean hasCriteria() {
        return !criteria.isEmpty();
    }

    /**
     * Adds a new criterion to this request. The criterion is checked for validity before being added.
     *
     * @param criterion The criterion to add to this request
     */
    @Override
    public void accept(Criterion criterion) {
        if (criterion.valid()) {
            if (criterion instanceof AllOfCriterion allOf) {
                criteria.addAll(allOf.criteria);
            } else {
                this.criteria.add(criterion);
            }
        } else {
            ChestTracker.LOGGER.warn("Invalid criterion: {}", criterion);
        }
    }

    /**
     * Test all of this request's criteria against a given ItemStack. Returns false if any criterion
     * fails. If there are no criteria, returns true.
     *
     * @param stack Stack to test against
     * @return If no criteria fail against the ItemStack.
     */
    private boolean test(ItemStack stack) {
        for (Criterion criterion : criteria)
            if (!criterion.test(stack)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "SearchRequest[" + criteria.stream().map(Criterion::toString).collect(Collectors.joining(", ")) + "]";
    }
}
