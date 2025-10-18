package second.fastfoodsimulator.model.entities;

public class Server {
    private final int serverId;
    private int currentOrderId = -1;
    private boolean busy;
    private final Object lock = new Object();

    public Server(int serverId) {
        this.serverId = serverId;
        this.busy = false;
    }

    public int startServing(int orderId) {
        synchronized (lock) {
            if (busy) return -1;

            busy = true;
            currentOrderId = orderId;
            return currentOrderId;
        }
    }

    public void completeServing() {
        synchronized (lock) {
            busy = false;
            currentOrderId = -1;
        }
    }

    public int getCurrentOrderId() {
        synchronized (lock) {
            return currentOrderId;
        }
    }

    public boolean isBusy() {
        synchronized (lock) {
            return busy;
        }
    }

    public int getServerId() {
        return serverId;
    }

    public String getStatus() {
        synchronized (lock) {
            if (busy) {
                return "Официант #" + serverId + " выдает заказ #" + currentOrderId;
            } else {
                return "Официант #" + serverId + " свободен";
            }
        }
    }
}