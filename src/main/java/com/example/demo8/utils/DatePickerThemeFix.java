package com.example.demo8.utils;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.DatePicker;

/**
 * Подключает стили сцены к popup-календарю (он вне дерева с .light-theme).
 */
public final class DatePickerThemeFix {

    private DatePickerThemeFix() {}

    public static void install(Node root) {
        if (root == null) return;
        if (root instanceof DatePicker dp) {
            wire(dp);
        }
        if (root instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                install(child);
            }
        }
    }

    private static void wire(DatePicker dp) {
        dp.setOnShowing(e -> Platform.runLater(() -> syncPopupStyles(dp)));
    }

    private static void syncPopupStyles(DatePicker dp) {
        Scene scene = dp.getScene();
        if (scene == null) return;
        Node popup = dp.lookup(".date-picker-popup");
        if (popup == null || popup.getScene() == null) return;
        Scene popupScene = popup.getScene();
        popupScene.getStylesheets().clear();
        popupScene.getStylesheets().addAll(scene.getStylesheets());
    }
}
