package second.fastfoodsimulator.model.queues;

import second.fastfoodsimulator.model.entities.Customer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.ArrayList;
import java.util.List;

public class ServingLine {
    private final ConcurrentLinkedQueue<Customer> waitingCustomers;
    private final ConcurrentHashMap<Integer, Customer> orderIdToCustomerMap;

    public ServingLine() {
        waitingCustomers = new ConcurrentLinkedQueue<>();
        orderIdToCustomerMap = new ConcurrentHashMap<>();
    }

    public synchronized void addCustomer(Customer customer, int orderId) {
        customer.setOrderId(orderId);
        customer.setState(Customer.CustomerState.WAITING_PICKUP);
        waitingCustomers.add(customer);
        orderIdToCustomerMap.put(orderId, customer);
    }

    public synchronized Customer getCustomerByOrderId(int orderId) {
        return orderIdToCustomerMap.get(orderId);
    }

    // ДОБАВЛЯЕМ МЕТОД ДЛЯ УДАЛЕНИЯ КЛИЕНТА ПО orderId
    public synchronized Customer removeCustomerByOrderId(int orderId) {
        Customer customer = orderIdToCustomerMap.remove(orderId);
        if (customer != null) {
            waitingCustomers.remove(customer);
            customer.setState(Customer.CustomerState.COMPLETED);
            System.out.println("Клиент #" + customer.getCustomerId() + " удален из serving line (заказ #" + orderId + ")");
        }
        return customer;
    }

    public synchronized int getWaitingCustomerCount() {
        return waitingCustomers.size();
    }

    public synchronized List<Customer> getAllWaitingCustomers() {
        return new ArrayList<>(waitingCustomers);
    }

    public synchronized List<Integer> getWaitingOrderIds() {
        return new ArrayList<>(orderIdToCustomerMap.keySet());
    }

    public synchronized boolean isEmpty() {
        return waitingCustomers.isEmpty();
    }

    public synchronized void clear() {
        waitingCustomers.clear();
        orderIdToCustomerMap.clear();
    }
}