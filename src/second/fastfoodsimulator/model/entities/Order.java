package second.fastfoodsimulator.model.entities;

public class Order {
    private final int orderId;
    private OrderState state;
    private final long creationTime;

    public enum OrderState {
        NEW,
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

    public void setState(OrderState state) {
        this.state = state;
    }
}
