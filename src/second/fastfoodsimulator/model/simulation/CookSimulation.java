package second.fastfoodsimulator.model.simulation;

import second.fastfoodsimulator.model.entities.Cook;
import second.fastfoodsimulator.model.entities.Order;
import second.fastfoodsimulator.model.queues.KitchenQueue;
import second.fastfoodsimulator.model.queues.ServingQueue;
import second.fastfoodsimulator.view.controllers.MainController;
import javafx.application.Platform;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CookSimulation {
    private final MainController controller;
    private final Cook cook;
    private final KitchenQueue kitchenQueue;
    private final ServingQueue servingQueue;
    private ScheduledExecutorService executor; // УБИРАЕМ FINAL

    public CookSimulation(MainController controller, KitchenQueue kitchenQueue, ServingQueue servingQueue) {
        this.controller = controller;
        this.cook = new Cook();
        this.kitchenQueue = kitchenQueue;
        this.servingQueue = servingQueue;
        this.executor = Executors.newScheduledThreadPool(1);
    }

    public void startCooking(int cookingInterval) {
        // ЕСЛИ EXECUTOR БЫЛ ЗАВЕРШЕН, СОЗДАЕМ НОВЫЙ
        if (executor == null || executor.isShutdown() || executor.isTerminated()) {
            executor = Executors.newScheduledThreadPool(1);
        }
        executor.scheduleAtFixedRate(this::processOrder, 0, cookingInterval, TimeUnit.MILLISECONDS);
    }

    public void stopCooking() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
            }
        }
    }

    private void processOrder() {
        if (!cook.isBusy()) {
            Order order = kitchenQueue.getNextOrder();
            if (order != null) {
                int orderId = cook.startCooking(order.getOrderId());

                Platform.runLater(() -> {
                    controller.updateCookStatus(orderId);
                    controller.updateKitchenQueue(kitchenQueue.getWaitingCount());
                    System.out.println("Повар начинает готовить заказ #" + orderId);
                });

                // Имитация приготовления заказа
                executor.schedule(() -> {
                    cook.completeCooking();
                    servingQueue.addReadyOrder(order);

                    Platform.runLater(() -> {
                        controller.updateCookStatus(-1);
                        controller.updateServingQueue(servingQueue.getReadyCount());
                        System.out.println("Заказ #" + orderId + " готов и передан на выдачу");
                    });
                }, 1000, TimeUnit.MILLISECONDS);
            }
        }
    }

    // ДОБАВЛЯЕМ МЕТОД ДЛЯ СБРОСА СОСТОЯНИЯ
    public void reset() {
        // Сбрасываем состояние повара
        cook.completeCooking();

        // Останавливаем и пересоздаем executor
        stopCooking();
        executor = Executors.newScheduledThreadPool(1);

        System.out.println("Состояние CookSimulation сброшено");
    }

}