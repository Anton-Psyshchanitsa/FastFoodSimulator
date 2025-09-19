package main.fastfoodsimulator;

import java.util.concurrent.CompletableFuture;

public class Order {
    private int orderNumber;
    private CompletableFuture<Void> preparationFuture; // Промис для приготовления (Cook -> Server)
    private CompletableFuture<Void> servingFuture;    // Промис для обслуживания (Customer ждет)

    public Order(int orderNumber) {
        this.orderNumber = orderNumber;
        this.preparationFuture = new CompletableFuture<>();
        this.servingFuture = new CompletableFuture<>();
    }

    public int getOrderNumber() {
        return orderNumber;
    }

    // Методы для завершения промисов
    public void completePreparation() {
        preparationFuture.complete(null);
    }

    public void completeServing() {
        servingFuture.complete(null);
    }

    public CompletableFuture<Void> getPreparationFuture() {
        return preparationFuture;
    }

    public CompletableFuture<Void> getServingFuture() {
        return servingFuture;
    }
}