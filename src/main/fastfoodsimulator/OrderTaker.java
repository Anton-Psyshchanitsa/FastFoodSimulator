package main.fastfoodsimulator;

import javafx.application.Platform;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

public class OrderTaker extends SimulatorEntity {
    private final Queue<Customer> orderLine;
    private final Queue<Order> kitchenQueue;
    private final Queue<Customer> servingLine;
    private final AtomicInteger orderCounter;
    private final SimulatorUI ui;
    private final AnimationManager animationManager; // Новое поле

    public OrderTaker(int id, Queue<Customer> orderLine, Queue<Order> kitchenQueue, Queue<Customer> servingLine, AtomicInteger orderCounter, SimulatorUI ui, AnimationManager animationManager) {
        super(id);
        this.orderLine = orderLine;
        this.kitchenQueue = kitchenQueue;
        this.servingLine = servingLine;
        this.orderCounter = orderCounter;
        this.ui = ui;
        this.animationManager = animationManager;
    }

    @Override
    public void performAction() {
        if (!orderLine.isEmpty()) {
            Customer customer = orderLine.poll();
            int orderNumber = orderCounter.incrementAndGet();
            Order order = new Order(orderNumber);
            kitchenQueue.add(order);
            customer.placeOrder(order, servingLine, ui, animationManager); // Передача animationManager
            setStatus("Processed order " + orderNumber + " for customer " + customer.getId());
            Platform.runLater(() -> {
                ui.updateCurrentOrder(orderNumber);
                ui.updatePickup(0, servingLine.size());
            });
        } else {
            setStatus("Idle");
        }
    }
}