package red.jackf.chesttracker.impl.search;

import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * Acts as an 'OR' gate for a list of criteria. Build one, then (recommended) call {@link #compact()}
 * before adding it to a search request.
 *
 * <p>Adapted from Where Is It's {@code AnyOfCriterion}.</p>
 */
public class AnyOfCriterion implements Criterion, Consumer<Criterion> {
    private final List<Criterion> criteria = new ArrayList<>();

    public AnyOfCriterion() {}

    public AnyOfCriterion(Collection<Criterion> criteria) {
        this.criteria.addAll(criteria);
    }

    /**
     * Flattens the OR condition if just one criterion is specified.
     */
    @Override
    public Criterion compact() {
        if (criteria.size() == 1) return criteria.get(0);
        return this;
    }

    @Override
    public boolean valid() {
        return !criteria.isEmpty() && criteria.stream().allMatch(Criterion::valid);
    }

    @Override
    public boolean test(ItemStack stack) {
        return criteria.stream().anyMatch(c -> c.test(stack));
    }

    @Override
    public void accept(Criterion criterion) {
        this.criteria.add(criterion);
    }

    @Override
    public String toString() {
        return "AnyOfCriterion{criteria=" + criteria + '}';
    }
}
