package main.fastfoodsimulator;

import java.util.Queue;

public class Server extends SimulatorEntity {
    private final Queue<Order> serviceQueue;

    public Server(int id, Queue<Order> serviceQueue) {
        super(id);
        this.serviceQueue = serviceQueue;
    }

    // Полиморфная реализация: обслуживание следующего заказа
    @Override
    public void performAction() {
        if (!serviceQueue.isEmpty()) {
            Order order = serviceQueue.poll(); // Взять следующий готовый заказ (FIFO)
            setStatus("Serving order " + order.getOrderNumber());
            order.completeServing(); // Завершить промис для клиента
            // Клиент заберёт заказ асинхронно через future
        } else {
            setStatus("Idle");
        }
    }
}