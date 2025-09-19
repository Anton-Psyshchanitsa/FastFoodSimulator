package main.fastfoodsimulator;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class SimulatorUI extends Application {

    private TextField customerIntervalField;
    private TextField orderFulfillmentField;
    private Button startButton;
    private Button stopButton;
    private Label orderLineLabel; // Кол-во клиентов в очереди
    private Label currentOrderLabel; // Текущий заказ
    private TextArea kitchenArea; // Очередь на кухне
    private Label pickupOrderLabel; // Заказ на pickup
    private Label servingLineLabel; // Кол-во клиентов в serving line

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Fast Food Simulator");

        // Панель ввода (верхняя часть)
        HBox inputPanel = new HBox(10);
        inputPanel.setPadding(new Insets(10));
        customerIntervalField = new TextField("5"); // По умолчанию 5 сек
        orderFulfillmentField = new TextField("10"); // По умолчанию 10 сек
        startButton = new Button("Start");
        stopButton = new Button("Stop");
        inputPanel.getChildren().addAll(
                new Label("Customer Arrival Interval (sec):"), customerIntervalField,
                new Label("Order Fulfillment Interval (sec):"), orderFulfillmentField,
                startButton, stopButton
        );

        // Центральная панель для симуляции (GridPane с 4 колонками)
        GridPane simulationPanel = new GridPane();
        simulationPanel.setPadding(new Insets(10));
        simulationPanel.setHgap(10);
        simulationPanel.setVgap(10);

        // Order Line
        orderLineLabel = new Label("Customers in line: 0");
        simulationPanel.add(new Label("Order Line:"), 0, 0);
        simulationPanel.add(orderLineLabel, 0, 1);

        // Order Area
        currentOrderLabel = new Label("Current Order: None");
        simulationPanel.add(new Label("Order Area:"), 1, 0);
        simulationPanel.add(currentOrderLabel, 1, 1);

        // Kitchen Area
        kitchenArea = new TextArea("Waiting orders: 0\n");
        kitchenArea.setEditable(false);
        kitchenArea.setPrefHeight(100);
        simulationPanel.add(new Label("Kitchen Area:"), 2, 0);
        simulationPanel.add(kitchenArea, 2, 1);

        // Pickup Area
        pickupOrderLabel = new Label("Available Order: None");
        servingLineLabel = new Label("Customers waiting: 0");
        simulationPanel.add(new Label("Pickup Area:"), 3, 0);
        simulationPanel.add(pickupOrderLabel, 3, 1);
        simulationPanel.add(servingLineLabel, 3, 2);

        // Основной layout
        BorderPane root = new BorderPane();
        root.setTop(inputPanel);
        root.setCenter(simulationPanel);

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Логика кнопок (пока заглушки, реализуем в следующих этапах)
        startButton.setOnAction(e -> {
            try {
                int customerInterval = Integer.parseInt(customerIntervalField.getText());
                int fulfillmentInterval = Integer.parseInt(orderFulfillmentField.getText());
                if (customerInterval <= 0 || fulfillmentInterval <= 0) {
                    showWarning("Intervals must be positive integers!");
                    return;
                }
                // TODO: Запуск симуляции (в Этапе 2)
            } catch (NumberFormatException ex) {
                showWarning("Please enter valid integers for intervals!");
            }
        });

        stopButton.setOnAction(e -> {
            // TODO: Остановка симуляции (в Этапе 3)
        });
    }

    // Метод для показа предупреждения (аналог JOptionPane)
    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Invalid Input");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Методы для обновления UI (будут использоваться в следующих этапах)
    public void updateOrderLine(int count) {
        orderLineLabel.setText("Customers in line: " + count);
    }

    public void updateCurrentOrder(int orderNumber) {
        currentOrderLabel.setText("Current Order: " + orderNumber);
    }

    public void updateKitchen(String orders, int count) {
        kitchenArea.setText("Waiting orders: " + count + "\n" + orders);
    }

    public void updatePickup(int orderNumber, int waitingCustomers) {
        pickupOrderLabel.setText("Available Order: " + orderNumber);
        servingLineLabel.setText("Customers waiting: " + waitingCustomers);
    }

    public static void main(String[] args) {
        launch(args);
    }
}