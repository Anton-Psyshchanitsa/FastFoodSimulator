package second.fastfoodsimulator.model.entities;

public class Customer {
    private final int customerId;
    private Integer orderId;
    private CustomerState state;
    private long arrivalTime;
    private long orderStartTime = 0;

    public enum CustomerState {
        WAITING_ORDER,
        WAITING_PICKUP,
        COMPLETED
    }

    public Customer(int customerId) {
        this.customerId = customerId;
        this.state = CustomerState.WAITING_ORDER;
        this.arrivalTime = System.currentTimeMillis();
        this.orderId = null;
    }

    public int getCustomerId() {
        return customerId;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public void setState(CustomerState state) {
        this.state = state;
    }

    public long getOrderStartTime() {
        return orderStartTime;
    }

    public void setOrderStartTime(long orderStartTime) {
        this.orderStartTime = orderStartTime;
    }
}