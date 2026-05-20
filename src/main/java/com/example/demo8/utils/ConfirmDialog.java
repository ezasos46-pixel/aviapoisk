package com.example.demo8.utils;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

/**
 * Модальное окно подтверждения в стилистике приложения (тёмная тема, акцент #4fc3f7).
 */
public final class ConfirmDialog {

    private ConfirmDialog() {}

    /**
     * @return true, если пользователь нажал «Да»
     */
    public static boolean show(Window owner, String title, String message) {
        final boolean[] confirmed = {false};

        Stage dialog = new Stage();
        if (owner != null) {
            dialog.initOwner(owner);
        }
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);

        VBox root = new VBox(0);
        root.setStyle(ThemeStyles.dialogRoot());
        root.setMinWidth(400);

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle(ThemeStyles.dialogHeader().replace("16 20 16 24", "14 20 14 24"));
        Label titleLbl = new Label(title);
        titleLbl.setStyle(ThemeStyles.labelPrimaryBold("16px"));
        Region hSpacer = new Region();
        HBox.setHgrow(hSpacer, Priority.ALWAYS);
        Button closeBtn = new Button("✕");
        closeBtn.setStyle(ThemeStyles.labelMuted("16px") + " -fx-background-color: transparent; -fx-padding: 0 4; -fx-cursor: hand; -fx-border-color: transparent;");
        closeBtn.setOnAction(e -> dialog.close());
        header.getChildren().addAll(titleLbl, hSpacer, closeBtn);

        Label messageLbl = new Label(message);
        messageLbl.setWrapText(true);
        messageLbl.setMaxWidth(360);
        messageLbl.setStyle(ThemeStyles.labelPrimary("14px"));
        VBox content = new VBox(messageLbl);
        content.setAlignment(Pos.CENTER);
        content.setStyle("-fx-padding: 24 24 8 24;");

        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER);
        footer.setStyle("-fx-padding: 16 24 20 24; -fx-border-color: " + ThemeStyles.p().divider + "; -fx-border-width: 1 0 0 0;");

        Button cancelBtn = new Button("Отмена");
        cancelBtn.setStyle(ThemeStyles.confirmCancelButton());
        cancelBtn.setOnAction(e -> dialog.close());

        Button yesBtn = new Button("Да");
        yesBtn.setStyle(ThemeStyles.buyButton().replace("9 28", "9 28").replace("14px", "13px"));
        yesBtn.setDefaultButton(true);
        yesBtn.setOnAction(e -> {
            confirmed[0] = true;
            dialog.close();
        });

        footer.getChildren().addAll(cancelBtn, yesBtn);
        root.getChildren().addAll(header, content, footer);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        SceneThemeSupport.install(scene);
        dialog.setScene(scene);

        dialog.setOnShown(ev -> {
            if (owner != null) {
                dialog.setX(owner.getX() + (owner.getWidth() - dialog.getWidth()) / 2);
                dialog.setY(owner.getY() + (owner.getHeight() - dialog.getHeight()) / 2);
            }
        });

        dialog.showAndWait();
        return confirmed[0];
    }
}
