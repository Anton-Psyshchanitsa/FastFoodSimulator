package second.fastfoodsimulator.model.simulation;

import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import second.fastfoodsimulator.model.entities.*;
import second.fastfoodsimulator.model.queues.KitchenQueue;
import second.fastfoodsimulator.model.queues.ServingQueue;
import second.fastfoodsimulator.model.queues.ServingLine;
import second.fastfoodsimulator.view.controllers.MainController;
import javafx.application.Platform;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class SimulationManager {
    private final MainController controller;
    private final OrderTaker orderTaker;
    private final KitchenQueue kitchenQueue;
    private final ServingQueue servingQueue;
    private final ServingLine servingLine;
    private ScheduledExecutorService executor; // УБИРАЕМ FINAL
    private final CookSimulation cookSimulation;
    private final ServerSimulation serverSimulation;

    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private int customerCount = 0;

    public SimulationManager(MainController controller) {
        this.controller = controller;
        this.orderTaker = new OrderTaker();
        this.kitchenQueue = new KitchenQueue();
        this.servingQueue = new ServingQueue();
        this.servingLine = new ServingLine();
        this.executor = Executors.newScheduledThreadPool(4); // ИНИЦИАЛИЗИРУЕМ ЗДЕСЬ
        this.cookSimulation = new CookSimulation(controller, kitchenQueue, servingQueue);
        this.serverSimulation = new ServerSimulation(controller, servingQueue, servingLine);
    }

    public void startSimulation(int customerInterval, int orderInterval, int cookingInterval, int servingInterval) {
        if (isRunning.get()) {
            System.out.println("Симуляция уже запущена");
            return;
        }

        try {
            validateIntervals(customerInterval, orderInterval, cookingInterval, servingInterval);

            // ЕСЛИ EXECUTOR БЫЛ ЗАВЕРШЕН, СОЗДАЕМ НОВЫЙ
            if (executor == null || executor.isShutdown() || executor.isTerminated()) {
                System.out.println("Создаем новый ScheduledExecutorService");
                executor = Executors.newScheduledThreadPool(4);
            }

            // СБРАСЫВАЕМ СОСТОЯНИЕ ПЕРЕД ЗАПУСКОМ
            resetSimulationState();

            isRunning.set(true);

            // Запускаем все компоненты симуляции
            executor.scheduleAtFixedRate(this::generateCustomer, 0, customerInterval, TimeUnit.MILLISECONDS);
            executor.scheduleAtFixedRate(this::processOrder, 0, orderInterval, TimeUnit.MILLISECONDS);
            cookSimulation.startCooking(cookingInterval);
            serverSimulation.startServing(servingInterval);

            System.out.println("Симуляция запущена успешно. Начинаем с чистого состояния.");

        } catch (IllegalArgumentException e) {
            Platform.runLater(() -> showErrorDialog(e.getMessage()));
        } catch (Exception e) {
            Platform.runLater(() -> showErrorDialog("Ошибка запуска симуляции: " + e.getMessage()));
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
            if (executor != null && !executor.isShutdown()) {
                executor.shutdown();
                if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            }

            System.out.println("Симуляция остановлена");

        } catch (InterruptedException e) {
            if (executor != null) {
                executor.shutdownNow();
            }
            Thread.currentThread().interrupt();
        }
    }

    private void resetSimulationState() {
        // Очищаем очереди
        kitchenQueue.clear();
        servingQueue.clear();
        servingLine.clear();

        // Сбрасываем счетчики
        customerCount = 0;

        // Сбрасываем состояния всех работников
        orderTaker.completeOrder();
        cookSimulation.reset();
        serverSimulation.reset();

        // Сбрасываем генератор заказов (теперь через публичный метод)
        second.fastfoodsimulator.util.OrderNumberGenerator.reset();

        System.out.println("Состояние симуляции полностью сброшено");
    }

    // ДОБАВЛЯЕМ МЕТОД ДЛЯ СБРОСА ГЕНЕРАТОРА ЗАКАЗОВ
    private void resetOrderNumberGenerator() {
        try {
            // Используем рефлексию для сброса счетчика
            java.lang.reflect.Field counterField = second.fastfoodsimulator.util.OrderNumberGenerator.class.getDeclaredField("counter");
            counterField.setAccessible(true);
            counterField.set(null, 0);
            System.out.println("Генератор номеров заказов сброшен");
        } catch (Exception e) {
            System.err.println("Не удалось сбросить генератор заказов: " + e.getMessage());
            // Создаем новый экземпляр OrderNumberGenerator если рефлексия не работает
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
            // Получаем следующего клиента
            Customer customer = controller.getNextCustomer();
            if (customer == null) return;

            int orderId = orderTaker.takeOrder();
            if (orderId != -1) {
                Order order = new Order(orderId);
                kitchenQueue.addOrder(order);

                // ДОБАВЛЯЕМ КЛИЕНТА В SERVING LINE
                servingLine.addCustomer(customer, orderId);

                Platform.runLater(() -> {
                    controller.removeCustomerFromQueue(customer.getCustomerId());
                    controller.updateOrderTakerStatus(orderId);
                    controller.updateKitchenQueue(kitchenQueue.getWaitingCount());
                    controller.updateWaitingCustomers(servingLine.getWaitingCustomerCount());
                    System.out.println("Заказ #" + orderId + " оформлен для клиента #" + customer.getCustomerId());
                });

                executor.schedule(() -> {
                    orderTaker.completeOrder();
                    Platform.runLater(() -> {
                        controller.updateOrderTakerStatus(-1);
                    });
                }, 500, TimeUnit.MILLISECONDS);
            } else {
                // Если не удалось оформить заказ, возвращаем клиента
                controller.returnCustomerToQueue(customer);
            }
        } catch (Exception e) {
            System.err.println("Ошибка при обработке заказа: " + e.getMessage());
            orderTaker.completeOrder();
        }
    }

    // ДОБАВЛЯЕМ МЕТОД ДЛЯ ПОКАЗА ОШИБОК
    private void showErrorDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка симуляции");
        alert.setHeaderText("Произошла ошибка");
        alert.setContentText(message);

        try {
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.getIcons().add(new Image(getClass().getResource("/styles/error-icon.png").toExternalForm()));
        } catch (Exception e) {
            System.err.println("Не удалось загрузить иконку ошибки: " + e.getMessage());
        }

        alert.showAndWait();
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

    public boolean isRunning() {
        return isRunning.get();
    }

    public KitchenQueue getKitchenQueue() {
        return kitchenQueue;
    }

    public ServingQueue getServingQueue() {
        return servingQueue;
    }

    public ServingLine getServingLine() {
        return servingLine;
    }
}