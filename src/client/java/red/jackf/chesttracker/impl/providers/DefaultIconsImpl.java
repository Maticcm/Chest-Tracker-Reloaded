package red.jackf.chesttracker.impl.providers;

import com.google.common.collect.Lists;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import red.jackf.chesttracker.api.memory.CommonKeys;
import red.jackf.chesttracker.api.providers.MemoryKeyIcon;

import java.util.List;

public class DefaultIconsImpl {
    // 26.x binds item components after mod client-init runs, so an ItemStack cannot be
    // created in a static initialiser - doing so throws "Components not bound yet".
    // The defaults are therefore built on first access instead.
    private static List<MemoryKeyIcon> icons = null;

    /**
     * Registrations queued before the icon list is first built. Needed because callers run during
     * client init, when ItemStacks cannot yet be constructed.
     */
    private static final List<Runnable> pendingRegistrations = Lists.newArrayList();

    /**
     * Queue an icon registration to run once item components are bound. The runnable is executed on
     * first access to the icon list.
     */
    public static void registerDeferred(Runnable registration) {
        if (icons != null) {
            registration.run();
        } else {
            pendingRegistrations.add(registration);
        }
    }

    private static List<MemoryKeyIcon> icons() {
        if (icons == null) {
            icons = Lists.newArrayList(
                    new MemoryKeyIcon(CommonKeys.ENDER_CHEST_KEY, Items.ENDER_CHEST.getDefaultInstance()),

                    new MemoryKeyIcon(CommonKeys.OVERWORLD, Items.GRASS_BLOCK.getDefaultInstance()),
                    new MemoryKeyIcon(CommonKeys.THE_NETHER, Items.NETHERRACK.getDefaultInstance()),
                    new MemoryKeyIcon(CommonKeys.THE_END, Items.END_STONE.getDefaultInstance())
            );

            // `icons` is assigned before draining, so re-entrant registerIcon* calls are safe.
            List<Runnable> queued = Lists.newArrayList(pendingRegistrations);
            pendingRegistrations.clear();
            queued.forEach(Runnable::run);
        }
        return icons;
    }

    public static List<MemoryKeyIcon> getDefaultIcons() {
        return icons().stream()
                .map(MemoryKeyIcon::copy)
                .toList();
    }


    public static void registerIcon(MemoryKeyIcon icon) {
        icons().add(icon);
    }

    public static void registerIconAbove(Identifier target, MemoryKeyIcon icon) {
        int targetIndex = 0;
        while (targetIndex < icons().size() && !icons().get(targetIndex).id().equals(target)) {
            targetIndex++;
        }
        if (targetIndex == icons().size()) targetIndex = 0;
        icons().add(targetIndex, icon);
    }

    public static void registerIconBelow(Identifier target, MemoryKeyIcon icon) {
        int targetIndex = 0;
        while (targetIndex < icons().size() && !icons().get(targetIndex).id().equals(target)) {
            targetIndex++;
        }
        icons().add(targetIndex + 1, icon);
    }
}
