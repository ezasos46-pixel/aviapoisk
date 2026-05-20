package com.example.demo8;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class YandexMapApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo8/yandex-map-view.fxml"));
        Scene scene = new Scene(loader.load(), 1200, 800);
        stage.setTitle("Карта городов России - Яндекс.Карты");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
