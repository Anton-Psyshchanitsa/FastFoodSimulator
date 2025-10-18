package second.fastfoodsimulator.model.queues;

import second.fastfoodsimulator.model.entities.Order;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.List;
import java.util.ArrayList;

public class KitchenQueue {
    private final ConcurrentLinkedQueue<Order> orders;

    public KitchenQueue() {
        orders = new ConcurrentLinkedQueue<>();
    }

    public synchronized void addOrder(Order order) {
        orders.add(order);
    }

    public synchronized Order getNextOrder() {
        return orders.poll();
    }

    public synchronized int getWaitingCount() {
        return orders.size();
    }

    public synchronized boolean isEmpty() {
        return orders.isEmpty();
    }

    // Метод для получения всех заказов с реальными ID
    public synchronized List<Order> getAllOrders() {
        return new ArrayList<>(orders);
    }

    // Метод для получения списка ID заказов
    public synchronized List<Integer> getOrderIds() {
        List<Integer> orderIds = new ArrayList<>();
        for (Order order : orders) {
            orderIds.add(order.getOrderId());
        }
        return orderIds;
    }

    // ДОБАВЛЯЕМ МЕТОД ДЛЯ ОЧИСТКИ ОЧЕРЕДИ
    public synchronized void clear() {
        orders.clear();
    }

}