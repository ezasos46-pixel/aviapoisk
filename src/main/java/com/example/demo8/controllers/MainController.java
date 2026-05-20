package com.example.demo8.controllers;

import com.example.demo8.models.City;
import com.example.demo8.models.SearchParams;
import com.example.demo8.services.SupabaseClient;
import com.example.demo8.utils.CitySearchHelper;
import com.example.demo8.utils.ConfirmDialog;
import com.example.demo8.utils.SceneTransition;
import com.example.demo8.utils.SessionManager;
import com.example.demo8.utils.MainThemeApplier;
import com.example.demo8.utils.SceneThemeSupport;
import com.example.demo8.utils.ThemeManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    /** Карточки на главной: только эти города, в заданном порядке. */
    private static final List<String> MAIN_TOP_CITY_NAMES = List.of(
            "Екатеринбург", "Казань", "Москва", "Новосибирск");

    @FXML private BorderPane rootPane;
    @FXML private HBox mainHeaderHBox;
    @FXML private Button themeToggleButton;
    @FXML private VBox mainScrollVBox;
    @FXML private VBox mainHeroVBox;
    @FXML private Label heroTitleLabel;
    @FXML private Label heroSubtitleLabel;
    @FXML private VBox mainSearchCard;
    @FXML private Button btnShortestPath;
    @FXML private Button btnFindTickets;
    @FXML private VBox mainCitiesSection;
    @FXML private Label citiesTitleLabel;
    @FXML private Label citiesHintLabel;
    @FXML private HBox mainFeaturesRow;
    @FXML private HBox mainFooterHBox;
    @FXML private HBox citiesBox;
    @FXML private ComboBox<City> fromCityCombo;
    @FXML private ComboBox<City> stopCityCombo;
    @FXML private ComboBox<City> toCityCombo;
    @FXML private DatePicker departureDatePicker;
    @FXML private DatePicker returnDatePicker;
    @FXML private Button passengersButton;
    @FXML private CheckBox extraBaggageCheck;
    @FXML private Button authButton;
    @FXML private Button profileButton;

    private SupabaseClient supabaseClient;
    private SearchParams searchParams;
    private ObservableList<City> cities;
    /** Повторный setup() вешает слушатели дважды и ломает ComboBox. */
    private boolean citySearchHelperInstalled;
    private List<City> lastCityListForCards = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        supabaseClient = SupabaseClient.getInstance();
        searchParams = new SearchParams();
        cities = FXCollections.observableArrayList();

        departureDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });

        returnDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate dep = departureDatePicker.getValue();
                setDisable(empty || (dep != null && date.isBefore(dep)));
            }
        });

        departureDatePicker.valueProperty().addListener((obs, o, n) -> {
            if (n != null && returnDatePicker.getValue() != null && returnDatePicker.getValue().isBefore(n))
                returnDatePicker.setValue(null);
        });

        loadCities();
        updateUI();

        if (profileButton != null) {
            profileButton.setTooltip(new Tooltip("Профиль"));
        }
        if (themeToggleButton != null) {
            themeToggleButton.setTooltip(new Tooltip(ThemeManager.getInstance().getToggleTooltip()));
        }
        Platform.runLater(() -> {
            if (rootPane != null && rootPane.getScene() != null) {
                ThemeManager.getInstance().applyToScene(rootPane.getScene());
            }
            applyMainTheme();
        });
    }

    @FXML
    private void handleThemeToggle() {
        ThemeManager.getInstance().toggle();
        if (rootPane != null && rootPane.getScene() != null) {
            SceneThemeSupport.install(rootPane.getScene());
        }
        applyMainTheme();
        if (!lastCityListForCards.isEmpty()) {
            loadCityCards(lastCityListForCards);
        }
    }

    private void applyMainTheme() {
        ThemeManager.Palette p = ThemeManager.getInstance().getPalette();

        if (rootPane != null) {
            rootPane.setStyle("-fx-background-color: " + p.bgMain + ";");
        }
        if (mainHeaderHBox != null) {
            mainHeaderHBox.setStyle("-fx-background-color: " + p.bgHeader + "; -fx-padding: 0 32; -fx-min-height: 64; " +
                    "-fx-border-color: " + p.borderSoft + "; -fx-border-width: 0 0 1 0;");
            styleHeaderLogo(mainHeaderHBox, p);
        }
        if (mainScrollVBox != null) {
            mainScrollVBox.setStyle("-fx-background-color: " + p.bgMain + ";");
        }
        if (mainHeroVBox != null) {
            mainHeroVBox.setStyle("-fx-padding: 60 80 50 80; -fx-background-color: linear-gradient(to bottom, " +
                    p.bgHeroGradientTop + ", " + p.bgHeroGradientBottom + ");");
        }
        if (heroTitleLabel != null) {
            heroTitleLabel.setStyle("-fx-text-fill: " + p.textPrimary + "; -fx-font-size: 38px; -fx-font-weight: bold;");
        }
        if (heroSubtitleLabel != null) {
            heroSubtitleLabel.setStyle("-fx-text-fill: " + p.textSecondary + "; -fx-font-size: 15px;");
        }
        if (mainSearchCard != null) {
            String shadow = ThemeManager.getInstance().getCurrent() == ThemeManager.Theme.LIGHT
                    ? "dropshadow(gaussian, rgba(2,136,209,0.15), 20, 0, 0, 6)"
                    : "dropshadow(gaussian, rgba(0,0,0,0.6), 30, 0, 0, 10)";
            mainSearchCard.setStyle("-fx-background-color: " + p.bgCard + "; -fx-background-radius: 16; " +
                    "-fx-border-color: " + p.borderSoft + "; -fx-border-width: 1; -fx-border-radius: 16; " +
                    "-fx-effect: " + shadow + ";");
        }
        if (mainCitiesSection != null) {
            mainCitiesSection.setStyle("-fx-padding: 40 80 30 80; -fx-background-color: " + p.bgMain + ";");
        }
        if (citiesTitleLabel != null) {
            citiesTitleLabel.setStyle("-fx-text-fill: " + p.textPrimary + "; -fx-font-size: 20px; -fx-font-weight: bold; -fx-text-alignment: center;");
        }
        if (citiesHintLabel != null) {
            citiesHintLabel.setStyle("-fx-text-fill: " + p.textMuted + "; -fx-font-size: 12px;");
        }
        if (mainFeaturesRow != null) {
            mainFeaturesRow.setStyle("-fx-padding: 24 80 50 80; -fx-background-color: " + p.bgMain + ";");
            styleFeatureCards(mainFeaturesRow, p);
        }
        if (mainFooterHBox != null) {
            mainFooterHBox.setStyle("-fx-background-color: " + p.bgFooter + "; -fx-padding: 14 32; " +
                    "-fx-border-color: " + p.borderSoft + "; -fx-border-width: 1 0 0 0;");
            for (Node node : mainFooterHBox.getChildren()) {
                if (node instanceof Label label) {
                    label.setStyle("-fx-text-fill: " + p.footerText + "; -fx-font-size: 12px;");
                } else if (node instanceof Button btn) {
                    btn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + p.footerText + "; " +
                            "-fx-font-size: 12px; -fx-border-color: transparent; -fx-cursor: hand; -fx-underline: true; -fx-padding: 4 0;");
                }
            }
        }

        if (passengersButton != null) {
            passengersButton.setStyle("-fx-background-color: " + p.btnSecondaryBg + "; -fx-text-fill: " + p.btnSecondaryText + "; " +
                    "-fx-font-size: 13px; -fx-padding: 9 16; -fx-background-radius: 8; -fx-border-color: " + p.btnSecondaryBorder + "; " +
                    "-fx-border-width: 1; -fx-border-radius: 8; -fx-cursor: hand;");
        }
        if (extraBaggageCheck != null) {
            extraBaggageCheck.setStyle("-fx-font-size: 13px; -fx-text-fill: " + p.checkboxText + ";");
        }
        if (btnShortestPath != null) {
            btnShortestPath.setStyle("-fx-background-color: " + p.btnRouteBg + "; -fx-text-fill: " + p.btnRouteText + "; " +
                    "-fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 8; " +
                    "-fx-border-color: " + p.btnRouteBorder + "; -fx-border-width: 1; -fx-border-radius: 8; -fx-cursor: hand;");
        }
        if (btnFindTickets != null) {
            String buyShadow = ThemeManager.getInstance().getCurrent() == ThemeManager.Theme.LIGHT
                    ? "dropshadow(gaussian, rgba(2,136,209,0.25), 10, 0, 0, 3)"
                    : "dropshadow(gaussian, rgba(229,57,53,0.4), 12, 0, 0, 4)";
            btnFindTickets.setStyle("-fx-background-color: " + p.btnBuy + "; -fx-text-fill: " + p.btnBuyText + "; " +
                    "-fx-font-size: 15px; -fx-font-weight: bold; -fx-padding: 10 32; -fx-background-radius: 8; " +
                    "-fx-effect: " + buyShadow + "; -fx-cursor: hand;");
        }
        if (themeToggleButton != null) {
            themeToggleButton.setText(ThemeManager.getInstance().getToggleIcon());
            themeToggleButton.setTooltip(new Tooltip(ThemeManager.getInstance().getToggleTooltip()));
            themeToggleButton.setStyle("-fx-background-color: transparent; -fx-text-fill: " + p.accent + "; " +
                    "-fx-font-size: 18px; -fx-padding: 6 10; -fx-cursor: hand; -fx-border-color: " + p.border + "; " +
                    "-fx-border-width: 1; -fx-background-radius: 6;");
        }

        styleSearchCardLabels(p);
        updateUI();
    }

    private void styleSearchCardLabels(ThemeManager.Palette p) {
        if (mainSearchCard == null) return;
        styleLabelsRecursive(mainSearchCard, p.accent, p.textSecondary);
    }

    private void styleLabelsRecursive(Node node, String accent, String secondary) {
        if (node instanceof Label label) {
            String t = label.getText();
            if (t != null && (t.equals("⇄") || t.contains("ОТКУДА") || t.contains("КУДА") || t.contains("ПЕРЕСАДКА") || t.contains("ДАТА"))) {
                boolean muted = t.contains("ПЕРЕСАДКА") || t.equals("ОБРАТНО");
                label.setStyle("-fx-text-fill: " + (muted ? secondary : accent) + "; -fx-font-size: " +
                        (t.equals("⇄") ? "18px; -fx-padding: 0 4;" : "10px; -fx-font-weight: bold;"));
            }
        }
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                styleLabelsRecursive(child, accent, secondary);
            }
        }
    }

    private void styleHeaderLogo(HBox header, ThemeManager.Palette p) {
        for (Node node : header.getChildren()) {
            if (!(node instanceof HBox logoRow)) continue;
            for (Node child : logoRow.getChildren()) {
                if (child instanceof Label lbl) {
                    if ("✈".equals(lbl.getText())) {
                        lbl.setStyle("-fx-text-fill: " + p.accent + "; -fx-font-size: 26px;");
                    }
                } else if (child instanceof VBox titles) {
                    int i = 0;
                    for (Node t : titles.getChildren()) {
                        if (t instanceof Label lbl) {
                            if (i == 0) {
                                lbl.setStyle("-fx-text-fill: " + p.textPrimary + "; -fx-font-size: 18px; -fx-font-weight: bold;");
                            } else {
                                lbl.setStyle("-fx-text-fill: " + p.accentSoft + "; -fx-font-size: 10px;");
                            }
                            i++;
                        }
                    }
                }
            }
        }
    }

    private void styleFeatureCards(HBox row, ThemeManager.Palette p) {
        for (Node node : row.getChildren()) {
            if (node instanceof VBox card) {
                card.setStyle("-fx-background-color: " + p.featureCardBg + "; -fx-background-radius: 10; -fx-padding: 18; " +
                        "-fx-border-color: " + p.featureCardBorder + "; -fx-border-width: 1; -fx-border-radius: 10;");
                int i = 0;
                for (Node c : card.getChildren()) {
                    if (c instanceof Label lbl) {
                        if (i == 0) {
                            lbl.setStyle("-fx-text-fill: " + p.textPrimary + "; -fx-font-size: 14px; -fx-font-weight: bold;");
                        } else {
                            lbl.setStyle("-fx-text-fill: " + p.textSecondary + "; -fx-font-size: 12px;");
                        }
                        i++;
                    }
                }
            }
        }
    }

    private void loadCities() {
        new Thread(() -> {
            try {
                List<City> cityList = supabaseClient.getCities();
                System.out.println("getCities() вернул: " + cityList.size() + " городов");

                Platform.runLater(() -> {
                    cities.setAll(cityList);
                    fromCityCombo.setItems(cities);
                    stopCityCombo.setItems(cities);
                    toCityCombo.setItems(cities);
                    if (!citySearchHelperInstalled) {
                        CitySearchHelper.setup(fromCityCombo, cities);
                        CitySearchHelper.setup(stopCityCombo, cities);
                        CitySearchHelper.setup(toCityCombo, cities);
                        citySearchHelperInstalled = true;
                    }
                });
                loadCityCards(cityList);
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Ошибка подключения");
                    alert.setHeaderText("Не удалось подключиться к серверу");
                    
                    String errorMsg = "Возможные причины:\n" +
                            "• Отсутствует подключение к интернету\n" +
                            "• Проблемы с DNS (попробуйте сменить DNS на 8.8.8.8)\n" +
                            "• Файрвол или антивирус блокирует соединение\n" +
                            "• Сервер временно недоступен\n\n" +
                            "Техническая информация: " + e.getMessage();
                    
                    alert.setContentText(errorMsg);
                    ButtonType retry = new ButtonType("Повторить", ButtonBar.ButtonData.OK_DONE);
                    ButtonType cancel = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);
                    alert.getButtonTypes().setAll(retry, cancel);
                    alert.showAndWait().ifPresent(btn -> { if (btn == retry) loadCities(); });
                });
            }
        }).start();
    }

    private void loadCityCards(List<City> cityList) {
        lastCityListForCards = new ArrayList<>(cityList);
        String[] emojis = {"🏔", "🏛", "🏙", "🌆"};
        List<City> topCities = new ArrayList<>();
        for (String name : MAIN_TOP_CITY_NAMES) {
            cityList.stream()
                    .filter(c -> name.equalsIgnoreCase(c.getName()))
                    .findFirst()
                    .ifPresent(topCities::add);
        }
        ThemeManager.Palette p = ThemeManager.getInstance().getPalette();
        Platform.runLater(() -> {
            citiesBox.getChildren().clear();
            for (int i = 0; i < topCities.size(); i++) {
                City city = topCities.get(i);
                VBox card = MainThemeApplier.buildCityCard(city, emojis[i % emojis.length], p);
                card.setOnMouseClicked(e -> openCityScreen(city));
                citiesBox.getChildren().add(card);
            }
        });
    }

    @FXML
    private void handlePassengers() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo8/passengers-view.fxml"));
            Stage stage = new Stage();
            Scene scene = new Scene(loader.load());
            SceneThemeSupport.install(scene);
            stage.setScene(scene);
            stage.setTitle("Выбор пассажиров");
            stage.setResizable(false);
            PassengersController controller = loader.getController();
            if (controller != null) {
                controller.setSearchParams(searchParams);
                controller.setMainController(this);
            }
            stage.showAndWait();
            updatePassengersButton();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Ошибка открытия окна выбора пассажиров: " + e.getMessage());
        }
    }

    @FXML
    private void handleSearch() {
        if (fromCityCombo.getValue() == null) { showError("Выберите город отправления"); return; }
        if (toCityCombo.getValue() == null) { showError("Выберите город назначения"); return; }
        if (departureDatePicker.getValue() == null) { showError("Выберите дату вылета"); return; }
        if (fromCityCombo.getValue().getId().equals(toCityCombo.getValue().getId())) {
            showError("Города отправления и назначения должны отличаться"); return;
        }
        searchParams.setFromCity(fromCityCombo.getValue());
        searchParams.setStopCity(stopCityCombo.getValue());
        searchParams.setToCity(toCityCombo.getValue());
        searchParams.setDepartureDate(departureDatePicker.getValue());
        searchParams.setReturnDate(returnDatePicker.getValue());
        searchParams.setExtraBaggage(extraBaggageCheck.isSelected());

        // Сохраняем в историю
        com.example.demo8.utils.SearchHistory.save(
            fromCityCombo.getValue().getName(),
            toCityCombo.getValue().getName(),
            departureDatePicker.getValue()
        );
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo8/results-view.fxml"));
            Stage stage = (Stage) fromCityCombo.getScene().getWindow();
            Scene scene = new Scene(loader.load());
            SceneThemeSupport.install(scene);
            stage.setScene(scene);
            stage.setTitle("Результаты поиска");
            ResultsController controller = loader.getController();
            controller.setSearchParams(searchParams);
            controller.loadFlights();
        } catch (IOException e) {
            showError("Ошибка открытия результатов поиска: " + e.getMessage());
        }
    }

    @FXML
    private void handleAllRoutes() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo8/all-routes-view.fxml"));
            Stage stage = (Stage) fromCityCombo.getScene().getWindow();
            Scene scene = new Scene(loader.load(), 1200, 800);
            SceneThemeSupport.install(scene);
            stage.setTitle("Все маршруты");
            SceneTransition.apply(stage, scene);
            Platform.runLater(() -> SceneThemeSupport.install(stage.getScene()));
        } catch (IOException e) {
            showError("Ошибка открытия: " + e.getMessage());
        }
    }

    @FXML
    private void handleShortestPath() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo8/shortest-path-view.fxml"));
            Stage stage = (Stage) fromCityCombo.getScene().getWindow();
            Scene scene = new Scene(loader.load(), 1200, 800);
            SceneThemeSupport.install(scene);
            stage.setTitle("Кратчайший маршрут");
            SceneTransition.apply(stage, scene);
            Platform.runLater(() -> SceneThemeSupport.install(stage.getScene()));
        } catch (IOException e) {
            showError("Ошибка открытия: " + e.getMessage());
        }
    }

    @FXML
    private void handleAuth() {
        if (SessionManager.getInstance().isLoggedIn()) {
            Window owner = authButton != null && authButton.getScene() != null
                    ? authButton.getScene().getWindow()
                    : null;
            if (ConfirmDialog.show(owner, "Выход", "Вы уверены, что хотите выйти?")) {
                SessionManager.getInstance().logout();
                updateUI();
            }
        } else {
            showAuthDialog(true);
        }
    }

    @FXML
    private void handleProfile() {
        if (!SessionManager.getInstance().isLoggedIn()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Создайте профиль");
            alert.setHeaderText("Создайте профиль");
            alert.setContentText("Для доступа к личному кабинету необходимо зарегистрироваться.");
            alert.showAndWait();
            showAuthDialog(false);
        } else {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo8/profile-view.fxml"));
                Stage stage = (Stage) fromCityCombo.getScene().getWindow();
                Scene scene = new Scene(loader.load(), 1200, 800);
                SceneThemeSupport.install(scene);
                stage.setTitle("Личный кабинет");
                SceneTransition.apply(stage, scene);
                Platform.runLater(() -> SceneThemeSupport.install(stage.getScene()));
            } catch (IOException e) {
                showError("Ошибка открытия личного кабинета: " + e.getMessage());
            }
        }
    }

    @FXML
    private void openRules() {
        try {
            java.awt.Desktop.getDesktop().browse(new java.net.URI("https://ulyanovsk.sledcom.ru/folder/1284969/item/1883405/"));
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Правила поведения на самолете");
            alert.setHeaderText("Правила поведения на самолете");
            alert.setContentText("Ссылка: https://ulyanovsk.sledcom.ru/folder/1284969/item/1883405/");
            alert.showAndWait();
        }
    }

    private void showAuthDialog(boolean isLogin) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo8/auth-view.fxml"));
            Stage stage = new Stage();
            Scene scene = new Scene(loader.load());
            SceneThemeSupport.install(scene);
            stage.setScene(scene);
            stage.setTitle(isLogin ? "Вход" : "Регистрация");
            stage.setResizable(false);
            AuthController controller = loader.getController();
            controller.setLoginMode(isLogin);
            controller.setMainController(this);
            stage.showAndWait();
            updateUI();
        } catch (IOException e) {
            showError("Ошибка открытия окна авторизации: " + e.getMessage());
        }
    }

    public void updatePassengersButton() {
        int total = searchParams.getTotalPassengers();
        String text = total + " " + getPassengerWord(total) + ", " + searchParams.getServiceClass();
        passengersButton.setText(text);
    }

    private String getPassengerWord(int count) {
        if (count % 10 == 1 && count % 100 != 11) return "пассажир";
        if (count % 10 >= 2 && count % 10 <= 4 && (count % 100 < 10 || count % 100 >= 20)) return "пассажира";
        return "пассажиров";
    }

    public void updateUI() {
        boolean loggedIn = SessionManager.getInstance().isLoggedIn();
        ThemeManager.Palette p = ThemeManager.getInstance().getPalette();
        if (authButton != null) {
            authButton.setVisible(true);
            authButton.setText(loggedIn ? "Выйти" : "Войти");
            authButton.setStyle("-fx-background-color: transparent; -fx-text-fill: " + p.btnOutlineText + "; " +
                    "-fx-font-size: 13px; -fx-padding: 8 20; -fx-border-color: " + p.accent + "; -fx-border-width: 1.5; " +
                    "-fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand;");
        }
        if (profileButton != null) {
            profileButton.setVisible(true);
            profileButton.setStyle("-fx-background-color: " + p.btnProfileBg + "; -fx-text-fill: " + p.btnProfileText + "; " +
                    "-fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 6; -fx-cursor: hand;");
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setDestinationCity(City city) {
        for (City c : cities) {
            if (c.getId().equals(city.getId())) {
                toCityCombo.setValue(c);
                break;
            }
        }
    }

    private void openCityScreen(City city) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo8/city-view.fxml"));
            Stage cityStage = new Stage();
            Scene scene = new Scene(loader.load());
            SceneThemeSupport.install(scene);
            cityStage.setScene(scene);
            cityStage.setTitle(city.getName());
            cityStage.setResizable(true);
            CityController controller = loader.getController();
            controller.setCity(city, cityStage, this);
            cityStage.show();
        } catch (IOException e) {
            showError("Ошибка открытия: " + e.getMessage());
        }
    }

    public SearchParams getSearchParams() { return searchParams; }
}
