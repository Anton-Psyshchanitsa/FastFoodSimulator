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
            // УДАЛИТЕ ЭТУ СТРОКУ:
            // controller.updateCustomerQueueCount(customerQueue.size());
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
                    // Передаем количество заказов на кухне
                    controller.updateKitchenQueue(kitchenQueue.getWaitingCount());
                    System.out.println("Заказ #" + orderId + " добавлен в кухонную очередь");
                });

                // Имитация обработки заказа
                executor.schedule(() -> {
                    orderTaker.completeOrder();
                    Platform.runLater(() -> {
                        controller.updateOrderTakerStatus(-1);
                        // После обработки обновляем количество заказов на кухне
                        controller.updateKitchenQueue(kitchenQueue.getWaitingCount());
                        System.out.println("Кассир свободен");
                    });
                }, 500, TimeUnit.MILLISECONDS);
            }
        }
    }
}
