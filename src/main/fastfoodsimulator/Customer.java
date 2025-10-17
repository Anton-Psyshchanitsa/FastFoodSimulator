package main.fastfoodsimulator;

import javafx.application.Platform;

import java.util.Queue;
import java.util.concurrent.CompletableFuture;

public class Customer extends SimulatorEntity {
    private Order order;

    public Customer(int id) {
        super(id);
        setStatus("Arrived");
    }

    @Override
    public void performAction() {
        setStatus("Waiting for order");
    }

    public void placeOrder(Order newOrder, Queue<Customer> servingLine, SimulatorUI ui, AnimationManager animationManager) {
        this.order = newOrder;
        setStatus("Order placed: " + newOrder.getOrderNumber());
        servingLine.add(this);
        animationManager.animateCustomerMovement(getId(), 50, 100); // Старт анимации от order line (координаты примерные)
        new Thread(() -> {
            try {
                order.getServingFuture().get();
                setStatus("Picked up order " + order.getOrderNumber());
                servingLine.remove(this);
                Platform.runLater(() -> ui.updatePickup(0, servingLine.size()));
                // Анимация уже запущена, она завершится автоматически
            } catch (Exception e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    public CompletableFuture<Void> getServingFuture() {
        return order != null ? order.getServingFuture() : null;
    }

    public Order getOrder() {
        return order;
    }
}