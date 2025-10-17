package main.fastfoodsimulator;

import java.util.Queue;

public class Cook extends SimulatorEntity {
    private final Queue<Order> kitchenQueue;
    private final Queue<Order> serviceQueue;
    private final int fulfillmentInterval;
    private final AnimationManager animationManager; // Новое

    public Cook(int id, Queue<Order> kitchenQueue, Queue<Order> serviceQueue, int fulfillmentInterval, AnimationManager animationManager) {
        super(id);
        this.kitchenQueue = kitchenQueue;
        this.serviceQueue = serviceQueue;
        this.fulfillmentInterval = fulfillmentInterval;
        this.animationManager = animationManager;
    }

    @Override
    public void performAction() {
        if (!kitchenQueue.isEmpty()) {
            Order order = kitchenQueue.poll();
            setStatus("Preparing order " + order.getOrderNumber());
            try {
                Thread.sleep(fulfillmentInterval * 1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            order.completePreparation();
            serviceQueue.add(order);
            setStatus("Completed order " + order.getOrderNumber());
            animationManager.animateOrderMovement(order.getOrderNumber(), 300, 100); // Старт анимации от kitchen
        } else {
            setStatus("Idle");
        }
    }
}