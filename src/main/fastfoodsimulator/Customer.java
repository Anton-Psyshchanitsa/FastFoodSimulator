package main.fastfoodsimulator;

import java.util.concurrent.CompletableFuture;

public class Customer extends SimulatorEntity {
    private Order order; // Заказ клиента (с промисами)

    public Customer(int id) {
        super(id);
        setStatus("Arrived");
    }

    // Полиморфная реализация: действие клиента (пока симуляция ожидания заказа)
    @Override
    public void performAction() {
        setStatus("Waiting for order");
        // В будущем: ожидание servingFuture для pickup (в Этапе 3)
    }

    // Метод для получения заказа от OrderTaker
    public void placeOrder(Order newOrder) {
        this.order = newOrder;
        setStatus("Order placed: " + newOrder.getOrderNumber());
        // Клиент перемещается в serving line и ждёт servingFuture
    }

    public CompletableFuture<Void> getServingFuture() {
        return order != null ? order.getServingFuture() : null;
    }

    public Order getOrder() {
        return order;
    }
}