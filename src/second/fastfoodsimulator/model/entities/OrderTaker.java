package second.fastfoodsimulator.model.entities;

import second.fastfoodsimulator.model.queues.KitchenQueue;
import second.fastfoodsimulator.util.OrderNumberGenerator;

public class OrderTaker {
    private int currentOrderId;
    private boolean busy;

    public OrderTaker() {
        this.currentOrderId = -1;
        this.busy = false;
    }

    public synchronized int takeOrder() {
        if (busy) return -1;

        busy = true;
        currentOrderId = OrderNumberGenerator.generate();
        return currentOrderId;
    }

    public synchronized void completeOrder() {
        busy = false;
        currentOrderId = -1;
    }

    public synchronized int getCurrentOrderId() {
        return currentOrderId;
    }

    public synchronized boolean isBusy() {
        return busy;
    }
}
