package second.fastfoodsimulator.model;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class StatisticsManager {
    private final AtomicInteger totalCustomers = new AtomicInteger(0);
    private final AtomicInteger totalOrders = new AtomicInteger(0);
    private final AtomicInteger maxCustomerQueue = new AtomicInteger(0);
    private final AtomicInteger maxKitchenQueue = new AtomicInteger(0);
    private final AtomicLong totalWaitTime = new AtomicLong(0);
    private final AtomicInteger completedOrders = new AtomicInteger(0);

    private long simulationStartTime;
    private int currentCustomerQueue = 0;
    private int currentKitchenQueue = 0;

    public void startSimulation() {
        simulationStartTime = System.currentTimeMillis();
        // УБИРАЕМ reset() чтобы статистика не сбрасывалась при каждом запуске
    }

    public void reset() {
        totalCustomers.set(0);
        totalOrders.set(0);
        maxCustomerQueue.set(0);
        maxKitchenQueue.set(0);
        totalWaitTime.set(0);
        completedOrders.set(0);
        currentCustomerQueue = 0;
        currentKitchenQueue = 0;
        simulationStartTime = System.currentTimeMillis();
    }

    // Методы для обновления статистики
    public void customerArrived() {
        totalCustomers.incrementAndGet();
        currentCustomerQueue++;
        updateMaxCustomerQueue();
    }

    public void customerServed() {
        currentCustomerQueue--;
    }

    public void orderCreated() {
        totalOrders.incrementAndGet();
        currentKitchenQueue++;
        updateMaxKitchenQueue();
    }

    // ДОБАВЛЯЕМ МЕТОД ДЛЯ УЧЕТА ЗАВЕРШЕННЫХ ЗАКАЗОВ
    public void orderCompleted(long waitTime) {
        completedOrders.incrementAndGet();
        totalWaitTime.addAndGet(waitTime);
        currentKitchenQueue--;
        System.out.println("Статистика: заказ завершен. Время ожидания: " + waitTime + "мс. Всего завершено: " + completedOrders.get());
    }

    public void updateCustomerQueue(int size) {
        currentCustomerQueue = size;
        updateMaxCustomerQueue();
    }

    public void updateKitchenQueue(int size) {
        currentKitchenQueue = size;
        updateMaxKitchenQueue();
    }

    private void updateMaxCustomerQueue() {
        maxCustomerQueue.updateAndGet(currentMax -> Math.max(currentMax, currentCustomerQueue));
    }

    private void updateMaxKitchenQueue() {
        maxKitchenQueue.updateAndGet(currentMax -> Math.max(currentMax, currentKitchenQueue));
    }

    // Геттеры для статистики
    public int getTotalCustomers() {
        return totalCustomers.get();
    }

    public int getTotalOrders() {
        return totalOrders.get();
    }

    public int getCompletedOrders() {
        return completedOrders.get();
    }

    public int getMaxCustomerQueue() {
        return maxCustomerQueue.get();
    }

    public int getMaxKitchenQueue() {
        return maxKitchenQueue.get();
    }

    public long getAverageWaitTime() {
        int completed = completedOrders.get();
        return completed > 0 ? totalWaitTime.get() / completed : 0;
    }

    public double getCurrentSpeed() {
        if (simulationStartTime == 0) return 0.0;

        long elapsedTime = System.currentTimeMillis() - simulationStartTime;
        long elapsedMinutes = Math.max(1, elapsedTime / 60000); // минимум 1 минута
        return (double) completedOrders.get() / elapsedMinutes;
    }
}