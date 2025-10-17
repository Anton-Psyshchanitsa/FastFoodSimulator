package main.fastfoodsimulator;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

public class AnimationManager {
    private final Pane animationPane;

    public AnimationManager(Pane animationPane) {
        this.animationPane = animationPane;
    }

    // Анимация движения клиента (Circle) от order line к serving line к dining area
    public void animateCustomerMovement(int customerId, double startX, double startY) {
        Platform.runLater(() -> {
            Circle customerIcon = new Circle(10, Color.BLUE);
            customerIcon.setTranslateX(startX);
            customerIcon.setTranslateY(startY);
            animationPane.getChildren().add(customerIcon);

            // Перемещение к serving line (например, +200 по X)
            TranslateTransition toServing = new TranslateTransition(Duration.seconds(2), customerIcon);
            toServing.setByX(200);
            toServing.setOnFinished(e -> {
                // Перемещение к dining area (например, +200 по X, +100 по Y)
                TranslateTransition toDining = new TranslateTransition(Duration.seconds(2), customerIcon);
                toDining.setByX(200);
                toDining.setByY(100);
                toDining.setOnFinished(ev -> animationPane.getChildren().remove(customerIcon)); // Удалить после
                toDining.play();
            });
            toServing.play();
        });
    }

    // Анимация движения заказа (Label) от kitchen к pickup
    public void animateOrderMovement(int orderNumber, double startX, double startY) {
        Platform.runLater(() -> {
            Label orderLabel = new Label("Order " + orderNumber);
            orderLabel.setTranslateX(startX);
            orderLabel.setTranslateY(startY);
            animationPane.getChildren().add(orderLabel);

            TranslateTransition transition = new TranslateTransition(Duration.seconds(3), orderLabel);
            transition.setByX(150); // Перемещение к pickup
            transition.setOnFinished(e -> animationPane.getChildren().remove(orderLabel)); // Удалить после
            transition.play();
        });
    }

    // Очистка всех анимаций
    public void clearAnimations() {
        Platform.runLater(() -> animationPane.getChildren().clear());
    }
}