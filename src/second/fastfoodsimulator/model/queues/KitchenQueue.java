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

    public synchronized List<Integer> getOrderIds() {
        List<Integer> orderIds = new ArrayList<>();
        for (Order order : orders) {
            orderIds.add(order.getOrderId());
        }
        return orderIds;
    }

    public synchronized void clear() {
        orders.clear();
    }

}