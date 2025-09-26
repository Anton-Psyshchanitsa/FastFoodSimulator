package main.fastfoodsimulator;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

public class OrderTaker extends SimulatorEntity {
    private final Queue<Customer> orderLine; // Очередь клиентов
    private final Queue<Order> kitchenQueue; // Очередь заказов на кухню
    private final AtomicInteger orderCounter; // Уникальный счётчик номеров заказов (thread-safe)

    public OrderTaker(int id, Queue<Customer> orderLine, Queue<Order> kitchenQueue, AtomicInteger orderCounter) {
        super(id);
        this.orderLine = orderLine;
        this.kitchenQueue = kitchenQueue;
        this.orderCounter = orderCounter;
    }

    // Полиморфная реализация: обработка следующего клиента
    @Override
    public void performAction() {
        if (!orderLine.isEmpty()) {
            Customer customer = orderLine.poll(); // Взять следующего клиента (FIFO)
            int orderNumber = orderCounter.incrementAndGet(); // Генерация уникального номера
            Order order = new Order(orderNumber);
            kitchenQueue.add(order); // Добавить в очередь кухни
            customer.placeOrder(order); // Вернуть заказ клиенту
            setStatus("Processed order " + orderNumber + " for customer " + customer.getId());
        } else {
            setStatus("Idle");
        }
    }
}