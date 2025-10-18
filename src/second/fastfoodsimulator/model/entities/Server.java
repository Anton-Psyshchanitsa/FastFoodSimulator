package second.fastfoodsimulator.model.entities;

public class Server {
    private int currentOrderId = -1;
    private boolean busy;

    public synchronized int startServing(int orderId) {
        if (busy) return -1;

        busy = true;
        currentOrderId = orderId;
        return currentOrderId;
    }

    public synchronized void completeServing() {
        busy = false;
        currentOrderId = -1;
        System.out.println("Сервер завершил выдачу, состояние сброшено");
    }

    public synchronized int getCurrentOrderId() {
        return currentOrderId;
    }

    public synchronized boolean isBusy() {
        return busy;
    }
}
