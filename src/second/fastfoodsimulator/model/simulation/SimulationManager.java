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
    private CooksManager cooksManager;
    private final ServerSimulation serverSimulation;

    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private int customerCount = 0;
    private int cooksCount = 2;

    public SimulationManager(MainController controller) {
        this.controller = controller;
        this.orderTaker = new OrderTaker();
        this.kitchenQueue = new KitchenQueue();
        this.servingQueue = new ServingQueue();
        this.servingLine = new ServingLine();
        this.executor = Executors.newScheduledThreadPool(6);
        this.serverSimulation = new ServerSimulation(controller, servingQueue, servingLine);
    }

    public void startSimulation(int customerInterval, int orderInterval, int cookingInterval, int servingInterval, int cooksCount) {
        if (isRunning.get()) {
            System.out.println("Симуляция уже запущена");
            return;
        }

        try {
            validateIntervals(customerInterval, orderInterval, cookingInterval, servingInterval);
            this.cooksCount = cooksCount;

            if (executor == null || executor.isShutdown() || executor.isTerminated()) {
                System.out.println("Создаем новый ScheduledExecutorService");
                executor = Executors.newScheduledThreadPool(4 + cooksCount);
            }

            this.cooksManager = new CooksManager(cooksCount);
            resetSimulationState();
            isRunning.set(true);

            // ЗАПУСКАЕМ ПРОЦЕСС ПРИГОТОВЛЕНИЯ С БОЛЕЕ ЧАСТОЙ ПЕРИОДИЧНОСТЬЮ
            executor.scheduleAtFixedRate(this::generateCustomer, 0, customerInterval, TimeUnit.MILLISECONDS);
            executor.scheduleAtFixedRate(this::processOrder, 0, orderInterval, TimeUnit.MILLISECONDS);
            executor.scheduleAtFixedRate(this::processCooking, 0, Math.max(100, cookingInterval / 2), TimeUnit.MILLISECONDS); // УВЕЛИЧИВАЕМ ЧАСТОТУ
            serverSimulation.startServing(servingInterval);

            System.out.println("Симуляция запущена успешно с " + cooksCount + " поварами");

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
        kitchenQueue.clear();
        servingQueue.clear();
        servingLine.clear();
        customerCount = 0;
        orderTaker.completeOrder();
        if (cooksManager != null) {
            cooksManager.reset();
        }
        serverSimulation.reset();
        second.fastfoodsimulator.util.OrderNumberGenerator.reset();

        System.out.println("Состояние симуляции полностью сброшено");
    }

    private void processCooking() {
        if (!isRunning.get()) return;

        try {
            // ОБРАБАТЫВАЕМ ВСЕХ СВОБОДНЫХ ПОВАРОВ ОДНОВРЕМЕННО
            int processed = 0;
            int maxProcessPerCycle = cooksManager.getTotalCooksCount(); // Максимум поваров за цикл

            while (processed < maxProcessPerCycle) {
                Cook availableCook = cooksManager.getAvailableCook();
                if (availableCook == null) break; // Нет свободных поваров

                Order order = kitchenQueue.getNextOrder();
                if (order == null) break; // Нет заказов в очереди

                startCooking(availableCook, order);
                processed++;
            }

            // ОБНОВЛЯЕМ СТАТУСЫ ВСЕХ ПОВАРОВ ДАЖЕ ЕСЛИ НИЧЕГО НЕ ПРОИСХОДИТ
            Platform.runLater(() -> {
                controller.updateCooksStatus(cooksManager);
            });

        } catch (Exception e) {
            System.err.println("Ошибка при обработке приготовления: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void startCooking(Cook cook, Order order) {
        int orderId = cook.startCooking(order.getOrderId());

        Platform.runLater(() -> {
            controller.updateCookStatus(cook.getCookId(), orderId);
            controller.updateKitchenQueue(kitchenQueue.getWaitingCount());
            controller.updateCooksStatus(cooksManager);
            System.out.println("Повар #" + cook.getCookId() + " начинает готовить заказ #" + orderId);
        });

        // Имитация приготовления заказа
        executor.schedule(() -> {
            cook.completeCooking();
            servingQueue.addReadyOrder(order);

            Platform.runLater(() -> {
                controller.updateCookStatus(cook.getCookId(), -1);
                controller.updateServingQueue(servingQueue.getReadyCount());
                controller.updateCooksStatus(cooksManager);
                System.out.println("Повар #" + cook.getCookId() + " завершил заказ #" + orderId);
            });
        }, 1000, TimeUnit.MILLISECONDS);
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
            Customer customer = controller.getNextCustomer();
            if (customer == null) return;

            int orderId = orderTaker.takeOrder();
            if (orderId != -1) {
                Order order = new Order(orderId);
                kitchenQueue.addOrder(order);

                servingLine.addCustomer(customer, orderId);

                // ЗАПОМИНАЕМ ВРЕМЯ НАЧАЛА ОЖИДАНИЯ
                customer.setOrderStartTime(System.currentTimeMillis());

                Platform.runLater(() -> {
                    controller.removeCustomerFromQueue(customer.getCustomerId());
                    controller.updateOrderTakerStatus(orderId);
                    controller.updateKitchenQueue(kitchenQueue.getWaitingCount());
                    controller.updateWaitingCustomers(servingLine.getWaitingCustomerCount());
                    // УВЕДОМЛЯЕМ О СОЗДАНИИ ЗАКАЗА
                    controller.orderCreated(orderId);
                    System.out.println("Заказ #" + orderId + " оформлен для клиента #" + customer.getCustomerId());
                });

                executor.schedule(() -> {
                    orderTaker.completeOrder();
                    Platform.runLater(() -> {
                        controller.updateOrderTakerStatus(-1);
                    });
                }, 500, TimeUnit.MILLISECONDS);
            } else {
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

    public CooksManager getCooksManager() {
        return cooksManager;
    }

}