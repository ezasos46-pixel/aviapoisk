package com.example.demo8.controllers;

import com.example.demo8.models.City;
import com.example.demo8.utils.ThemeStyleRemap;
import com.example.demo8.utils.ThemeStyles;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class CityController {

    @FXML private Label titleLabel;
    @FXML private Label codeLabel;
    @FXML private Label heroEmoji;
    @FXML private Label heroName;
    @FXML private Label heroCountry;
    @FXML private Label heroDesc;
    @FXML private HBox factsBox;
    @FXML private VBox attractionsBox;
    @FXML private VBox tipsBox;

    private City city;
    private Stage stage;
    private MainController mainController;

    // ── статические данные по коду аэропорта ──────────────────────────────────

    private record CityInfo(String emoji, String desc,
                            List<String[]> facts,        // {icon, label, value}
                            List<String[]> attractions,  // {emoji, name, desc}
                            List<String> tips) {}

    private static final Map<String, CityInfo> DATA = Map.ofEntries(
        Map.entry("MOW", new CityInfo("🏛",
            "Москва — столица России, крупнейший мегаполис страны с богатейшей историей, мировым уровнем культуры и бесконечными возможностями.",
            List.of(new String[]{"👥","Население","12,5 млн"}, new String[]{"🌡","Климат","Умеренный"}, new String[]{"💰","Валюта","Рубль ₽"}, new String[]{"🕐","Часовой пояс","UTC+3"}),
            List.of(new String[]{"🏰","Московский Кремль","Сердце России, резиденция президента, объект ЮНЕСКО"},
                    new String[]{"⛪","Храм Василия Блаженного","Символ Москвы на Красной площади, XVI век"},
                    new String[]{"🎨","Третьяковская галерея","Крупнейшее собрание русского искусства в мире"},
                    new String[]{"🌳","Парк Горького","Главный городской парк на берегу Москвы-реки"}),
            List.of("🚇 Метро — самый быстрый транспорт, работает до 01:00",
                    "🎫 Карта «Тройка» экономит на проезде",
                    "🏨 Бронируйте жильё заранее — город всегда загружен",
                    "🌂 Берите зонт: погода переменчива"))),

        Map.entry("LED", new CityInfo("🌉",
            "Санкт-Петербург — культурная столица России, город белых ночей, дворцов и каналов, основанный Петром I в 1703 году.",
            List.of(new String[]{"👥","Население","5,6 млн"}, new String[]{"🌡","Климат","Морской"}, new String[]{"🌅","Белые ночи","Июнь–июль"}, new String[]{"🕐","Часовой пояс","UTC+3"}),
            List.of(new String[]{"🏛","Эрмитаж","Один из крупнейших музеев мира, более 3 млн экспонатов"},
                    new String[]{"⛪","Петропавловская крепость","Основана в 1703 г., усыпальница Романовых"},
                    new String[]{"🌊","Петергоф","Дворцово-парковый ансамбль с фонтанами, «русский Версаль»"},
                    new String[]{"🎭","Мариинский театр","Один из лучших оперных театров мира"}),
            List.of("🌉 Разводные мосты — с 01:00 до 05:00, планируйте маршрут",
                    "🎫 Единый билет в Эрмитаж — берите онлайн",
                    "🚌 Маршрутки быстрее автобусов в час пик",
                    "☀️ Белые ночи — лучшее время для прогулок"))),

        Map.entry("OVB", new CityInfo("🌲",
            "Новосибирск — третий по величине город России, научный центр страны, ворота в Сибирь.",
            List.of(new String[]{"👥","Население","1,6 млн"}, new String[]{"🌡","Климат","Резко-континентальный"}, new String[]{"🔬","Академгородок","Центр науки"}, new String[]{"🕐","Часовой пояс","UTC+7"}),
            List.of(new String[]{"🎭","Новосибирский театр оперы","Крупнейший оперный театр России"},
                    new String[]{"🔬","Академгородок","Научный центр СО РАН, уникальная атмосфера"},
                    new String[]{"🌊","Обское море","Крупнейшее водохранилище Сибири"},
                    new String[]{"🏛","Краеведческий музей","История и природа Сибири"}),
            List.of("🧥 Зимой температура до -40°C — одевайтесь тепло",
                    "🚇 Метро — 2 линии, охватывает центр",
                    "🌲 Академгородок стоит отдельной поездки",
                    "🎭 Опера мирового уровня по доступным ценам"))),

        Map.entry("SVX", new CityInfo("⛰",
            "Екатеринбург — столица Урала, четвёртый город России, место расстрела царской семьи и родина Ельцина.",
            List.of(new String[]{"👥","Население","1,5 млн"}, new String[]{"🌡","Климат","Континентальный"}, new String[]{"🗺","Граница","Европа/Азия"}, new String[]{"🕐","Часовой пояс","UTC+5"}),
            List.of(new String[]{"⛪","Храм на Крови","Воздвигнут на месте расстрела Романовых"},
                    new String[]{"🗺","Граница Европа–Азия","Символический монумент в 17 км от города"},
                    new String[]{"🎨","Ельцин Центр","Современный музей истории России 90-х"},
                    new String[]{"🏛","Исторический сквер","Сердце города с музеями и фонтанами"}),
            List.of("🗺 Посетите границу Европа–Азия — уникальное фото",
                    "🎨 Ельцин Центр — must-see даже для скептиков",
                    "🚇 Метро одна линия, центр удобнее пешком",
                    "🧥 Весна и осень холодные, берите куртку"))),

        Map.entry("AER", new CityInfo("🌴",
            "Сочи — главный курорт России, субтропический климат, горнолыжные трассы и тёплое Чёрное море.",
            List.of(new String[]{"👥","Население","0,5 млн"}, new String[]{"🌡","Климат","Субтропический"}, new String[]{"🏖","Пляжный сезон","Май–октябрь"}, new String[]{"🕐","Часовой пояс","UTC+3"}),
            List.of(new String[]{"🏔","Роза Хутор","Горнолыжный курорт мирового уровня"},
                    new String[]{"🌿","Дендрарий","Уникальный парк с растениями со всего мира"},
                    new String[]{"🏟","Олимпийский парк","Наследие Олимпиады 2014 года"},
                    new String[]{"🌊","Набережная","5 км вдоль Чёрного моря"}),
            List.of("🏖 Пляжи галечные — берите коврик и тапочки",
                    "🚗 Пробки летом огромные, используйте электрички",
                    "🎿 Зимой Роза Хутор — отличные трассы",
                    "🌡 Купальный сезон с мая по октябрь"))),

        Map.entry("KZN", new CityInfo("🕌",
            "Казань — столица Татарстана, город двух культур и религий, один из красивейших городов Поволжья.",
            List.of(new String[]{"👥","Население","1,3 млн"}, new String[]{"🌡","Климат","Умеренный"}, new String[]{"🕌","Религии","Ислам и православие"}, new String[]{"🕐","Часовой пояс","UTC+3"}),
            List.of(new String[]{"🏰","Казанский Кремль","Объект ЮНЕСКО, мечеть Кул-Шариф и башня Сююмбике"},
                    new String[]{"🕌","Мечеть Кул-Шариф","Главная мечеть России, символ Казани"},
                    new String[]{"🌊","Казанка","Набережная реки с видом на Кремль"},
                    new String[]{"🎭","Театр им. Камала","Главный татарский театр страны"}),
            List.of("🕌 Кремль — начните знакомство с города отсюда",
                    "🍽 Попробуйте чак-чак — татарский десерт",
                    "🚇 Метро одна линия, центр компактный",
                    "🌍 Город двух культур — уважайте традиции"))),

        Map.entry("KJA", new CityInfo("🌲",
            "Красноярск — крупнейший город Восточной Сибири, стоит на Енисее, окружён заповедными «Столбами».",
            List.of(new String[]{"👥","Население","1,1 млн"}, new String[]{"🌡","Климат","Резко-континентальный"}, new String[]{"🌲","Природа","Заповедник Столбы"}, new String[]{"🕐","Часовой пояс","UTC+7"}),
            List.of(new String[]{"🪨","Заповедник Столбы","Уникальные скальные останцы, символ города"},
                    new String[]{"🌊","Набережная Енисея","Красивейшая набережная Сибири"},
                    new String[]{"🎨","Красноярский музейный центр","Один из лучших музеев Сибири"},
                    new String[]{"🌉","Коммунальный мост","Один из самых длинных мостов России"}),
            List.of("🪨 Столбы — обязательный поход, берите удобную обувь",
                    "🧥 Зима суровая, лето жаркое — одевайтесь по сезону",
                    "🚌 Общественный транспорт развит хорошо",
                    "🌊 Прогулка по набережной Енисея — бесплатно и красиво"))),

        Map.entry("KHV", new CityInfo("🦅",
            "Хабаровск — крупнейший город Дальнего Востока, стоит на Амуре, ворота в Азию.",
            List.of(new String[]{"👥","Население","0,6 млн"}, new String[]{"🌡","Климат","Муссонный"}, new String[]{"🌏","Граница","Китай рядом"}, new String[]{"🕐","Часовой пояс","UTC+10"}),
            List.of(new String[]{"🌊","Набережная Амура","Одна из красивейших набережных России"},
                    new String[]{"🏛","Краеведческий музей","Богатейшая коллекция по истории Дальнего Востока"},
                    new String[]{"🎨","Дальневосточный художественный музей","Уникальные коллекции искусства"},
                    new String[]{"🌳","Городской парк","Отдых с видом на Амур"}),
            List.of("🌏 Китай в часе езды — можно съездить на день",
                    "🦟 Летом много комаров — берите репеллент",
                    "🌊 Набережная — главное место прогулок",
                    "🍣 Морепродукты здесь свежие и дешёвые"))),

        Map.entry("VVO", new CityInfo("🌊",
            "Владивосток — тихоокеанские ворота России, город мостов, морепродуктов и близости к Азии.",
            List.of(new String[]{"👥","Население","0,6 млн"}, new String[]{"🌡","Климат","Муссонный"}, new String[]{"🌏","До Японии","800 км"}, new String[]{"🕐","Часовой пояс","UTC+10"}),
            List.of(new String[]{"🌉","Русский мост","Один из крупнейших вантовых мостов мира"},
                    new String[]{"🏰","Владивостокская крепость","Уникальный военно-исторический комплекс"},
                    new String[]{"🌊","Набережная Цесаревича","Современная набережная с видом на залив"},
                    new String[]{"🦁","Приморский сафари-парк","Амурские тигры и другие редкие животные"}),
            List.of("🌉 Русский мост — фото обязательно",
                    "🍣 Морепродукты — главная гастрономия города",
                    "🌏 Япония и Корея рядом — паромы и авиа",
                    "🌁 Туманы летом — нормальное явление")))
    );

    private static final CityInfo DEFAULT = new CityInfo("✈",
        "Один из городов России с богатой историей и уникальным характером.",
        List.of(new String[]{"🌍","Страна","Россия"}, new String[]{"💰","Валюта","Рубль ₽"}),
        List.of(new String[]{"🏛","Исторический центр","Старинная архитектура и памятники"},
                new String[]{"🌳","Городской парк","Место отдыха горожан"},
                new String[]{"🎭","Местный театр","Культурная жизнь города"}),
        List.of("📍 Уточните достопримечательности у местных жителей",
                "🚌 Общественный транспорт — основной способ передвижения",
                "💳 Карты принимают везде в центре"));

    // ── инициализация ─────────────────────────────────────────────────────────

    public void setCity(City city, Stage stage, MainController mainController) {
        this.city = city;
        this.stage = stage;
        this.mainController = mainController;

        CityInfo info = DATA.getOrDefault(city.getCode(), DEFAULT);

        titleLabel.setText(city.getName());
        codeLabel.setText(city.getCode());
        heroEmoji.setText(info.emoji());
        heroName.setText(city.getName());
        heroCountry.setText(city.getCountry() != null ? city.getCountry() : "Россия");
        heroDesc.setText(info.desc());

        buildFacts(info);
        buildAttractions(info);
        buildTips(info);
        Platform.runLater(() -> {
            if (titleLabel != null) {
                ThemeStyleRemap.bindScene(titleLabel);
            }
        });
    }

    private void buildFacts(CityInfo info) {
        factsBox.getChildren().clear();
        for (String[] f : info.facts()) {
            VBox card = new VBox(4);
            card.setStyle(ThemeStyles.cardDefault().replace("12", "10"));
            HBox.setHgrow(card, Priority.ALWAYS);
            Label icon = new Label(f[0] + "  " + f[1]);
            icon.setStyle(ThemeStyles.labelSecondary("11px"));
            Label val = new Label(f[2]);
            val.setStyle(ThemeStyles.labelPrimaryBold("15px"));
            card.getChildren().addAll(icon, val);
            factsBox.getChildren().add(card);
        }
    }

    private void buildAttractions(CityInfo info) {
        attractionsBox.getChildren().clear();
        for (String[] a : info.attractions()) {
            HBox row = new HBox(14);
            row.setStyle(ThemeStyles.cardDefault().replace("12", "10"));
            row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            Label emoji = new Label(a[0]);
            emoji.setStyle("-fx-font-size: 28px; -fx-min-width: 40;");

            VBox text = new VBox(3);
            Label name = new Label(a[1]);
            name.setStyle(ThemeStyles.labelPrimaryBold("14px"));
            Label desc = new Label(a[2]);
            desc.setStyle(ThemeStyles.labelSecondary("12px"));
            text.getChildren().addAll(name, desc);

            row.getChildren().addAll(emoji, text);
            attractionsBox.getChildren().add(row);
        }
    }

    private void buildTips(CityInfo info) {
        tipsBox.getChildren().clear();
        for (String tip : info.tips()) {
            Label lbl = new Label(tip);
            lbl.setStyle(ThemeStyles.tipLabel());
            lbl.setWrapText(true);
            tipsBox.getChildren().add(lbl);
        }
    }

    @FXML
    private void handleBack() {
        stage.close();
    }

    @FXML
    private void handleSearchFlights() {
        if (mainController != null) {
            mainController.setDestinationCity(city);
        }
        stage.close();
    }
}
