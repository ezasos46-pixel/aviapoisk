package com.example.demo8;

import com.example.demo8.utils.SceneThemeSupport;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/com/example/demo8/main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1200, 800);
        SceneThemeSupport.install(scene);
        stage.setTitle("Авиакасса - Поиск билетов");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}