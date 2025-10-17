package second.fastfoodsimulator.model.simulation;

import second.fastfoodsimulator.model.entities.Customer;
import second.fastfoodsimulator.model.entities.Order;
import second.fastfoodsimulator.model.entities.OrderTaker;
import second.fastfoodsimulator.model.queues.KitchenQueue;
import second.fastfoodsimulator.view.controllers.MainController;
import javafx.application.Platform;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SimulationManager {
    private final MainController controller;
    private final OrderTaker orderTaker;
    private final KitchenQueue kitchenQueue;
    private final ScheduledExecutorService executor;

    private int customerCount = 0;
    private int servingLineCount = 0;

    public SimulationManager(MainController controller) {
        this.controller = controller;
        this.orderTaker = new OrderTaker();
        this.kitchenQueue = new KitchenQueue();
        this.executor = Executors.newScheduledThreadPool(3);
    }

    public void startSimulation(int customerInterval, int orderInterval) {
        System.out.println("Запуск симуляции с интервалами: " + customerInterval + " и " + orderInterval);
        executor.scheduleAtFixedRate(this::generateCustomer, 0, customerInterval, TimeUnit.MILLISECONDS);
        executor.scheduleAtFixedRate(this::processOrder, 0, orderInterval, TimeUnit.MILLISECONDS);
    }

    public void stopSimulation() {
        executor.shutdown();
    }

    private void generateCustomer() {
        customerCount++;
        Customer customer = new Customer(customerCount);

        Platform.runLater(() -> {
            controller.addCustomerToQueue(customer);
            controller.updateCustomerQueueCount(customerCount);
            System.out.println("Добавлен клиент #" + customerCount);
        });
    }

    private void processOrder() {
        if (!orderTaker.isBusy()) {
            int orderId = orderTaker.takeOrder();
            if (orderId != -1) {
                Order order = new Order(orderId);
                kitchenQueue.addOrder(order);

                Platform.runLater(() -> {
                    controller.updateOrderTakerStatus(orderId);
                    controller.updateKitchenQueue(kitchenQueue.getWaitingCount());
                    System.out.println("Обработан заказ #" + orderId);
                });
            }
        }
    }
}
