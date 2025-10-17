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

    // Добавляем метод для получения всех заказов
    public synchronized List<Order> getAllOrders() {
        return new ArrayList<>(orders);
    }
}

