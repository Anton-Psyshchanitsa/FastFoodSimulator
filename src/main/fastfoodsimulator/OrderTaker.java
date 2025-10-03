package main.fastfoodsimulator;

import javafx.application.Platform;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

public class OrderTaker extends SimulatorEntity {
    private final Queue<Customer> orderLine;
    private final Queue<Order> kitchenQueue;
    private final Queue<Customer> servingLine;
    private final AtomicInteger orderCounter;
    private final SimulatorUI ui; // Для обновлений UI

    public OrderTaker(int id, Queue<Customer> orderLine, Queue<Order> kitchenQueue, Queue<Customer> servingLine, AtomicInteger orderCounter, SimulatorUI ui) {
        super(id);
        this.orderLine = orderLine;
        this.kitchenQueue = kitchenQueue;
        this.servingLine = servingLine;
        this.orderCounter = orderCounter;
        this.ui = ui;
    }

    @Override
    public void performAction() {
        if (!orderLine.isEmpty()) {
            Customer customer = orderLine.poll();
            int orderNumber = orderCounter.incrementAndGet();
            Order order = new Order(orderNumber);
            kitchenQueue.add(order);
            customer.placeOrder(order, servingLine, ui); // Клиент размещает заказ и ждёт
            setStatus("Processed order " + orderNumber + " for customer " + customer.getId());
            Platform.runLater(() -> {
                ui.updateCurrentOrder(orderNumber);
                ui.updatePickup(0, servingLine.size()); // Обновить serving line
            });
        } else {
            setStatus("Idle");
        }
    }
}