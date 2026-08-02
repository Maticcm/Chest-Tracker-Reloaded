package red.jackf.chesttracker.impl.search;

/**
 * Tunables for the absorbed search/highlight layer.
 *
 * <p>These were previously supplied by Where Is It's own config screen
 * ({@code WhereIsItConfig}). Defaults here match Where Is It's, so behaviour is unchanged out of
 * the box; ChestTracker's own YACL config screen writes to these.</p>
 */
public final class SearchConfig {
    private SearchConfig() {}

    /** Whether searches should descend into shulker boxes, bundles etc. */
    private static boolean nestedSearch = true;

    /** Whether the open screen is closed when a search returns results, so they can be seen. */
    private static boolean closeGuiOnFoundResults = true;

    /**
     * Peak opacity of the highlight box, 0..1. Upstream drew these fully opaque, which hides the
     * block underneath; this defaults lower so the container stays recognisable.
     */
    private static float highlightOpacity = 0.45f;

    /** Whether the highlight box pulses (breathes in size and opacity) to be easier to spot. */
    private static boolean highlightPulse = true;

    /**
     * How long, in ticks, a highlight lasts before it has fully faded. 20 ticks = 1 second.
     * Upstream used 100 (5s), which is short if you have to turn around to find the container.
     */
    private static float fadeoutTimeTicks = 240f;

    /** Whether container name labels are drawn through terrain. */
    private static boolean labelsAreSeeThrough = false;

    /** Whether to draw container names next to highlight results. */
    private static boolean showContainerNamesInResults = true;

    /** Scale factor applied to container name labels. */
    private static float containerNameLabelScale = 1f;

    /** Speed of the highlight colour cycle. */
    private static float highlightTimeFactor = 1f;

    public static boolean nestedSearch() {
        return nestedSearch;
    }

    public static void nestedSearch(boolean value) {
        nestedSearch = value;
    }

    public static boolean closeGuiOnFoundResults() {
        return closeGuiOnFoundResults;
    }

    public static void closeGuiOnFoundResults(boolean value) {
        closeGuiOnFoundResults = value;
    }

    public static boolean highlightPulse() {
        return highlightPulse;
    }

    public static void highlightPulse(boolean value) {
        highlightPulse = value;
    }

    public static float highlightOpacity() {
        return highlightOpacity;
    }

    public static void highlightOpacity(float value) {
        highlightOpacity = Math.max(0f, Math.min(1f, value));
    }

    public static float fadeoutTimeTicks() {
        return fadeoutTimeTicks;
    }

    public static void fadeoutTimeTicks(float value) {
        fadeoutTimeTicks = value;
    }

    public static boolean labelsAreSeeThrough() {
        return labelsAreSeeThrough;
    }

    public static void labelsAreSeeThrough(boolean value) {
        labelsAreSeeThrough = value;
    }

    public static boolean showContainerNamesInResults() {
        return showContainerNamesInResults;
    }

    public static void showContainerNamesInResults(boolean value) {
        showContainerNamesInResults = value;
    }

    public static float containerNameLabelScale() {
        return containerNameLabelScale;
    }

    public static void containerNameLabelScale(float value) {
        containerNameLabelScale = value;
    }

    public static float highlightTimeFactor() {
        return highlightTimeFactor;
    }

    public static void highlightTimeFactor(float value) {
        highlightTimeFactor = value;
    }
}
