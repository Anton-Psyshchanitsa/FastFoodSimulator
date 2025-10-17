package second.fastfoodsimulator.view.controllers;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.animation.TranslateTransition;
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

    @FXML
    private VBox customerQueueBox;
    @FXML
    private VBox kitchenQueueBox;
    @FXML
    private Circle orderTakerIndicator;
    @FXML
    private Circle kitchenIndicator;

    private final SimulationManager simulationManager;
    private final List<Customer> customerQueue = new ArrayList<>();
    private final List<Label> customerLabels = new ArrayList<>();

    public MainController() {
        this.simulationManager = new SimulationManager(this);
    }

    @FXML
    private void initialize() {
        initializeAnimations();
        setupEventHandlers();
    }

    private void initializeAnimations() {
        // Анимация индикатора кассира
        orderTakerIndicator.setFill(javafx.scene.paint.Color.GRAY);
        final Timeline orderTakerTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(orderTakerIndicator.fillProperty(), javafx.scene.paint.Color.GRAY)),
                new KeyFrame(Duration.seconds(1), new KeyValue(orderTakerIndicator.fillProperty(), javafx.scene.paint.Color.LIMEGREEN))
        );
        orderTakerTimeline.setAutoReverse(true);
        orderTakerTimeline.setCycleCount(Timeline.INDEFINITE);
        orderTakerTimeline.play();

        // Анимация индикатора кухни
        kitchenIndicator.setFill(javafx.scene.paint.Color.GRAY);
        final Timeline kitchenTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(kitchenIndicator.fillProperty(), javafx.scene.paint.Color.GRAY)),
                new KeyFrame(Duration.seconds(1.5), new KeyValue(kitchenIndicator.fillProperty(), javafx.scene.paint.Color.ORANGERED))
        );
        kitchenTimeline.setAutoReverse(true);
        kitchenTimeline.setCycleCount(Timeline.INDEFINITE);
        kitchenTimeline.play();
    }

    private void setupEventHandlers() {
        // Валидация ввода
        customerIntervalField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) customerIntervalField.setText(oldVal);
        });
        orderIntervalField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) orderIntervalField.setText(oldVal);
        });
    }

    public void addCustomerToQueue(Customer customer) {
        Label customerLabel = new Label("Заказ #" + customer.getOrderId());
        customerLabel.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 5; -fx-background-radius: 5;");

        // Анимация появления клиента
        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), customerLabel);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        TranslateTransition slideIn = new TranslateTransition(Duration.millis(500), customerLabel);
        slideIn.setFromY(-20);
        slideIn.setToY(0);

        ParallelTransition parallelTransition = new ParallelTransition(fadeIn, slideIn);

        customerQueueBox.getChildren().add(customerLabel);
        customerLabels.add(customerLabel);
        customerQueue.add(customer);

        parallelTransition.play();
    }

    public void updateCustomerQueueCount(int count) {
        animateTextChange(customerQueueCount, String.valueOf(count));
    }

    public void updateOrderTakerStatus(int orderId) {
        animateTextChange(currentOrderTaker, "Заказ #" + orderId);
    }

    public void updateKitchenQueue(int count) {
        animateTextChange(kitchenQueueCount, String.valueOf(count));
    }

    private void animateTextChange(Label label, String newText) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), label);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), label);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        SequentialTransition sequentialTransition = new SequentialTransition(
                fadeOut,
                new Timeline(new KeyFrame(Duration.ZERO, evt -> label.setText(newText))),
                fadeIn
        );

        sequentialTransition.play();
    }
}