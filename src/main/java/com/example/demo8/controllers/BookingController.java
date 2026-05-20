package com.example.demo8.controllers;

import com.example.demo8.models.*;
import com.example.demo8.services.SupabaseClient;
import com.example.demo8.utils.SceneTransition;
import com.example.demo8.utils.SessionManager;
import com.example.demo8.utils.SceneThemeSupport;
import com.example.demo8.utils.ThemeStyleRemap;
import com.example.demo8.utils.ThemeStyles;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public class BookingController {
    @FXML private VBox summaryBox;
    @FXML private VBox summaryContent;
    @FXML private Label totalPriceLabel;
    @FXML private VBox passengersForms;
    @FXML private TextField cardNumber;
    
    public void initialize() {
        Platform.runLater(() -> {
            if (backButton != null) ThemeStyleRemap.bindScene(backButton);
        });
        // Настройка кнопки "Назад"
        if (backButton != null) {
            backButton.setOnMouseEntered(e -> {
                backButton.setStyle("-fx-background-color: rgba(255,255,255,0.3); -fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 10 20; -fx-background-radius: 5; -fx-cursor: hand;");
            });
            backButton.setOnMouseExited(e -> {
                backButton.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 10 20; -fx-background-radius: 5; -fx-cursor: hand;");
            });
        }
        
        // Форматирование номера карты
        cardNumber.textProperty().addListener((observable, oldValue, newValue) -> {
            String digits = newValue.replaceAll("\\D", "");
            if (digits.length() > 16) {
                digits = digits.substring(0, 16);
            }
            StringBuilder formatted = new StringBuilder();
            for (int i = 0; i < digits.length(); i++) {
                if (i > 0 && i % 4 == 0) {
                    formatted.append(" ");
                }
                formatted.append(digits.charAt(i));
            }
            if (!formatted.toString().equals(newValue)) {
                cardNumber.setText(formatted.toString());
            }
        });
        
        // Форматирование срока действия
        cardExpiry.textProperty().addListener((observable, oldValue, newValue) -> {
            String digits = newValue.replaceAll("\\D", "");
            if (digits.length() > 4) {
                digits = digits.substring(0, 4);
            }
            if (digits.length() >= 2) {
                String formatted = digits.substring(0, 2) + "/" + (digits.length() > 2 ? digits.substring(2) : "");
                if (!formatted.equals(newValue)) {
                    cardExpiry.setText(formatted);
                }
            }
        });
        
        // Ограничение CVV
        cardCVV.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                cardCVV.setText(newValue.replaceAll("\\D", ""));
            }
            if (cardCVV.getText().length() > 3) {
                cardCVV.setText(cardCVV.getText().substring(0, 3));
            }
        });
    }
    @FXML private TextField cardExpiry;
    @FXML private TextField cardCVV;
    @FXML private Button backButton;
    
    private Flight flight;
    private RoundTrip roundTrip;
    private SearchParams searchParams;
    private SupabaseClient supabaseClient;
    private List<PassengerForm> passengerFormList;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    
    private static final Pattern NAME_PATTERN = Pattern.compile("^[А-Яа-я\\s]{1,50}$"); // Только русские буквы, 1-50 символов
    private static final Pattern PASSPORT_SERIES_PATTERN = Pattern.compile("^\\d{4}$"); // Серия: 4 цифры
    private static final Pattern PASSPORT_NUMBER_PATTERN = Pattern.compile("^\\d{6}$"); // Номер: 6 цифр
    private static final Pattern CARD_PATTERN = Pattern.compile("^\\d{16}$");
    private static final Pattern CVV_PATTERN = Pattern.compile("^\\d{3}$");
    private static final Pattern EXPIRY_PATTERN = Pattern.compile("^(0[1-9]|1[0-2])/(\\d{2})$");
    
    public void setFlight(Flight flight) {
        this.flight = flight;
        this.roundTrip = new RoundTrip(flight, null);
    }
    
    public void setRoundTrip(RoundTrip roundTrip) {
        this.roundTrip = roundTrip;
        if (roundTrip != null && roundTrip.getOutboundFlight() != null) {
            this.flight = roundTrip.getOutboundFlight();
        }
    }
    
    public void setSearchParams(SearchParams searchParams) {
        this.searchParams = searchParams;
        supabaseClient = SupabaseClient.getInstance();
        passengerFormList = new ArrayList<>();
        initializeForms();
        updateSummary();
        Platform.runLater(() -> {
            if (summaryBox != null) ThemeStyleRemap.bindScene(summaryBox);
        });
    }
    
    private void initializeForms() {
        passengersForms.getChildren().clear();
        int totalPassengers = searchParams.getTotalPassengers();
        
        for (int i = 0; i < totalPassengers; i++) {
            PassengerForm form = new PassengerForm(i + 1);
            passengersForms.getChildren().add(form.getContainer());
            passengerFormList.add(form);
        }
        ThemeStyleRemap.applyTree(passengersForms);
    }
    
    private void updateSummary() {
        summaryContent.getChildren().clear();
        
        if (roundTrip == null || roundTrip.getOutboundFlight() == null) {
            return;
        }
        
        Flight outbound = roundTrip.getOutboundFlight();
        Flight returnFlight = roundTrip.getReturnFlight();
        
        // Рейс туда
        Label outboundTitle = new Label("РЕЙС ТУДА:");
        outboundTitle.setStyle(ThemeStyles.labelPrimaryBold("16px"));
        summaryContent.getChildren().add(outboundTitle);
        
        Label routeLabel = new Label("Маршрут: " + outbound.getFromCityName() + " → " + outbound.getToCityName());
        routeLabel.setStyle(ThemeStyles.labelPrimary("14px"));
        summaryContent.getChildren().add(routeLabel);
        
        Label dateLabel = new Label("Дата вылета: " + outbound.getDepartureTime().format(dateFormatter));
        dateLabel.setStyle(ThemeStyles.labelPrimary("14px"));
        summaryContent.getChildren().add(dateLabel);
        
        Label flightLabel = new Label("Рейс: " + outbound.getAirlineName() + " " + outbound.getFlightNumber());
        flightLabel.setStyle(ThemeStyles.labelPrimary("14px"));
        summaryContent.getChildren().add(flightLabel);
        
        Label timeLabel = new Label("Время: " + outbound.getDepartureTime().format(timeFormatter) + " - " + outbound.getArrivalTime().format(timeFormatter));
        timeLabel.setStyle(ThemeStyles.labelPrimary("14px"));
        summaryContent.getChildren().add(timeLabel);
        
        // Рейс обратно (если есть)
        if (returnFlight != null) {
            Label separator = new Label("─────────────────");
            separator.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5 0;");
            summaryContent.getChildren().add(separator);
            
            Label returnTitle = new Label("РЕЙС ОБРАТНО:");
            returnTitle.setStyle(ThemeStyles.labelPrimaryBold("16px"));
            summaryContent.getChildren().add(returnTitle);
            
            Label returnRouteLabel = new Label("Маршрут: " + returnFlight.getFromCityName() + " → " + returnFlight.getToCityName());
            returnRouteLabel.setStyle(ThemeStyles.labelPrimary("14px"));
            summaryContent.getChildren().add(returnRouteLabel);
            
            Label returnDateLabel = new Label("Дата вылета: " + returnFlight.getDepartureTime().format(dateFormatter));
            returnDateLabel.setStyle(ThemeStyles.labelPrimary("14px"));
            summaryContent.getChildren().add(returnDateLabel);
            
            Label returnFlightLabel = new Label("Рейс: " + returnFlight.getAirlineName() + " " + returnFlight.getFlightNumber());
            returnFlightLabel.setStyle(ThemeStyles.labelPrimary("14px"));
            summaryContent.getChildren().add(returnFlightLabel);
            
            Label returnTimeLabel = new Label("Время: " + returnFlight.getDepartureTime().format(timeFormatter) + " - " + returnFlight.getArrivalTime().format(timeFormatter));
            returnTimeLabel.setStyle(ThemeStyles.labelPrimary("14px"));
            summaryContent.getChildren().add(returnTimeLabel);
        }
        
        Label separator2 = new Label("─────────────────");
        separator2.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5 0;");
        summaryContent.getChildren().add(separator2);
        
        Label passengersLabel = new Label("Пассажиры: " + searchParams.getAdults() + " взросл., " + searchParams.getChildren() + " дет., " + searchParams.getInfants() + " млад.");
        passengersLabel.setStyle(ThemeStyles.labelPrimary("14px"));
        summaryContent.getChildren().add(passengersLabel);
        
        Label classLabel = new Label("Класс: " + searchParams.getServiceClass());
        classLabel.setStyle(ThemeStyles.labelPrimary("14px"));
        summaryContent.getChildren().add(classLabel);
        
        if (searchParams.isExtraBaggage()) {
            Label baggageLabel = new Label("Доп. багаж: Да");
            baggageLabel.setStyle(ThemeStyles.labelPrimary("14px"));
            summaryContent.getChildren().add(baggageLabel);
        }
        
        BigDecimal totalPrice = calculateTotalPrice();
        totalPriceLabel.setText("Итого: " + totalPrice + " ₽");
        totalPriceLabel.setStyle(com.example.demo8.utils.ThemeStyles.labelPriceAccent("22px"));
        ThemeStyleRemap.applyTree(summaryContent);
    }
    
    private BigDecimal calculateTotalPrice() {
        if (roundTrip == null || roundTrip.getOutboundFlight() == null) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal totalPrice = BigDecimal.ZERO;
        int totalPassengers = searchParams.getTotalPassengers();
        
        double classMultiplier = 1.0;
        switch (searchParams.getServiceClass()) {
            case "Премиум": classMultiplier = 1.5; break;
            case "Бизнес": classMultiplier = 2.0; break;
        }
        
        // Цена за рейс туда
        BigDecimal outboundPrice = roundTrip.getOutboundFlight().getBasePrice()
                .multiply(new BigDecimal(classMultiplier))
                .multiply(new BigDecimal(totalPassengers));
        totalPrice = totalPrice.add(outboundPrice);
        
        // Цена за рейс обратно
        if (roundTrip.getReturnFlight() != null) {
            BigDecimal returnPrice = roundTrip.getReturnFlight().getBasePrice()
                    .multiply(new BigDecimal(classMultiplier))
                    .multiply(new BigDecimal(totalPassengers));
            totalPrice = totalPrice.add(returnPrice);
        }
        
        // Дополнительный багаж (один раз для всей поездки)
        BigDecimal baggageFee = searchParams.isExtraBaggage() ? new BigDecimal("2000") : BigDecimal.ZERO;
        totalPrice = totalPrice.add(baggageFee);
        
        return totalPrice;
    }
    
    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo8/main-view.fxml"));
            Stage stage = (Stage) summaryBox.getScene().getWindow();
            Scene scene = new Scene(loader.load());
            try {
                SceneThemeSupport.install(scene);
            } catch (Exception e) {}
            stage.setTitle("Поиск авиабилетов");
            SceneTransition.apply(stage, scene);
        } catch (IOException e) {
            showError("Ошибка возврата: " + e.getMessage());
        }
    }
    
    @FXML
    private void handlePayment() {
        // Валидация данных пассажиров
        for (PassengerForm form : passengerFormList) {
            if (!form.validate()) {
                showError("Заполните корректно все данные пассажиров");
                return;
            }
        }
        
        // Валидация карты
        if (!validateCard()) {
            return;
        }
        
        // Создание бронирований
        new Thread(() -> {
            try {
                if (roundTrip == null || roundTrip.getOutboundFlight() == null) {
                    Platform.runLater(() -> {
                        openPaymentResult(null, false);
                    });
                    return;
                }
                
                // Создаем одно бронирование для рейса туда-обратно
                Booking booking = new Booking();
                booking.setUserId(SessionManager.getInstance().getCurrentUser() != null ? 
                    SessionManager.getInstance().getCurrentUser().getId() : null);
                booking.setFlightId(roundTrip.getOutboundFlight().getId());
                
                // Устанавливаем обратный рейс
                if (roundTrip.getReturnFlight() != null) {
                    booking.setReturnFlightId(roundTrip.getReturnFlight().getId());
                }
                
                booking.setAdults(searchParams.getAdults());
                booking.setChildren(searchParams.getChildren());
                booking.setInfants(searchParams.getInfants());
                booking.setServiceClass(searchParams.getServiceClass());
                booking.setExtraBaggage(searchParams.isExtraBaggage());
                
                // Рассчитываем общую цену для обоих рейсов
                BigDecimal totalPrice = calculateTotalPrice();
                booking.setTotalPrice(totalPrice);
                
                Booking createdBooking = supabaseClient.createBooking(booking);
                
                if (createdBooking == null || createdBooking.getId() == null) {
                    throw new IOException("Не удалось создать бронирование: получен null");
                }
                
                // Создание пассажиров (места назначаются автоматически через триггер в БД)
                // Триггер создаст записи в passenger_seats для обоих рейсов (туда и обратно)
                for (PassengerForm form : passengerFormList) {
                    try {
                        Passenger passenger = form.getPassenger();
                        passenger.setBookingId(createdBooking.getId());
                        supabaseClient.createPassenger(passenger);
                    } catch (IOException e) {
                        System.err.println("Ошибка создания пассажира: " + e.getMessage());
                        // Продолжаем создавать остальных пассажиров
                    }
                }
                
                Platform.runLater(() -> {
                    openPaymentResult(createdBooking, true);
                });
            } catch (IOException e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    openPaymentResult(null, false);
                });
            }
        }).start();
    }
    
    private BigDecimal calculateFlightPrice(Flight flight) {
        BigDecimal basePrice = flight.getBasePrice();
        int totalPassengers = searchParams.getTotalPassengers();
        
        double classMultiplier = 1.0;
        switch (searchParams.getServiceClass()) {
            case "Премиум": classMultiplier = 1.5; break;
            case "Бизнес": classMultiplier = 2.0; break;
        }
        
        return basePrice.multiply(new BigDecimal(classMultiplier))
                .multiply(new BigDecimal(totalPassengers));
    }
    
    private boolean validateCard() {
        String cardNum = cardNumber.getText().replaceAll("\\s", "");
        String expiry = cardExpiry.getText().trim();
        String cvv = cardCVV.getText().trim();
        
        if (cardNum.length() != 16 || !cardNum.matches("\\d{16}")) {
            showError("Номер карты должен содержать 16 цифр");
            return false;
        }
        
        if (!EXPIRY_PATTERN.matcher(expiry).matches()) {
            showError("Неверный формат срока действия (ММ/ГГ)");
            return false;
        }
        
        // Проверка актуальности даты
        String[] parts = expiry.split("/");
        int month = Integer.parseInt(parts[0]);
        int year = 2000 + Integer.parseInt(parts[1]);
        LocalDate expiryDate = LocalDate.of(year, month, 1).withDayOfMonth(
            LocalDate.of(year, month, 1).lengthOfMonth()
        );
        if (expiryDate.isBefore(LocalDate.now())) {
            showError("Срок действия карты истек");
            return false;
        }
        
        if (!CVV_PATTERN.matcher(cvv).matches()) {
            showError("CVV должен содержать 3 цифры");
            return false;
        }
        
        return true;
    }
    
    private void openPaymentResult(Booking booking, boolean success) {
        try {
            if (summaryBox == null || summaryBox.getScene() == null) {
                showError("Не удалось открыть окно результата оплаты: окно не найдено");
                return;
            }
            
            Stage stage = (Stage) summaryBox.getScene().getWindow();
            if (stage == null) {
                showError("Не удалось открыть окно результата оплаты: stage не найден");
                return;
            }
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo8/payment-result-view.fxml"));
            Scene scene = new Scene(loader.load(), stage.getWidth(), stage.getHeight());
            try {
                SceneThemeSupport.install(scene);
            } catch (Exception e) {}
            stage.setTitle("Результат оплаты");
            SceneTransition.apply(stage, scene);
            
            PaymentResultController controller = loader.getController();
            if (controller != null) {
                controller.setResult(booking, success);
            }
        } catch (IOException e) {
            showError("Ошибка открытия результата оплаты: " + e.getMessage());
        } catch (Exception e) {
            showError("Неожиданная ошибка: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // Внутренний класс для формы пассажира
    private class PassengerForm {
        private TextField firstNameField;
        private TextField lastNameField;
        private TextField dateOfBirthField;
        private TextField passportSeriesField; // Серия паспорта (4 цифры)
        private TextField passportNumberField; // Номер паспорта (6 цифр)
        private VBox container;
        
        public PassengerForm(int passengerNumber) {
            container = new VBox(10);
            container.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 5; -fx-padding: 15;");
            
            Label title = new Label("Пассажир " + passengerNumber);
            title.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
            container.getChildren().add(title);
            
            HBox nameBox = new HBox(10);
            VBox firstNameBox = new VBox(5);
            Label firstNameLabel = new Label("Имя *");
            firstNameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
            firstNameField = new TextField();
            firstNameField.setStyle("-fx-background-color: #1e2d50; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8;");
            
            // Валидация имени - только русские буквы, максимум 50 символов
            UnaryOperator<TextFormatter.Change> nameFilter = change -> {
                String newText = change.getControlNewText();
                if (newText.length() <= 50 && (newText.isEmpty() || newText.matches("^[А-Яа-яЁё\\s]*$"))) {
                    return change;
                }
                return null;
            };
            firstNameField.setTextFormatter(new TextFormatter<>(nameFilter));
            firstNameField.textProperty().addListener((obs, oldVal, newVal) -> {
                validateNameField(firstNameField, newVal);
            });
            
            firstNameBox.getChildren().addAll(firstNameLabel, firstNameField);
            
            VBox lastNameBox = new VBox(5);
            Label lastNameLabel = new Label("Фамилия *");
            lastNameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
            lastNameField = new TextField();
            lastNameField.setStyle("-fx-background-color: #1e2d50; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8;");
            
            // Валидация фамилии - только русские буквы, максимум 50 символов
            lastNameField.setTextFormatter(new TextFormatter<>(nameFilter));
            lastNameField.textProperty().addListener((obs, oldVal, newVal) -> {
                validateNameField(lastNameField, newVal);
            });
            
            lastNameBox.getChildren().addAll(lastNameLabel, lastNameField);
            
            nameBox.getChildren().addAll(firstNameBox, lastNameBox);
            container.getChildren().add(nameBox);
            
            HBox detailsBox = new HBox(10);
            VBox dobBox = new VBox(5);
            Label dobLabel = new Label("Дата рождения (ДД.ММ.ГГГГ) *");
            dobLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
            dateOfBirthField = new TextField();
            dateOfBirthField.setPromptText("ДД.ММ.ГГГГ");
            dateOfBirthField.setStyle("-fx-background-color: #1e2d50; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8;");
            
            // Валидация даты рождения - только цифры и точки, формат ДД.ММ.ГГГГ
            // Пользователь сам вводит точки
            UnaryOperator<TextFormatter.Change> dateFilter = change -> {
                String newText = change.getControlNewText();
                // Разрешаем только цифры и точки, максимум 10 символов (ДД.ММ.ГГГГ)
                if (newText.matches("^\\d{0,2}(\\.\\d{0,2}(\\.\\d{0,4})?)?$")) {
                    return change;
                }
                return null;
            };
            dateOfBirthField.setTextFormatter(new TextFormatter<>(dateFilter));
            dateOfBirthField.textProperty().addListener((obs, oldVal, newVal) -> {
                validateDateField(dateOfBirthField, newVal);
            });
            dobBox.getChildren().addAll(dobLabel, dateOfBirthField);
            
            // Поля паспорта: серия и номер
            HBox passportBox = new HBox(10);
            VBox seriesBox = new VBox(5);
            Label seriesLabel = new Label("Серия паспорта (4 цифры) *");
            seriesLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
            passportSeriesField = new TextField();
            passportSeriesField.setPromptText("1234");
            passportSeriesField.setStyle("-fx-background-color: #1e2d50; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8;");
            
            // Валидация серии паспорта - только цифры, максимум 4
            UnaryOperator<TextFormatter.Change> seriesFilter = change -> {
                String newText = change.getControlNewText();
                if (newText.matches("\\d{0,4}")) {
                    return change;
                }
                return null;
            };
            passportSeriesField.setTextFormatter(new TextFormatter<>(seriesFilter));
            passportSeriesField.textProperty().addListener((obs, oldVal, newVal) -> {
                validatePassportSeriesField(passportSeriesField, newVal);
            });
            seriesBox.getChildren().addAll(seriesLabel, passportSeriesField);
            
            VBox numberBox = new VBox(5);
            Label numberLabel = new Label("Номер паспорта (6 цифр) *");
            numberLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
            passportNumberField = new TextField();
            passportNumberField.setPromptText("567890");
            passportNumberField.setStyle("-fx-background-color: #1e2d50; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8;");
            
            // Валидация номера паспорта - только цифры, максимум 6
            UnaryOperator<TextFormatter.Change> numberFilter = change -> {
                String newText = change.getControlNewText();
                if (newText.matches("\\d{0,6}")) {
                    return change;
                }
                return null;
            };
            passportNumberField.setTextFormatter(new TextFormatter<>(numberFilter));
            passportNumberField.textProperty().addListener((obs, oldVal, newVal) -> {
                validatePassportNumberField(passportNumberField, newVal);
            });
            numberBox.getChildren().addAll(numberLabel, passportNumberField);
            
            passportBox.getChildren().addAll(seriesBox, numberBox);
            
            detailsBox.getChildren().addAll(dobBox, passportBox);
            container.getChildren().add(detailsBox);
            ThemeStyleRemap.applyTree(container);
        }
        
        private void validateNameField(TextField field, String value) {
            if (value.isEmpty()) {
                field.setStyle("-fx-background-color: #1e2d50; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8; -fx-border-color: transparent;");
            } else if (NAME_PATTERN.matcher(value).matches() && value.length() <= 50) {
                field.setStyle("-fx-background-color: #1e2d50; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8; -fx-border-color: #4CAF50; -fx-border-width: 2; -fx-border-radius: 3;");
            } else {
                field.setStyle("-fx-background-color: #2d1a1a; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8; -fx-border-color: #f44336; -fx-border-width: 2; -fx-border-radius: 3;");
            }
        }
        
        private void validatePassportSeriesField(TextField field, String value) {
            if (value.isEmpty()) {
                field.setStyle("-fx-background-color: #1e2d50; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8; -fx-border-color: transparent;");
            } else if (PASSPORT_SERIES_PATTERN.matcher(value).matches()) {
                field.setStyle("-fx-background-color: #1e2d50; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8; -fx-border-color: #4CAF50; -fx-border-width: 2; -fx-border-radius: 3;");
            } else {
                field.setStyle("-fx-background-color: #2d1a1a; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8; -fx-border-color: #f44336; -fx-border-width: 2; -fx-border-radius: 3;");
            }
        }
        
        private void validatePassportNumberField(TextField field, String value) {
            if (value.isEmpty()) {
                field.setStyle("-fx-background-color: #1e2d50; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8; -fx-border-color: transparent;");
            } else if (PASSPORT_NUMBER_PATTERN.matcher(value).matches()) {
                field.setStyle("-fx-background-color: #1e2d50; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8; -fx-border-color: #4CAF50; -fx-border-width: 2; -fx-border-radius: 3;");
            } else {
                field.setStyle("-fx-background-color: #2d1a1a; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8; -fx-border-color: #f44336; -fx-border-width: 2; -fx-border-radius: 3;");
            }
        }
        
        private void validateDateField(TextField field, String value) {
            if (value.isEmpty()) {
                field.setStyle("-fx-background-color: white; -fx-font-size: 14px; -fx-padding: 8; -fx-border-color: transparent;");
            } else {
                // Проверяем формат ДД.ММ.ГГГГ (точки автоматически расставляются)
                if (value.matches("^\\d{2}\\.\\d{2}\\.\\d{4}$")) {
                    try {
                        String[] parts = value.split("\\.");
                        int day = Integer.parseInt(parts[0]);
                        int month = Integer.parseInt(parts[1]);
                        int year = Integer.parseInt(parts[2]);
                        if (day < 1 || day > 31 || month < 1 || month > 12 || year < 1900) {
                            field.setStyle("-fx-background-color: #2d1a1a; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8; -fx-border-color: #f44336; -fx-border-width: 2; -fx-border-radius: 3;");
                            return;
                        }
                        LocalDate date = LocalDate.of(year, month, day);
                        LocalDate today = LocalDate.now();
                        if (date.isAfter(today) || date.isBefore(today.minusYears(120))) {
                            field.setStyle("-fx-background-color: #2d1a1a; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8; -fx-border-color: #f44336; -fx-border-width: 2; -fx-border-radius: 3;");
                        } else {
                            field.setStyle("-fx-background-color: #1e2d50; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8; -fx-border-color: #4CAF50; -fx-border-width: 2; -fx-border-radius: 3;");
                        }
                    } catch (Exception e) {
                        field.setStyle("-fx-background-color: #2d1a1a; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8; -fx-border-color: #f44336; -fx-border-width: 2; -fx-border-radius: 3;");
                    }
                } else if (value.matches("^\\d{1,2}(\\.\\d{1,2}(\\.\\d{0,4})?)?$")) {
                    field.setStyle("-fx-background-color: #1e2d50; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8; -fx-border-color: transparent;");
                } else {
                    field.setStyle("-fx-background-color: #2d1a1a; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8; -fx-border-color: #f44336; -fx-border-width: 2; -fx-border-radius: 3;");
                }
            }
        }
        
        
        public boolean validate() {
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            String dobText = dateOfBirthField.getText().trim();
            String passportSeries = passportSeriesField.getText().trim();
            String passportNumber = passportNumberField.getText().trim();
            
            // Валидация имени: только русские буквы, максимум 50 символов
            if (firstName.isEmpty() || !NAME_PATTERN.matcher(firstName).matches() || firstName.length() > 50) {
                return false;
            }
            // Валидация фамилии: только русские буквы, максимум 50 символов
            if (lastName.isEmpty() || !NAME_PATTERN.matcher(lastName).matches() || lastName.length() > 50) {
                return false;
            }
            // Проверяем формат даты ДД.ММ.ГГГГ
            if (dobText.isEmpty() || !dobText.matches("^\\d{2}\\.\\d{2}\\.\\d{4}$")) {
                return false;
            }
            try {
                String[] parts = dobText.split("\\.");
                int day = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]);
                int year = Integer.parseInt(parts[2]);
                LocalDate dob = LocalDate.of(year, month, day);
                LocalDate today = LocalDate.now();
                if (dob.isAfter(today) || dob.isBefore(today.minusYears(120))) {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
            // Валидация серии паспорта: 4 цифры
            if (!PASSPORT_SERIES_PATTERN.matcher(passportSeries).matches()) {
                return false;
            }
            // Валидация номера паспорта: 6 цифр
            if (!PASSPORT_NUMBER_PATTERN.matcher(passportNumber).matches()) {
                return false;
            }
            
            return true;
        }
        
        public Passenger getPassenger() {
            Passenger passenger = new Passenger();
            passenger.setFirstName(firstNameField.getText().trim());
            passenger.setLastName(lastNameField.getText().trim());
            
            // Парсим дату из текстового поля
            String dobText = dateOfBirthField.getText().trim();
            try {
                String[] parts = dobText.split("\\.");
                int day = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]);
                int year = Integer.parseInt(parts[2]);
                passenger.setDateOfBirth(LocalDate.of(year, month, day));
            } catch (Exception e) {
                // Если не удалось распарсить, используем текущую дату (не должно произойти после валидации)
                passenger.setDateOfBirth(LocalDate.now());
            }
            
            // Устанавливаем серию и номер паспорта
            String passportSeries = passportSeriesField.getText().trim();
            String passportNumber = passportNumberField.getText().trim();
            passenger.setPassportSeries(passportSeries);
            passenger.setPassportNumber(passportNumber);
            
            // Для обратной совместимости сохраняем объединенное значение в documentNumber
            passenger.setDocumentNumber(passportSeries + passportNumber);
            
            return passenger;
        }
        
        public VBox getContainer() {
            return container;
        }
    }
}

