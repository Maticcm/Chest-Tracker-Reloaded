package red.jackf.chesttracker.impl.search;

import com.google.common.collect.Lists;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ItemContainerContents;
import red.jackf.chesttracker.api.EventPhases;

import java.util.List;
import java.util.stream.Stream;

/**
 * Recursively gets all items directly contained within another item - shulker boxes, bundles, and so
 * on. Not meant for indirect storage such as ender pouches.
 *
 * <p>Adapted from Where Is It's {@code red.jackf.whereisit.api.search.NestedItemsGrabber}, with its
 * container and bundle handlers folded in.</p>
 */
public interface NestedItemsGrabber {
    Event<NestedItemsGrabber> EVENT = EventFactory.createWithPhases(NestedItemsGrabber.class, listeners -> stack -> {
        List<ItemStack> result = Lists.newArrayList();

        for (NestedItemsGrabber listener : listeners) {
            listener.grab(stack).forEach(nested -> {
                result.add(nested);
                result.addAll(get(nested).toList());
            });
        }

        return result.stream();
    }, EventPhases.PRIORITY_PHASE, EventPhases.DEFAULT_PHASE, EventPhases.FALLBACK_PHASE);

    /**
     * Get a stream of all items directly contained within this item.
     */
    static Stream<ItemStack> get(ItemStack source) {
        return EVENT.invoker().grab(source);
    }

    /**
     * Registers the built-in handlers. Called once during client setup.
     */
    static void setupDefaults() {
        // Shulker boxes and other container items
        EVENT.register(source -> {
            ItemContainerContents container = source.get(DataComponents.CONTAINER);
            if (container == null) return Stream.empty();
            return container.nonEmptyItemCopyStream();
        });

        // Bundles
        EVENT.register(source -> {
            BundleContents contents = source.get(DataComponents.BUNDLE_CONTENTS);
            if (contents == null) return Stream.empty();
            return contents.itemCopyStream().filter(stack -> !stack.isEmpty());
        });
    }

    /**
     * Pulls a stream of item stacks from a source stack. Returns {@link Stream#empty()} if none are
     * contained.
     */
    Stream<ItemStack> grab(ItemStack source);
}
