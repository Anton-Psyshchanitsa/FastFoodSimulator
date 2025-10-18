package second.fastfoodsimulator.model.simulation;

import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import second.fastfoodsimulator.model.entities.*;
import second.fastfoodsimulator.model.queues.KitchenQueue;
import second.fastfoodsimulator.model.queues.ServingQueue;
import second.fastfoodsimulator.view.controllers.MainController;
import javafx.application.Platform;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class SimulationManager {
    private final MainController controller;
    private final OrderTaker orderTaker;
    private final KitchenQueue kitchenQueue;
    private final ServingQueue servingQueue;
    private final ScheduledExecutorService executor;
    private final CookSimulation cookSimulation;
    private final ServerSimulation serverSimulation;

    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private int customerCount = 0;

    public SimulationManager(MainController controller) {
        this.controller = controller;
        this.orderTaker = new OrderTaker();
        this.kitchenQueue = new KitchenQueue();
        this.servingQueue = new ServingQueue();
        this.executor = Executors.newScheduledThreadPool(4);
        this.cookSimulation = new CookSimulation(controller, kitchenQueue, servingQueue);
        this.serverSimulation = new ServerSimulation(controller, servingQueue);
    }

    public void startSimulation(int customerInterval, int orderInterval, int cookingInterval, int servingInterval) {
        if (isRunning.get()) {
            System.out.println("Симуляция уже запущена");
            return;
        }

        try {
            validateIntervals(customerInterval, orderInterval, cookingInterval, servingInterval);

            isRunning.set(true);

            // Запускаем все компоненты симуляции
            executor.scheduleAtFixedRate(this::generateCustomer, 0, customerInterval, TimeUnit.MILLISECONDS);
            executor.scheduleAtFixedRate(this::processOrder, 0, orderInterval, TimeUnit.MILLISECONDS);
            cookSimulation.startCooking(cookingInterval);
            serverSimulation.startServing(servingInterval);

            System.out.println("Симуляция запущена успешно");

        } catch (IllegalArgumentException e) {
            Platform.runLater(() -> controller.showError(e.getMessage()));
        } catch (Exception e) {
            Platform.runLater(() -> controller.showError("Ошибка запуска симуляции: " + e.getMessage()));
            stopSimulation();
        }
    }

    public void stopSimulation() {
        if (!isRunning.get()) {
            return;
        }

        isRunning.set(false);

        try {
            // Останавливаем все компоненты
            cookSimulation.stopCooking();
            serverSimulation.stopServing();

            // Завершаем executor
            executor.shutdown();
            if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }

            System.out.println("Симуляция остановлена");

        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private void validateIntervals(int... intervals) {
        for (int interval : intervals) {
            if (interval <= 0) {
                throw new IllegalArgumentException("Все интервалы должны быть положительными числами");
            }
            if (interval < 100) {
                throw new IllegalArgumentException("Интервалы не должны быть меньше 100 мс для стабильной работы");
            }
        }
    }

    private void generateCustomer() {
        if (!isRunning.get()) return;

        try {
            customerCount++;
            Customer customer = new Customer(customerCount);

            Platform.runLater(() -> {
                controller.addCustomerToQueue(customer);
            });

        } catch (Exception e) {
            System.err.println("Ошибка при генерации клиента: " + e.getMessage());
        }
    }

    private void processOrder() {
        if (!isRunning.get() || orderTaker.isBusy()) return;

        try {
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

                // Имитация обработки заказа кассиром
                executor.schedule(() -> {
                    orderTaker.completeOrder();
                    Platform.runLater(() -> {
                        controller.updateOrderTakerStatus(-1);
                    });
                }, 500, TimeUnit.MILLISECONDS);
            }
        } catch (Exception e) {
            System.err.println("Ошибка при обработке заказа: " + e.getMessage());
            orderTaker.completeOrder(); // Сбрасываем состояние в случае ошибки
        }
    }

    public boolean isRunning() {
        return isRunning.get();
    }

    public KitchenQueue getKitchenQueue() {
        return kitchenQueue;
    }

    public ServingQueue getServingQueue() {
        return servingQueue;
    }
}