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
import second.fastfoodsimulator.model.StatisticsManager;
import second.fastfoodsimulator.model.entities.Cook;
import second.fastfoodsimulator.model.entities.Customer;
import second.fastfoodsimulator.model.entities.CooksManager;
import second.fastfoodsimulator.model.entities.Server;
import second.fastfoodsimulator.model.entities.ServersManager;
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
    private TextField cooksCountField;
    @FXML
    private TextField serversCountField;

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

    // Статистика
    @FXML private Label totalCustomersLabel;
    @FXML private Label totalOrdersLabel;
    @FXML private Label avgWaitTimeLabel;
    @FXML private Label maxCustomerQueueLabel;
    @FXML private Label maxKitchenQueueLabel;
    @FXML private Label currentSpeedLabel;

    // Повара
    @FXML private Label activeCooksLabel;

    // Официанты
    @FXML private Label activeServersLabel;

    @FXML
    private VBox customerQueueBox;
    @FXML
    private VBox kitchenQueueBox;
    @FXML
    private VBox servingQueueBox;
    @FXML
    private VBox waitingCustomersBox;
    @FXML
    private VBox cooksStatusBox;
    @FXML
    private VBox serversStatusBox;

    @FXML
    private Circle orderTakerIndicator;
    @FXML
    private Circle customerIndicator;
    @FXML
    private Circle serverIndicator;
    @FXML
    private Circle cooksIndicator;

    @FXML
    private Button startButton;
    @FXML
    private Button stopButton;

    private final SimulationManager simulationManager;
    private final StatisticsManager statisticsManager;
    private final List<Customer> customerQueue = new ArrayList<>();
    private final List<Label> customerLabels = new ArrayList<>();

    private Animation orderTakerPulseAnimation;
    private Animation customerPulseAnimation;
    private Animation serverPulseAnimation;
    private Animation cooksPulseAnimation;

    public MainController() {
        this.simulationManager = new SimulationManager(this);
        this.statisticsManager = new StatisticsManager();
    }

    @FXML
    private void initialize() {
        System.out.println("Инициализация MainController с новым UI");

        // Проверяем, что все элементы не null
        checkComponent("customerIntervalField", customerIntervalField);
        checkComponent("orderIntervalField", orderIntervalField);
        checkComponent("cookingIntervalField", cookingIntervalField);
        checkComponent("servingIntervalField", servingIntervalField);
        checkComponent("cooksCountField", cooksCountField);
        checkComponent("serversCountField", serversCountField);
        checkComponent("customerIndicator", customerIndicator);
        checkComponent("orderTakerIndicator", orderTakerIndicator);
        checkComponent("serverIndicator", serverIndicator);

        // Если serversCountField все еще null, создаем временное решение
        if (serversCountField == null) {
            System.err.println("CRITICAL: serversCountField is null! Creating temporary field.");
            serversCountField = new TextField();
            serversCountField.setText("1");
        }

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

    private void checkComponent(String name, Object component) {
        if (component == null) {
            System.err.println("ERROR: " + name + " is null!");
        } else {
            System.out.println("OK: " + name + " initialized");
        }
    }

    private void resetUI() {
        customerQueueCount.setText("0");
        currentOrderTaker.setText("Нет заказа");
        currentKitchenOrder.setText("Нет заказа");
        kitchenQueueCount.setText("0");
        currentPickupOrder.setText("Нет заказа");
        servingQueueCount.setText("0");
        waitingCustomersCount.setText("0");
        activeCooksLabel.setText("(0/0 активны)");
        activeServersLabel.setText("(0/0 активны)");

        customerQueue.clear();
        customerLabels.clear();
        customerQueueBox.getChildren().clear();
        kitchenQueueBox.getChildren().clear();
        servingQueueBox.getChildren().clear();
        waitingCustomersBox.getChildren().clear();
        cooksStatusBox.getChildren().clear();
        serversStatusBox.getChildren().clear();

        customerIndicator.setFill(Color.GRAY);
        orderTakerIndicator.setFill(Color.GRAY);
        serverIndicator.setFill(Color.GRAY);
        cooksIndicator.setFill(Color.GRAY);

        stopAllAnimations();

        statisticsManager.reset();
        updateStatistics();

        System.out.println("UI полностью сброшен для нового дизайна");
    }

    @FXML
    private void startSimulation() {
        try {
            statisticsManager.startSimulation();

            int customerInterval = Integer.parseInt(customerIntervalField.getText());
            int orderInterval = Integer.parseInt(orderIntervalField.getText());
            int cookingInterval = Integer.parseInt(cookingIntervalField.getText());
            int servingInterval = Integer.parseInt(servingIntervalField.getText());
            int cooksCount = Integer.parseInt(cooksCountField.getText());
            int serversCount = Integer.parseInt(serversCountField.getText());

            if (cooksCount <= 0 || cooksCount > 10) {
                showError("Количество поваров должно быть от 1 до 10");
                return;
            }
            if (serversCount <= 0 || serversCount > 10) {
                showError("Количество официантов должно быть от 1 до 10");
                return;
            }

            simulationManager.startSimulation(customerInterval, orderInterval, cookingInterval,
                    servingInterval, cooksCount, serversCount);

            // Блокируем поля ввода во время симуляции
            customerIntervalField.setDisable(true);
            orderIntervalField.setDisable(true);
            cookingIntervalField.setDisable(true);
            servingIntervalField.setDisable(true);
            cooksCountField.setDisable(true);
            serversCountField.setDisable(true);
            startButton.setDisable(true);
            stopButton.setDisable(false);

            updateStatistics();

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
        cooksCountField.setDisable(false);
        serversCountField.setDisable(false);
        startButton.setDisable(false);
        stopButton.setDisable(true);

        resetUI();
    }

    public void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка симуляции");
            alert.setHeaderText("Произошла ошибка");
            alert.setContentText(message);

            try {
                Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                stage.getIcons().add(new Image("/styles/error-icon.png"));
            } catch (Exception e) {
                System.err.println("Не удалось загрузить иконку ошибки: " + e.getMessage());
            }

            alert.showAndWait();
        });
    }

    public void addCustomerToQueue(Customer customer) {
        System.out.println("Добавление клиента #" + customer.getCustomerId());

        // СБОР СТАТИСТИКИ - КЛИЕНТ ПРИШЕЛ
        statisticsManager.customerArrived();

        synchronized (customerQueue) {
            customerQueue.add(customer);
        }

        Platform.runLater(() -> {
            updateCustomerQueueCount(customerQueue.size());

            Label customerLabel = new Label("Клиент #" + customer.getCustomerId());
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
            updateStatistics();
        });
    }

    public void removeCustomerFromQueue(int customerId) {
        System.out.println("Удаление клиента #" + customerId);

        // СБОР СТАТИСТИКИ - КЛИЕНТ ОБСЛУЖЕН
        statisticsManager.customerServed();

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
            updateStatistics();
        });
    }

    public void updateCustomerQueueCount(int count) {
        // СБОР СТАТИСТИКИ - ОБНОВЛЕНИЕ ОЧЕРЕДИ КЛИЕНТОВ
        statisticsManager.updateCustomerQueue(count);

        Platform.runLater(() -> {
            customerQueueCount.setText(String.valueOf(count));
            updateStatistics();
        });
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
            customerQueue.add(0, customer);
        }
        Platform.runLater(() -> {
            updateCustomerQueueCount(customerQueue.size());
            System.out.println("Клиент #" + customer.getCustomerId() + " возвращен в очередь");
        });
    }

    public boolean hasWaitingCustomers() {
        synchronized (customerQueue) {
            return !customerQueue.isEmpty();
        }
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

    public void updateKitchenQueue(int count) {
        // СБОР СТАТИСТИКИ - ОБНОВЛЕНИЕ ОЧЕРЕДИ ЗАКАЗОВ
        statisticsManager.updateKitchenQueue(count);

        Platform.runLater(() -> {
            kitchenQueueBox.getChildren().clear();
            kitchenQueueCount.setText(String.valueOf(count));

            if (count > 0) {
                List<Integer> orderIds = simulationManager.getKitchenQueue().getOrderIds();
                for (int orderId : orderIds) {
                    Label orderLabel = new Label("Заказ #" + orderId);
                    orderLabel.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 5; -fx-background-radius: 5;");

                    FadeTransition fadeIn = new FadeTransition(Duration.millis(300), orderLabel);
                    fadeIn.setFromValue(0.0);
                    fadeIn.setToValue(1.0);

                    kitchenQueueBox.getChildren().add(orderLabel);
                    fadeIn.play();
                }
            } else {
                Label emptyLabel = new Label("Нет заказов");
                emptyLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-style: italic;");
                kitchenQueueBox.getChildren().add(emptyLabel);
            }

            updateStatistics();
        });
    }

    public void updateCookStatus(int cookId, int orderId) {
        Platform.runLater(() -> {
            updateCooksStatus(simulationManager.getCooksManager());
        });
    }

    public void updateCooksStatus(CooksManager cooksManager) {
        Platform.runLater(() -> {
            if (cooksManager == null) return;

            int busyCooks = cooksManager.getBusyCooksCount();
            int totalCooks = cooksManager.getTotalCooksCount();

            activeCooksLabel.setText("(" + busyCooks + "/" + totalCooks + " активны)");

            updateCooksIndicator(busyCooks);

            cooksStatusBox.getChildren().clear();

            for (Cook cook : cooksManager.getCooks()) {
                Label cookLabel = new Label(cook.getStatus());
                if (cook.isBusy()) {
                    cookLabel.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 8; -fx-background-radius: 5; -fx-font-weight: bold;");
                } else {
                    cookLabel.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-padding: 8; -fx-background-radius: 5;");
                }
                cooksStatusBox.getChildren().add(cookLabel);
            }
        });
    }

    private void updateCooksIndicator(int busyCooksCount) {
        if (busyCooksCount > 0) {
            // Есть активные повара - красный цвет с анимацией
            cooksIndicator.setFill(Color.RED);

            if (cooksPulseAnimation != null) {
                cooksPulseAnimation.stop();
            }

            ScaleTransition pulse = new ScaleTransition(Duration.millis(500), cooksIndicator);
            pulse.setFromX(1.0);
            pulse.setFromY(1.0);
            pulse.setToX(0.6);
            pulse.setToY(0.6);
            pulse.setAutoReverse(true);
            pulse.setCycleCount(Animation.INDEFINITE);
            pulse.play();

            cooksPulseAnimation = pulse;
        } else {
            cooksIndicator.setFill(Color.GRAY);
            if (cooksPulseAnimation != null) {
                cooksPulseAnimation.stop();
                cooksPulseAnimation = null;
            }
        }
    }

    public void updateServingQueue(int count) {
        Platform.runLater(() -> {
            servingQueueCount.setText(String.valueOf(count));
            servingQueueBox.getChildren().clear();

            if (count > 0) {
                List<Integer> orderIds = simulationManager.getServingQueue().getReadyOrderIds();
                for (int orderId : orderIds) {
                    Label orderLabel = new Label("Заказ #" + orderId);
                    orderLabel.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-padding: 5; -fx-background-radius: 5;");

                    FadeTransition fadeIn = new FadeTransition(Duration.millis(300), orderLabel);
                    fadeIn.setFromValue(0.0);
                    fadeIn.setToValue(1.0);

                    servingQueueBox.getChildren().add(orderLabel);
                    fadeIn.play();
                }
            } else {
                Label emptyLabel = new Label("Нет заказов");
                emptyLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-style: italic;");
                servingQueueBox.getChildren().add(emptyLabel);
            }
        });
    }

    public void updateServerStatus(int serverId, int orderId) {
        Platform.runLater(() -> {
            // ИСПРАВЛЕННАЯ СТРОКА - получаем serversManager из simulationManager
            updateServersStatus(simulationManager.getServersManager());

            // Обновляем общий индикатор serverIndicator
            if (orderId != -1) {
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
            } else {
                // Проверить, есть ли активные официанты
                ServersManager serversManager = simulationManager.getServersManager();
                if (serversManager != null && serversManager.getBusyServersCount() == 0) {
                    serverIndicator.setFill(Color.GRAY);
                    if (serverPulseAnimation != null) {
                        serverPulseAnimation.stop();
                        serverPulseAnimation = null;
                    }
                }
            }
        });
    }

    public void updateServersStatus(ServersManager serversManager) {
        Platform.runLater(() -> {
            if (serversManager == null) return;

            int busyServers = serversManager.getBusyServersCount();
            int totalServers = serversManager.getTotalServersCount();

            activeServersLabel.setText("(" + busyServers + "/" + totalServers + " активны)");

            serversStatusBox.getChildren().clear();

            for (Server server : serversManager.getServers()) {
                Label serverLabel = new Label(server.getStatus());
                if (server.isBusy()) {
                    serverLabel.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-padding: 8; -fx-background-radius: 5; -fx-font-weight: bold;");
                } else {
                    serverLabel.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-padding: 8; -fx-background-radius: 5;");
                }
                serversStatusBox.getChildren().add(serverLabel);
            }
        });
    }

    public void updateWaitingCustomers(int count) {
        Platform.runLater(() -> {
            waitingCustomersCount.setText(String.valueOf(count));
            updateWaitingCustomersList();
            updateStatistics();
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

    public void orderCreated(int orderId) {
        // СБОР СТАТИСТИКИ - ЗАКАЗ СОЗДАН
        statisticsManager.orderCreated();
        System.out.println("Статистика: заказ #" + orderId + " создан");

        updateStatistics();
    }

    public void completeOrder(int orderId, long waitTime) {
        // СБОР СТАТИСТИКИ - ЗАКАЗ ЗАВЕРШЕН С УЧЕТОМ ВРЕМЕНИ
        statisticsManager.orderCompleted(waitTime);

        Platform.runLater(() -> {
            System.out.println("Заказ #" + orderId + " завершен. Время ожидания: " + waitTime + "мс");
            updateStatistics();
        });
    }

    public void updateStatistics() {
        Platform.runLater(() -> {
            totalCustomersLabel.setText(String.valueOf(statisticsManager.getTotalCustomers()));
            totalOrdersLabel.setText(String.valueOf(statisticsManager.getTotalOrders()));
            avgWaitTimeLabel.setText(statisticsManager.getAverageWaitTime() + " мс");
            maxCustomerQueueLabel.setText(String.valueOf(statisticsManager.getMaxCustomerQueue()));
            maxKitchenQueueLabel.setText(String.valueOf(statisticsManager.getMaxKitchenQueue()));
            currentSpeedLabel.setText(String.format("%.1f зак/мин", statisticsManager.getCurrentSpeed()));
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

    private void initializeAnimations() {
        customerIndicator.setFill(Color.GRAY);
        orderTakerIndicator.setFill(Color.GRAY);
        serverIndicator.setFill(Color.GRAY);
        cooksIndicator.setFill(Color.GRAY);
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
        if (serverPulseAnimation != null) {
            serverPulseAnimation.stop();
            serverPulseAnimation = null;
        }
        if (cooksPulseAnimation != null) {
            cooksPulseAnimation.stop();
            cooksPulseAnimation = null;
        }
    }

    private void setupEventHandlers() {
        customerIntervalField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) customerIntervalField.setText(oldVal);
        });
        orderIntervalField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) orderIntervalField.setText(oldVal);
        });
        cookingIntervalField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) cookingIntervalField.setText(oldVal);
        });
        servingIntervalField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) servingIntervalField.setText(oldVal);
        });
        cooksCountField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) cooksCountField.setText(oldVal);
        });
        serversCountField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) serversCountField.setText(oldVal);
        });
    }
}