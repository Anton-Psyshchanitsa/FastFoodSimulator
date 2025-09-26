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

    // Новые поля для симуляции (инкапсулированы)
    private Queue<Customer> orderLine = new LinkedList<>(); // Очередь клиентов на заказ
    private Queue<Order> kitchenQueue = new LinkedList<>(); // Очередь заказов на кухню
    private Queue<Customer> servingLine = new LinkedList<>(); // Очередь клиентов на pickup (для Этапа 3)
    private AtomicInteger orderCounter = new AtomicInteger(0); // Счётчик заказов
    private AtomicInteger customerCounter = new AtomicInteger(0); // Счётчик клиентов
    private ScheduledExecutorService customerArrivalExecutor; // Для прибытия клиентов
    private ScheduledExecutorService uiUpdateExecutor; // Для обновления UI
    private Thread orderTakerThread; // Поток для OrderTaker
    private volatile boolean isRunning = false; // Флаг для остановки

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Fast Food Simulator");

        // Панель ввода (верхняя часть)
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

        // Центральная панель для симуляции
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

        kitchenArea = new TextArea("Waiting orders: 0\n");
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

        // Логика кнопки Start
        startButton.setOnAction(e -> {
            try {
                int customerInterval = Integer.parseInt(customerIntervalField.getText());
                int fulfillmentInterval = Integer.parseInt(orderFulfillmentField.getText()); // Сохраним для Этапа 3
                if (customerInterval <= 0 || fulfillmentInterval <= 0) {
                    showWarning("Intervals must be positive integers!");
                    return;
                }
                startSimulation(customerInterval, fulfillmentInterval);
            } catch (NumberFormatException ex) {
                showWarning("Please enter valid integers for intervals!");
            }
        });

        // Логика кнопки Stop
        stopButton.setOnAction(e -> stopSimulation());
    }

    // Метод запуска симуляции
    private void startSimulation(int customerInterval, int fulfillmentInterval) {
        if (isRunning) return; // Не запускать заново
        isRunning = true;

        // Инициализация очередей и счётчиков
        orderLine.clear();
        kitchenQueue.clear();
        servingLine.clear();
        orderCounter.set(0);
        customerCounter.set(0);

        // Создание OrderTaker
        OrderTaker orderTaker = new OrderTaker(1, orderLine, kitchenQueue, orderCounter);

        // Поток для OrderTaker: обрабатывает клиентов каждые 1 сек (симуляция времени взятия заказа)
        orderTakerThread = new Thread(() -> {
            while (isRunning) {
                orderTaker.performAction();
                // После обработки: клиент перемещается в servingLine
                if (orderTaker.getStatus().startsWith("Processed")) {
                    // Найти последнего обработанного клиента (для простоты, предполагаем последнего в логике)
                    // В реальности: можно добавить метод, но пока симулируем
                    int lastCustomerId = customerCounter.get();
                    Customer processedCustomer = new Customer(lastCustomerId); // Заглушка, в полной версии - реальный
                    servingLine.add(processedCustomer); // Переместить в serving line
                    Platform.runLater(() -> updateCurrentOrder(orderCounter.get())); // Обновить UI
                }
                try {
                    Thread.sleep(1000); // Интервал обработки (можно настроить)
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        orderTakerThread.start();

        // ScheduledExecutor для прибытия клиентов с фиксированным интервалом
        customerArrivalExecutor = Executors.newScheduledThreadPool(1);
        customerArrivalExecutor.scheduleAtFixedRate(() -> {
            if (isRunning) {
                int customerId = customerCounter.incrementAndGet();
                Customer newCustomer = new Customer(customerId);
                orderLine.add(newCustomer);
                Platform.runLater(() -> updateOrderLine(orderLine.size())); // Обновить UI thread-safe
            }
        }, 0, customerInterval, TimeUnit.SECONDS);

        // ScheduledExecutor для обновления UI каждые 1 сек
        uiUpdateExecutor = Executors.newScheduledThreadPool(1);
        uiUpdateExecutor.scheduleAtFixedRate(() -> {
            if (isRunning) {
                Platform.runLater(() -> {
                    updateOrderLine(orderLine.size());
                    updateKitchen(getKitchenOrdersString(), kitchenQueue.size());
                    updatePickup(-1, servingLine.size()); // -1 значит none, для Этапа 3
                });
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    // Метод остановки симуляции
    private void stopSimulation() {
        isRunning = false;
        if (customerArrivalExecutor != null) customerArrivalExecutor.shutdownNow();
        if (uiUpdateExecutor != null) uiUpdateExecutor.shutdownNow();
        if (orderTakerThread != null) orderTakerThread.interrupt();
        Platform.runLater(() -> {
            updateOrderLine(0);
            updateCurrentOrder(-1);
            updateKitchen("", 0);
            updatePickup(-1, 0);
        });
    }

    // Вспомогательный метод для строки заказов на кухне
    private String getKitchenOrdersString() {
        StringBuilder sb = new StringBuilder();
        for (Order order : kitchenQueue) {
            sb.append("Order ").append(order.getOrderNumber()).append("\n");
        }
        return sb.toString();
    }

    // Метод для показа предупреждения
    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Invalid Input");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Методы обновления UI (расширенные)
    public void updateOrderLine(int count) {
        orderLineLabel.setText("Customers in line: " + count);
    }

    public void updateCurrentOrder(int orderNumber) {
        currentOrderLabel.setText("Current Order: " + (orderNumber > 0 ? orderNumber : "None"));
    }

    public void updateKitchen(String orders, int count) {
        kitchenArea.setText("Waiting orders: " + count + "\n" + orders);
    }

    public void updatePickup(int orderNumber, int waitingCustomers) {
        pickupOrderLabel.setText("Available Order: " + (orderNumber > 0 ? orderNumber : "None"));
        servingLineLabel.setText("Customers waiting: " + waitingCustomers);
    }

    public static void main(String[] args) {
        launch(args);
    }
}