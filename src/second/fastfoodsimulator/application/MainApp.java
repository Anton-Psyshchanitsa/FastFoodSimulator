package second.fastfoodsimulator.application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class MainApp extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/second/fastfoodsimulator/view/MainView.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles/application.css")).toExternalForm());

        primaryStage.setTitle("Fast Food Simulator");
        primaryStage.setScene(scene);

        // УСТАНАВЛИВАЕМ ПОЛНОЭКРАННЫЙ РЕЖИМ
        primaryStage.setMaximized(true);
        primaryStage.setFullScreen(false); // Можно использовать true для настоящего fullscreen
        primaryStage.setMinWidth(1200);
        primaryStage.setMinHeight(800);

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}