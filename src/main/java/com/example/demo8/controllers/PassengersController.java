package com.example.demo8.controllers;

import com.example.demo8.models.SearchParams;
import com.example.demo8.utils.ThemeStyleRemap;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class PassengersController {
    @FXML private Label adultsLabel;
    @FXML private Label childrenLabel;
    @FXML private Label infantsLabel;
    @FXML private RadioButton economyRadio;
    @FXML private RadioButton premiumRadio;
    @FXML private RadioButton businessRadio;
    
    private SearchParams searchParams;
    private MainController mainController;
    private boolean initialized = false;
    private javafx.scene.control.ToggleGroup classToggleGroup;
    
    @FXML
    public void initialize() {
        initialized = true;
        // Создаем ToggleGroup программно
        classToggleGroup = new javafx.scene.control.ToggleGroup();
        economyRadio.setToggleGroup(classToggleGroup);
        premiumRadio.setToggleGroup(classToggleGroup);
        businessRadio.setToggleGroup(classToggleGroup);
        
        Platform.runLater(() -> {
            if (adultsLabel != null) {
                ThemeStyleRemap.bindScene(adultsLabel);
            }
            if (searchParams != null) {
                updateLabels();
                selectServiceClass();
            }
        });
    }
    
    public void setSearchParams(SearchParams searchParams) {
        this.searchParams = searchParams;
        if (initialized) {
            updateLabels();
            selectServiceClass();
        }
    }
    
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }
    
    private void updateLabels() {
        if (searchParams != null && adultsLabel != null) {
            adultsLabel.setText(String.valueOf(searchParams.getAdults()));
            childrenLabel.setText(String.valueOf(searchParams.getChildren()));
            infantsLabel.setText(String.valueOf(searchParams.getInfants()));
        }
    }
    
    private void selectServiceClass() {
        if (searchParams == null || classToggleGroup == null) return;
        
        String serviceClass = searchParams.getServiceClass();
        switch (serviceClass) {
            case "Эконом":
                economyRadio.setSelected(true);
                break;
            case "Премиум":
                premiumRadio.setSelected(true);
                break;
            case "Бизнес":
                businessRadio.setSelected(true);
                break;
        }
    }
    
    @FXML
    private void increaseAdults() {
        searchParams.setAdults(searchParams.getAdults() + 1);
        updateLabels();
    }
    
    @FXML
    private void decreaseAdults() {
        if (searchParams.getAdults() > 1) {
            searchParams.setAdults(searchParams.getAdults() - 1);
            updateLabels();
        }
    }
    
    @FXML
    private void increaseChildren() {
        searchParams.setChildren(searchParams.getChildren() + 1);
        updateLabels();
    }
    
    @FXML
    private void decreaseChildren() {
        if (searchParams.getChildren() > 0) {
            searchParams.setChildren(searchParams.getChildren() - 1);
            updateLabels();
        }
    }
    
    @FXML
    private void increaseInfants() {
        searchParams.setInfants(searchParams.getInfants() + 1);
        updateLabels();
    }
    
    @FXML
    private void decreaseInfants() {
        if (searchParams.getInfants() > 0) {
            searchParams.setInfants(searchParams.getInfants() - 1);
            updateLabels();
        }
    }
    
    @FXML
    private void handleDone() {
        if (classToggleGroup != null) {
            RadioButton selected = (RadioButton) classToggleGroup.getSelectedToggle();
            if (selected != null && searchParams != null) {
                searchParams.setServiceClass(selected.getText());
            }
        }
        
        if (mainController != null) {
            mainController.updatePassengersButton();
        }
        
        Stage stage = (Stage) adultsLabel.getScene().getWindow();
        stage.close();
    }
}

