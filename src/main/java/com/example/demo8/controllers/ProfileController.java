package com.example.demo8.controllers;

import com.example.demo8.models.Booking;
import com.example.demo8.models.Flight;
import com.example.demo8.models.User;
import com.example.demo8.services.SupabaseClient;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.example.demo8.utils.SceneTransition;
import com.example.demo8.utils.SessionManager;
import com.example.demo8.utils.SceneThemeSupport;
import com.example.demo8.utils.ThemeManager;
import com.example.demo8.utils.ThemeStyleRemap;
import com.example.demo8.utils.ThemeStyles;
import java.util.ArrayList;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Pattern;

public class ProfileController {
    @FXML private BorderPane rootPane;
    @FXML private HBox profileHeader;
    @FXML private TabPane profileTabPane;
    @FXML private TextField bookingSearchField;
    @FXML private VBox bookingResultBox;
    @FXML private VBox bookingsList;
    @FXML private HBox statsCardsBox;
    @FXML private VBox statsDetailsBox;
    @FXML private VBox statsHeroBox;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    
    private SupabaseClient supabaseClient;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{11}$");
    
    public void initialize() {
        supabaseClient = SupabaseClient.getInstance();
        applyScreenTheme();
        loadUserInfo();
        loadBookings();
        loadStats();
    }

    private void applyScreenTheme() {
        Platform.runLater(() -> {
            if (rootPane == null || rootPane.getScene() == null) {
                Platform.runLater(this::applyScreenTheme);
                return;
            }
            SceneThemeSupport.install(rootPane.getScene());
            ThemeManager.Palette p = ThemeStyles.p();
            rootPane.setStyle("-fx-background-color: " + p.bgMain + ";");
            if (profileHeader != null) {
                profileHeader.setStyle(ThemeStyles.screenHeader());
            }
            if (profileTabPane != null) {
                profileTabPane.setStyle("-fx-background-color: " + p.bgMain + ";");
                ThemeStyleRemap.applyTabPane(profileTabPane);
            }
            ThemeStyleRemap.applyTree(rootPane);
        });
    }

    private void loadStats() {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null) return;
        new Thread(() -> {
            try {
                List<Booking> bookings = supabaseClient.getUserBookings(user.getId());
                Platform.runLater(() -> buildStats(bookings));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void buildStats(List<Booking> bookings) {
        int totalFlights = bookings.size();
        java.math.BigDecimal totalSpent = bookings.stream()
                .map(b -> b.getTotalPrice() != null ? b.getTotalPrice() : java.math.BigDecimal.ZERO)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        java.math.BigDecimal maxPrice = java.math.BigDecimal.ZERO;
        java.math.BigDecimal minPrice = null;

        for (Booking b : bookings) {
            if (b.getTotalPrice() != null) {
                if (b.getTotalPrice().compareTo(maxPrice) > 0) maxPrice = b.getTotalPrice();
                if (minPrice == null || b.getTotalPrice().compareTo(minPrice) < 0) minPrice = b.getTotalPrice();
            }
        }

        java.math.BigDecimal avg = totalFlights > 0
                ? totalSpent.divide(java.math.BigDecimal.valueOf(totalFlights), 0, java.math.RoundingMode.HALF_UP)
                : java.math.BigDecimal.ZERO;

        // ── HERO ──────────────────────────────────────────────────────────────
        statsHeroBox.getChildren().clear();
        Label heroTitle = new Label("Ваша статистика путешествий");
        heroTitle.setStyle(ThemeStyles.labelSecondary("13px") + " -fx-font-weight: bold; -fx-letter-spacing: 2;");
        Label heroAmount = new Label(totalSpent.toPlainString() + " ₽");
        heroAmount.setStyle(ThemeStyles.labelPrice("48px"));
        statsHeroBox.getChildren().addAll(heroTitle, heroAmount);

        // ── КАРТОЧКИ ──────────────────────────────────────────────────────────
        statsCardsBox.getChildren().clear();
        ThemeManager.Palette p = ThemeStyles.p();
        addStatCard("✈",  "Рейсов",       String.valueOf(totalFlights),      p.accent, p.featureCardBg);
        addStatCard("💰",  "Средний чек",  avg.toPlainString() + " ₽",       "#81c784", p.featureCardBg);

        // ── ДЕТАЛИ ────────────────────────────────────────────────────────────
        statsDetailsBox.getChildren().clear();

        if (totalFlights == 0) {
            Label empty = new Label("🛫  Совершите первый рейс — здесь появится ваша статистика");
            empty.setStyle(ThemeStyles.labelMuted("14px") + " -fx-padding: 20 0;");
            statsDetailsBox.getChildren().add(empty);
            ThemeStyleRemap.applyTree(statsHeroBox);
            applyScreenTheme();
            return;
        }

        // левая колонка — рекорды
        addSectionTitle(statsDetailsBox, "🏆  Статистика");
        statsDetailsBox.setSpacing(8);
        addDetailRow(statsDetailsBox, "💸  Самый дорогой билет", maxPrice.toPlainString() + " ₽", "#ff8a65");
        if (minPrice != null)
            addDetailRow(statsDetailsBox, "💵  Самый дешёвый билет", minPrice.toPlainString() + " ₽", "#81c784");
        addDetailRow(statsDetailsBox, "📊  Средний чек", avg.toPlainString() + " ₽", p.accent);
        ThemeStyleRemap.applyTree(statsHeroBox);
        ThemeStyleRemap.applyTree(statsCardsBox);
        ThemeStyleRemap.applyTree(statsDetailsBox);
        applyScreenTheme();
    }

    private void addStatCard(String emoji, String label, String value, String color, String bg) {
        javafx.scene.layout.VBox card = new javafx.scene.layout.VBox(8);
        card.setStyle("-fx-background-color: " + bg + "; -fx-background-radius: 14; -fx-padding: 22 20; " +
                "-fx-border-color: " + color + "; -fx-border-width: 0 0 2 0; -fx-border-radius: 14;");
        javafx.scene.layout.HBox.setHgrow(card, javafx.scene.layout.Priority.ALWAYS);

        javafx.scene.layout.HBox top = new javafx.scene.layout.HBox(8);
        top.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label emojiLbl = new Label(emoji);
        emojiLbl.setStyle("-fx-font-size: 22px;");
        Label labelLbl = new Label(label.toUpperCase());
        labelLbl.setStyle(ThemeStyles.labelMuted("10px") + " -fx-font-weight: bold;");
        top.getChildren().addAll(emojiLbl, labelLbl);

        Label valueLbl = new Label(value);
        valueLbl.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 20px; -fx-font-weight: bold;");
        valueLbl.setWrapText(true);

        card.getChildren().addAll(top, valueLbl);
        statsCardsBox.getChildren().add(card);
    }

    private void addSectionTitle(javafx.scene.layout.VBox box, String text) {
        Label lbl = new Label(text);
        lbl.setStyle(ThemeStyles.labelPrimaryBold("15px") + " -fx-padding: 4 0 6 0;");
        box.getChildren().add(lbl);
    }

    private void addDetailRow(javafx.scene.layout.VBox box, String label, String value, String color) {
        javafx.scene.layout.HBox row = new javafx.scene.layout.HBox();
        row.setStyle("-fx-background-color: " + ThemeStyles.p().bgCard + "; -fx-background-radius: 10; -fx-padding: 14 20; " +
                "-fx-border-color: " + ThemeStyles.p().borderSoft + "; -fx-border-width: 1; -fx-border-radius: 10;");
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label lbl = new Label(label);
        lbl.setStyle(ThemeStyles.labelSecondary("13px"));
        javafx.scene.layout.HBox.setHgrow(lbl, javafx.scene.layout.Priority.ALWAYS);
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        spacer.setMinWidth(24);
        Label val = new Label(value);
        val.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 14px; -fx-font-weight: bold;");
        row.getChildren().addAll(lbl, spacer, val);
        box.getChildren().add(row);
    }

    private void loadUserInfo() {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user != null) {
            emailField.setText(user.getEmail());
            phoneField.setText(user.getPhone());
        }
    }
    
    private void loadBookings() {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null) return;

        new Thread(() -> {
            try {
                List<Booking> bookings = supabaseClient.getUserBookings(user.getId());
                List<Booking> normalized = new ArrayList<>();
                for (Booking booking : bookings) {
                    normalized.add(ensureBookingDetailsFromDb(booking));
                }
                Platform.runLater(() -> {
                    bookingsList.getChildren().clear();
                    if (normalized.isEmpty()) {
                        Label noBookings = new Label("Нет бронирований");
                        noBookings.setStyle(ThemeStyles.labelPrimary("16px"));
                        bookingsList.getChildren().add(noBookings);
                    } else {
                        for (Booking booking : normalized) {
                            bookingsList.getChildren().add(createBookingCard(booking));
                        }
                    }
                    ThemeStyleRemap.applyTree(bookingsList);
                    applyScreenTheme();
                });
            } catch (IOException e) {
                Platform.runLater(() -> {
                    Label error = new Label("Ошибка загрузки бронирований: " + e.getMessage());
                    error.setStyle("-fx-text-fill: red; -fx-font-size: 14px;");
                    bookingsList.getChildren().add(error);
                });
            }
        }).start();
    }

    @FXML
    private void handleSearchBooking() {
        String bookingNumber = bookingSearchField.getText().trim();
        if (bookingNumber.isEmpty()) {
            showError("Введите номер бронирования");
            return;
        }
        
        new Thread(() -> {
            try {
                Booking booking = supabaseClient.findBookingByNumber(bookingNumber);
                Platform.runLater(() -> {
                    bookingResultBox.getChildren().clear();
                    if (booking != null) {
                        bookingResultBox.setVisible(true);
                        
                        Label numberLabel = new Label("Номер бронирования: " + booking.getBookingNumber());
                        numberLabel.setStyle(ThemeStyles.labelPrimaryBold("18px"));
                        bookingResultBox.getChildren().add(numberLabel);

                        int totalPassengers = (booking.getAdults() != null ? booking.getAdults() : 0)
                                + (booking.getChildren() != null ? booking.getChildren() : 0)
                                + (booking.getInfants() != null ? booking.getInfants() : 0);

                        Label passengersLabel = new Label("Всего пассажиров: " + totalPassengers);
                        passengersLabel.setStyle("-fx-text-fill: " + ThemeStyles.p().textPrimary + "; -fx-font-size: 14px;");
                        bookingResultBox.getChildren().add(passengersLabel);

                        Label statusLabel = new Label("Статус: " + (booking.getStatus() != null ? booking.getStatus() : "Нет данных"));
                        statusLabel.setStyle("-fx-text-fill: " + ThemeStyles.p().textPrimary + "; -fx-font-size: 14px;");
                        bookingResultBox.getChildren().add(statusLabel);

                        Label priceLabel = new Label("Цена: " + (booking.getTotalPrice() != null ? booking.getTotalPrice() : "0") + " ₽");
                        priceLabel.setStyle("-fx-text-fill: " + ThemeStyles.p().textPrimary + "; -fx-font-size: 14px;");
                        bookingResultBox.getChildren().add(priceLabel);

                        Label classLabel = new Label("Класс: " + (booking.getServiceClass() != null ? booking.getServiceClass() : "Эконом"));
                        classLabel.setStyle("-fx-text-fill: " + ThemeStyles.p().textPrimary + "; -fx-font-size: 14px;");
                        bookingResultBox.getChildren().add(classLabel);

                        Label seatsLabel = new Label("Место: " + extractSeatSummary(booking.getPassengersJson()));
                        seatsLabel.setStyle("-fx-text-fill: " + ThemeStyles.p().textPrimary + "; -fx-font-size: 14px;");
                        seatsLabel.setWrapText(true);
                        bookingResultBox.getChildren().add(seatsLabel);
                    } else {
                        bookingResultBox.setVisible(true);
                        Label notFound = new Label("Бронирование не найдено");
                        notFound.setStyle(ThemeStyles.labelPrimary("16px"));
                        bookingResultBox.getChildren().add(notFound);
                    }
                    ThemeStyleRemap.applyTree(bookingResultBox);
                    applyScreenTheme();
                });
            } catch (IOException e) {
                Platform.runLater(() -> {
                    showError("Ошибка поиска: " + e.getMessage());
                });
            }
        }).start();
    }
    
    @FXML
    private void handleSaveProfile() {
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        
        if (email.isEmpty() || phone.isEmpty()) {
            showError("Заполните все поля");
            return;
        }
        
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            showError("Неверный формат email");
            return;
        }
        
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            showError("Номер телефона должен содержать 11 цифр");
            return;
        }
        
        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null) return;
        
        new Thread(() -> {
            try {
                supabaseClient.updateUser(user.getId(), email, phone);
                Platform.runLater(() -> {
                    user.setEmail(email);
                    user.setPhone(phone);
                    showSuccess("Данные успешно обновлены");
                });
            } catch (IOException e) {
                Platform.runLater(() -> {
                    showError("Ошибка обновления данных: " + e.getMessage());
                });
            }
        }).start();
    }
    
    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo8/main-view.fxml"));
            Stage stage = (Stage) bookingSearchField.getScene().getWindow();
            Scene scene = new Scene(loader.load(), 1200, 800);
            SceneThemeSupport.install(scene);
            stage.setTitle("Поиск авиабилетов");
            SceneTransition.apply(stage, scene);
        } catch (IOException e) {
            showError("Ошибка возврата: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleLogout() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Выход из аккаунта");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("Вы уверены, что хотите выйти из аккаунта?");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                // Очищаем сессию
                SessionManager.getInstance().logout();
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo8/main-view.fxml"));
                    Stage stage = (Stage) bookingSearchField.getScene().getWindow();
                    Scene scene = new Scene(loader.load(), 1200, 800);
                    SceneThemeSupport.install(scene);
                    stage.setTitle("Поиск авиабилетов");
                    SceneTransition.apply(stage, scene);
                } catch (IOException e) {
                    showError("Ошибка открытия главного окна: " + e.getMessage());
                }
            }
        });
    }
    
    @FXML
    private void handleDeleteAccount() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Удаление аккаунта");
        confirmAlert.setHeaderText("Внимание!");
        confirmAlert.setContentText("Вы уверены, что хотите удалить аккаунт?\nЭто действие нельзя отменить. Все ваши данные будут удалены безвозвратно.");
        
        // Изменяем тип кнопок для более строгого подтверждения
        ButtonType deleteButton = new ButtonType("Удалить", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmAlert.getButtonTypes().setAll(deleteButton, cancelButton);
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == deleteButton) {
                User user = SessionManager.getInstance().getCurrentUser();
                if (user == null) {
                    showError("Пользователь не найден");
                    return;
                }
                
                // Показываем индикатор загрузки
                new Thread(() -> {
                    try {
                        supabaseClient.deleteUser(user.getId());
                        Platform.runLater(() -> {
                            // Очищаем сессию
                            SessionManager.getInstance().logout();
                            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                            successAlert.setTitle("Аккаунт удален");
                            successAlert.setHeaderText(null);
                            successAlert.setContentText("Ваш аккаунт успешно удален.");
                            successAlert.showAndWait();
                            try {
                                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo8/main-view.fxml"));
                                Stage stage = (Stage) bookingSearchField.getScene().getWindow();
                                Scene scene = new Scene(loader.load(), 1200, 800);
                                SceneThemeSupport.install(scene);
                                stage.setTitle("Поиск авиабилетов");
                                SceneTransition.apply(stage, scene);
                            } catch (IOException e) {
                                showError("Ошибка открытия главного окна: " + e.getMessage());
                            }
                        });
                    } catch (IOException e) {
                        Platform.runLater(() -> {
                            showError("Ошибка удаления аккаунта: " + e.getMessage());
                        });
                    }
                }).start();
            }
        });
    }
    
    private VBox createBookingCard(Booking booking) {
        VBox card = new VBox(10);
        card.setStyle(ThemeStyles.cardDefault() + " -fx-padding: 20; -fx-pref-width: 1100;");
        
        // Номер бронирования
        card.getChildren().add(createBookingNumberRow(booking.getBookingNumber()));
        
        // Город отправления и назначения
        String fromCity = "нет данных";
        String toCity = "нет данных";
        if (booking.getFlight() != null) {
            if (booking.getFlight().getFromCityName() != null) fromCity = booking.getFlight().getFromCityName();
            if (booking.getFlight().getToCityName() != null) toCity = booking.getFlight().getToCityName();
        }
        Label routeLabel = new Label("Город отправления и назначения: " + fromCity + " → " + toCity);
        routeLabel.setStyle("-fx-text-fill: " + ThemeStyles.p().textPrimary + "; -fx-font-size: 14px;");
        card.getChildren().add(routeLabel);
        
        // Время отправления и прибытия
        String departureText = "нет данных";
        String arrivalText = "нет данных";
        if (booking.getFlight() != null && booking.getFlight().getDepartureTime() != null) {
            departureText = booking.getFlight().getDepartureTime().format(dateFormatter) + " " +
                    booking.getFlight().getDepartureTime().format(timeFormatter);
        }
        if (booking.getFlight() != null && booking.getFlight().getArrivalTime() != null) {
            arrivalText = booking.getFlight().getArrivalTime().format(dateFormatter) + " " +
                    booking.getFlight().getArrivalTime().format(timeFormatter);
        }
        Label departureLabel = new Label("Время отправления: " + departureText);
        departureLabel.setStyle("-fx-text-fill: " + ThemeStyles.p().textPrimary + "; -fx-font-size: 14px;");
        card.getChildren().add(departureLabel);

        Label arrivalLabel = new Label("Время прибытия: " + arrivalText);
        arrivalLabel.setStyle("-fx-text-fill: " + ThemeStyles.p().textPrimary + "; -fx-font-size: 14px;");
        card.getChildren().add(arrivalLabel);

        if (booking.getFlight() != null) {
            Flight f = booking.getFlight();
            String airline = f.getAirlineName() != null && !f.getAirlineName().isBlank() ? f.getAirlineName() : "нет данных";
            String flNum = f.getFlightNumber() != null && !f.getFlightNumber().isBlank() ? f.getFlightNumber() : "нет данных";
            String duration = formatFlightDuration(f);

            Label flightSection = new Label("Рейс");
            flightSection.setStyle(ThemeStyles.labelAccent("15px") + " -fx-padding: 6 0 0 0;");
            card.getChildren().add(flightSection);

            Label airlineLbl = new Label("Авиакомпания: " + airline);
            airlineLbl.setStyle("-fx-text-fill: " + ThemeStyles.p().textPrimary + "; -fx-font-size: 14px;");
            card.getChildren().add(airlineLbl);

            Label numberLbl = new Label("Номер рейса: " + flNum);
            numberLbl.setStyle("-fx-text-fill: " + ThemeStyles.p().textPrimary + "; -fx-font-size: 14px;");
            card.getChildren().add(numberLbl);

            Label durationLbl = new Label("Длительность полёта: " + duration);
            durationLbl.setStyle("-fx-text-fill: " + ThemeStyles.p().textPrimary + "; -fx-font-size: 14px;");
            card.getChildren().add(durationLbl);
        }
        
        // Всего пассажиров
        int totalPassengers = (booking.getAdults() != null ? booking.getAdults() : 0) + 
                             (booking.getChildren() != null ? booking.getChildren() : 0) + 
                             (booking.getInfants() != null ? booking.getInfants() : 0);
        Label passengersLabel = new Label("Всего пассажиров: " + totalPassengers);
        passengersLabel.setStyle("-fx-text-fill: " + ThemeStyles.p().textPrimary + "; -fx-font-size: 14px;");
        card.getChildren().add(passengersLabel);
        
        // Класс обслуживания
        Label classLabel = new Label("Класс: " + (booking.getServiceClass() != null ? booking.getServiceClass() : "Эконом"));
        classLabel.setStyle("-fx-text-fill: " + ThemeStyles.p().textPrimary + "; -fx-font-size: 14px;");
        card.getChildren().add(classLabel);
        
        // Цена
        Label priceLabel = new Label("Цена: " + (booking.getTotalPrice() != null ? booking.getTotalPrice() : "0") + " ₽");
        priceLabel.setStyle("-fx-text-fill: " + ThemeStyles.p().textPrimary + "; -fx-font-size: 14px;");
        card.getChildren().add(priceLabel);
        
        // Статус
        Label statusLabel = new Label("Статус: " + (booking.getStatus() != null ? booking.getStatus() : "Оплачено"));
        statusLabel.setStyle("-fx-text-fill: " + ThemeStyles.p().textPrimary + "; -fx-font-size: 14px;");
        card.getChildren().add(statusLabel);
        
        // Пассажиры с местами (твоя БД: passengers.seat_number + flight_direction)
        if (booking.getPassengersJson() != null && !booking.getPassengersJson().isEmpty()) {
            try {
                JsonArray passengersArray = JsonParser.parseString(booking.getPassengersJson()).getAsJsonArray();
                
                if (passengersArray.size() > 0) {
                    // Заголовок "Пассажиры:"
                    Label passengersHeader = new Label("Пассажиры:");
                    passengersHeader.setStyle(ThemeStyles.labelPrimaryBold("16px") + " -fx-padding: 10 0 5 0;");
                    card.getChildren().add(passengersHeader);

                    VBox passengersContainer = new VBox(8);
                    passengersContainer.setStyle("-fx-padding: 5 0 5 0;");

                    for (int i = 0; i < passengersArray.size(); i++) {
                        JsonObject p = passengersArray.get(i).getAsJsonObject();
                        String firstName = p.has("first_name") ? p.get("first_name").getAsString() : "";
                        String lastName = p.has("last_name") ? p.get("last_name").getAsString() : "";

                        String seat = p.has("seat_number") && !p.get("seat_number").isJsonNull()
                                ? p.get("seat_number").getAsString()
                                : "не назначено";
                        String direction = p.has("flight_direction") && !p.get("flight_direction").isJsonNull()
                                ? p.get("flight_direction").getAsString()
                                : "outbound";

                        String suffix = "";
                        if ("return".equals(direction)) {
                            suffix = " (обратно)";
                        }

                        String passengerText = firstName + " " + lastName + " - Место: " + seat + suffix;
                        Label passengerLabel = new Label(passengerText);

                        boolean hasSeat = !"не назначено".equals(seat);
                        String baseStyle = hasSeat
                                ? "-fx-text-fill: #90EE90; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 8px; -fx-background-color: rgba(144, 238, 144, 0.2); -fx-background-radius: 5;"
                                : "-fx-text-fill: #FFD700; -fx-font-size: 14px; -fx-padding: 8px; -fx-background-color: rgba(255, 215, 0, 0.2); -fx-background-radius: 5;";

                        passengerLabel.setStyle(baseStyle);
                        passengerLabel.setWrapText(true);
                        passengerLabel.setMaxWidth(Double.MAX_VALUE);
                        passengerLabel.setPrefWidth(Region.USE_COMPUTED_SIZE);
                        passengerLabel.setMinHeight(Region.USE_PREF_SIZE);

                        passengersContainer.getChildren().add(passengerLabel);
                    }

                    if (!passengersContainer.getChildren().isEmpty()) {
                        card.getChildren().add(passengersContainer);
                    }
                }
            } catch (Exception e) {
                // Показываем сообщение об ошибке
                Label errorLabel = new Label("Ошибка загрузки информации о местах");
                errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
                card.getChildren().add(errorLabel);
            }
        } else {
            Label noPassengers = new Label("Пассажиры: нет данных");
            noPassengers.setStyle(ThemeStyles.labelMuted("13px"));
            card.getChildren().add(noPassengers);
        }
        
        return card;
    }

    private Booking ensureBookingDetailsFromDb(Booking booking) {
        if (booking == null || booking.getBookingNumber() == null || booking.getBookingNumber().isBlank()) {
            return booking;
        }
        boolean missingFlight = booking.getFlight() == null;
        boolean missingPassengers = booking.getPassengersJson() == null || booking.getPassengersJson().isBlank();
        if (!missingFlight && !missingPassengers) return booking;
        try {
            Booking fromDb = supabaseClient.findBookingByNumber(booking.getBookingNumber());
            if (fromDb != null) return fromDb;
        } catch (Exception ignored) {}
        return booking;
    }
    
    private String formatFlightDuration(Flight f) {
        if (f.getDepartureTime() == null || f.getArrivalTime() == null) return "нет данных";
        java.time.Duration d = java.time.Duration.between(f.getDepartureTime(), f.getArrivalTime());
        if (d.isNegative()) d = d.negated();
        long h = d.toHours();
        long m = d.toMinutes() % 60;
        if (h > 0 && m > 0) return h + " ч " + m + " мин";
        if (h > 0) return h + " ч";
        return m + " мин";
    }

    private String extractSeatSummary(String passengersJson) {
        if (passengersJson == null || passengersJson.isEmpty()) return "не назначено";
        try {
            JsonArray passengersArray = JsonParser.parseString(passengersJson).getAsJsonArray();
            if (passengersArray.isEmpty()) return "не назначено";

            List<String> seats = new ArrayList<>();
            for (int i = 0; i < passengersArray.size(); i++) {
                JsonObject p = passengersArray.get(i).getAsJsonObject();
                String seat = p.has("seat_number") && !p.get("seat_number").isJsonNull()
                        ? p.get("seat_number").getAsString()
                        : "не назначено";
                seats.add(seat);
            }
            return String.join(", ", seats);
        } catch (Exception e) {
            return "не назначено";
        }
    }

    private HBox createBookingNumberRow(String bookingNumber) {
        HBox row = new HBox(8);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label numberLabel = new Label("Номер бронирования: " + bookingNumber);
        numberLabel.setStyle(ThemeStyles.labelPrimaryBold("18px"));

        Button copyButton = new Button("📋");
        copyButton.setStyle("-fx-background-color: " + ThemeStyles.p().btnSecondaryBg + "; -fx-text-fill: " + ThemeStyles.p().accent + "; " +
                "-fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 4 8; -fx-background-radius: 5; -fx-cursor: hand;");
        copyButton.setOnAction(e -> {
            if (bookingNumber == null || bookingNumber.isBlank()) return;
            ClipboardContent content = new ClipboardContent();
            content.putString(bookingNumber);
            Clipboard.getSystemClipboard().setContent(content);
        });

        row.getChildren().addAll(numberLabel, copyButton);
        return row;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Успех");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

