package second.fastfoodsimulator.model.entities;

import second.fastfoodsimulator.util.OrderNumberGenerator;

public class OrderTaker {
    private int currentOrderId = -1;
    private boolean busy = false;

    public synchronized int takeOrder() {
        if (busy) return -1;

        busy = true;
        currentOrderId = OrderNumberGenerator.generate();
        return currentOrderId;
    }

    public synchronized void completeOrder() {
        busy = false;
        currentOrderId = -1;
        System.out.println("Кассир завершил оформление заказа, состояние сброшено");
    }

    public synchronized int getCurrentOrderId() {
        return currentOrderId;
    }

    public synchronized boolean isBusy() {
        return busy;
    }
}