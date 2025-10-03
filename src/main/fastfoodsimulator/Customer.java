package main.fastfoodsimulator;

import javafx.application.Platform;
import java.util.concurrent.CompletableFuture;
import java.util.Queue;

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

    public void placeOrder(Order newOrder, Queue<Customer> servingLine, SimulatorUI ui) {
        this.order = newOrder;
        setStatus("Order placed: " + newOrder.getOrderNumber());
        servingLine.add(this); // Добавить в serving line
        // Асинхронно ждать servingFuture и уйти при завершении
        new Thread(() -> {
            try {
                order.getServingFuture().get(); // Ждать завершения (блокирует этот поток)
                setStatus("Picked up order " + order.getOrderNumber());
                servingLine.remove(this); // Удалить из очереди
                Platform.runLater(() -> ui.updatePickup(0, servingLine.size())); // Обновить UI
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