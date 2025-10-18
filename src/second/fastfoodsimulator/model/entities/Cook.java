package second.fastfoodsimulator.model.entities;

public class Cook {
    private int currentOrderId = -1;
    private boolean busy;

    public synchronized int startCooking(int orderId) {
        if (busy) return -1;

        busy = true;
        currentOrderId = orderId;
        return currentOrderId;
    }

    public synchronized void completeCooking() {
        busy = false;
        currentOrderId = -1;
        System.out.println("Повар завершил готовку, состояние сброшено");
    }

    public synchronized int getCurrentOrderId() {
        return currentOrderId;
    }

    public synchronized boolean isBusy() {
        return busy;
    }
}