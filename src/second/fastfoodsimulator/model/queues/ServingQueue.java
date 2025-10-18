package second.fastfoodsimulator.model.queues;

import second.fastfoodsimulator.model.entities.Order;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.List;
import java.util.ArrayList;

public class ServingQueue {
    private final ConcurrentLinkedQueue<Order> readyOrders;

    public ServingQueue() {
        readyOrders = new ConcurrentLinkedQueue<>();
    }

    public synchronized void addReadyOrder(Order order) {
        order.setState(Order.OrderState.READY);
        readyOrders.add(order);
    }

    public synchronized Order getNextReadyOrder() {
        return readyOrders.poll();
    }

    public synchronized int getReadyCount() {
        return readyOrders.size();
    }

    public synchronized boolean isEmpty() {
        return readyOrders.isEmpty();
    }

    // Метод для получения всех готовых заказов
    public synchronized List<Order> getAllReadyOrders() {
        return new ArrayList<>(readyOrders);
    }

    // Метод для получения списка ID готовых заказов
    public synchronized List<Integer> getReadyOrderIds() {
        List<Integer> orderIds = new ArrayList<>();
        for (Order order : readyOrders) {
            orderIds.add(order.getOrderId());
        }
        return orderIds;
    }

    // ДОБАВЛЯЕМ МЕТОД ДЛЯ ОЧИСТКИ ОЧЕРЕДИ
    public synchronized void clear() {
        readyOrders.clear();
    }

}