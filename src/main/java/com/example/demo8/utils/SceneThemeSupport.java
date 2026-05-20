package com.example.demo8.utils;

import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;

/**
 * Единая точка применения темы к сцене после загрузки FXML.
 */
public final class SceneThemeSupport {

    private SceneThemeSupport() {}

    public static void install(Scene scene) {
        if (scene == null) return;
        ThemeManager.getInstance().attachToScene(scene);
        stripDuplicateNodeStylesheets(scene.getRoot());
        ThemeStyleRemap.applyTree(scene.getRoot());
        DatePickerThemeFix.install(scene.getRoot());
        applyAllTabPanes(scene.getRoot());
        Platform.runLater(() -> {
            ThemeStyleRemap.applyTree(scene.getRoot());
            DatePickerThemeFix.install(scene.getRoot());
            applyAllTabPanes(scene.getRoot());
        });
    }

    private static void applyAllTabPanes(javafx.scene.Node root) {
        if (root instanceof TabPane tabPane) {
            ThemeStyleRemap.applyTabPane(tabPane);
        }
        if (root instanceof Parent parent) {
            for (javafx.scene.Node child : parent.getChildrenUnmodifiable()) {
                applyAllTabPanes(child);
            }
        }
    }

    /** FXML stylesheets="@styles.css" на корне перекрывает theme-light.css — убираем. */
    private static void stripDuplicateNodeStylesheets(javafx.scene.Node root) {
        if (root == null) return;
        if (root instanceof Parent parent) {
            parent.getStylesheets().removeIf(url ->
                    url != null && (url.contains("styles.css") || url.contains("theme-light.css")));
            for (javafx.scene.Node child : parent.getChildrenUnmodifiable()) {
                stripDuplicateNodeStylesheets(child);
            }
        }
    }
}
