package red.jackf.chesttracker.impl.search;

import net.minecraft.client.Minecraft;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import red.jackf.chesttracker.impl.ChestTracker;

import java.util.ArrayList;
import java.util.function.Consumer;

/**
 * Takes a screen and mouse position and attempts to add search criteria to a request.
 *
 * <p>Adapted from Where Is It's {@code SearchRequestPopulator}. The recipe-book handler and recipe
 * viewer (JEI/REI/EMI) plugins are not carried over - those were Where Is It features.</p>
 */
public interface SearchRequestPopulator {
    Event<SearchRequestPopulator> EVENT = EventFactory.createArrayBacked(SearchRequestPopulator.class,
            listeners -> (request, screen, mouseX, mouseY) -> {
                for (SearchRequestPopulator listener : listeners) {
                    try {
                        listener.grabStack(request, screen, mouseX, mouseY);
                        if (request.hasCriteria()) break;
                    } catch (Exception ex) {
                        ChestTracker.LOGGER.error("Error populating from stack, class {}",
                                listener.getClass().getName(), ex);
                    }
                }
            });

    /**
     * Registers the built-in handler, which reads the hovered slot of a container screen.
     */
    static void setupDefaults() {
        EVENT.register((request, screen, mouseX, mouseY) -> {
            if (screen instanceof AbstractContainerScreen<?> containerScreen && containerScreen.hoveredSlot != null) {
                var stack = containerScreen.hoveredSlot.getItem();
                if (!stack.isEmpty()) {
                    addItemStack(request, stack, Context.inventory());
                }
            }
        });
    }

    /**
     * Add search criteria from a given screen.
     */
    void grabStack(SearchRequest request, Screen screen, int mouseX, int mouseY);

    /**
     * Adds an ItemStack to a given request. Components are ignored unless the context asks for
     * precision.
     */
    static void addItemStack(Consumer<Criterion> consumer, ItemStack stack, Context context) {
        ChestTracker.LOGGER.debug("Adding {}, context: {}", stack, context);
        var criteria = new ArrayList<Criterion>();

        criteria.add(Criteria.item(stack.getItem()));

        if (context == Context.INVENTORY_PRECISE) {
            criteria.add(Criteria.components(stack.getComponentsPatch()));
        } else if (context == Context.FAVOURITE) {
            if (stack.has(DataComponents.CUSTOM_NAME))
                criteria.add(Criteria.name(stack.getHoverName().getString()));

            ItemEnchantments enchantments = stack.get(EnchantmentHelper.getComponentType(stack));
            if (enchantments != null) {
                for (Object2IntMap.Entry<Holder<Enchantment>> entry : enchantments.entrySet()) {
                    criteria.add(Criteria.enchantment(entry.getKey(), entry.getIntValue()));
                }
            }

            PotionContents potion = stack.get(DataComponents.POTION_CONTENTS);
            if (potion != null && potion.potion().isPresent()) {
                criteria.add(Criteria.potion(potion.potion().get().value()));
            }
        }

        consumer.accept(new AllOfCriterion(criteria).compact());
    }

    enum Context {
        /**
         * Tangible item in an inventory. Only uses the item ID.
         */
        INVENTORY,
        /**
         * Tangible item in an inventory, with precision because the user is holding shift.
         * Additionally matches an exact component patch.
         */
        INVENTORY_PRECISE,
        /**
         * An ingredient in a recipe display. Uses an item ID or relevant tags.
         */
        RECIPE,
        /**
         * An item the player specifically remembers, such as ChestTracker's remembered items. Adds
         * selective criteria such as enchantments, name and potions, but not general components, so
         * that e.g. a shulker box or pickaxe can be favourited.
         */
        FAVOURITE;

        public static Context inventory() {
            return Minecraft.getInstance().hasShiftDown() ? INVENTORY_PRECISE : INVENTORY;
        }
    }
}
