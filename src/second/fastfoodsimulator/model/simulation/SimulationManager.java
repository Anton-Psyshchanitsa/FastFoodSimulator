package second.fastfoodsimulator.model.simulation;

import second.fastfoodsimulator.model.entities.Customer;
import second.fastfoodsimulator.model.entities.Order;
import second.fastfoodsimulator.model.entities.OrderTaker;
import second.fastfoodsimulator.model.queues.KitchenQueue;
import second.fastfoodsimulator.model.queues.ServingQueue;
import second.fastfoodsimulator.view.controllers.MainController;
import javafx.application.Platform;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SimulationManager {
    private final MainController controller;
    private final OrderTaker orderTaker;
    private final KitchenQueue kitchenQueue;
    private final ServingQueue servingQueue;
    private final ScheduledExecutorService executor;
    private final CookSimulation cookSimulation;

    private int customerCount = 0;
    private final int servingLineCount = 0;

    public SimulationManager(MainController controller) {
        this.controller = controller;
        this.orderTaker = new OrderTaker();
        this.kitchenQueue = new KitchenQueue();
        this.servingQueue = new ServingQueue();
        this.executor = Executors.newScheduledThreadPool(4);
        this.cookSimulation = new CookSimulation(controller, kitchenQueue, servingQueue);
    }

    public void startSimulation(int customerInterval, int orderInterval, int cookingInterval) {
        executor.scheduleAtFixedRate(this::generateCustomer, 0, customerInterval, TimeUnit.MILLISECONDS);
        executor.scheduleAtFixedRate(this::processOrder, 0, orderInterval, TimeUnit.MILLISECONDS);
        cookSimulation.startCooking(cookingInterval);
    }

    public void stopSimulation() {
        executor.shutdown();
        cookSimulation.stopCooking();
    }

    private void generateCustomer() {
        customerCount++;
        Customer customer = new Customer(customerCount);

        Platform.runLater(() -> {
            controller.addCustomerToQueue(customer);
        });
    }

    private void processOrder() {
        if (!orderTaker.isBusy()) {
            int orderId = orderTaker.takeOrder();
            if (orderId != -1) {
                Order order = new Order(orderId);
                kitchenQueue.addOrder(order);

                Platform.runLater(() -> {
                    controller.removeCustomerFromQueue(orderId);
                    controller.updateOrderTakerStatus(orderId);
                    controller.updateKitchenQueue(kitchenQueue.getWaitingCount());
                    System.out.println("Заказ #" + orderId + " добавлен в кухонную очередь");
                });

                executor.schedule(() -> {
                    orderTaker.completeOrder();
                    Platform.runLater(() -> {
                        controller.updateOrderTakerStatus(-1);
                        System.out.println("Кассир свободен");
                    });
                }, 500, TimeUnit.MILLISECONDS);
            }
        }
    }
    public boolean isRunning() {
        return !executor.isShutdown();
    }
}
