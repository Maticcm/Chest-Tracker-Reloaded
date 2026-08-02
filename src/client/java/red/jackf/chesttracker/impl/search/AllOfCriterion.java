package red.jackf.chesttracker.impl.search;

import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Acts as an 'AND' gate for a list of criteria.
 *
 * <p>Adapted from Where Is It's {@code AllOfCriterion}.</p>
 */
public class AllOfCriterion implements Criterion {
    final List<Criterion> criteria = new ArrayList<>();

    public AllOfCriterion(Collection<Criterion> criteria) {
        this.criteria.addAll(criteria);
    }

    /**
     * Flattens the AND condition if just one criterion is specified.
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
        return criteria.stream().allMatch(c -> c.test(stack));
    }

    @Override
    public String toString() {
        return "AllOfCriterion{criteria=" + criteria + '}';
    }
}
