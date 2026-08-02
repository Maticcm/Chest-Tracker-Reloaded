package red.jackf.chesttracker.impl.search.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import red.jackf.chesttracker.impl.search.SearchConfig;
import red.jackf.chesttracker.impl.search.SearchRequest;
import red.jackf.chesttracker.impl.search.SearchResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Draws the fading highlight boxes and container name labels over search results.
 *
 * <p>Adapted from Where Is It's {@code red.jackf.whereisit.client.render.Rendering}, which targeted
 * the pre-1.21.9 immediate-mode renderer. 26.2 removed {@code MultiBufferSource},
 * {@code LightTexture} and {@code WorldRenderEvents} entirely - world drawing now goes through
 * {@link net.minecraft.client.renderer.SubmitNodeCollector}, so boxes are submitted as custom
 * geometry and labels as name tags.</p>
 */
public class HighlightRenderer {
    private static final Map<BlockPos, SearchResult> results = new HashMap<>();
    private static final Map<BlockPos, SearchResult> namedResults = new HashMap<>();
    private static final List<ScheduledLabel> scheduledLabels = new ArrayList<>();

    private record ScheduledLabel(Vec3 position, Component text, boolean seeThrough) {}

    /** Starts "long expired" so nothing renders until the first search. */
    private static long ticksSinceSearch = Long.MAX_VALUE / 2;
    private static @Nullable SearchRequest lastRequest = null;
    private static Gradient gradient = Gradient.RED;

    public static void setup() {
        // Queue labels for results that carry a container name.
        LevelRenderEvents.START_MAIN.register(context -> {
            if (!shouldBeRendering()) return;
            if (!SearchConfig.showContainerNamesInResults()) return;
            for (SearchResult value : namedResults.values()) {
                if (value.name() == null) continue;
                scheduleLabel(Vec3.atCenterOf(value.pos()).add(value.nameOffset()),
                        value.name(),
                        SearchConfig.labelsAreSeeThrough());
            }
        });

        // Draw queued labels.
        LevelRenderEvents.BEFORE_BLOCK_OUTLINE.register((context, outline) -> {
            if (!scheduledLabels.isEmpty()) {
                renderLabels(context);
                scheduledLabels.clear();
            }
            return true;
        });

        // Draw the highlight boxes.
        LevelRenderEvents.END_MAIN.register(context -> {
            if (!shouldBeRendering() || results.isEmpty()) return;
            renderBoxes(context, getRenderingProgress());
        });
    }

    ///////////
    // STATE //
    ///////////

    public static void addResults(Collection<SearchResult> newResults) {
        for (SearchResult result : newResults) {
            results.put(result.pos(), result);
            if (result.name() != null) namedResults.put(result.pos(), result);
        }
    }

    public static void setLastRequest(@Nullable SearchRequest request) {
        lastRequest = request;
    }

    public static @Nullable SearchRequest getLastRequest() {
        return lastRequest;
    }

    public static void clearResults() {
        lastRequest = null;
        results.clear();
        namedResults.clear();
    }

    public static Map<BlockPos, SearchResult> getResults() {
        return results;
    }

    public static Map<BlockPos, SearchResult> getNamedResults() {
        return namedResults;
    }

    public static void setGradient(Gradient gradient) {
        HighlightRenderer.gradient = gradient;
    }

    public static long getTicksSinceSearch() {
        return ticksSinceSearch;
    }

    public static void incrementTicksSinceSearch() {
        if (ticksSinceSearch < Long.MAX_VALUE / 2) ticksSinceSearch++;
    }

    public static void resetSearchTime() {
        ticksSinceSearch = 0;
    }

    public static boolean shouldBeRendering() {
        return ticksSinceSearch <= SearchConfig.fadeoutTimeTicks();
    }

    private static float partialTick() {
        return Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true);
    }

    private static float getRenderingProgress() {
        return Mth.clamp((ticksSinceSearch + partialTick()) / SearchConfig.fadeoutTimeTicks(), 0f, 1f);
    }

    /**
     * Position along the colour cycle, shared by the boxes and the in-screen slot highlight.
     */
    public static float getBaseProgress(float delta) {
        float base = (ticksSinceSearch + delta) * SearchConfig.highlightTimeFactor();
        return (base % 80) / 80;
    }

    public static int sampleColour(float factor) {
        return gradient.sample(factor);
    }

    /** Slightly larger than a block, so the box encloses container models rather than sitting in them. */
    private static final float BOX_SCALE = 1.02f;

    /** Fraction of the lifetime spent growing to full size. */
    private static final float GROW_FRACTION = 0.06f;

    /** Fraction of the lifetime held at full opacity before fading out. */
    private static final float FADE_START = 0.6f;

    /** Pulses per second. */
    private static final float PULSE_HZ = 1.3f;

    /**
     * How far the box breathes, as a fraction of its size. Kept small deliberately: combined with
     * {@link #BOX_SCALE} the box never drops below ~0.96 of a block, so it always stays outside
     * container models (a chest is 14/16) and never sinks inside them.
     */
    private static final float PULSE_SCALE_AMOUNT = 0.06f;

    /** Opacity at the dimmest point of the pulse, as a fraction of the configured opacity. */
    private static final float PULSE_MIN_ALPHA = 0.5f;

    /**
     * Smooth 0..1 wave driving the pulse, so the highlight throbs and catches the eye.
     */
    private static float pulse01() {
        float seconds = (ticksSinceSearch + partialTick()) / 20f;
        return (Mth.sin(seconds * PULSE_HZ * ((float) Math.PI * 2f)) + 1f) * 0.5f;
    }

    /**
     * Box scale over the highlight's lifetime.
     *
     * <p>Upstream shrank the box as it faded. That looks reasonable in isolation, but container
     * models are smaller than a full block - a chest is 14/16 - so once the box shrinks past that
     * it ends up <em>inside</em> the model and depth testing hides every face except the one flush
     * with the lid. Instead the box pops in quickly and then holds just proud of the block, with
     * the fade carried entirely by alpha.</p>
     */
    private static float boxScale(float progress) {
        float grow = Mth.clamp(progress / GROW_FRACTION, 0f, 1f);
        float eased = 1f - (float) Math.pow(1 - grow, 3); // ease-out cubic
        return BOX_SCALE * eased;
    }

    /** Opacity multiplier: hold, then fade to nothing over the tail of the lifetime. */
    private static float fadeFactor(float progress) {
        if (progress <= FADE_START) return 1f;
        return 1f - ((progress - FADE_START) / (1f - FADE_START));
    }

    ////////////
    // LABELS //
    ////////////

    public static void scheduleLabel(Vec3 pos, Component name, boolean seeThrough) {
        if (pos == null || name == null) return;
        scheduledLabels.add(new ScheduledLabel(pos, name, seeThrough));
    }

    private static void renderLabels(LevelRenderContext context) {
        // Same reasoning as renderBoxes: use this frame's captured camera state, not the live one.
        var cameraState = context.levelState().cameraRenderState;
        PoseStack pose = context.poseStack();

        scheduledLabels.stream()
                .sorted(Comparator.comparingDouble(label ->
                        // furthest from the camera first, so nearer labels draw over them
                        -cameraState.orientation.transformInverse(label.position.toVector3f()).z))
                .forEach(label -> {
                    Vec3 relative = label.position.subtract(cameraState.pos);
                    context.submitNodeCollector().submitNameTag(
                            pose,
                            relative,
                            0,
                            label.text,
                            label.seeThrough,
                            0xF000F0, // full brightness
                            cameraState);
                });
    }

    ///////////
    // BOXES //
    ///////////

    private static void renderBoxes(LevelRenderContext context, float progress) {
        // Use the camera position captured in this frame's render state, NOT the live camera from
        // Minecraft.gameRenderer. 26.x extracts render state and draws it later, so the live camera
        // has usually moved on by the time this is drawn, which makes the boxes sit off the block.
        Vec3 cameraPos = context.levelState().cameraRenderState.pos;

        // Held at the configured opacity, then faded out over the tail so it disappears smoothly
        // rather than popping away. The pulse throbs on top of that to draw the eye.
        float pulse = SearchConfig.highlightPulse() ? pulse01() : 1f;

        var alpha = SearchConfig.highlightOpacity()
                * fadeFactor(progress)
                * (PULSE_MIN_ALPHA + (1f - PULSE_MIN_ALPHA) * pulse);
        var colour = sampleColour(getBaseProgress(partialTick()));
        var scale = boxScale(progress) * (1f + PULSE_SCALE_AMOUNT * (pulse - 0.5f) * 2f);

        final int r = ARGB.red(colour);
        final int g = ARGB.green(colour);
        final int b = ARGB.blue(colour);
        final int a = (int) (alpha * 255);

        PoseStack pose = context.poseStack();

        for (SearchResult result : results.values()) {
            submitBox(context, pose, cameraPos, result.pos(), scale, r, g, b, a);
            for (BlockPos otherPos : result.otherPositions()) {
                submitBox(context, pose, cameraPos, otherPos, scale, r, g, b, a);
            }
        }
    }

    private static void submitBox(LevelRenderContext context, PoseStack pose, Vec3 cameraPos, BlockPos pos,
                                  float scale, int r, int g, int b, int a) {
        pose.pushPose();

        // Translate relative to the camera to avoid floating point precision loss far from origin.
        pose.translate(pos.getX() + (0.5 - cameraPos.x),
                pos.getY() + (0.5 - cameraPos.y),
                pos.getZ() + (0.5 - cameraPos.z));
        pose.scale(scale * 0.5f, scale * 0.5f, scale * 0.5f);

        context.submitNodeCollector().submitCustomGeometry(pose, RenderTypes.debugFilledBox(),
                (p, consumer) -> emitCube(consumer, p.pose(), r, g, b, a));

        pose.popPose();
    }

    private static void emitCube(VertexConsumer c, Matrix4f m, int r, int g, int b, int a) {
        // -Z
        quad(c, m, -1, -1, -1, -1, 1, -1, 1, 1, -1, 1, -1, -1, r, g, b, a);
        // +Z
        quad(c, m, -1, -1, 1, 1, -1, 1, 1, 1, 1, -1, 1, 1, r, g, b, a);
        // -Y
        quad(c, m, -1, -1, -1, 1, -1, -1, 1, -1, 1, -1, -1, 1, r, g, b, a);
        // +Y
        quad(c, m, -1, 1, -1, -1, 1, 1, 1, 1, 1, 1, 1, -1, r, g, b, a);
        // -X
        quad(c, m, -1, -1, -1, -1, -1, 1, -1, 1, 1, -1, 1, -1, r, g, b, a);
        // +X
        quad(c, m, 1, -1, -1, 1, 1, -1, 1, 1, 1, 1, -1, 1, r, g, b, a);
    }

    private static void quad(VertexConsumer c, Matrix4f m,
                             float x1, float y1, float z1, float x2, float y2, float z2,
                             float x3, float y3, float z3, float x4, float y4, float z4,
                             int r, int g, int b, int a) {
        c.addVertex(m, x1, y1, z1).setColor(r, g, b, a);
        c.addVertex(m, x2, y2, z2).setColor(r, g, b, a);
        c.addVertex(m, x3, y3, z3).setColor(r, g, b, a);
        c.addVertex(m, x4, y4, z4).setColor(r, g, b, a);
    }
}
