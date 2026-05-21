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
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class ResultsController {

    @FXML private VBox flightsContainer;
    @FXML private Label noResultsLabel;
    @FXML private Label routeLabel;
    @FXML private Label countLabel;
    @FXML private Button sortPriceBtn;
    @FXML private Button sortTimeBtn;
    @FXML private Button sortDurBtn;
    @FXML private Button filterMorningBtn;
    @FXML private Button filterDayBtn;
    @FXML private Button filterEveningBtn;
    @FXML private Button filterAllBtn;

    private SearchParams searchParams;
    private SupabaseClient supabaseClient;
    private List<RoundTrip> allRoundTrips = new ArrayList<>();
    private List<ConnectingFlight> allConnectingFlights = new ArrayList<>();
    private String currentSort = "price";
    private String currentFilter = "all";

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM", new Locale("ru"));

    public void setSearchParams(SearchParams params) { this.searchParams = params; }

    @FXML
    private void initialize() {
        Platform.runLater(() -> {
            if (flightsContainer != null) {
                ThemeStyleRemap.bindScene(flightsContainer);
                if (sortPriceBtn != null) setActiveSort(sortPriceBtn);
                if (filterAllBtn != null) setActiveFilter(filterAllBtn);
            }
        });
    }

    public void loadFlights() {
        if (searchParams == null || searchParams.getFromCity() == null || searchParams.getToCity() == null) {
            noResultsLabel.setText("Ошибка: не указаны параметры поиска");
            return;
        }
        supabaseClient = SupabaseClient.getInstance();

        if (routeLabel != null)
            routeLabel.setText(searchParams.getFromCity().getName() + "  ->  " + searchParams.getToCity().getName()
                + (searchParams.getReturnDate() != null ? "  (туда-обратно)" : ""));

        flightsContainer.getChildren().clear();
        noResultsLabel.setText("Загрузка рейсов...");
        noResultsLabel.setVisible(true);

        new Thread(() -> {
            try {
                if (searchParams.getStopCity() != null) {
                    List<ConnectingFlight> cf = supabaseClient.searchConnectingFlights(
                            searchParams.getFromCity().getId(), searchParams.getStopCity().getId(),
                            searchParams.getToCity().getId(), searchParams.getDepartureDate());
                    allConnectingFlights = cf;
                    Platform.runLater(() -> {
                        noResultsLabel.setVisible(false);
                        if (cf.isEmpty()) showEmpty();
                        else { updateCount(cf.size()); displayConnectingFlights(cf); }
                    });
                    return;
                }

                List<Flight> outbound = supabaseClient.searchFlights(
                        searchParams.getFromCity().getId(), searchParams.getToCity().getId(),
                        searchParams.getDepartureDate());
                List<Flight> ret = searchParams.getReturnDate() != null
                        ? supabaseClient.searchReturnFlights(searchParams.getFromCity().getId(),
                            searchParams.getToCity().getId(), searchParams.getReturnDate())
                        : Collections.emptyList();

                List<RoundTrip> trips = new ArrayList<>();
                if (!ret.isEmpty()) {
                    for (Flight o : outbound) for (Flight r : ret) trips.add(new RoundTrip(o, r));
                } else {
                    for (Flight o : outbound) trips.add(new RoundTrip(o, null));
                }
                allRoundTrips = trips;

                Platform.runLater(() -> {
                    noResultsLabel.setVisible(false);
                    if (trips.isEmpty()) showEmpty();
                    else { updateCount(trips.size()); applyAndDisplay(); }
                });
            } catch (IOException e) {
                Platform.runLater(() -> { noResultsLabel.setText("Ошибка: " + e.getMessage()); noResultsLabel.setVisible(true); });
            }
        }).start();
    }

    // ===== Сортировка =====

    @FXML private void handleSortPrice()    { currentSort = "price";    setActiveSort(sortPriceBtn); applyAndDisplay(); }
    @FXML private void handleSortTime()     { currentSort = "time";     setActiveSort(sortTimeBtn);  applyAndDisplay(); }
    @FXML private void handleSortDuration() { currentSort = "duration"; setActiveSort(sortDurBtn);   applyAndDisplay(); }

    // ===== Фильтры по времени =====

    @FXML private void handleFilterMorning() { currentFilter = "morning"; setActiveFilter(filterMorningBtn); applyAndDisplay(); }
    @FXML private void handleFilterDay()     { currentFilter = "day";     setActiveFilter(filterDayBtn);     applyAndDisplay(); }
    @FXML private void handleFilterEvening() { currentFilter = "evening"; setActiveFilter(filterEveningBtn); applyAndDisplay(); }
    @FXML private void handleFilterAll()     { currentFilter = "all";     setActiveFilter(filterAllBtn);     applyAndDisplay(); }

    private void applyAndDisplay() {
        if (!allConnectingFlights.isEmpty()) {
            List<ConnectingFlight> filtered = allConnectingFlights.stream()
                    .filter(cf -> {
                        if (cf.getFirstLeg() == null) return false;
                        int hour = cf.getFirstLeg().getDepartureTime().getHour();
                        return switch (currentFilter) {
                            case "morning" -> hour >= 6  && hour < 12;
                            case "day"     -> hour >= 12 && hour < 18;
                            case "evening" -> hour >= 18;
                            default -> true;
                        };
                    })
                    .sorted(switch (currentSort) {
                        case "time"  -> Comparator.comparing(cf -> cf.getFirstLeg().getDepartureTime());
                        case "duration" -> Comparator.comparingLong(cf -> cf.getFirstLeg().getDurationInHours() + cf.getSecondLeg().getDurationInHours());
                        default      -> Comparator.comparing(cf -> cf.getTotalPrice()
                                .multiply(new BigDecimal(getClassMultiplier()))
                                .multiply(new BigDecimal(searchParams.getTotalPassengers())));
                    })
                    .collect(Collectors.toList());
            updateCount(filtered.size());
            if (filtered.isEmpty()) showEmpty();
            else displayConnectingFlights(filtered);
            return;
        }
        List<RoundTrip> filtered = allRoundTrips.stream()
                .filter(rt -> {
                    if (rt.getOutboundFlight() == null) return false;
                    int hour = rt.getOutboundFlight().getDepartureTime().getHour();
                    return switch (currentFilter) {
                        case "morning" -> hour >= 6  && hour < 12;
                        case "day"     -> hour >= 12 && hour < 18;
                        case "evening" -> hour >= 18;
                        default -> true;
                    };
                })
                .sorted(switch (currentSort) {
                    case "time"     -> Comparator.comparing(rt -> rt.getOutboundFlight().getDepartureTime());
                    case "duration" -> Comparator.comparingLong(rt -> rt.getOutboundFlight().getDurationInHours());
                    default         -> Comparator.comparing(this::calcPrice);
                })
                .collect(Collectors.toList());

        updateCount(filtered.size());
        displayRoundTrips(filtered);
    }

    // ===== Отображение =====

    private void displayRoundTrips(List<RoundTrip> trips) {
        flightsContainer.getChildren().clear();
        if (trips.isEmpty()) { showEmpty(); return; }

        BigDecimal minPrice = trips.stream().map(this::calcPrice).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);

        for (RoundTrip rt : trips) {
            flightsContainer.getChildren().add(buildCard(rt, calcPrice(rt).equals(minPrice)));
        }
        ThemeStyleRemap.applyTree(flightsContainer);
    }

    private VBox buildCard(RoundTrip rt, boolean isBest) {
        VBox card = new VBox(0);
        card.setStyle(isBest ? ThemeStyles.cardBest() : ThemeStyles.cardDefault());

        // Бейдж "Лучшая цена"
        if (isBest) {
            Label badge = new Label("  Лучшая цена  ");
            badge.setStyle(ThemeStyles.badgeBest());
            HBox badgeRow = new HBox(badge);
            card.getChildren().add(badgeRow);
        }

        // Рейс туда
        card.getChildren().add(buildFlightRow(rt.getOutboundFlight(), false));

        // Рейс обратно
        if (rt.getReturnFlight() != null) {
            Separator sep = new Separator();
            sep.setStyle("-fx-padding: 0 20;");
            card.getChildren().add(sep);
            card.getChildren().add(buildFlightRow(rt.getReturnFlight(), true));
        }

        // Футер
        card.getChildren().add(buildFooter(rt));
        return card;
    }

    private HBox buildFlightRow(Flight f, boolean isReturn) {
        HBox row = new HBox(0);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-padding: 16 20;");

        // Авиакомпания
        VBox airlineBox = new VBox(4);
        airlineBox.setMinWidth(140);
        Label direction = new Label(isReturn ? "ОБРАТНО" : "ТУДА");
        direction.setStyle(ThemeStyles.labelAccent("10px") + (isReturn ? " -fx-opacity: 0.75;" : ""));
        Label airline = new Label(f.getAirlineName() != null ? f.getAirlineName() : "");
        airline.setStyle(ThemeStyles.labelPrimaryBold("13px"));
        Label flightNum = new Label(f.getFlightNumber());
        flightNum.setStyle(ThemeStyles.labelMuted("11px"));
        airlineBox.getChildren().addAll(direction, airline, flightNum);

        // Время вылета
        VBox depBox = new VBox(2);
        depBox.setAlignment(Pos.CENTER);
        depBox.setMinWidth(80);
        Label depTime = new Label(f.getDepartureTime().format(TIME_FMT));
        depTime.setStyle(ThemeStyles.labelPrimaryBold("22px"));
        Label depDate = new Label(f.getDepartureTime().format(DATE_FMT));
        depDate.setStyle(ThemeStyles.labelMuted("11px"));
        Label depCity = new Label(f.getFromCityName());
        depCity.setStyle(ThemeStyles.labelSecondary("12px"));
        depBox.getChildren().addAll(depTime, depDate, depCity);

        // Длительность + линия
        VBox durBox = new VBox(4);
        durBox.setAlignment(Pos.CENTER);
        HBox.setHgrow(durBox, Priority.ALWAYS);
        Label durLabel = new Label(f.getDurationInHours() + " ч в пути");
        durLabel.setStyle(ThemeStyles.labelMuted("11px"));
        durLabel.setMaxWidth(Double.MAX_VALUE);
        durLabel.setAlignment(Pos.CENTER);
        Region line = new Region();
        line.setStyle(ThemeStyles.durationLine());
        line.setMaxWidth(Double.MAX_VALUE);
        Label directLabel = new Label("Прямой");
        directLabel.setStyle("-fx-text-fill: rgba(100,220,100,0.8); -fx-font-size: 10px;");
        directLabel.setMaxWidth(Double.MAX_VALUE);
        directLabel.setAlignment(Pos.CENTER);
        durBox.getChildren().addAll(durLabel, line, directLabel);
        durBox.setStyle("-fx-padding: 0 16;");

        // Время прилёта
        VBox arrBox = new VBox(2);
        arrBox.setAlignment(Pos.CENTER);
        arrBox.setMinWidth(80);
        Label arrTime = new Label(f.getArrivalTime().format(TIME_FMT));
        arrTime.setStyle(ThemeStyles.labelPrimaryBold("22px"));
        Label arrDate = new Label(f.getArrivalTime().format(DATE_FMT));
        arrDate.setStyle(ThemeStyles.labelMuted("11px"));
        Label arrCity = new Label(f.getToCityName());
        arrCity.setStyle(ThemeStyles.labelSecondary("12px"));
        arrBox.getChildren().addAll(arrTime, arrDate, arrCity);

        row.getChildren().addAll(airlineBox, depBox, durBox, arrBox);
        return row;
    }

    private HBox buildFooter(RoundTrip rt) {
        HBox footer = new HBox(16);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setStyle("-fx-padding: 12 20; -fx-background-color: rgba(0,0,0,0.2); " +
                "-fx-background-radius: 0 0 12 12; -fx-border-color: rgba(79,195,247,0.08); -fx-border-width: 1 0 0 0;");

        BigDecimal price = calcPrice(rt);

        // Детали цены
        VBox priceBox = new VBox(2);
        Label priceLabel = new Label(price + " ₽");
        priceLabel.setStyle(ThemeStyles.labelPriceAccent("22px"));
        String perPax = searchParams.getTotalPassengers() > 1
                ? "за " + searchParams.getTotalPassengers() + " пасс."
                : "за 1 пасс.";
        Label perLabel = new Label(perPax + " · " + searchParams.getServiceClass());
        perLabel.setStyle(ThemeStyles.labelMuted("11px"));
        priceBox.getChildren().addAll(priceLabel, perLabel);

        if (searchParams.isExtraBaggage()) {
            Label bagLabel = new Label("+ доп. багаж");
            bagLabel.setStyle(ThemeStyles.labelMuted("10px"));
            priceBox.getChildren().add(bagLabel);
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Кнопки
        Button detailsBtn = new Button("Подробнее");
        detailsBtn.setStyle(ThemeStyles.detailsButton());
        detailsBtn.setOnAction(e -> showDetails(rt));

        Button selectBtn = new Button("Выбрать");
        selectBtn.setStyle(ThemeStyles.buyButton());
        selectBtn.setOnAction(e -> handleSelect(rt));

        footer.getChildren().addAll(priceBox, spacer, detailsBtn, selectBtn);
        return footer;
    }

    private void showDetails(RoundTrip rt) {
        Stage dialog = new Stage();
        dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        dialog.initStyle(javafx.stage.StageStyle.UNDECORATED);
        dialog.setTitle("Детали рейса");

        VBox root = new VBox(0);
        root.setStyle(ThemeStyles.dialogRoot());
        root.setMinWidth(480);

        // Шапка
        HBox header = new HBox();
        header.setStyle(ThemeStyles.dialogHeader());
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("✈  " + rt.getOutboundFlight().getFromCityName() + "  →  " + rt.getOutboundFlight().getToCityName());
        title.setStyle(ThemeStyles.labelPrimaryBold("16px"));
        Region hSpacer = new Region(); HBox.setHgrow(hSpacer, Priority.ALWAYS);
        Button closeBtn = new Button("✕");
        closeBtn.setStyle(ThemeStyles.labelMuted("16px") + " -fx-background-color: transparent; -fx-padding: 0 4; -fx-cursor: hand; -fx-border-color: transparent;");
        closeBtn.setOnAction(e -> dialog.close());
        header.getChildren().addAll(title, hSpacer, closeBtn);

        VBox content = new VBox(12);
        content.setStyle("-fx-padding: 20 24;");

        content.getChildren().add(buildFlightDetailBlock(rt.getOutboundFlight(), "ТУДА", "#4fc3f7"));

        if (rt.getReturnFlight() != null) {
            Separator sep = new Separator();
            sep.setStyle("-fx-padding: 4 0;");
            content.getChildren().add(sep);
            content.getChildren().add(buildFlightDetailBlock(rt.getReturnFlight(), "ОБРАТНО", "rgba(79,195,247,0.6)"));
        }

        // Итого
        HBox totalRow = new HBox();
        totalRow.setStyle("-fx-background-color: rgba(79,195,247,0.07); -fx-padding: 12 24; -fx-background-radius: 0 0 14 14; -fx-border-color: rgba(79,195,247,0.15); -fx-border-width: 1 0 0 0;");
        totalRow.setAlignment(Pos.CENTER_LEFT);
        Label totalLbl = new Label("ИТОГО:");
        totalLbl.setStyle(ThemeStyles.labelSecondary("13px"));
        Region tSpacer = new Region(); HBox.setHgrow(tSpacer, Priority.ALWAYS);
        Label totalVal = new Label(calcPrice(rt) + " ₽");
        totalVal.setStyle(ThemeStyles.labelPriceAccent("22px"));
        totalRow.getChildren().addAll(totalLbl, tSpacer, totalVal);

        root.getChildren().addAll(header, content, totalRow);

        Scene scene = new Scene(root);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        SceneThemeSupport.install(scene);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private VBox buildFlightDetailBlock(Flight f, String direction, String dirColor) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        VBox block = new VBox(10);

        Label dirLbl = new Label(direction);
        dirLbl.setStyle("-fx-text-fill: " + dirColor + "; -fx-font-size: 11px; -fx-font-weight: bold;");

        // Маршрут с временем
        HBox routeRow = new HBox(12);
        routeRow.setAlignment(Pos.CENTER_LEFT);

        VBox depCol = new VBox(2);
        Label depTime = new Label(f.getDepartureTime().format(TIME_FMT));
        depTime.setStyle("-fx-text-fill: white; -fx-font-size: 26px; -fx-font-weight: bold;");
        Label depCity = new Label(f.getFromCityName());
        depCity.setStyle("-fx-text-fill: rgba(180,200,255,0.7); -fx-font-size: 12px;");
        Label depDate = new Label(f.getDepartureTime().format(DateTimeFormatter.ofPattern("dd MMM yyyy", new Locale("ru"))));
        depDate.setStyle("-fx-text-fill: rgba(180,200,255,0.45); -fx-font-size: 11px;");
        depCol.getChildren().addAll(depTime, depCity, depDate);

        VBox midCol = new VBox(4);
        midCol.setAlignment(Pos.CENTER);
        HBox.setHgrow(midCol, Priority.ALWAYS);
        Label durLbl = new Label(f.getDurationInHours() + " ч в пути");
        durLbl.setStyle("-fx-text-fill: rgba(180,200,255,0.45); -fx-font-size: 11px;");
        durLbl.setMaxWidth(Double.MAX_VALUE);
        durLbl.setAlignment(Pos.CENTER);
        Region arrow = new Region();
        arrow.setStyle("-fx-background-color: rgba(79,195,247,0.3); -fx-pref-height: 1;");
        arrow.setMaxWidth(Double.MAX_VALUE);
        Label directLbl = new Label("✈ Прямой");
        directLbl.setStyle("-fx-text-fill: rgba(100,220,100,0.8); -fx-font-size: 10px;");
        directLbl.setMaxWidth(Double.MAX_VALUE);
        directLbl.setAlignment(Pos.CENTER);
        midCol.getChildren().addAll(durLbl, arrow, directLbl);
        midCol.setStyle("-fx-padding: 0 16;");

        VBox arrCol = new VBox(2);
        arrCol.setAlignment(Pos.CENTER_RIGHT);
        Label arrTime = new Label(f.getArrivalTime().format(TIME_FMT));
        arrTime.setStyle("-fx-text-fill: white; -fx-font-size: 26px; -fx-font-weight: bold;");
        Label arrCity = new Label(f.getToCityName());
        arrCity.setStyle("-fx-text-fill: rgba(180,200,255,0.7); -fx-font-size: 12px;");
        Label arrDate = new Label(f.getArrivalTime().format(DateTimeFormatter.ofPattern("dd MMM yyyy", new Locale("ru"))));
        arrDate.setStyle("-fx-text-fill: rgba(180,200,255,0.45); -fx-font-size: 11px;");
        arrCol.getChildren().addAll(arrTime, arrCity, arrDate);

        routeRow.getChildren().addAll(depCol, midCol, arrCol);

        // Детали рейса
        HBox infoRow = new HBox(24);
        infoRow.setStyle("-fx-background-color: rgba(255,255,255,0.04); -fx-background-radius: 8; -fx-padding: 10 14;");
        infoRow.getChildren().addAll(
            buildInfoCell("Авиакомпания", f.getAirlineName() != null ? f.getAirlineName() : "—"),
            buildInfoCell("Номер рейса", f.getFlightNumber()),
            buildInfoCell("Вылет", f.getDepartureTime().format(fmt)),
            buildInfoCell("Прилёт", f.getArrivalTime().format(fmt)),
            buildInfoCell("Цена за место", f.getBasePrice() + " ₽")
        );

        block.getChildren().addAll(dirLbl, routeRow, infoRow);
        return block;
    }

    private VBox buildInfoCell(String label, String value) {
        VBox cell = new VBox(3);
        Label lbl = new Label(label.toUpperCase());
        lbl.setStyle("-fx-text-fill: rgba(180,200,255,0.45); -fx-font-size: 10px; -fx-font-weight: bold;");
        Label val = new Label(value);
        val.setStyle(ThemeStyles.labelPrimary("12px"));
        cell.getChildren().addAll(lbl, val);
        return cell;
    }

    private void displayConnectingFlights(List<ConnectingFlight> flights) {
        flightsContainer.getChildren().clear();
        for (ConnectingFlight cf : flights) {
            VBox card = new VBox(0);
            card.setStyle(ThemeStyles.cardDefault());
            card.getChildren().add(buildFlightRow(cf.getFirstLeg(), false));
            Label stopLabel = new Label("  ⟳  Пересадка: " + cf.getFirstLeg().getToCityName());
            stopLabel.setStyle("-fx-background-color: " + (ThemeStyles.isLight() ? "rgba(230,126,34,0.1)" : "rgba(255,200,0,0.08)") + "; -fx-text-fill: " + ThemeStyles.priceColor() + "; " +
                    "-fx-font-size: 11px; -fx-padding: 4 20;");
            card.getChildren().add(stopLabel);
            card.getChildren().add(buildFlightRow(cf.getSecondLeg(), false));
            card.getChildren().add(buildConnectingFooter(cf));
            flightsContainer.getChildren().add(card);
        }
        ThemeStyleRemap.applyTree(flightsContainer);
    }

    private HBox buildConnectingFooter(ConnectingFlight cf) {
        BigDecimal total = cf.getTotalPrice()
                .multiply(new BigDecimal(getClassMultiplier()))
                .multiply(new BigDecimal(searchParams.getTotalPassengers()));
        if (searchParams.isExtraBaggage()) total = total.add(new BigDecimal("2000"));
        final BigDecimal finalTotal = total;

        HBox footer = new HBox(16);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setStyle("-fx-padding: 12 20; -fx-background-color: rgba(0,0,0,0.2); " +
                "-fx-background-radius: 0 0 12 12; -fx-border-color: rgba(79,195,247,0.08); -fx-border-width: 1 0 0 0;");

        VBox priceBox = new VBox(2);
        Label priceLabel = new Label(finalTotal + " ₽");
        priceLabel.setStyle(ThemeStyles.labelPriceAccent("22px"));
        String perPax = searchParams.getTotalPassengers() > 1
                ? "за " + searchParams.getTotalPassengers() + " пасс."
                : "за 1 пасс.";
        Label perLabel = new Label(perPax + " · " + searchParams.getServiceClass() + " · 2 рейса");
        perLabel.setStyle(ThemeStyles.labelMuted("11px"));
        priceBox.getChildren().addAll(priceLabel, perLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button detailsBtn = new Button("Подробнее");
        detailsBtn.setStyle(ThemeStyles.detailsButton());
        detailsBtn.setOnAction(e -> showConnectingDetails(cf, finalTotal));

        Button selectBtn = new Button("Выбрать");
        selectBtn.setStyle(ThemeStyles.buyButton());
        selectBtn.setOnAction(e -> handleConnectingSelect(cf));

        footer.getChildren().addAll(priceBox, spacer, detailsBtn, selectBtn);
        return footer;
    }

    private void showConnectingDetails(ConnectingFlight cf, BigDecimal total) {
        Stage dialog = new Stage();
        dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        dialog.initStyle(javafx.stage.StageStyle.UNDECORATED);

        VBox root = new VBox(0);
        root.setStyle(ThemeStyles.dialogRoot());
        root.setMinWidth(480);

        // Шапка
        HBox header = new HBox();
        header.setStyle(ThemeStyles.dialogHeader());
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("✈  " + cf.getFirstLeg().getFromCityName() + "  →  "
                + cf.getFirstLeg().getToCityName() + "  →  " + cf.getSecondLeg().getToCityName());
        title.setStyle("-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold;");
        Region hSpacer = new Region(); HBox.setHgrow(hSpacer, Priority.ALWAYS);
        Button closeBtn = new Button("✕");
        closeBtn.setStyle(ThemeStyles.labelMuted("16px") + " -fx-background-color: transparent; -fx-padding: 0 4; -fx-cursor: hand; -fx-border-color: transparent;");
        closeBtn.setOnAction(e -> dialog.close());
        header.getChildren().addAll(title, hSpacer, closeBtn);

        VBox content = new VBox(12);
        content.setStyle("-fx-padding: 20 24;");

        content.getChildren().add(buildFlightDetailBlock(cf.getFirstLeg(), "РЕЙС 1", "#4fc3f7"));

        // Плашка пересадки
        HBox stopRow = new HBox(8);
        stopRow.setAlignment(Pos.CENTER_LEFT);
        stopRow.setStyle("-fx-background-color: rgba(255,200,0,0.08); -fx-background-radius: 8; -fx-padding: 8 14; -fx-border-color: rgba(255,224,102,0.2); -fx-border-width: 1; -fx-border-radius: 8;");
        Label stopIcon = new Label("⟳");
        stopIcon.setStyle(ThemeStyles.labelPrice("16px"));
        Label stopLbl = new Label("ПЕРЕСАДКА  •  " + cf.getFirstLeg().getToCityName());
        stopLbl.setStyle(ThemeStyles.labelPrice("12px"));
        stopRow.getChildren().addAll(stopIcon, stopLbl);
        content.getChildren().add(stopRow);

        content.getChildren().add(buildFlightDetailBlock(cf.getSecondLeg(), "РЕЙС 2", "#4fc3f7"));

        // Итого
        HBox totalRow = new HBox();
        totalRow.setStyle("-fx-background-color: rgba(79,195,247,0.07); -fx-padding: 12 24; -fx-background-radius: 0 0 14 14; -fx-border-color: rgba(79,195,247,0.15); -fx-border-width: 1 0 0 0;");
        totalRow.setAlignment(Pos.CENTER_LEFT);
        Label totalLbl = new Label("ИТОГО:");
        totalLbl.setStyle(ThemeStyles.labelSecondary("13px"));
        Region tSpacer = new Region(); HBox.setHgrow(tSpacer, Priority.ALWAYS);
        Label totalVal = new Label(total + " ₽");
        totalVal.setStyle(ThemeStyles.labelPriceAccent("22px"));
        totalRow.getChildren().addAll(totalLbl, tSpacer, totalVal);

        root.getChildren().addAll(header, content, totalRow);

        Scene scene = new Scene(root);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        SceneThemeSupport.install(scene);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void handleConnectingSelect(ConnectingFlight cf) {
        if (!SessionManager.getInstance().isLoggedIn()) {
            showAuthDialogForConnecting(cf);
        } else {
            openBookingForConnecting(cf);
        }
    }

    private void showAuthDialogForConnecting(ConnectingFlight cf) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo8/auth-view.fxml"));
            Stage stage = new Stage();
            Scene scene = new Scene(loader.load());
            SceneThemeSupport.install(scene);
            stage.setScene(scene); stage.setTitle("Вход"); stage.setResizable(false);
            AuthController ctrl = loader.getController();
            ctrl.setLoginMode(true);
            stage.showAndWait();
            if (SessionManager.getInstance().isLoggedIn()) openBookingForConnecting(cf);
        } catch (IOException e) { showError("Ошибка: " + e.getMessage()); }
    }

    private void openBookingForConnecting(ConnectingFlight cf) {
        // Бронируем первое плечо маршрута
        RoundTrip rt = new RoundTrip(cf.getFirstLeg(), null);
        openBookingWindow(rt);
    }

    private void handleSelect(RoundTrip rt) {
        if (!SessionManager.getInstance().isLoggedIn()) showAuthDialog(rt);
        else openBookingWindow(rt);
    }

    private void showAuthDialog(RoundTrip rt) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo8/auth-view.fxml"));
            Stage stage = new Stage();
            Scene scene = new Scene(loader.load());
            SceneThemeSupport.install(scene);
            stage.setScene(scene); stage.setTitle("Вход"); stage.setResizable(false);
            AuthController ctrl = loader.getController();
            ctrl.setLoginMode(true); ctrl.setSelectedRoundTrip(rt); ctrl.setResultsController(this);
            stage.showAndWait();
        } catch (IOException e) { showError("Ошибка: " + e.getMessage()); }
    }

    public void openBookingWindow(RoundTrip rt) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo8/booking-view.fxml"));
            Stage stage = (Stage) flightsContainer.getScene().getWindow();
            Scene scene = new Scene(loader.load());
            SceneThemeSupport.install(scene);
            stage.setTitle("Бронирование");
            SceneTransition.apply(stage, scene);
            BookingController ctrl = loader.getController();
            ctrl.setRoundTrip(rt); ctrl.setSearchParams(searchParams);
        } catch (IOException e) { showError("Ошибка: " + e.getMessage()); }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo8/main-view.fxml"));
            Stage stage = (Stage) flightsContainer.getScene().getWindow();
            Scene scene = new Scene(loader.load());
            SceneThemeSupport.install(scene);
            stage.setTitle("Поиск авиабилетов");
            SceneTransition.apply(stage, scene);
        } catch (IOException e) { showError("Ошибка: " + e.getMessage()); }
    }

    // ===== Helpers =====

    private BigDecimal calcPrice(RoundTrip rt) {
        BigDecimal total = BigDecimal.ZERO;
        if (rt.getOutboundFlight() != null)
            total = total.add(rt.getOutboundFlight().getBasePrice()
                    .multiply(new BigDecimal(getClassMultiplier()))
                    .multiply(new BigDecimal(searchParams.getTotalPassengers())));
        if (rt.getReturnFlight() != null)
            total = total.add(rt.getReturnFlight().getBasePrice()
                    .multiply(new BigDecimal(getClassMultiplier()))
                    .multiply(new BigDecimal(searchParams.getTotalPassengers())));
        if (searchParams.isExtraBaggage()) total = total.add(new BigDecimal("2000"));
        return total;
    }

    private double getClassMultiplier() {
        return switch (searchParams.getServiceClass()) {
            case "Премиум" -> 1.5;
            case "Бизнес"  -> 2.0;
            default        -> 1.0;
        };
    }

    private void updateCount(int count) {
        if (countLabel != null)
            countLabel.setText("Найдено: " + count + " " + pluralVariants(count, "вариант", "варианта", "вариантов"));
    }

    private String pluralVariants(int n, String one, String few, String many) {
        int mod = n % 100;
        if (mod >= 11 && mod <= 19) return many;
        return switch (n % 10) { case 1 -> one; case 2, 3, 4 -> few; default -> many; };
    }

    private void showEmpty() {
        noResultsLabel.setText("Рейсы не найдены.\nПопробуйте изменить дату или маршрут.");
        noResultsLabel.setStyle(ThemeStyles.labelSecondary("16px") + " -fx-text-alignment: center;");
        noResultsLabel.setVisible(true);
    }

    private void setActiveSort(Button btn) {
        sortPriceBtn.setStyle(ThemeStyles.chipSortInactive());
        sortTimeBtn.setStyle(ThemeStyles.chipSortInactive());
        sortDurBtn.setStyle(ThemeStyles.chipSortInactive());
        btn.setStyle(ThemeStyles.chipSortActive());
    }

    private void setActiveFilter(Button btn) {
        filterMorningBtn.setStyle(ThemeStyles.chipFilterInactive());
        filterDayBtn.setStyle(ThemeStyles.chipFilterInactive());
        filterEveningBtn.setStyle(ThemeStyles.chipFilterInactive());
        filterAllBtn.setStyle(ThemeStyles.chipFilterInactive());
        btn.setStyle(ThemeStyles.chipFilterActive());
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Ошибка"); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}
