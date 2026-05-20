package com.example.demo8.controllers;

import com.example.demo8.models.Booking;
import com.example.demo8.utils.SceneThemeSupport;
import com.example.demo8.utils.ThemeStyleRemap;
import com.example.demo8.utils.ThemeStyles;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class PaymentResultController {
    @FXML private Label resultLabel;
    @FXML private VBox successContent;
    @FXML private Label bookingNumberLabel;
    @FXML private javafx.scene.control.Button copyBookingButton;
    @FXML private VBox summaryBox;
    @FXML private VBox summaryContent;
    
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    
    public void setResult(Booking booking, boolean success) {
        Platform.runLater(() -> {
            if (resultLabel != null) {
                ThemeStyleRemap.bindScene(resultLabel);
            }
        });
        if (success && booking != null) {
            resultLabel.setText("Оплата успешна!");
            successContent.setVisible(true);
            bookingNumberLabel.setText(booking.getBookingNumber());
            
            if (booking.getFlight() != null) {
                summaryContent.getChildren().clear();
                
                Label routeLabel = new Label("Маршрут: " + booking.getFlight().getFromCityName() + " → " + booking.getFlight().getToCityName());
                routeLabel.setStyle(ThemeStyles.labelPrimary("14px"));
                summaryContent.getChildren().add(routeLabel);
                
                Label flightLabel = new Label("Рейс: " + booking.getFlight().getAirlineName() + " " + booking.getFlight().getFlightNumber());
                flightLabel.setStyle(ThemeStyles.labelPrimary("14px"));
                summaryContent.getChildren().add(flightLabel);
                
                Label timeLabel = new Label("Время: " + booking.getFlight().getDepartureTime().format(timeFormatter) + " - " + booking.getFlight().getArrivalTime().format(timeFormatter));
                timeLabel.setStyle(ThemeStyles.labelPrimary("14px"));
                summaryContent.getChildren().add(timeLabel);
                
                Label priceLabel = new Label("Сумма: " + booking.getTotalPrice() + " ₽");
                priceLabel.setStyle(ThemeStyles.labelPrimary("14px"));
                summaryContent.getChildren().add(priceLabel);
            }
        } else {
            resultLabel.setText("Оплата не прошла");
            successContent.setVisible(false);
        }
    }
    
    @FXML
    private void handleBackToMain() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo8/main-view.fxml"));
            Stage stage = (Stage) resultLabel.getScene().getWindow();
            Scene scene = new Scene(loader.load());
            SceneThemeSupport.install(scene);
            stage.setScene(scene);
            stage.setTitle("Поиск авиабилетов");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCopyBookingNumber() {
        String bookingNumber = bookingNumberLabel.getText();
        if (bookingNumber == null || bookingNumber.isBlank()) return;
        ClipboardContent content = new ClipboardContent();
        content.putString(bookingNumber);
        Clipboard.getSystemClipboard().setContent(content);
    }
}

