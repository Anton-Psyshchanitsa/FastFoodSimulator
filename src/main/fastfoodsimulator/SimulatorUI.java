package main.fastfoodsimulator;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class SimulatorUI extends Application {

    private TextField customerIntervalField;
    private TextField orderFulfillmentField;
    private Button startButton;
    private Button stopButton;
    private Label orderLineLabel;
    private Label currentOrderLabel;
    private TextArea kitchenArea;
    private Label pickupOrderLabel;
    private Label servingLineLabel;

    // Очереди и счётчики
    private Queue<Customer> orderLine = new LinkedList<>();
    private Queue<Order> kitchenQueue = new LinkedList<>();
    private Queue<Order> serviceQueue = new LinkedList<>(); // Новая: для готовых заказов
    private Queue<Customer> servingLine = new LinkedList<>();
    private AtomicInteger orderCounter = new AtomicInteger(0);
    private AtomicInteger customerCounter = new AtomicInteger(0);

    // Executors и threads
    private ScheduledExecutorService customerArrivalExecutor;
    private ScheduledExecutorService uiUpdateExecutor;
    private Thread orderTakerThread;
    private Thread cookThread;
    private Thread serverThread;
    private volatile boolean isRunning = false;

    // Для отображения текущих
    private int currentCookingOrder = -1;
    private int currentServingOrder = -1;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Fast Food Simulator");

        HBox inputPanel = new HBox(10);
        inputPanel.setPadding(new Insets(10));
        customerIntervalField = new TextField("5");
        orderFulfillmentField = new TextField("10");
        startButton = new Button("Start");
        stopButton = new Button("Stop");
        inputPanel.getChildren().addAll(
                new Label("Customer Arrival Interval (sec):"), customerIntervalField,
                new Label("Order Fulfillment Interval (sec):"), orderFulfillmentField,
                startButton, stopButton
        );

        GridPane simulationPanel = new GridPane();
        simulationPanel.setPadding(new Insets(10));
        simulationPanel.setHgap(10);
        simulationPanel.setVgap(10);

        orderLineLabel = new Label("Customers in line: 0");
        simulationPanel.add(new Label("Order Line:"), 0, 0);
        simulationPanel.add(orderLineLabel, 0, 1);

        currentOrderLabel = new Label("Current Order: None");
        simulationPanel.add(new Label("Order Area:"), 1, 0);
        simulationPanel.add(currentOrderLabel, 1, 1);

        kitchenArea = new TextArea("Waiting orders: 0\nCurrent preparing: None");
        kitchenArea.setEditable(false);
        kitchenArea.setPrefHeight(100);
        simulationPanel.add(new Label("Kitchen Area:"), 2, 0);
        simulationPanel.add(kitchenArea, 2, 1);

        pickupOrderLabel = new Label("Available Order: None");
        servingLineLabel = new Label("Customers waiting: 0");
        simulationPanel.add(new Label("Pickup Area:"), 3, 0);
        simulationPanel.add(pickupOrderLabel, 3, 1);
        simulationPanel.add(servingLineLabel, 3, 2);

        BorderPane root = new BorderPane();
        root.setTop(inputPanel);
        root.setCenter(simulationPanel);

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();

        startButton.setOnAction(e -> {
            try {
                int customerInterval = Integer.parseInt(customerIntervalField.getText());
                int fulfillmentInterval = Integer.parseInt(orderFulfillmentField.getText());
                if (customerInterval <= 0 || fulfillmentInterval <= 0) {
                    showWarning("Intervals must be positive integers!");
                    return;
                }
                startSimulation(customerInterval, fulfillmentInterval);
            } catch (NumberFormatException ex) {
                showWarning("Please enter valid integers for intervals!");
            }
        });

        stopButton.setOnAction(e -> stopSimulation());
    }

    private void startSimulation(int customerInterval, int fulfillmentInterval) {
        if (isRunning) return;
        isRunning = true;

        orderLine.clear();
        kitchenQueue.clear();
        serviceQueue.clear();
        servingLine.clear();
        orderCounter.set(0);
        customerCounter.set(0);
        currentCookingOrder = -1;
        currentServingOrder = -1;

        // Создание ролей
        OrderTaker orderTaker = new OrderTaker(1, orderLine, kitchenQueue, servingLine, orderCounter, this);
        Cook cook = new Cook(1, kitchenQueue, serviceQueue, fulfillmentInterval);
        Server server = new Server(1, serviceQueue);

        // Поток для OrderTaker
        orderTakerThread = new Thread(() -> {
            while (isRunning) {
                orderTaker.performAction();
                try {
                    Thread.sleep(1000); // Интервал обработки
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        orderTakerThread.start();

        // Поток для Cook
        cookThread = new Thread(() -> {
            while (isRunning) {
                cook.performAction();
                if (cook.getStatus().startsWith("Preparing")) {
                    currentCookingOrder = Integer.parseInt(cook.getStatus().split(" ")[2]); // Парсинг для UI
                } else if (cook.getStatus().startsWith("Completed")) {
                    currentCookingOrder = -1;
                }
            }
        });
        cookThread.start();

        // Поток для Server (каждые 1 сек проверяет)
        serverThread = new Thread(() -> {
            while (isRunning) {
                server.performAction();
                if (server.getStatus().startsWith("Serving")) {
                    currentServingOrder = Integer.parseInt(server.getStatus().split(" ")[2]);
                    Platform.runLater(() -> updatePickup(currentServingOrder, servingLine.size()));
                }
                try {
                    Thread.sleep(1000); // Интервал обслуживания
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        serverThread.start();

        // Прибытие клиентов
        customerArrivalExecutor = Executors.newScheduledThreadPool(1);
        customerArrivalExecutor.scheduleAtFixedRate(() -> {
            if (isRunning) {
                int customerId = customerCounter.incrementAndGet();
                Customer newCustomer = new Customer(customerId);
                orderLine.add(newCustomer);
                Platform.runLater(() -> updateOrderLine(orderLine.size()));
            }
        }, 0, customerInterval, TimeUnit.SECONDS);

        // Обновление UI каждые 1 сек
        uiUpdateExecutor = Executors.newScheduledThreadPool(1);
        uiUpdateExecutor.scheduleAtFixedRate(() -> {
            if (isRunning) {
                Platform.runLater(() -> {
                    updateOrderLine(orderLine.size());
                    updateKitchen(getKitchenOrdersString(), kitchenQueue.size(), currentCookingOrder);
                    updatePickup(currentServingOrder, servingLine.size());
                });
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    private void stopSimulation() {
        isRunning = false;
        if (customerArrivalExecutor != null) customerArrivalExecutor.shutdownNow();
        if (uiUpdateExecutor != null) uiUpdateExecutor.shutdownNow();
        if (orderTakerThread != null) orderTakerThread.interrupt();
        if (cookThread != null) cookThread.interrupt();
        if (serverThread != null) serverThread.interrupt();

        // Завершить unfinished futures для чистоты
        for (Order order : kitchenQueue) order.completePreparation();
        for (Order order : serviceQueue) order.completeServing();

        Platform.runLater(() -> {
            updateOrderLine(0);
            updateCurrentOrder(-1);
            updateKitchen("", 0, -1);
            updatePickup(-1, 0);
        });
    }

    // Вспомогательные методы
    private String getKitchenOrdersString() {
        StringBuilder sb = new StringBuilder();
        for (Order order : kitchenQueue) {
            sb.append("Order ").append(order.getOrderNumber()).append("\n");
        }
        return sb.toString();
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Invalid Input");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Обновлённые методы UI
    public void updateOrderLine(int count) {
        orderLineLabel.setText("Customers in line: " + count);
    }

    public void updateCurrentOrder(int orderNumber) {
        currentOrderLabel.setText("Current Order: " + (orderNumber > 0 ? orderNumber : "None"));
    }

    public void updateKitchen(String orders, int count, int cookingOrder) {
        kitchenArea.setText("Waiting orders: " + count + "\nCurrent preparing: " + (cookingOrder > 0 ? cookingOrder : "None") + "\n" + orders);
    }

    public void updatePickup(int orderNumber, int waitingCustomers) {
        pickupOrderLabel.setText("Available Order: " + (orderNumber > 0 ? orderNumber : "None"));
        servingLineLabel.setText("Customers waiting: " + waitingCustomers);
    }

    public static void main(String[] args) {
        launch(args);
    }
}