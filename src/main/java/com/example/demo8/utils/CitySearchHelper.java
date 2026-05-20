package com.example.demo8.utils;

import com.example.demo8.models.City;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.StringConverter;

import java.util.List;
import java.util.stream.Collectors;

public class CitySearchHelper {

    /**
     * Делает ComboBox с городами редактируемым с живым поиском.
     * Показывает все города при открытии, фильтрует по вводу.
     */
    public static void setup(ComboBox<City> comboBox, ObservableList<City> allCities) {
        comboBox.setEditable(true);
        comboBox.setVisibleRowCount(20);

        comboBox.setConverter(new StringConverter<City>() {
            @Override
            public String toString(City city) {
                return city == null ? "" : city.getName();
            }

            @Override
            public City fromString(String s) {
                if (s == null || s.isBlank()) return null;
                return allCities.stream()
                        .filter(c -> c.getName().equalsIgnoreCase(s.trim()))
                        .findFirst().orElse(null);
            }
        });

        TextField editor = comboBox.getEditor();

        // При вводе текста — фильтруем список
        editor.textProperty().addListener((obs, oldVal, newVal) -> {
            City selected = comboBox.getValue();
            // Если выбранный город совпадает с текстом — не фильтруем
            if (selected != null && selected.getName().equals(newVal)) return;

            String filter = newVal == null ? "" : newVal.trim().toLowerCase();
            ObservableList<City> filtered;
            if (filter.isEmpty()) {
                filtered = allCities;
            } else {
                List<City> result = allCities.stream()
                        .filter(c -> c.getName().toLowerCase().contains(filter))
                        .collect(Collectors.toList());
                filtered = FXCollections.observableArrayList(result);
            }

            City current = comboBox.getValue();
            comboBox.setItems(filtered);
            comboBox.setValue(current);

            // show() при пустом фильтре на старте открывает все ComboBox сразу и ломает отображение списков
            if (!filter.isEmpty() && !filtered.isEmpty()) {
                comboBox.show();
            }
        });

        // При открытии — показываем все города
        comboBox.showingProperty().addListener((obs, wasShowing, isShowing) -> {
            if (isShowing) {
                String text = editor.getText();
                if (text == null || text.isEmpty()) {
                    comboBox.setItems(allCities);
                }
                // Прокручиваем к выбранному элементу
                City val = comboBox.getValue();
                if (val != null) {
                    int idx = comboBox.getItems().indexOf(val);
                    if (idx >= 0) {
                        ListView<?> lv = (ListView<?>) comboBox.lookup(".list-view");
                        if (lv != null) lv.scrollTo(idx);
                    }
                }
            }
        });

        // При выборе из списка — ставим имя города в поле
        comboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                editor.setText(newVal.getName());
                editor.positionCaret(newVal.getName().length());
            }
        });

        // Escape — сбрасываем фильтр
        editor.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                comboBox.setItems(allCities);
                comboBox.hide();
            }
        });

        // Потеря фокуса — если текст не совпадает с городом, сбрасываем
        editor.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) {
                City val = comboBox.getValue();
                String text = editor.getText();
                if (val == null || !val.getName().equals(text)) {
                    // Ищем точное совпадение
                    City match = allCities.stream()
                            .filter(c -> c.getName().equalsIgnoreCase(text))
                            .findFirst().orElse(null);
                    if (match != null) {
                        comboBox.setValue(match);
                    } else if (val != null) {
                        editor.setText(val.getName());
                    } else {
                        editor.clear();
                    }
                }
                comboBox.setItems(allCities);
            }
        });
    }
}
