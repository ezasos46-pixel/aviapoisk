module com.example.demo8 {
    requires javafx.base;
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires com.google.gson;
    requires okhttp3;
    requires java.desktop;
    requires jdk.httpserver;
    requires java.net.http;
    requires java.prefs;

    // Открываем пакет для JavaFX и java.desktop
    opens com.example.demo8.controllers to javafx.fxml, java.desktop;

    opens com.example.demo8 to javafx.fxml;
    opens com.example.demo8.models to com.google.gson;

    exports com.example.demo8;
    exports com.example.demo8.controllers;
    exports com.example.demo8.models;
}