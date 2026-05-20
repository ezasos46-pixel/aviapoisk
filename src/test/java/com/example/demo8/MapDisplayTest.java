package com.example.demo8;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Проверка артефакта отображения карты: ресурс {@code map.html} и контракт интеграции с JavaFX WebView
 * (глобальная функция {@code drawRoute}, подключение JS API Яндекс.Карт, отрисовка линии маршрута).
 */
class MapDisplayTest {

    private static final String MAP_RESOURCE = "/com/example/demo8/leaflet/map.html";

    @Test
    void mapHtml_isOnClasspath_andContainsYandexDrawRouteContract() throws IOException {
        try (InputStream in = MapDisplayTest.class.getResourceAsStream(MAP_RESOURCE)) {
            assertNotNull(in, "Файл map.html должен лежать в resources и быть доступен из classpath");
            String html = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(html.contains("api-maps.yandex.ru"), "Должен подключаться JavaScript API Яндекс.Карт");
            assertTrue(html.contains("window.drawRoute"), "Должна быть функция drawRoute для вызова из WebEngine.executeScript");
            assertTrue(html.contains("ymaps.Polyline"), "Должна использоваться линия маршрута (Polyline) на карте");
        }
    }
}
