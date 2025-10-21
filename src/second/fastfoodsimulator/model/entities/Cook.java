package second.fastfoodsimulator.model.entities;

public class Cook {
    private final int cookId;
    private int currentOrderId = -1;
    private boolean busy;
    private final Object lock = new Object();

    public Cook(int cookId) {
        this.cookId = cookId;
        this.busy = false;
    }

    public int startCooking(int orderId) {
        synchronized (lock) {
            if (busy) return -1;

            busy = true;
            currentOrderId = orderId;
            return currentOrderId;
        }
    }

    public void completeCooking() {
        synchronized (lock) {
            busy = false;
            currentOrderId = -1;
        }
    }

    public boolean isBusy() {
        synchronized (lock) {
            return busy;
        }
    }

    public int getCookId() {
        return cookId;
    }

    public String getStatus() {
        synchronized (lock) {
            if (busy) {
                return "Повар #" + cookId + " готовит заказ #" + currentOrderId;
            } else {
                return "Повар #" + cookId + " свободен";
            }
        }
    }
}