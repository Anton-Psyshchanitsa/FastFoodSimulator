package second.fastfoodsimulator.model.entities;

public class Customer {
    private final int orderId;
    private CustomerState state;
    private long arrivalTime;

    public enum CustomerState {
        WAITING_ORDER,
        WAITING_PICKUP,
        COMPLETED
    }

    public Customer(int orderId) {
        this.orderId = orderId;
        this.state = CustomerState.WAITING_ORDER;
        this.arrivalTime = System.currentTimeMillis();
    }

    public int getOrderId() {
        return orderId;
    }

    public CustomerState getState() {
        return state;
    }

    public void setState(CustomerState state) {
        this.state = state;
    }

    public long getArrivalTime() {
        return arrivalTime;
    }
}
