package com.example.demo8.utils;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.paint.Color;

import java.util.Objects;
import java.util.prefs.Preferences;

public final class ThemeManager {

    public enum Theme {
        DARK, LIGHT
    }

    public static final class Palette {
        public final String bgMain;
        public final String bgCard;
        public final String bgHeader;
        public final String bgFooter;
        public final String bgHeroGradientTop;
        public final String bgHeroGradientBottom;
        public final String accent;
        public final String accentSoft;
        public final String textPrimary;
        public final String textSecondary;
        public final String textMuted;
        public final String border;
        public final String borderSoft;
        public final String btnBuy;
        public final String btnBuyText;
        public final String btnOutlineText;
        public final String btnProfileBg;
        public final String btnProfileText;
        public final String btnSecondaryBg;
        public final String btnSecondaryText;
        public final String btnSecondaryBorder;
        public final String btnRouteBg;
        public final String btnRouteText;
        public final String btnRouteBorder;
        public final String checkboxText;
        public final String featureCardBg;
        public final String featureCardBorder;
        public final String cityCardBg;
        public final String cityCodeBg;
        public final String footerText;
        public final String stageFill;
        public final String divider;

        public Palette(String bgMain, String bgCard, String bgHeader, String bgFooter,
                       String bgHeroGradientTop, String bgHeroGradientBottom,
                       String accent, String accentSoft,
                       String textPrimary, String textSecondary, String textMuted,
                       String border, String borderSoft,
                       String btnBuy, String btnBuyText, String btnOutlineText,
                       String btnProfileBg, String btnProfileText,
                       String btnSecondaryBg, String btnSecondaryText, String btnSecondaryBorder,
                       String btnRouteBg, String btnRouteText, String btnRouteBorder,
                       String checkboxText, String featureCardBg, String featureCardBorder,
                       String cityCardBg, String cityCodeBg, String footerText,
                       String stageFill, String divider) {
            this.bgMain = bgMain;
            this.bgCard = bgCard;
            this.bgHeader = bgHeader;
            this.bgFooter = bgFooter;
            this.bgHeroGradientTop = bgHeroGradientTop;
            this.bgHeroGradientBottom = bgHeroGradientBottom;
            this.accent = accent;
            this.accentSoft = accentSoft;
            this.textPrimary = textPrimary;
            this.textSecondary = textSecondary;
            this.textMuted = textMuted;
            this.border = border;
            this.borderSoft = borderSoft;
            this.btnBuy = btnBuy;
            this.btnBuyText = btnBuyText;
            this.btnOutlineText = btnOutlineText;
            this.btnProfileBg = btnProfileBg;
            this.btnProfileText = btnProfileText;
            this.btnSecondaryBg = btnSecondaryBg;
            this.btnSecondaryText = btnSecondaryText;
            this.btnSecondaryBorder = btnSecondaryBorder;
            this.btnRouteBg = btnRouteBg;
            this.btnRouteText = btnRouteText;
            this.btnRouteBorder = btnRouteBorder;
            this.checkboxText = checkboxText;
            this.featureCardBg = featureCardBg;
            this.featureCardBorder = featureCardBorder;
            this.cityCardBg = cityCardBg;
            this.cityCodeBg = cityCodeBg;
            this.footerText = footerText;
            this.stageFill = stageFill;
            this.divider = divider;
        }
    }

    private static final Palette DARK = new Palette(
            "#0b1628", "#131f3e", "rgba(13,27,62,0.95)", "#080f1e",
            "#0d1b3e", "#0b1628",
            "#4fc3f7", "rgba(79,195,247,0.7)",
            "white", "rgba(180,200,255,0.7)", "rgba(180,200,255,0.5)",
            "rgba(79,195,247,0.3)", "rgba(79,195,247,0.18)",
            "#e53935", "white", "#4fc3f7", "#4fc3f7", "#0b1628",
            "rgba(79,195,247,0.1)", "#c8e6ff", "rgba(79,195,247,0.25)",
            "rgba(0,150,136,0.2)", "#4db6ac", "rgba(0,150,136,0.4)",
            "#c8e6ff",
            "rgba(79,195,247,0.05)", "rgba(79,195,247,0.08)",
            "#131f3e", "rgba(79,195,247,0.12)",
            "rgba(255,255,255,0.3)", "#0b1628", "rgba(255,255,255,0.06)"
    );

    private static final Palette LIGHT = new Palette(
            "#e8f4f8", "#ffffff", "#ffffff", "#ffffff",
            "#d4eaf4", "#e8f4f8",
            "#0288d1", "rgba(2,136,209,0.75)",
            "#1a2a4f", "#5a6e8a", "rgba(90,110,138,0.65)",
            "#b0d4e8", "rgba(176,212,232,0.5)",
            "#0288d1", "white", "#0288d1", "#0288d1", "white",
            "rgba(2,136,209,0.08)", "#1a2a4f", "#b0d4e8",
            "rgba(2,136,209,0.1)", "#0288d1", "#b0d4e8",
            "#1a2a4f",
            "rgba(2,136,209,0.06)", "rgba(176,212,232,0.6)",
            "#ffffff", "rgba(2,136,209,0.1)",
            "rgba(90,110,138,0.55)", "#e8f4f8", "rgba(1ыЫ76,212,232,0.4)"
    );

    private static final String LIGHT_STYLESHEET = "/com/example/demo8/theme-light.css";
    private static final String PREFS_KEY = "app_theme";
    private static ThemeManager instance;
    private final Preferences prefs = Preferences.userNodeForPackage(ThemeManager.class);
    private Theme current = Theme.DARK;
    private ThemeManager() {
        String saved = prefs.get(PREFS_KEY, Theme.DARK.name());
        try {
            current = Theme.valueOf(saved);
        } catch (IllegalArgumentException ignored) {
            current = Theme.DARK;
        }
    }
    public static ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }
    public Theme getCurrent() {
        return current;
    }
    public Palette getPalette() {
        return current == Theme.LIGHT ? LIGHT : DARK;
    }
    public String getToggleIcon() {
        return current == Theme.LIGHT ? "☀" : "🌙";
    }
    public String getToggleTooltip() {
        return current == Theme.LIGHT
                ? "Светлая тема (нажмите для тёмной)"
                : "Тёмная тема (нажмите для светлой)";
    }

    public Theme toggle() {
        current = current == Theme.DARK ? Theme.LIGHT : Theme.DARK;
        prefs.put(PREFS_KEY, current.name());
        return current;
    }

    public void applyToScene(Scene scene) {
        if (scene == null) return;
        Node root = scene.getRoot();
        if (root == null) return;

        root.getStyleClass().remove("light-theme");
        root.getStyleClass().remove("dark-theme");
        root.getStyleClass().add(current == Theme.LIGHT ? "light-theme" : "dark-theme");

        String lightCss = Objects.requireNonNull(
                ThemeManager.class.getResource(LIGHT_STYLESHEET),
                LIGHT_STYLESHEET
        ).toExternalForm();

        scene.getStylesheets().removeIf(s -> s.equals(lightCss));
        if (current == Theme.LIGHT && !scene.getStylesheets().contains(lightCss)) {
            scene.getStylesheets().add(lightCss);
        }

        scene.setFill(Color.web(getPalette().stageFill));
    }

    public Color getStageFillColor() {
        return Color.web(getPalette().stageFill);
    }

    /** Подключает styles.css и активную тему к сцене. */
    public void attachToScene(Scene scene) {
        if (scene == null) return;
        String baseCss;
        String lightCss;
        try {
            baseCss = Objects.requireNonNull(
                    ThemeManager.class.getResource("/com/example/demo8/styles.css")
            ).toExternalForm();
            lightCss = Objects.requireNonNull(
                    ThemeManager.class.getResource(LIGHT_STYLESHEET)
            ).toExternalForm();
        } catch (Exception e) {
            System.err.println("Не удалось загрузить CSS: " + e.getMessage());
            applyToScene(scene);
            return;
        }

        scene.getStylesheets().removeIf(url ->
                url != null && (url.equals(baseCss) || url.equals(lightCss)));
        scene.getStylesheets().add(baseCss);
        if (current == Theme.LIGHT) {
            scene.getStylesheets().add(lightCss);
        }
        applyToScene(scene);
    }
}
