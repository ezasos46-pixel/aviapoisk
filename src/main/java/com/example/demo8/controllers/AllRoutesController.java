package com.example.demo8.controllers;

import com.example.demo8.models.*;
import com.example.demo8.services.ShortestPathService;
import com.example.demo8.services.SupabaseClient;
import com.example.demo8.utils.SceneThemeSupport;
import com.example.demo8.utils.ThemeStyleRemap;
import com.example.demo8.utils.ThemeStyles;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.example.demo8.utils.SceneTransition;

public class AllRoutesController {

    @FXML private VBox routesBox;
    @FXML private Label statusLabel;
    @FXML private TextField filterField;

    private final SupabaseClient supabaseClient = SupabaseClient.getInstance();
    private final ShortestPathService pathService = new ShortestPathService(supabaseClient);
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    // Все найденные маршруты для фильтрации
    private final List<RouteRow> allRoutes = new ArrayList<>();

    @FXML
    private void initialize() {
        javafx.application.Platform.runLater(() -> {
            if (routesBox != null) ThemeStyleRemap.bindScene(routesBox);
        });
        statusLabel.setText("Загружаем рейсы и вычисляем маршруты...");
        new Thread(() -> {
            try {
                List<Flight> allFlights = supabaseClient.getAllFlights();
                // Собираем уникальные города
                Map<UUID, String> cityNames = new LinkedHashMap<>();
                for (Flight f : allFlights) {
                    if (f.getFromCityId() != null) cityNames.put(f.getFromCityId(), f.getFromCityName());
                    if (f.getToCityId() != null) cityNames.put(f.getToCityId(), f.getToCityName());
                }

                List<UUID> cityIds = new ArrayList<>(cityNames.keySet());
                int total = cityIds.size();
                int[] done = {0};

                for (UUID fromId : cityIds) {
                    Map<UUID, ShortestPathService.PathResult> paths =
                            pathService.findAllShortestPathsFrom(fromId, allFlights);
                    for (Map.Entry<UUID, ShortestPathService.PathResult> e : paths.entrySet()) {
                        UUID toId = e.getKey();
                        ShortestPathService.PathResult result = e.getValue();
                        allRoutes.add(new RouteRow(
                                cityNames.get(fromId), fromId,
                                cityNames.getOrDefault(toId, "?"), toId,
                                result
                        ));
                    }
                    done[0]++;
                    int pct = done[0] * 100 / total;
                    Platform.runLater(() -> statusLabel.setText("Вычисляем... " + pct + "%"));
                }

                // Сортируем по цене
                allRoutes.sort(Comparator.comparing(r -> r.result.totalPrice()));

                Platform.runLater(() -> {
                    statusLabel.setText("Найдено маршрутов: " + allRoutes.size());
                    renderRoutes(allRoutes);
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() -> statusLabel.setText("Ошибка: " + ex.getMessage()));
            }
        }).start();

        filterField.textProperty().addListener((obs, o, n) -> applyFilter(n));
    }

    private void applyFilter(String text) {
        String q = text.trim().toLowerCase();
        List<RouteRow> filtered = q.isEmpty() ? allRoutes :
                allRoutes.stream().filter(r ->
                        r.fromName.toLowerCase().contains(q) || r.toName.toLowerCase().contains(q)
                ).collect(java.util.stream.Collectors.toList());
        renderRoutes(filtered);
    }

    private void renderRoutes(List<RouteRow> rows) {
        routesBox.getChildren().clear();
        // Показываем максимум 200 строк для производительности
        int limit = Math.min(rows.size(), 200);
        for (int i = 0; i < limit; i++) {
            routesBox.getChildren().add(buildRow(rows.get(i)));
        }
        if (rows.size() > 200) {
            Label more = new Label("... и ещё " + (rows.size() - 200) + " маршрутов. Уточните фильтр.");
            more.setStyle("-fx-text-fill: rgba(255,255,255,0.5); -fx-font-size: 12px; -fx-padding: 8 0 0 0;");
            routesBox.getChildren().add(more);
        }
    }

    private HBox buildRow(RouteRow row) {
        HBox hbox = new HBox(12);
        hbox.setStyle(ThemeStyles.listRow());

        List<Flight> legs = row.result.legs();
        String stops = legs.size() == 1 ? "Прямой" : "Пересадок: " + (legs.size() - 1);
        String dep = legs.isEmpty() ? "" : legs.get(0).getDepartureTime().format(FMT);

        Label route = new Label(row.fromName + " → " + row.toName);
        route.setStyle(ThemeStyles.labelPrimaryBold("13px"));
        HBox.setHgrow(route, Priority.ALWAYS);

        Label stopsLbl = new Label(stops);
        stopsLbl.setStyle("-fx-text-fill: rgba(255,255,255,0.6); -fx-font-size: 12px; -fx-min-width: 100;");

        Label depLbl = new Label(dep);
        depLbl.setStyle("-fx-text-fill: rgba(255,255,255,0.7); -fx-font-size: 12px; -fx-min-width: 130;");

        Label price = new Label(row.result.totalPrice() + " ₽");
        price.setStyle(ThemeStyles.labelPrice("14px") + " -fx-min-width: 90;");

        Button detailsBtn = new Button("Подробнее");
        detailsBtn.setStyle(ThemeStyles.detailsButton().replace("13px", "12px").replace("9 20", "6 14"));
        detailsBtn.setOnAction(e -> { e.consume(); showDetails(row); });

        Button buyBtn = new Button("Купить");
        buyBtn.setStyle(ThemeStyles.buyButton().replace("14px", "12px").replace("9 28", "6 16"));
        buyBtn.setOnAction(e -> { e.consume(); openBooking(row); });

        hbox.setOnMouseClicked(e -> showDetails(row));
        hbox.getChildren().addAll(route, stopsLbl, depLbl, price, detailsBtn, buyBtn);
        return hbox;
    }

    private void showDetails(RouteRow row) {
        List<Flight> legs = row.result.legs();

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Маршрут: " + row.fromName + " → " + row.toName);
        dialog.setHeaderText(null);

        VBox content = new VBox(10);
        content.setStyle("-fx-background-color: " + ThemeStyles.p().bgCard + "; -fx-padding: 20;");
        content.setPrefWidth(520);

        Label title = new Label(row.fromName + " → " + row.toName);
        title.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

        String stopsText = legs.size() == 1 ? "Прямой рейс" : "Пересадок: " + (legs.size() - 1);
        Label subtitle = new Label(stopsText + "  ·  Итого: " + row.result.totalPrice() + " ₽");
        subtitle.setStyle("-fx-text-fill: #ffe066; -fx-font-size: 13px;");

        content.getChildren().addAll(title, subtitle, new Separator());

        for (int i = 0; i < legs.size(); i++) {
            Flight f = legs.get(i);

            VBox legBox = new VBox(6);
            legBox.setStyle("-fx-background-color: rgba(255,255,255,0.07); -fx-background-radius: 8; -fx-padding: 12;");

            Label legTitle = new Label("Плечо " + (i + 1) + ": " + f.getFromCityName() + " → " + f.getToCityName());
            legTitle.setStyle("-fx-text-fill: #4fc3f7; -fx-font-size: 13px; -fx-font-weight: bold;");

            Label airlineLabel = new Label((f.getAirlineName() != null ? f.getAirlineName() : "") + "  " + f.getFlightNumber());
            airlineLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.8); -fx-font-size: 12px;");

            HBox times = new HBox(20);
            Label depLbl = new Label("Вылет: " + f.getDepartureTime().format(FMT));
            depLbl.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");
            Label arrLbl = new Label("Прилёт: " + f.getArrivalTime().format(FMT));
            arrLbl.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");
            Label durLbl = new Label(f.getDurationInHours() + " ч в пути");
            durLbl.setStyle("-fx-text-fill: rgba(180,200,255,0.5); -fx-font-size: 11px;");
            times.getChildren().addAll(depLbl, arrLbl, durLbl);

            Label priceLbl = new Label("Цена: " + f.getBasePrice() + " ₽");
            priceLbl.setStyle("-fx-text-fill: #ffe066; -fx-font-size: 13px; -fx-font-weight: bold;");

            legBox.getChildren().addAll(legTitle, airlineLabel, times, priceLbl);
            content.getChildren().add(legBox);

            if (i < legs.size() - 1) {
                Label transfer = new Label("Пересадка в " + f.getToCityName());
                transfer.setStyle("-fx-text-fill: rgba(255,200,0,0.7); -fx-font-size: 11px; -fx-padding: 0 0 0 8;");
                content.getChildren().add(transfer);
            }
        }

        content.getChildren().add(new Separator());

        HBox btnRow = new HBox(10);
        btnRow.setStyle("-fx-alignment: CENTER_RIGHT;");
        Button buyBtn = new Button("Купить первый рейс");
        buyBtn.setStyle("-fx-background-color: #e53935; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 6; -fx-cursor: hand;");
        buyBtn.setOnAction(e -> { dialog.close(); openBooking(row); });
        Button closeBtn = new Button("Закрыть");
        closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #4fc3f7; -fx-font-size: 13px; -fx-padding: 8 20; -fx-border-color: rgba(79,195,247,0.4); -fx-border-width: 1; -fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> dialog.close());
        btnRow.getChildren().addAll(closeBtn, buyBtn);
        content.getChildren().add(btnRow);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().lookupButton(ButtonType.CLOSE).setVisible(false);
        dialog.getDialogPane().setStyle(ThemeStyles.cardDefault());
        dialog.showAndWait();
    }

    private void openBooking(RouteRow row) {
        List<Flight> legs = row.result.legs();
        if (legs.isEmpty()) return;
        // Для бронирования используем первый рейс маршрута
        Flight firstFlight = legs.get(0);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo8/booking-view.fxml"));
            Stage stage = (Stage) routesBox.getScene().getWindow();
            Scene scene = new Scene(loader.load());
            SceneThemeSupport.install(scene);
            stage.setTitle("Бронирование");
            SceneTransition.apply(stage, scene);

            BookingController ctrl = loader.getController();
            SearchParams params = new SearchParams();
            params.setFromCity(null);
            params.setToCity(null);
            ctrl.setFlight(firstFlight);
            ctrl.setSearchParams(params);
        } catch (IOException ex) {
            new Alert(Alert.AlertType.ERROR, "Ошибка открытия бронирования: " + ex.getMessage()).showAndWait();
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo8/main-view.fxml"));
            Stage stage = (Stage) routesBox.getScene().getWindow();
            Scene scene = new Scene(loader.load(), 1200, 800);
            SceneThemeSupport.install(scene);
            stage.setTitle("Авиакасса - Поиск билетов");
            SceneTransition.apply(stage, scene);
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Ошибка: " + e.getMessage()).showAndWait();
        }
    }

    private record RouteRow(String fromName, UUID fromId, String toName, UUID toId, ShortestPathService.PathResult result) {}
}
