package red.jackf.chesttracker.vendor.jackfredlib.api.base;

/**
 * Minimal argument-validation helpers.
 *
 * <p>JackFredLib used {@code org.apache.http.util.Args} for these, which came in transitively via
 * Minecraft's dependencies. That is no longer on the 26.x classpath, so the three methods actually
 * used are reimplemented here.</p>
 */
public final class Args {
    private Args() {}

    public static void check(boolean expression, String message) {
        if (!expression) throw new IllegalArgumentException(message);
    }

    public static int notNegative(int n, String name) {
        if (n < 0) throw new IllegalArgumentException(name + " may not be negative");
        return n;
    }

    public static int positive(int n, String name) {
        if (n <= 0) throw new IllegalArgumentException(name + " may not be negative or zero");
        return n;
    }
}
