package second.fastfoodsimulator.view.controllers;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;
import second.fastfoodsimulator.model.entities.Customer;
import second.fastfoodsimulator.model.simulation.SimulationManager;

import java.util.ArrayList;
import java.util.List;

public class MainController {
    @FXML
    private TextField customerIntervalField;
    @FXML
    private TextField orderIntervalField;

    @FXML
    private TextField cookingIntervalField;

    @FXML
    private TextField servingIntervalField;
    @FXML
    private Label customerQueueCount;
    @FXML
    private Label currentOrderTaker;
    @FXML
    private Label currentKitchenOrder;
    @FXML
    private Label kitchenQueueCount;
    @FXML
    private Label currentPickupOrder;
    @FXML
    private Label servingQueueCount;

    @FXML
    private Label waitingCustomersCount;

    @FXML
    private VBox waitingCustomersBox;

    @FXML
    private VBox customerQueueBox;
    @FXML
    private VBox kitchenQueueBox;
    @FXML
    private VBox servingQueueBox;
    @FXML
    private Circle orderTakerIndicator;
    @FXML
    private Circle kitchenIndicator;
    @FXML
    private Circle cookIndicator;
    @FXML
    private Circle serverIndicator;
    @FXML
    private Circle customerIndicator;

    @FXML
    private Button startButton;
    @FXML
    private Button stopButton;

    private final SimulationManager simulationManager;
    private final List<Customer> customerQueue = new ArrayList<>();
    private final List<Label> customerLabels = new ArrayList<>();

    private Animation orderTakerPulseAnimation;
    private Animation customerPulseAnimation;
    private Animation kitchenPulseAnimation;
    private Animation cookPulseAnimation;
    private Animation serverPulseAnimation;

    public MainController() {
        this.simulationManager = new SimulationManager(this);
    }

    @FXML
    private void initialize() {
        System.out.println("Инициализация MainController");

        // Проверяем, что все элементы не null
        if (customerIndicator == null) System.err.println("customerIndicator is null!");
        if (orderTakerIndicator == null) System.err.println("orderTakerIndicator is null!");
        if (kitchenIndicator == null) System.err.println("kitchenIndicator is null!");
        if (cookIndicator == null) System.err.println("cookIndicator is null!");
        if (serverIndicator == null) System.err.println("serverIndicator is null!");

        initializeAnimations();
        setupEventHandlers();

        startButton.setOnAction(event -> {
            System.out.println("Нажата кнопка Старт");
            startSimulation();
        });

        stopButton.setOnAction(event -> {
            System.out.println("Нажата кнопка Стоп");
            stopSimulation();
        });

        resetUI();
    }

    @FXML
    private void startSimulation() {
        try {
            int customerInterval = Integer.parseInt(customerIntervalField.getText());
            int orderInterval = Integer.parseInt(orderIntervalField.getText());
            int cookingInterval = Integer.parseInt(cookingIntervalField.getText());
            int servingInterval = Integer.parseInt(servingIntervalField.getText());

            simulationManager.startSimulation(customerInterval, orderInterval, cookingInterval, servingInterval);

            // Блокируем поля ввода во время симуляции
            customerIntervalField.setDisable(true);
            orderIntervalField.setDisable(true);
            cookingIntervalField.setDisable(true);
            servingIntervalField.setDisable(true);
            startButton.setDisable(true);
            stopButton.setDisable(false);

        } catch (NumberFormatException e) {
            showError("Неверный формат ввода. Введите целые числа");
        } catch (Exception e) {
            showError("Ошибка запуска симуляции: " + e.getMessage());
        }
    }

    @FXML
    private void stopSimulation() {
        simulationManager.stopSimulation();

        // Разблокируем поля ввода
        customerIntervalField.setDisable(false);
        orderIntervalField.setDisable(false);
        cookingIntervalField.setDisable(false);
        servingIntervalField.setDisable(false);
        startButton.setDisable(false);
        stopButton.setDisable(true);

        resetUI();
    }

    private void resetUI() {
        customerQueueCount.setText("0");
        currentOrderTaker.setText("Нет заказа");
        currentKitchenOrder.setText("Нет заказа");
        kitchenQueueCount.setText("0");
        currentPickupOrder.setText("Нет заказа");
        servingQueueCount.setText("0");
        waitingCustomersCount.setText("0"); // СБРАСЫВАЕМ НОВЫЙ СЧЕТЧИК

        customerQueue.clear();
        customerLabels.clear();
        customerQueueBox.getChildren().clear();
        kitchenQueueBox.getChildren().clear();
        servingQueueBox.getChildren().clear();
        waitingCustomersBox.getChildren().clear(); // ОЧИЩАЕМ НОВЫЙ БЛОК

        customerIndicator.setFill(Color.GRAY);
        orderTakerIndicator.setFill(Color.GRAY);
        kitchenIndicator.setFill(Color.GRAY);
        cookIndicator.setFill(Color.GRAY);
        serverIndicator.setFill(Color.GRAY);

        // Останавливаем анимации
        stopAllAnimations();

        System.out.println("UI полностью сброшен");
    }

    private void stopAllAnimations() {
        if (customerPulseAnimation != null) {
            customerPulseAnimation.stop();
            customerPulseAnimation = null;
        }
        if (orderTakerPulseAnimation != null) {
            orderTakerPulseAnimation.stop();
            orderTakerPulseAnimation = null;
        }
        if (kitchenPulseAnimation != null) {
            kitchenPulseAnimation.stop();
            kitchenPulseAnimation = null;
        }
        if (cookPulseAnimation != null) {
            cookPulseAnimation.stop();
            cookPulseAnimation = null;
        }
        if (serverPulseAnimation != null) {
            serverPulseAnimation.stop();
            serverPulseAnimation = null;
        }
    }

    public void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка симуляции");
            alert.setHeaderText("Произошла ошибка");
            alert.setContentText(message);

            // Устанавливаем иконку окна
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            alert.showAndWait();
        });
    }

    public void addCustomerToQueue(Customer customer) {
        System.out.println("Добавление клиента #" + customer.getCustomerId()); // Используем getCustomerId()

        synchronized (customerQueue) {
            customerQueue.add(customer);
        }

        Platform.runLater(() -> {
            updateCustomerQueueCount(customerQueue.size());

            Label customerLabel = new Label("Клиент #" + customer.getCustomerId()); // Используем getCustomerId()
            customerLabel.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 5; -fx-background-radius: 5;");

            FadeTransition fadeIn = new FadeTransition(Duration.millis(500), customerLabel);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);

            TranslateTransition slideIn = new TranslateTransition(Duration.millis(500), customerLabel);
            slideIn.setFromY(-20);
            slideIn.setToY(0);

            ParallelTransition parallelTransition = new ParallelTransition(fadeIn, slideIn);

            customerQueueBox.getChildren().add(customerLabel);
            customerLabels.add(customerLabel);

            parallelTransition.play();
            updateCustomerIndicator();
        });
    }

    public void removeCustomerFromQueue(int customerId) { // Меняем параметр на customerId
        System.out.println("Удаление клиента #" + customerId);

        synchronized (customerQueue) {
            customerQueue.removeIf(customer -> customer.getCustomerId() == customerId);
        }

        Platform.runLater(() -> {
            updateCustomerQueueCount(customerQueue.size());

            customerLabels.removeIf(label -> label.getText().contains("Клиент #" + customerId));
            customerQueueBox.getChildren().removeIf(node ->
                    node instanceof Label && ((Label) node).getText().contains("Клиент #" + customerId)
            );

            updateCustomerIndicator();
        });
    }

    public void updateCustomerQueueCount(int count) {
        System.out.println("Обновление счетчика клиентов: " + count);
        customerQueueCount.setText(String.valueOf(count));
    }

    public void updateWaitingCustomers(int count) {
        Platform.runLater(() -> {
            waitingCustomersCount.setText(String.valueOf(count));
            updateWaitingCustomersList(); // ОБНОВЛЯЕМ СПИСОК ПРИ ИЗМЕНЕНИИ КОЛИЧЕСТВА
        });
    }

    private void updateWaitingCustomersList() {
        Platform.runLater(() -> {
            waitingCustomersBox.getChildren().clear();

            List<Customer> waitingCustomers = simulationManager.getServingLine().getAllWaitingCustomers();

            if (waitingCustomers.isEmpty()) {
                Label emptyLabel = new Label("Нет клиентов");
                emptyLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-style: italic;");
                waitingCustomersBox.getChildren().add(emptyLabel);
            } else {
                for (Customer customer : waitingCustomers) {
                    Label customerLabel = new Label("Клиент #" + customer.getCustomerId() + " (Заказ #" + customer.getOrderId() + ")");
                    customerLabel.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-padding: 5; -fx-background-radius: 5;");

                    FadeTransition fadeIn = new FadeTransition(Duration.millis(300), customerLabel);
                    fadeIn.setFromValue(0.0);
                    fadeIn.setToValue(1.0);

                    waitingCustomersBox.getChildren().add(customerLabel);
                    fadeIn.play();
                }
            }
        });
    }

    public void updateOrderTakerStatus(int orderId) {
        System.out.println("Обновление статуса кассира: orderId=" + orderId);

        Platform.runLater(() -> {
            if (orderId == -1) {
                currentOrderTaker.setText("Нет заказа");
                orderTakerIndicator.setFill(Color.GRAY);
                if (orderTakerPulseAnimation != null) {
                    orderTakerPulseAnimation.stop();
                    orderTakerPulseAnimation = null;
                }
            } else {
                currentOrderTaker.setText("Заказ #" + orderId);
                orderTakerIndicator.setFill(Color.LIMEGREEN);

                if (orderTakerPulseAnimation != null) {
                    orderTakerPulseAnimation.stop();
                }

                ScaleTransition pulse = new ScaleTransition(Duration.millis(500), orderTakerIndicator);
                pulse.setFromX(1.0);
                pulse.setFromY(1.0);
                pulse.setToX(0.6);
                pulse.setToY(0.6);
                pulse.setAutoReverse(true);
                pulse.setCycleCount(Animation.INDEFINITE);
                pulse.play();

                orderTakerPulseAnimation = pulse;
            }
        });
    }

    // ДОБАВЛЯЕМ МЕТОД ДЛЯ ПРОВЕРКИ НАЛИЧИЯ КЛИЕНТОВ
    public boolean hasWaitingCustomers() {
        synchronized (customerQueue) {
            return !customerQueue.isEmpty();
        }
    }

    public Customer getNextCustomer() {
        synchronized (customerQueue) {
            if (customerQueue.isEmpty()) {
                return null;
            }
            return customerQueue.remove(0);
        }
    }

    public void returnCustomerToQueue(Customer customer) {
        synchronized (customerQueue) {
            customerQueue.add(0, customer); // Возвращаем в начало очереди
        }
        Platform.runLater(() -> {
            updateCustomerQueueCount(customerQueue.size());
            // Можно добавить анимацию или сообщение
            System.out.println("Клиент #" + customer.getCustomerId() + " возвращен в очередь");
        });
    }


    public void updateKitchenQueue(int count) {
        System.out.println("Обновление кухни: " + count);

        Platform.runLater(() -> {
            kitchenQueueBox.getChildren().clear();
            kitchenQueueCount.setText(String.valueOf(count));

            if (count > 0) {
                // Получаем реальные ID заказов из кухонной очереди
                List<Integer> orderIds = simulationManager.getKitchenQueue().getOrderIds();

                for (int i = 0; i < orderIds.size(); i++) {
                    int orderId = orderIds.get(i);
                    Label orderLabel = new Label("Заказ #" + orderId);
                    orderLabel.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 5; -fx-background-radius: 5;");

                    FadeTransition fadeIn = new FadeTransition(Duration.millis(300), orderLabel);
                    fadeIn.setFromValue(0.0);
                    fadeIn.setToValue(1.0);

                    kitchenQueueBox.getChildren().add(orderLabel);
                    fadeIn.play();
                }

                kitchenIndicator.setFill(Color.ORANGERED);

                if (kitchenPulseAnimation != null) {
                    kitchenPulseAnimation.stop();
                }

                ScaleTransition pulse = new ScaleTransition(Duration.millis(700), kitchenIndicator);
                pulse.setFromX(1.0);
                pulse.setFromY(1.0);
                pulse.setToX(0.7);
                pulse.setToY(0.7);
                pulse.setAutoReverse(true);
                pulse.setCycleCount(Animation.INDEFINITE);
                pulse.play();

                kitchenPulseAnimation = pulse;
            } else {
                Label emptyLabel = new Label("Нет заказов");
                emptyLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-style: italic;");
                kitchenQueueBox.getChildren().add(emptyLabel);

                kitchenIndicator.setFill(Color.GRAY);
                if (kitchenPulseAnimation != null) {
                    kitchenPulseAnimation.stop();
                    kitchenPulseAnimation = null;
                }
            }
        });
    }

    public void updateCookStatus(int orderId) {
        System.out.println("Обновление статуса повара: orderId=" + orderId);

        Platform.runLater(() -> {
            // Проверяем, что cookIndicator не null
            if (cookIndicator == null) {
                System.err.println("cookIndicator is null!");
                return;
            }

            if (orderId == -1) {
                currentKitchenOrder.setText("Нет заказа");
                cookIndicator.setFill(Color.GRAY);
                if (cookPulseAnimation != null) {
                    cookPulseAnimation.stop();
                    cookPulseAnimation = null;
                }
            } else {
                currentKitchenOrder.setText("Готовит заказ #" + orderId);
                cookIndicator.setFill(Color.ORANGERED);

                if (cookPulseAnimation != null) {
                    cookPulseAnimation.stop();
                }

                ScaleTransition pulse = new ScaleTransition(Duration.millis(600), cookIndicator);
                pulse.setFromX(1.0);
                pulse.setFromY(1.0);
                pulse.setToX(0.7);
                pulse.setToY(0.7);
                pulse.setAutoReverse(true);
                pulse.setCycleCount(Animation.INDEFINITE);
                pulse.play();

                cookPulseAnimation = pulse;
            }
        });
    }

    public void updateServingQueue(int count) {
        System.out.println("Обновление зоны выдачи: " + count);

        Platform.runLater(() -> {
            servingQueueBox.getChildren().clear();
            servingQueueCount.setText(String.valueOf(count));

            if (count > 0) {
                // Получаем реальные ID заказов из очереди выдачи
                List<Integer> orderIds = simulationManager.getServingQueue().getReadyOrderIds();

                for (int i = 0; i < orderIds.size(); i++) {
                    int orderId = orderIds.get(i);
                    Label orderLabel = new Label("Заказ #" + orderId);
                    orderLabel.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-padding: 5; -fx-background-radius: 5;");

                    FadeTransition fadeIn = new FadeTransition(Duration.millis(300), orderLabel);
                    fadeIn.setFromValue(0.0);
                    fadeIn.setToValue(1.0);

                    servingQueueBox.getChildren().add(orderLabel);
                    fadeIn.play();
                }

                serverIndicator.setFill(Color.LIMEGREEN);

                if (serverPulseAnimation != null) {
                    serverPulseAnimation.stop();
                }

                ScaleTransition pulse = new ScaleTransition(Duration.millis(800), serverIndicator);
                pulse.setFromX(1.0);
                pulse.setFromY(1.0);
                pulse.setToX(0.7);
                pulse.setToY(0.7);
                pulse.setAutoReverse(true);
                pulse.setCycleCount(Animation.INDEFINITE);
                pulse.play();

                serverPulseAnimation = pulse;
            } else {
                Label emptyLabel = new Label("Нет заказов");
                emptyLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-style: italic;");
                servingQueueBox.getChildren().add(emptyLabel);

                serverIndicator.setFill(Color.GRAY);
                if (serverPulseAnimation != null) {
                    serverPulseAnimation.stop();
                    serverPulseAnimation = null;
                }
            }
        });
    }

    private void updateCustomerIndicator() {
        int count = customerQueue.size();

        Platform.runLater(() -> {
            if (count > 0) {
                customerIndicator.setFill(Color.LIMEGREEN);

                if (customerPulseAnimation != null) {
                    customerPulseAnimation.stop();
                }

                ScaleTransition pulse = new ScaleTransition(Duration.millis(600), customerIndicator);
                pulse.setFromX(1.0);
                pulse.setFromY(1.0);
                pulse.setToX(0.7);
                pulse.setToY(0.7);
                pulse.setAutoReverse(true);
                pulse.setCycleCount(Animation.INDEFINITE);
                pulse.play();

                customerPulseAnimation = pulse;
            } else {
                customerIndicator.setFill(Color.GRAY);
                if (customerPulseAnimation != null) {
                    customerPulseAnimation.stop();
                    customerPulseAnimation = null;
                }
            }
        });
    }

    public void updateServerStatus(int orderId) {
        Platform.runLater(() -> {
            if (orderId == -1) {
                currentPickupOrder.setText("Нет заказа");
                serverIndicator.setFill(Color.GRAY);
                if (serverPulseAnimation != null) {
                    serverPulseAnimation.stop();
                    serverPulseAnimation = null;
                }
            } else {
                currentPickupOrder.setText("Выдает заказ #" + orderId);
                serverIndicator.setFill(Color.GOLD);

                if (serverPulseAnimation != null) {
                    serverPulseAnimation.stop();
                }

                ScaleTransition pulse = new ScaleTransition(Duration.millis(600), serverIndicator);
                pulse.setFromX(1.0);
                pulse.setFromY(1.0);
                pulse.setToX(0.7);
                pulse.setToY(0.7);
                pulse.setAutoReverse(true);
                pulse.setCycleCount(Animation.INDEFINITE);
                pulse.play();

                serverPulseAnimation = pulse;
            }
        });
    }

    public void completeOrder(int orderId) {
        Platform.runLater(() -> {
            System.out.println("Заказ #" + orderId + " завершен. Клиент покидает ресторан.");
            // Автоматически обновляем список через updateWaitingCustomers
        });
    }

    private void initializeAnimations() {
        customerIndicator.setFill(Color.GRAY);
        orderTakerIndicator.setFill(Color.GRAY);
        kitchenIndicator.setFill(Color.GRAY);
        cookIndicator.setFill(Color.GRAY);
        serverIndicator.setFill(Color.GRAY);
    }

    private void setupEventHandlers() {
        customerIntervalField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) customerIntervalField.setText(oldVal);
        });
        orderIntervalField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) orderIntervalField.setText(oldVal);
        });
    }
}