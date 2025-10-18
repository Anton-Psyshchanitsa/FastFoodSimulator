package second.fastfoodsimulator.model.queues;

import second.fastfoodsimulator.model.entities.Order;
import java.util.concurrent.ConcurrentLinkedQueue;

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
}
