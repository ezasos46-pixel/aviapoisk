package com.example.demo8.utils;

import com.example.demo8.models.City;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Применяет палитру темы к элементам главного экрана.
 */
public final class MainThemeApplier {

    private MainThemeApplier() {}

    public static void apply(MainViewRefs refs) {
        ThemeManager.Palette p = ThemeManager.getInstance().getPalette();
        boolean light = ThemeManager.getInstance().getCurrent() == ThemeManager.Theme.LIGHT;

        if (refs.rootPane != null) {
            refs.rootPane.setStyle("-fx-background-color: " + p.bgMain + ";");
        }
        if (refs.mainHeaderHBox != null) {
            refs.mainHeaderHBox.setStyle("-fx-background-color: " + p.bgHeader + "; -fx-padding: 0 32; -fx-min-height: 64; " +
                    "-fx-border-color: " + p.borderSoft + "; -fx-border-width: 0 0 1 0;");
        }
        if (refs.mainScrollVBox != null) {
            refs.mainScrollVBox.setStyle("-fx-background-color: " + p.bgMain + ";");
        }
        if (refs.mainHeroVBox != null) {
            refs.mainHeroVBox.setStyle("-fx-padding: 60 80 50 80; -fx-background-color: linear-gradient(to bottom, " +
                    p.bgHeroGradientTop + ", " + p.bgHeroGradientBottom + ");");
        }
        if (refs.heroTitleLabel != null) {
            refs.heroTitleLabel.setStyle("-fx-text-fill: " + p.textPrimary + "; -fx-font-size: 38px; -fx-font-weight: bold;");
        }
        if (refs.heroSubtitleLabel != null) {
            refs.heroSubtitleLabel.setStyle("-fx-text-fill: " + p.textSecondary + "; -fx-font-size: 15px;");
        }
        if (refs.mainSearchCard != null) {
            String shadow = light
                    ? "dropshadow(gaussian, rgba(2,136,209,0.15), 20, 0, 0, 6)"
                    : "dropshadow(gaussian, rgba(0,0,0,0.6), 30, 0, 0, 10)";
            refs.mainSearchCard.setStyle("-fx-background-color: " + p.bgCard + "; -fx-background-radius: 16; " +
                    "-fx-border-color: " + p.borderSoft + "; -fx-border-width: 1; -fx-border-radius: 16; -fx-effect: " + shadow + ";");
            styleSearchCardLabels(refs.mainSearchCard, p);
        }
        if (refs.mainCitiesSection != null) {
            refs.mainCitiesSection.setStyle("-fx-padding: 40 80 30 80; -fx-background-color: " + p.bgMain + ";");
        }
        if (refs.citiesTitleLabel != null) {
            refs.citiesTitleLabel.setStyle("-fx-text-fill: " + p.textPrimary + "; -fx-font-size: 20px; -fx-font-weight: bold; -fx-text-alignment: center;");
        }
        if (refs.citiesHintLabel != null) {
            refs.citiesHintLabel.setStyle("-fx-text-fill: " + p.textMuted + "; -fx-font-size: 12px;");
        }
        if (refs.mainFeaturesRow != null) {
            refs.mainFeaturesRow.setStyle("-fx-padding: 24 80 50 80; -fx-background-color: " + p.bgMain + ";");
            styleFeatureCards(refs.mainFeaturesRow, p);
        }
        if (refs.mainFooterHBox != null) {
            refs.mainFooterHBox.setStyle("-fx-background-color: " + p.bgFooter + "; -fx-padding: 14 32; " +
                    "-fx-border-color: " + p.borderSoft + "; -fx-border-width: 1 0 0 0;");
            for (Node node : refs.mainFooterHBox.getChildren()) {
                if (node instanceof Label label) {
                    label.setStyle("-fx-text-fill: " + p.footerText + "; -fx-font-size: 12px;");
                } else if (node instanceof Button btn) {
                    btn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + p.footerText + "; " +
                            "-fx-font-size: 12px; -fx-border-color: transparent; -fx-cursor: hand; -fx-underline: true; -fx-padding: 4 0;");
                }
            }
        }
        if (refs.passengersButton != null) {
            refs.passengersButton.setStyle("-fx-background-color: " + p.btnSecondaryBg + "; -fx-text-fill: " + p.btnSecondaryText + "; " +
                    "-fx-font-size: 13px; -fx-padding: 9 16; -fx-background-radius: 8; -fx-border-color: " + p.btnSecondaryBorder + "; " +
                    "-fx-border-width: 1; -fx-border-radius: 8; -fx-cursor: hand;");
        }
        if (refs.extraBaggageCheck != null) {
            refs.extraBaggageCheck.setStyle("-fx-font-size: 13px; -fx-text-fill: " + p.checkboxText + ";");
        }
        if (refs.btnShortestPath != null) {
            refs.btnShortestPath.setStyle("-fx-background-color: " + p.btnRouteBg + "; -fx-text-fill: " + p.btnRouteText + "; " +
                    "-fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 8; " +
                    "-fx-border-color: " + p.btnRouteBorder + "; -fx-border-width: 1; -fx-border-radius: 8; -fx-cursor: hand;");
        }
        if (refs.btnFindTickets != null) {
            String buyShadow = light
                    ? "dropshadow(gaussian, rgba(2,136,209,0.25), 10, 0, 0, 3)"
                    : "dropshadow(gaussian, rgba(229,57,53,0.4), 12, 0, 0, 4)";
            refs.btnFindTickets.setStyle("-fx-background-color: " + p.btnBuy + "; -fx-text-fill: " + p.btnBuyText + "; " +
                    "-fx-font-size: 15px; -fx-font-weight: bold; -fx-padding: 10 32; -fx-background-radius: 8; " +
                    "-fx-effect: " + buyShadow + "; -fx-cursor: hand;");
        }
        if (refs.themeToggleButton != null) {
            refs.themeToggleButton.setText(ThemeManager.getInstance().getToggleIcon());
            refs.themeToggleButton.setStyle("-fx-background-color: transparent; -fx-text-fill: " + p.accent + "; " +
                    "-fx-font-size: 18px; -fx-padding: 6 10; -fx-cursor: hand; -fx-border-color: " + p.border + "; " +
                    "-fx-border-width: 1; -fx-background-radius: 6;");
        }
        if (refs.authButton != null) {
            boolean loggedIn = refs.loggedIn;
            if (loggedIn) {
                refs.authButton.setStyle("-fx-background-color: transparent; -fx-text-fill: " + p.btnOutlineText + "; " +
                        "-fx-font-size: 13px; -fx-padding: 8 20; -fx-border-color: " + p.accent + "; -fx-border-width: 1.5; " +
                        "-fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand;");
            } else {
                refs.authButton.setStyle("-fx-background-color: transparent; -fx-text-fill: " + p.btnOutlineText + "; " +
                        "-fx-font-size: 13px; -fx-padding: 8 20; -fx-border-color: " + p.accent + "; -fx-border-width: 1.5; " +
                        "-fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand;");
            }
        }
        if (refs.profileButton != null) {
            refs.profileButton.setStyle("-fx-background-color: " + p.btnProfileBg + "; -fx-text-fill: " + p.btnProfileText + "; " +
                    "-fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 6; -fx-cursor: hand;");
        }
    }

    public static VBox buildCityCard(City city, String emoji, ThemeManager.Palette p) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: " + p.cityCardBg + "; -fx-background-radius: 12; -fx-padding: 20 18; " +
                "-fx-border-color: " + p.borderSoft + "; -fx-border-width: 1; -fx-border-radius: 12; -fx-cursor: hand; " +
                "-fx-min-width: 130; -fx-pref-width: 150;");

        Label emojiLbl = new Label(emoji);
        emojiLbl.setStyle("-fx-font-size: 28px;");

        Label name = new Label(city.getName());
        name.setStyle("-fx-text-fill: " + p.textPrimary + "; -fx-font-size: 13px; -fx-font-weight: bold;");
        name.setWrapText(true);

        Label code = new Label(city.getCode());
        code.setStyle("-fx-text-fill: " + p.accent + "; -fx-font-size: 11px; -fx-font-weight: bold; " +
                "-fx-background-color: " + p.cityCodeBg + "; -fx-background-radius: 4; -fx-padding: 2 6;");

        Label country = new Label(city.getCountry() != null ? city.getCountry() : "Россия");
        country.setStyle("-fx-text-fill: " + p.textMuted + "; -fx-font-size: 10px;");

        card.getChildren().addAll(emojiLbl, name, code, country);
        return card;
    }

    private static void styleSearchCardLabels(VBox searchCard, ThemeManager.Palette p) {
        styleLabelsRecursive(searchCard, p.accent, p.textSecondary);
    }

    private static void styleLabelsRecursive(Node node, String accent, String secondary) {
        if (node instanceof Label label) {
            String t = label.getText();
            if (t != null && !t.isEmpty()) {
                if (t.equals("⇄")) {
                    label.setStyle("-fx-text-fill: " + accent + "; -fx-font-size: 18px; -fx-padding: 0 4;");
                } else if (t.length() <= 12 && t.toUpperCase().equals(t)) {
                    boolean muted = t.contains("ПЕРЕСАДКА") || t.equals("ОБРАТНО");
                    label.setStyle("-fx-text-fill: " + (muted ? secondary : accent) + "; -fx-font-size: 10px; -fx-font-weight: bold;");
                }
            }
        }
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                styleLabelsRecursive(child, accent, secondary);
            }
        }
    }

    private static void styleFeatureCards(HBox row, ThemeManager.Palette p) {
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

    public static class MainViewRefs {
        public BorderPane rootPane;
        public HBox mainHeaderHBox;
        public Button themeToggleButton;
        public VBox mainScrollVBox;
        public VBox mainHeroVBox;
        public Label heroTitleLabel;
        public Label heroSubtitleLabel;
        public VBox mainSearchCard;
        public Button passengersButton;
        public CheckBox extraBaggageCheck;
        public Button btnShortestPath;
        public Button btnFindTickets;
        public VBox mainCitiesSection;
        public Label citiesTitleLabel;
        public Label citiesHintLabel;
        public HBox mainFeaturesRow;
        public HBox mainFooterHBox;
        public Button authButton;
        public Button profileButton;
        public boolean loggedIn;
    }
}
