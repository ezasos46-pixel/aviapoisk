package com.example.demo8.utils;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

public class SceneTransition {

    private static final double DURATION_MS = 220;

    public static void apply(Stage stage, Scene newScene) {
        javafx.scene.paint.Color fill = ThemeManager.getInstance().getStageFillColor();
        stage.getScene().setFill(fill);
        newScene.setFill(fill);

        javafx.scene.Node oldRoot = stage.getScene().getRoot();

        // Создаём тёмный градиентный overlay поверх текущей сцены
        Rectangle overlay = new Rectangle();
        overlay.setFill(new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, fill.deriveColor(0, 0, 0, 0)),
                new Stop(1, fill.deriveColor(0, 0, 0, 0))));
        overlay.setOpacity(0);

        // Оборачиваем старый root в StackPane с overlay
        StackPane wrapper = new StackPane(oldRoot, overlay);
        stage.getScene().setRoot(wrapper);

        overlay.widthProperty().bind(wrapper.widthProperty());
        overlay.heightProperty().bind(wrapper.heightProperty());
        overlay.setFill(fill);

        // Fade overlay от прозрачного к непрозрачному (затемнение)
        FadeTransition fadeOut = new FadeTransition(Duration.millis(DURATION_MS), overlay);
        fadeOut.setFromValue(0.0);
        fadeOut.setToValue(1.0);
        fadeOut.setInterpolator(Interpolator.EASE_IN);

        fadeOut.setOnFinished(e -> {
            // Меняем сцену пока экран полностью тёмный
            newScene.getRoot().setOpacity(0.0);
            stage.setScene(newScene);

            // Fade-in нового экрана
            FadeTransition fadeIn = new FadeTransition(Duration.millis(DURATION_MS), newScene.getRoot());
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.setInterpolator(Interpolator.EASE_OUT);
            fadeIn.play();
        });

        fadeOut.play();
    }
}
