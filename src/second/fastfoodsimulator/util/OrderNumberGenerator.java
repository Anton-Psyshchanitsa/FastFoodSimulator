package second.fastfoodsimulator.util;

public class OrderNumberGenerator {
    private static int counter = 0;

    public static synchronized int generate() {
        return ++counter;
    }

    // ДОБАВЛЯЕМ МЕТОД ДЛЯ СБРОСА СЧЕТЧИКА
    public static synchronized void reset() {
        counter = 0;
        System.out.println("Счетчик заказов сброшен на 0");
    }
}