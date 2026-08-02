package red.jackf.chesttracker.impl.search.render;

import net.minecraft.util.Mth;

import java.util.List;

/**
 * A cyclic colour gradient sampled by a 0..1 factor.
 *
 * <p>Compact replacement for JackFredLib's {@code jackfredlib-colour} module ({@code Colour},
 * {@code Colours}, {@code Gradient}), which was the only part of that library Where Is It's renderer
 * needed. Only the operations actually used are implemented: construction from stops, {@link #sample},
 * and {@link #squish}.</p>
 */
public final class Gradient {
    /** Solid red - the default, matching Where Is It's default scheme. */
    public static final Gradient RED = of(0xFF_FF0000);

    /** Rainbow cycle. */
    public static final Gradient RAINBOW = of(
            0xFF_FF0000, 0xFF_FF7F00, 0xFF_FFFF00, 0xFF_00FF00,
            0xFF_0000FF, 0xFF_4B0082, 0xFF_9400D3);

    private final int[] stops;

    private Gradient(int[] stops) {
        this.stops = stops;
    }

    public static Gradient of(int... argb) {
        if (argb.length == 0) throw new IllegalArgumentException("A gradient needs at least one stop");
        return new Gradient(argb);
    }

    public static Gradient of(List<Integer> argb) {
        return of(argb.stream().mapToInt(Integer::intValue).toArray());
    }

    /**
     * Returns a gradient that repeats this one {@code times} times over the same 0..1 range.
     */
    public Gradient repeat(int times) {
        if (times <= 1) return this;
        int[] repeated = new int[stops.length * times];
        for (int i = 0; i < times; i++) {
            System.arraycopy(stops, 0, repeated, i * stops.length, stops.length);
        }
        return new Gradient(repeated);
    }

    /**
     * Blends the ends of the gradient together over the given fraction, so the cycle has no hard
     * seam when it wraps.
     */
    public Gradient squish(float factor) {
        if (stops.length < 2 || factor <= 0f) return this;
        // Appending the first stop makes sampling wrap smoothly back to the start.
        int[] wrapped = new int[stops.length + 1];
        System.arraycopy(stops, 0, wrapped, 0, stops.length);
        wrapped[stops.length] = stops[0];
        return new Gradient(wrapped);
    }

    /**
     * Sample this gradient. The factor wraps, so any value is valid.
     *
     * @param factor Position along the gradient
     * @return Packed ARGB colour
     */
    public int sample(float factor) {
        if (stops.length == 1) return stops[0];

        float wrapped = factor - Mth.floor(factor);
        float scaled = wrapped * (stops.length - 1);
        int index = Mth.clamp(Mth.floor(scaled), 0, stops.length - 2);
        float delta = scaled - index;

        return lerp(stops[index], stops[index + 1], delta);
    }

    private static int lerp(int from, int to, float delta) {
        int a = Mth.lerpInt(delta, (from >> 24) & 0xFF, (to >> 24) & 0xFF);
        int r = Mth.lerpInt(delta, (from >> 16) & 0xFF, (to >> 16) & 0xFF);
        int g = Mth.lerpInt(delta, (from >> 8) & 0xFF, (to >> 8) & 0xFF);
        int b = Mth.lerpInt(delta, from & 0xFF, to & 0xFF);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
