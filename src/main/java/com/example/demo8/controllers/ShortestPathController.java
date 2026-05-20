package com.example.demo8.controllers;

import com.example.demo8.models.City;
import com.example.demo8.models.Flight;
import com.example.demo8.models.SearchParams;
import com.example.demo8.services.ShortestPathService;
import com.example.demo8.services.SupabaseClient;
import com.example.demo8.services.UnsplashService;
import com.example.demo8.utils.CitySearchHelper;
import com.example.demo8.utils.SceneThemeSupport;
import com.example.demo8.utils.ThemeManager;
import com.example.demo8.utils.ThemeStyleRemap;
import com.example.demo8.utils.ThemeStyles;
import com.example.demo8.utils.SceneTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ShortestPathController {

    @FXML private ComboBox<City> fromCombo;
    @FXML private ComboBox<City> toCombo;
    @FXML private VBox resultBox;
    @FXML private WebView mapView;
    @FXML private BorderPane rootPane;
    @FXML private HBox screenHeader;
    @FXML private HBox centerHBox;
    @FXML private VBox leftPanel;
    @FXML private HBox searchPanelTop;
    @FXML private HBox searchBtnRow;
    @FXML private StackPane mapPanel;

    private final SupabaseClient supabaseClient = SupabaseClient.getInstance();
    private final ShortestPathService pathService = new ShortestPathService(supabaseClient);
    private final UnsplashService unsplashService = new UnsplashService();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private static final Map<String, double[]> CITY_COORDS = new HashMap<>();
    static {
        CITY_COORDS.put("Москва",                  new double[]{55.75, 37.6});
        CITY_COORDS.put("Санкт-Петербург",          new double[]{59.95, 30.3});
        CITY_COORDS.put("Новосибирск",              new double[]{55.05, 82.9});
        CITY_COORDS.put("Екатеринбург",             new double[]{56.85, 60.6});
        CITY_COORDS.put("Казань",                   new double[]{55.8,  49.1});
        CITY_COORDS.put("Нижний Новгород",          new double[]{56.3,  44.0});
        CITY_COORDS.put("Челябинск",                new double[]{55.15, 61.4});
        CITY_COORDS.put("Самара",                   new double[]{53.2,  50.2});
        CITY_COORDS.put("Уфа",                      new double[]{54.74, 55.97});
        CITY_COORDS.put("Ростов-на-Дону",           new double[]{47.23, 39.7});
        CITY_COORDS.put("Краснодар",                new double[]{45.04, 38.97});
        CITY_COORDS.put("Пермь",                    new double[]{58.0,  56.25});
        CITY_COORDS.put("Воронеж",                  new double[]{51.67, 39.2});
        CITY_COORDS.put("Волгоград",                new double[]{48.7,  44.5});
        CITY_COORDS.put("Красноярск",               new double[]{56.01, 92.87});
        CITY_COORDS.put("Саратов",                  new double[]{51.53, 46.03});
        CITY_COORDS.put("Тюмень",                   new double[]{57.15, 68.98});
        CITY_COORDS.put("Ижевск",                   new double[]{56.85, 53.2});
        CITY_COORDS.put("Барнаул",                  new double[]{53.35, 83.78});
        CITY_COORDS.put("Иркутск",                  new double[]{52.29, 104.3});
        CITY_COORDS.put("Хабаровск",                new double[]{48.48, 135.07});
        CITY_COORDS.put("Владивосток",              new double[]{43.12, 131.87});
        CITY_COORDS.put("Ярославль",                new double[]{57.63, 39.87});
        CITY_COORDS.put("Омск",                     new double[]{54.99, 73.37});
        CITY_COORDS.put("Томск",                    new double[]{56.5,  84.97});
        CITY_COORDS.put("Кемерово",                 new double[]{55.35, 86.09});
        CITY_COORDS.put("Новокузнецк",              new double[]{53.76, 87.1});
        CITY_COORDS.put("Рязань",                   new double[]{54.63, 39.72});
        CITY_COORDS.put("Астрахань",                new double[]{46.35, 48.03});
        CITY_COORDS.put("Пенза",                    new double[]{53.2,  45.0});
        CITY_COORDS.put("Липецк",                   new double[]{52.6,  39.6});
        CITY_COORDS.put("Тула",                     new double[]{54.19, 37.62});
        CITY_COORDS.put("Киров",                    new double[]{58.6,  49.67});
        CITY_COORDS.put("Чебоксары",                new double[]{56.13, 47.25});
        CITY_COORDS.put("Калининград",              new double[]{54.71, 20.5});
        CITY_COORDS.put("Брянск",                   new double[]{53.24, 34.37});
        CITY_COORDS.put("Курск",                    new double[]{51.73, 36.19});
        CITY_COORDS.put("Иваново",                  new double[]{57.0,  41.0});
        CITY_COORDS.put("Магнитогорск",             new double[]{53.41, 59.06});
        CITY_COORDS.put("Улан-Удэ",                 new double[]{51.83, 107.6});
        CITY_COORDS.put("Сочи",                     new double[]{43.6,  39.72});
        CITY_COORDS.put("Мурманск",                 new double[]{68.97, 33.08});
        CITY_COORDS.put("Архангельск",              new double[]{64.54, 40.55});
        CITY_COORDS.put("Якутск",                   new double[]{62.03, 129.73});
        CITY_COORDS.put("Южно-Сахалинск",           new double[]{46.96, 142.73});
        CITY_COORDS.put("Петропавловск-Камчатский", new double[]{53.01, 158.65});
        CITY_COORDS.put("Магадан",                  new double[]{59.57, 150.8});
        CITY_COORDS.put("Нальчик",                  new double[]{43.49, 43.62});
        CITY_COORDS.put("Махачкала",                new double[]{42.98, 47.5});
        CITY_COORDS.put("Грозный",                  new double[]{43.32, 45.7});
        CITY_COORDS.put("Владикавказ",              new double[]{43.02, 44.68});
        CITY_COORDS.put("Ставрополь",               new double[]{45.04, 41.97});
        CITY_COORDS.put("Симферополь",              new double[]{44.95, 34.1});
        CITY_COORDS.put("Минеральные Воды",         new double[]{44.21, 43.13});
        CITY_COORDS.put("Анапа",                    new double[]{44.9,  37.32});
        CITY_COORDS.put("Геленджик",                new double[]{44.56, 38.08});
        CITY_COORDS.put("Белгород",                 new double[]{50.6,  36.59});
        CITY_COORDS.put("Орёл",                     new double[]{52.97, 36.07});
        CITY_COORDS.put("Тамбов",                   new double[]{52.72, 41.43});
        CITY_COORDS.put("Смоленск",                 new double[]{54.78, 32.05});
        CITY_COORDS.put("Псков",                    new double[]{57.82, 28.33});
        CITY_COORDS.put("Великий Новгород",         new double[]{58.52, 31.27});
        CITY_COORDS.put("Вологда",                  new double[]{59.22, 39.9});
        CITY_COORDS.put("Петрозаводск",             new double[]{61.79, 34.35});
        CITY_COORDS.put("Сыктывкар",                new double[]{61.67, 50.83});
        CITY_COORDS.put("Нарьян-Мар",               new double[]{67.64, 53.09});
        CITY_COORDS.put("Салехард",                 new double[]{66.53, 66.6});
        CITY_COORDS.put("Ханты-Мансийск",           new double[]{61.0,  69.0});
        CITY_COORDS.put("Сургут",                   new double[]{61.25, 73.4});
        CITY_COORDS.put("Нижневартовск",            new double[]{60.93, 76.55});
        CITY_COORDS.put("Чита",                     new double[]{52.03, 113.5});
        CITY_COORDS.put("Благовещенск",             new double[]{50.27, 127.53});
        CITY_COORDS.put("Комсомольск-на-Амуре",     new double[]{50.55, 137.0});
        CITY_COORDS.put("Абакан",                   new double[]{53.72, 91.43});
        CITY_COORDS.put("Кызыл",                    new double[]{51.72, 94.45});
        CITY_COORDS.put("Горно-Алтайск",            new double[]{51.96, 85.96});
        CITY_COORDS.put("Элиста",                   new double[]{46.31, 44.27});
        CITY_COORDS.put("Саранск",                  new double[]{54.19, 45.18});
        CITY_COORDS.put("Йошкар-Ола",               new double[]{56.63, 47.88});
        CITY_COORDS.put("Ульяновск",                new double[]{54.32, 48.4});
        CITY_COORDS.put("Оренбург",                 new double[]{51.77, 55.1});
        CITY_COORDS.put("Курган",                   new double[]{55.45, 65.34});
        CITY_COORDS.put("Норильск",                 new double[]{69.35, 88.2});
        CITY_COORDS.put("Воркута",                  new double[]{67.5,  64.05});
        CITY_COORDS.put("Анадырь",                  new double[]{64.73, 177.5});
        CITY_COORDS.put("Певек",                    new double[]{69.7,  170.27});
        CITY_COORDS.put("Мирный",                   new double[]{62.53, 114.89});
        CITY_COORDS.put("Нерюнгри",                 new double[]{56.66, 124.72});
        CITY_COORDS.put("Орск",                     new double[]{51.23, 58.6});
        CITY_COORDS.put("Стерлитамак",              new double[]{53.63, 55.95});
        CITY_COORDS.put("Набережные Челны",         new double[]{55.74, 52.43});
        CITY_COORDS.put("Тобольск",                 new double[]{58.2,  68.2});
        CITY_COORDS.put("Нижнекамск",               new double[]{55.63, 51.82});
        CITY_COORDS.put("Новороссийск",             new double[]{44.72, 37.77});
        CITY_COORDS.put("Таганрог",                 new double[]{47.2,  38.9});
        CITY_COORDS.put("Армавир",                  new double[]{44.99, 41.12});
        CITY_COORDS.put("Волжский",                 new double[]{48.79, 44.77});
        CITY_COORDS.put("Тверь",                    new double[]{56.86, 35.9});
        CITY_COORDS.put("Калуга",                   new double[]{54.51, 36.27});
        CITY_COORDS.put("Кострома",                 new double[]{57.77, 40.93});
        CITY_COORDS.put("Владимир",                 new double[]{56.13, 40.42});
        CITY_COORDS.put("Тольятти",                 new double[]{53.51, 49.42});
        CITY_COORDS.put("Сызрань",                  new double[]{53.16, 48.47});
        CITY_COORDS.put("Шахты",                    new double[]{47.71, 40.22});
        CITY_COORDS.put("Батайск",                  new double[]{47.14, 39.75});
        CITY_COORDS.put("Новочеркасск",             new double[]{47.42, 40.1});
    }

    @FXML
    private void initialize() {
        if (mapView == null) {
            showError("Карта недоступна: компонент не инициализирован.");
            return;
        }
        mapView.setCache(false);
        mapView.setContextMenuEnabled(false);
        loadMap(Collections.emptyList()); // загружает map.html при старте
        applyScreenTheme();
        new Thread(() -> {
            try {
                List<City> cities = supabaseClient.getCities();
                Platform.runLater(() -> {
                    javafx.collections.ObservableList<City> cityList =
                            javafx.collections.FXCollections.observableArrayList(cities);
                    fromCombo.setItems(cityList);
                    toCombo.setItems(cityList);
                    CitySearchHelper.setup(fromCombo, cityList);
                    CitySearchHelper.setup(toCombo, cityList);
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showError("Ошибка загрузки городов: " + e.getMessage()));
            }
        }).start();
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
            if (screenHeader != null) screenHeader.setStyle(ThemeStyles.screenHeader());
            if (centerHBox != null) centerHBox.setStyle("-fx-background-color: " + p.bgMain + ";");
            if (leftPanel != null) leftPanel.setStyle(ThemeStyles.leftPanel());
            if (searchPanelTop != null) searchPanelTop.setStyle(ThemeStyles.searchPanel());
            if (searchBtnRow != null) searchBtnRow.setStyle(ThemeStyles.searchBtnRow());
            if (mapPanel != null) mapPanel.setStyle(ThemeStyles.mapPanelBg());
            if (resultBox != null) resultBox.setStyle("-fx-padding: 16 20; -fx-background-color: " + p.bgMain + ";");
            ThemeStyleRemap.applyTree(rootPane);
        });
    }

    @FXML
    private void handleFind() {
        City from = fromCombo.getValue();
        City to   = toCombo.getValue();
        if (from == null || to == null) { showError("Выберите города отправления и назначения"); return; }
        if (from.getId().equals(to.getId())) { showError("Города должны отличаться"); return; }

        resultBox.getChildren().clear();
        Label loading = new Label("Вычисляем кратчайший маршрут...");
        loading.setStyle(ThemeStyles.labelPrimary("14px"));
        resultBox.getChildren().add(loading);

        new Thread(() -> {
            try {
                ShortestPathService.PathResult result = pathService.findShortestPath(from.getId(), to.getId());
                Platform.runLater(() -> {
                    resultBox.getChildren().clear();
                    if (result == null) {
                        Label noPath = new Label("Маршрут не найден");
                        noPath.setStyle(ThemeStyles.labelPrimary("16px"));
                        resultBox.getChildren().add(noPath);
                        loadMap(Collections.emptyList());
                        return;
                    }
                    displayResult(result);
                    loadMap(result.legs());
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showError("Ошибка: " + e.getMessage()));
            }
        }).start();
    }

    private boolean mapLoaded = false;
    private boolean listenerAdded = false;
    private boolean mapBroken = false;
    private int mapLoadAttempts = 0;
    private List<Flight> pendingLegs = null;

    private void loadMap(List<Flight> legs) {
        if (mapBroken || mapView == null) return;
        pendingLegs = legs;
        if (!mapLoaded) {
            if (!listenerAdded) {
                listenerAdded = true;
                java.net.URL url = getClass().getResource("/com/example/demo8/leaflet/map.html");
                if (url == null) {
                    mapBroken = true;
                    showError("Не найден ресурс карты.");
                    return;
                }
                WebEngine engine = mapView.getEngine();
                engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                    if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                        mapLoaded = true;
                        mapBroken = false;
                        if (pendingLegs != null) {
                            drawRouteOnMap(pendingLegs);
                            pendingLegs = null;
                        }
                    } else if (newState == javafx.concurrent.Worker.State.FAILED) {
                        // WebView/сеть могут кратковременно падать. Даем 1 повтор, без отключения карты навсегда.
                        if (mapLoadAttempts < 1) {
                            mapLoadAttempts++;
                            try {
                                engine.reload();
                            } catch (Exception ignored) {}
                        }
                    }
                });
                try {
                    engine.load(url.toExternalForm());
                } catch (Exception ex) {
                    mapBroken = true;
                    showError("Ошибка инициализации карты: " + ex.getMessage());
                }
            }
        } else {
            drawRouteOnMap(legs);
            pendingLegs = null;
        }
    }

    private void drawRouteOnMap(List<Flight> legs) {
        if (mapBroken || mapView == null || !mapLoaded) return;
        List<Flight> safeLegs = legs != null ? legs : Collections.emptyList();
        if (safeLegs.isEmpty()) {
            try {
                mapView.getEngine().executeScript(
                        "if(typeof drawRoute==='function'){drawRoute([]);}" +
                                "else{console.error('drawRoute not ready');}"
                );
            } catch (RuntimeException ignored) {}
            return;
        }

        List<String> routeCities = new ArrayList<>();
        Flight first = safeLegs.get(0);
        if (first == null || first.getFromCityName() == null) return;
        routeCities.add(first.getFromCityName());
        for (Flight f : safeLegs) {
            if (f != null && f.getToCityName() != null) {
                routeCities.add(f.getToCityName());
            }
        }
        if (routeCities.size() < 2) return;
        drawCitiesWithPhotos(routeCities);
    }

    private void drawCitiesWithPhotos(List<String> cityNames) {
        if (cityNames == null || cityNames.isEmpty()) return;
        new Thread(() -> {
            StringBuilder points = new StringBuilder("[");
            for (int i = 0; i < cityNames.size(); i++) {
                String cityName = cityNames.get(i);
                double[] coords = CITY_COORDS.get(cityName);
                if (coords == null) continue;
                String color = ThemeStyles.mapMarkerColor(i, cityNames.size());
                String escapedName = cityName.replace("'", "\\'");
                String photoUrl = unsplashService.getCityPhotoUrl(cityName).replace("'", "\\'");
                points.append(String.format(java.util.Locale.US,
                        "[%.4f,%.4f,'%s','%s','%s'],",
                        coords[1], coords[0], color, escapedName, photoUrl));
            }
            points.append("]");
            Platform.runLater(() -> {
                try {
                    mapView.getEngine().executeScript(
                            "if(typeof drawRoute==='function'){drawRoute(" + points + ");}" +
                                    "else{console.error('drawRoute not ready');}"
                    );
                } catch (RuntimeException ex) {
                    try {
                        mapView.getEngine().reload();
                        mapLoaded = false;
                    } catch (Exception ignored) {}
                }
            });
        }).start();
    }

    private void displayResult(ShortestPathService.PathResult result) {
        List<Flight> legs = result.legs();
        if (legs == null || legs.isEmpty()) {
            Label noPath = new Label("Маршрут не найден");
            noPath.setStyle(ThemeStyles.labelPrimary("16px"));
            resultBox.getChildren().add(noPath);
            loadMap(Collections.emptyList());
            return;
        }

        Label summary = new Label(
            "Маршрут: " + legs.get(0).getFromCityName() + " → " + legs.get(legs.size()-1).getToCityName()
            + (legs.size() > 1 ? "  |  Пересадок: " + (legs.size()-1) : "  |  Прямой рейс")
            + "  |  " + result.totalPrice() + " руб."
        );
        summary.setStyle(ThemeStyles.labelPrimaryBold("13px") + " -fx-padding: 0 0 6 0;");
        summary.setWrapText(true);
        resultBox.getChildren().add(summary);

        for (int i = 0; i < legs.size(); i++) {
            Flight f = legs.get(i);

            VBox card = new VBox(6);
            card.setStyle(ThemeStyles.legCard());

            // Заголовок плеча
            Label legLbl = new Label("Рейс " + (i+1) + ": " + f.getFromCityName() + " → " + f.getToCityName());
            legLbl.setStyle(ThemeStyles.labelPrimaryBold("12px"));

            // Краткая строка: авиакомпания + рейс + цена
            HBox brief = new HBox(10);
            brief.setStyle("-fx-alignment: CENTER_LEFT;");
            Label flightLbl = new Label(f.getAirlineName() + "  " + f.getFlightNumber());
            flightLbl.setStyle(ThemeStyles.labelSecondary("11px"));
            Label priceLbl = new Label(f.getBasePrice() + " руб.");
            priceLbl.setStyle(ThemeStyles.labelPrice("12px"));
            brief.getChildren().addAll(flightLbl, priceLbl);

            // Детали (скрыты по умолчанию)
            VBox details = new VBox(4);
            details.setVisible(false);
            details.setManaged(false);
            details.setStyle("-fx-padding: 6 0 4 0;");

            Label depLbl = new Label("✈  Вылет:  " + f.getDepartureTime().format(FMT));
            depLbl.setStyle(ThemeStyles.labelSecondary("11px"));
            Label arrLbl = new Label("🛬  Прилёт: " + f.getArrivalTime().format(FMT));
            arrLbl.setStyle(ThemeStyles.labelSecondary("11px"));
            Label airlineLbl = new Label("🏢  Авиакомпания: " + f.getAirlineName());
            airlineLbl.setStyle(ThemeStyles.labelSecondary("11px"));
            Label flightNumLbl = new Label("🔢  Номер рейса: " + f.getFlightNumber());
            flightNumLbl.setStyle(ThemeStyles.labelSecondary("11px"));
            Label priceDtl = new Label("💰  Цена: " + f.getBasePrice() + " руб.");
            priceDtl.setStyle(ThemeStyles.labelPrice("11px"));
            details.getChildren().addAll(depLbl, arrLbl, airlineLbl, flightNumLbl, priceDtl);

            // Кнопки
            HBox buttons = new HBox(8);
            buttons.setStyle("-fx-alignment: CENTER_LEFT; -fx-padding: 4 0 0 0;");

            Button detailsBtn = new Button("Подробнее ▼");
            detailsBtn.setStyle(ThemeStyles.detailsButton().replace("13px", "11px").replace("9 20", "4 10").replace("8", "5"));
            detailsBtn.setOnAction(e -> {
                boolean nowVisible = !details.isVisible();
                details.setVisible(nowVisible);
                details.setManaged(nowVisible);
                detailsBtn.setText(nowVisible ? "Скрыть ▲" : "Подробнее ▼");
            });

            Button buyBtn = new Button("Купить");
            buyBtn.setStyle(ThemeStyles.buyButton().replace("14px", "11px").replace("9 28", "4 12").replace("8", "5"));
            buyBtn.setOnAction(e -> openBooking(f));

            buttons.getChildren().addAll(detailsBtn, buyBtn);
            card.getChildren().addAll(legLbl, brief, details, buttons);
            resultBox.getChildren().add(card);

            if (i < legs.size() - 1) {
                Label transfer = new Label("⟳  Пересадка в " + f.getToCityName());
                transfer.setStyle(ThemeStyles.labelMuted("11px") + " -fx-padding: 1 0 1 8;");
                resultBox.getChildren().add(transfer);
            }
        }

        resultBox.getChildren().add(new Separator());
        HBox footer = new HBox(12);
        footer.setStyle("-fx-alignment: CENTER_LEFT; -fx-padding: 4 0 0 0;");
        Label total = new Label("Итого: " + result.totalPrice() + " руб.");
        total.setStyle(ThemeStyles.labelPrice("15px"));
        footer.getChildren().add(total);
        resultBox.getChildren().add(footer);
        ThemeStyleRemap.applyTree(resultBox);
        applyScreenTheme();
    }

    private void openBooking(Flight flight) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo8/booking-view.fxml"));
            Stage stage = (Stage) resultBox.getScene().getWindow();
            Scene scene = new Scene(loader.load());
            SceneThemeSupport.install(scene);
            stage.setTitle("Бронирование");
            SceneTransition.apply(stage, scene);
            BookingController ctrl = loader.getController();
            ctrl.setFlight(flight);
            ctrl.setSearchParams(new SearchParams());
        } catch (IOException e) {
            showError("Ошибка открытия бронирования: " + e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo8/main-view.fxml"));
            Stage stage = (Stage) resultBox.getScene().getWindow();
            Scene scene = new Scene(loader.load(), 1200, 800);
            SceneThemeSupport.install(scene);
            stage.setTitle("Авиакасса - Поиск билетов");
            SceneTransition.apply(stage, scene);
        } catch (IOException e) {
            showError("Ошибка возврата: " + e.getMessage());
        }
    }

    private void showError(String msg) {
        Platform.runLater(() -> {
            resultBox.getChildren().clear();
            Label err = new Label(msg);
            err.setStyle("-fx-text-fill: #ff6b6b; -fx-font-size: 13px;");
            resultBox.getChildren().add(err);
        });
    }
}
