package second.fastfoodsimulator.model.simulation;

import second.fastfoodsimulator.model.entities.Order;
import second.fastfoodsimulator.model.entities.Server;
import second.fastfoodsimulator.model.queues.ServingQueue;
import second.fastfoodsimulator.model.queues.ServingLine;
import second.fastfoodsimulator.view.controllers.MainController;
import javafx.application.Platform;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ServerSimulation {
    private final MainController controller;
    private final Server server;
    private final ServingQueue servingQueue;
    private final ServingLine servingLine;
    private ScheduledExecutorService executor; // УБИРАЕМ FINAL

    public ServerSimulation(MainController controller, ServingQueue servingQueue, ServingLine servingLine) {
        this.controller = controller;
        this.server = new Server();
        this.servingQueue = servingQueue;
        this.servingLine = servingLine;
        this.executor = Executors.newScheduledThreadPool(1);
    }

    public void startServing(int servingInterval) {
        // ЕСЛИ EXECUTOR БЫЛ ЗАВЕРШЕН, СОЗДАЕМ НОВЫЙ
        if (executor == null || executor.isShutdown() || executor.isTerminated()) {
            executor = Executors.newScheduledThreadPool(1);
        }
        executor.scheduleAtFixedRate(this::processServing, 0, servingInterval, TimeUnit.MILLISECONDS);
    }

    public void stopServing() {
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

    private void processServing() {
        if (!server.isBusy()) {
            Order order = servingQueue.getNextReadyOrder();
            if (order != null) {
                int orderId = server.startServing(order.getOrderId());

                Platform.runLater(() -> {
                    controller.updateServerStatus(orderId);
                    controller.updateServingQueue(servingQueue.getReadyCount());
                    System.out.println("Сервер начинает выдавать заказ #" + orderId);
                });

                // Имитация выдачи заказа
                executor.schedule(() -> {
                    server.completeServing();
                    order.setState(Order.OrderState.COMPLETED);

                    // УДАЛЯЕМ КЛИЕНТА ИЗ SERVING LINE ПО orderId
                    servingLine.removeCustomerByOrderId(orderId);

                    Platform.runLater(() -> {
                        controller.updateServerStatus(-1);
                        controller.updateWaitingCustomers(servingLine.getWaitingCustomerCount()); // ОБНОВЛЯЕМ СЧЕТЧИК
                        controller.completeOrder(orderId);
                        System.out.println("Заказ #" + orderId + " выдан клиенту");
                    });
                }, 800, TimeUnit.MILLISECONDS);
            }
        }
    }

    // ДОБАВЛЯЕМ МЕТОД ДЛЯ СБРОСА СОСТОЯНИЯ
    public void reset() {
        // Сбрасываем состояние сервера
        server.completeServing();

        // Останавливаем и пересоздаем executor
        stopServing();
        executor = Executors.newScheduledThreadPool(1);

        System.out.println("Состояние ServerSimulation сброшено");
    }

}