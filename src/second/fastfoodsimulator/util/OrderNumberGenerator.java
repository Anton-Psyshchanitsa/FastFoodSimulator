package second.fastfoodsimulator.util;

public class OrderNumberGenerator {
    private static int counter = 0;

    public static synchronized int generate() {
        return ++counter;
    }
}