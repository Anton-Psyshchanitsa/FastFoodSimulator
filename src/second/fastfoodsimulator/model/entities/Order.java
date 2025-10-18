package second.fastfoodsimulator.model.entities;

public class Order {
    private final int orderId;
    private OrderState state;
    private final long creationTime;

    public enum OrderState {
        NEW,
        IN_PROGRESS,
        READY,
        COMPLETED
    }

    public Order(int orderId) {
        this.orderId = orderId;
        this.state = OrderState.NEW;
        this.creationTime = System.currentTimeMillis();
    }

    public int getOrderId() {
        return orderId;
    }

    public OrderState getState() {
        return state;
    }

    public void setState(OrderState state) {
        this.state = state;
    }

    public long getCreationTime() {
        return creationTime;
    }
}
