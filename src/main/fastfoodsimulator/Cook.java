package main.fastfoodsimulator;

import java.util.Queue;

public class Cook extends SimulatorEntity {
    private final Queue<Order> kitchenQueue;
    private final Queue<Order> serviceQueue;
    private final int fulfillmentInterval; // Время приготовления в секундах

    public Cook(int id, Queue<Order> kitchenQueue, Queue<Order> serviceQueue, int fulfillmentInterval) {
        super(id);
        this.kitchenQueue = kitchenQueue;
        this.serviceQueue = serviceQueue;
        this.fulfillmentInterval = fulfillmentInterval;
    }

    // Полиморфная реализация: приготовление следующего заказа
    @Override
    public void performAction() {
        if (!kitchenQueue.isEmpty()) {
            Order order = kitchenQueue.poll(); // Взять следующий заказ (FIFO)
            setStatus("Preparing order " + order.getOrderNumber());
            try {
                Thread.sleep(fulfillmentInterval * 1000); // Симуляция времени приготовления
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            order.completePreparation(); // Завершить промис для сервера
            serviceQueue.add(order); // Переместить в сервисную очередь
            setStatus("Completed order " + order.getOrderNumber());
        } else {
            setStatus("Idle");
        }
    }
}