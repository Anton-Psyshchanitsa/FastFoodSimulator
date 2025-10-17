package second.fastfoodsimulator.view.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class MainController {
    @FXML
    private TextField customerIntervalField;
    @FXML
    private TextField orderIntervalField;
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
    private Label servingLineCount;

    // Добавляем поля для кнопок
    @FXML
    private Button startButton;
    @FXML
    private Button stopButton;

    @FXML
    private void initialize() {
        // Инициализация начальных значений
        customerQueueCount.setText("0");
        currentOrderTaker.setText("Нет заказа");
        currentKitchenOrder.setText("Нет заказа");
        kitchenQueueCount.setText("0");
        currentPickupOrder.setText("Нет заказа");
        servingLineCount.setText("0");

        // Валидация ввода
        customerIntervalField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) customerIntervalField.setText(oldVal);
        });
        orderIntervalField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) orderIntervalField.setText(oldVal);
        });

        // Обработчики кнопок
        startButton.setOnAction(event -> startSimulation());
        stopButton.setOnAction(event -> stopSimulation());
    }

    private void startSimulation() {
        try {
            int customerInterval = Integer.parseInt(customerIntervalField.getText());
            int orderInterval = Integer.parseInt(orderIntervalField.getText());

            if (customerInterval <= 0 || orderInterval <= 0) {
                showError("Интервалы должны быть положительными числами");
                return;
            }

            // Инициализация симуляции будет добавлена на этапе 2
            System.out.println("Симуляция запущена");
        } catch (NumberFormatException e) {
            showError("Неверный формат ввода");
        }
    }

    private void stopSimulation() {
        // Логика остановки симуляции (добавится на этапе 4)
        System.out.println("Симуляция остановлена");
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}