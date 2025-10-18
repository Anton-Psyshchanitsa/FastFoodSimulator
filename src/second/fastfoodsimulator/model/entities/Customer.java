package second.fastfoodsimulator.model.entities;

public class Customer {
    private final int customerId; // Меняем orderId на customerId
    private Integer orderId; // Может быть null пока заказ не оформлен
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

    public CustomerState getState() {
        return state;
    }

    public void setState(CustomerState state) {
        this.state = state;
    }

    public long getArrivalTime() {
        return arrivalTime;
    }

    public long getOrderStartTime() {
        return orderStartTime;
    }

    public void setOrderStartTime(long orderStartTime) {
        this.orderStartTime = orderStartTime;
    }

    public boolean hasOrderStartTime() {
        return orderStartTime > 0;
    }

    public long getWaitTime() {
        return System.currentTimeMillis() - orderStartTime;
    }

}