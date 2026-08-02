package red.jackf.chesttracker.impl.search;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.Objects;

/**
 * Built-in {@link Criterion} implementations.
 *
 * <p>Where Is It modelled each of these as a separate registry-backed, codec-serialisable class so
 * criteria could be shipped to a server. ChestTracker searches only its own client-side memory, so
 * they collapse into simple factory methods here.</p>
 */
public final class Criteria {
    private Criteria() {}

    /**
     * Matches any stack of the given item.
     */
    public static Criterion item(Item item) {
        return new Criterion() {
            @Override
            public boolean test(ItemStack stack) {
                return stack.is(item);
            }

            @Override
            public String toString() {
                return "Item[" + item + "]";
            }
        };
    }

    /**
     * Matches stacks whose component patch exactly equals the given one. Used for precise
     * (shift-held) inventory searches.
     */
    public static Criterion components(DataComponentPatch patch) {
        return new Criterion() {
            @Override
            public boolean test(ItemStack stack) {
                return Objects.equals(stack.getComponentsPatch(), patch);
            }

            @Override
            public String toString() {
                return "Components[" + patch + "]";
            }
        };
    }

    /**
     * Matches stacks whose hover name equals the given string.
     */
    public static Criterion name(String name) {
        return new Criterion() {
            @Override
            public boolean test(ItemStack stack) {
                return stack.getHoverName().getString().equals(name);
            }

            @Override
            public boolean valid() {
                return name != null && !name.isBlank();
            }

            @Override
            public String toString() {
                return "Name[" + name + "]";
            }
        };
    }

    /**
     * Matches stacks carrying at least the given level of the given enchantment.
     */
    public static Criterion enchantment(Holder<Enchantment> enchantment, int level) {
        return new Criterion() {
            @Override
            public boolean test(ItemStack stack) {
                ItemEnchantments enchantments = stack.get(EnchantmentHelper.getComponentType(stack));
                if (enchantments == null) return false;
                return enchantments.getLevel(enchantment) >= level;
            }

            @Override
            public String toString() {
                return "Enchantment[" + enchantment + " >= " + level + "]";
            }
        };
    }

    /**
     * Matches stacks containing the given potion.
     */
    public static Criterion potion(Potion potion) {
        return new Criterion() {
            @Override
            public boolean test(ItemStack stack) {
                var contents = stack.get(DataComponents.POTION_CONTENTS);
                if (contents == null || contents.potion().isEmpty()) return false;
                return contents.potion().get().value() == potion;
            }

            @Override
            public String toString() {
                return "Potion[" + potion + "]";
            }
        };
    }
}
