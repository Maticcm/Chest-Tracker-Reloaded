package red.jackf.chesttracker.impl.search;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import red.jackf.chesttracker.impl.search.render.HighlightRenderer;

import java.util.Set;

/**
 * Utilities for working with the highlight renderer.
 *
 * <p>Adapted from Where Is It's {@code red.jackf.whereisit.client.api.RenderUtils}. The GUI helpers
 * ({@code drawTexturedRect}, {@code forceDraw}) are gone - they only existed for the Litematica
 * integration, which has been dropped.</p>
 */
@SuppressWarnings("unused")
public interface RenderUtils {
    /**
     * @return All positions currently being highlighted.
     */
    static Set<BlockPos> getCurrentlyRendered() {
        return HighlightRenderer.getResults().keySet();
    }

    /**
     * @return All highlighted positions that specifically carry a label. Used to skip duplicate label
     * rendering when results are shown for the same position.
     */
    static Set<BlockPos> getCurrentlyRenderedWithNames() {
        return HighlightRenderer.getNamedResults().keySet();
    }

    /**
     * Schedule a label to be rendered on the next frame.
     */
    static void scheduleLabelRender(Vec3 pos, Component name) {
        scheduleLabelRender(pos, name, false);
    }

    /**
     * Schedule a label to be rendered on the next frame, optionally drawn through terrain.
     */
    static void scheduleLabelRender(Vec3 pos, Component name, boolean seeThrough) {
        HighlightRenderer.scheduleLabel(pos, name, seeThrough);
    }
}
