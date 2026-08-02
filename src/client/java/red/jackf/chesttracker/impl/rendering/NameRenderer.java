package red.jackf.chesttracker.impl.rendering;

import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.api.memory.Memory;
import red.jackf.chesttracker.api.memory.MemoryKey;
import red.jackf.chesttracker.api.providers.ProviderUtils;
import red.jackf.chesttracker.impl.config.ChestTrackerConfig;
import red.jackf.chesttracker.impl.memory.MemoryBankAccessImpl;
import red.jackf.chesttracker.impl.memory.MemoryBankImpl;
import red.jackf.chesttracker.impl.search.RenderUtils;

import java.util.Map;
import java.util.Set;

public class NameRenderer {
    public static void setup() {
        // Scheduled on START_MAIN rather than BEFORE_BLOCK_OUTLINE: the highlight renderer flushes
        // the scheduled-label queue on BEFORE_BLOCK_OUTLINE and registers first, so anything queued
        // there would be dropped. START_MAIN runs earlier in the frame.
        LevelRenderEvents.START_MAIN.register(context -> {
            if (ChestTrackerConfig.INSTANCE.instance().debug.disableContainerNames) return;

            MemoryBankAccessImpl.INSTANCE.getLoadedInternal().ifPresent(bank -> {
                if (bank.getMetadata().getCompatibilitySettings().nameRenderMode == NameRenderMode.DISABLED)
                    return;
                bank.getKey(ProviderUtils.getPlayersCurrentKey())
                        .ifPresent(key -> NameRenderer.renderNamesForKey(bank, key, Minecraft.getInstance().hitResult));
            });
        });
    }

    private static void renderNamesForKey(MemoryBankImpl bank, MemoryKey key, @Nullable HitResult hitResult) {
        @Nullable Memory focused = null;

        if (hitResult instanceof BlockHitResult blockHitResult && hitResult.getType() != HitResult.Type.MISS) {
            var targetedMemory = key.get(blockHitResult.getBlockPos());

            if (targetedMemory.isPresent() && targetedMemory.get().hasCustomName()) {
                focused = targetedMemory.get();
            }
        }

        if (bank.getMetadata().getCompatibilitySettings().nameRenderMode == NameRenderMode.FULL) {
            Map<BlockPos, Memory> named = key.getNamedMemories();
            final int maxRangeSq = ChestTrackerConfig.INSTANCE.instance().rendering.nameRange
                    * ChestTrackerConfig.INSTANCE.instance().rendering.nameRange;
            Set<BlockPos> alreadyRendering = RenderUtils.getCurrentlyRenderedWithNames();
            Vec3 cameraPos = Minecraft.getInstance().gameRenderer.mainCamera().position();

            for (var entry : named.entrySet()) {
                if (entry.getValue() == focused) continue;
                if (alreadyRendering.contains(entry.getKey())) continue;
                if (entry.getKey().distToCenterSqr(cameraPos) < maxRangeSq) {
                    Component name = entry.getValue().renderName();
                    if (name == null) continue;
                    RenderUtils.scheduleLabelRender(entry.getValue().getCenterPosition().add(0, 1, 0), name);
                }
            }
        }

        if (focused != null) {
            Component name = focused.renderName();
            if (name != null) {
                RenderUtils.scheduleLabelRender(focused.getCenterPosition().add(0, 1, 0), name, true);
            }
        }
    }
}
