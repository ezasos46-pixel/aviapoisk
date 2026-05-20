package com.example.demo8.utils;

/**
 * Стили UI, зависящие от активной темы (тёмная / светлая голубая).
 */
public final class ThemeStyles {

    private ThemeStyles() {}

    public static ThemeManager.Palette p() {
        return ThemeManager.getInstance().getPalette();
    }

    public static boolean isLight() {
        return ThemeManager.getInstance().getCurrent() == ThemeManager.Theme.LIGHT;
    }

    public static String priceColor() {
        return isLight() ? "#e67e22" : "#ffe066";
    }

    public static String inputBg() {
        return isLight() ? "#f0f7fa" : "#1e2d50";
    }

    public static String inputBgAccent() {
        return isLight() ? "rgba(2,136,209,0.12)" : "#1e3a50";
    }

    public static String footerRowBg() {
        return isLight() ? "rgba(2,136,209,0.06)" : "rgba(0,0,0,0.2)";
    }

    public static String dialogBg() {
        return isLight() ? "#ffffff" : "#0d1b3e";
    }

    public static String dialogHeaderBg() {
        return p().bgFooter;
    }

    public static String rootBg() {
        return "-fx-background-color: " + p().bgMain + ";";
    }

    public static String card(String borderColor) {
        return "-fx-background-color: " + p().bgCard + "; -fx-background-radius: 12; " +
                "-fx-border-color: " + borderColor + "; -fx-border-width: 1; -fx-border-radius: 12;";
    }

    public static String cardDefault() {
        return card(p().borderSoft);
    }

    public static String cardBest() {
        return card(p().accent);
    }

    public static String headerBar() {
        return "-fx-background-color: " + p().bgFooter + "; -fx-border-color: " + p().borderSoft + "; -fx-border-width: 0 0 1 0;";
    }

    public static String toolbarBar() {
        return "-fx-padding: 10 32; -fx-background-color: " + p().bgHeroGradientTop + "; " +
                "-fx-border-color: " + p().divider + "; -fx-border-width: 1 0 0 0;";
    }

    public static String backButton() {
        return "-fx-background-color: transparent; -fx-text-fill: " + p().accent + "; -fx-font-size: 13px; " +
                "-fx-padding: 8 16; -fx-border-color: " + p().border + "; -fx-border-width: 1; " +
                "-fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand;";
    }

    public static String primaryButton() {
        return "-fx-background-color: " + p().accent + "; -fx-text-fill: " + p().btnProfileText + "; " +
                "-fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 11; -fx-background-radius: 6; -fx-cursor: hand;";
    }

    public static String buyButton() {
        String shadow = isLight()
                ? "dropshadow(gaussian, rgba(2,136,209,0.25), 8, 0, 0, 2)"
                : "dropshadow(gaussian, rgba(229,57,53,0.4), 8, 0, 0, 2)";
        return "-fx-background-color: " + p().btnBuy + "; -fx-text-fill: " + p().btnBuyText + "; -fx-font-size: 14px; " +
                "-fx-font-weight: bold; -fx-padding: 9 28; -fx-background-radius: 8; -fx-effect: " + shadow + "; -fx-cursor: hand;";
    }

    public static String detailsButton() {
        return "-fx-background-color: transparent; -fx-text-fill: " + p().btnOutlineText + "; -fx-font-size: 13px; " +
                "-fx-padding: 9 20; -fx-border-color: " + p().border + "; -fx-border-width: 1; " +
                "-fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;";
    }

    public static String chipSortActive() {
        return "-fx-background-color: " + (isLight() ? "rgba(2,136,209,0.15)" : "rgba(79,195,247,0.2)") + "; " +
                "-fx-text-fill: " + p().accent + "; -fx-font-size: 12px; -fx-padding: 5 14; -fx-background-radius: 20; " +
                "-fx-border-color: " + p().accent + "; -fx-border-width: 1; -fx-border-radius: 20; -fx-cursor: hand;";
    }

    public static String chipSortInactive() {
        return "-fx-background-color: transparent; -fx-text-fill: " + p().textSecondary + "; -fx-font-size: 12px; " +
                "-fx-padding: 5 14; -fx-background-radius: 20; -fx-border-color: " + p().btnSecondaryBorder + "; " +
                "-fx-border-width: 1; -fx-border-radius: 20; -fx-cursor: hand;";
    }

    public static String chipFilterActive() {
        return "-fx-background-color: " + (isLight() ? "rgba(230,126,34,0.15)" : "rgba(255,224,102,0.2)") + "; " +
                "-fx-text-fill: " + priceColor() + "; -fx-font-size: 12px; -fx-padding: 5 14; -fx-background-radius: 20; " +
                "-fx-border-color: " + priceColor() + "; -fx-border-width: 1; -fx-border-radius: 20; -fx-cursor: hand;";
    }

    public static String chipFilterInactive() {
        return chipSortInactive();
    }

    public static String labelPrimary(String fontSize) {
        return "-fx-text-fill: " + p().textPrimary + "; -fx-font-size: " + fontSize + ";";
    }

    public static String labelPrimaryBold(String fontSize) {
        return labelPrimary(fontSize) + " -fx-font-weight: bold;";
    }

    public static String labelSecondary(String fontSize) {
        return "-fx-text-fill: " + p().textSecondary + "; -fx-font-size: " + fontSize + ";";
    }

    public static String labelMuted(String fontSize) {
        return "-fx-text-fill: " + p().textMuted + "; -fx-font-size: " + fontSize + ";";
    }

    public static String labelAccent(String fontSize) {
        return "-fx-text-fill: " + p().accent + "; -fx-font-size: " + fontSize + "; -fx-font-weight: bold;";
    }

    public static String labelPrice(String fontSize) {
        return "-fx-text-fill: " + priceColor() + "; -fx-font-size: " + fontSize + "; -fx-font-weight: bold;";
    }

    public static String labelPriceAccent(String fontSize) {
        return "-fx-text-fill: " + p().accent + "; -fx-font-size: " + fontSize + "; -fx-font-weight: bold;";
    }

    public static String badgeBest() {
        return "-fx-background-color: " + p().accent + "; -fx-text-fill: " + p().btnProfileText + "; -fx-font-size: 11px; " +
                "-fx-font-weight: bold; -fx-padding: 3 10; -fx-background-radius: 0 0 8 0;";
    }

    public static String minusButton() {
        return "-fx-background-color: " + inputBg() + "; -fx-text-fill: " + p().textPrimary + "; -fx-font-size: 20px; " +
                "-fx-font-weight: bold; -fx-pref-width: 36; -fx-pref-height: 36; -fx-background-radius: 18; " +
                "-fx-border-color: " + p().border + "; -fx-border-width: 1; -fx-border-radius: 18; -fx-cursor: hand; -fx-padding: 0;";
    }

    public static String plusButton() {
        return "-fx-background-color: " + inputBgAccent() + "; -fx-text-fill: " + p().accent + "; -fx-font-size: 20px; " +
                "-fx-font-weight: bold; -fx-pref-width: 36; -fx-pref-height: 36; -fx-background-radius: 18; " +
                "-fx-border-color: " + p().accent + "; -fx-border-width: 1; -fx-border-radius: 18; -fx-cursor: hand; -fx-padding: 0;";
    }

    public static String cardPanel() {
        return "-fx-background-color: " + p().bgCard + "; -fx-background-radius: 12; -fx-padding: 24; " +
                "-fx-border-color: " + p().borderSoft + "; -fx-border-width: 1; -fx-border-radius: 12;";
    }

    public static String dialogRoot() {
        String shadow = isLight()
                ? "dropshadow(gaussian, rgba(2,136,209,0.2), 20, 0, 0, 6)"
                : "dropshadow(gaussian, rgba(0,0,0,0.8), 30, 0, 0, 8)";
        return "-fx-background-color: " + dialogBg() + "; -fx-border-color: " + p().border + "; -fx-border-width: 1; " +
                "-fx-background-radius: 14; -fx-border-radius: 14; -fx-effect: " + shadow + ";";
    }

    public static String dialogHeader() {
        return "-fx-background-color: " + dialogHeaderBg() + "; -fx-padding: 16 20 16 24; -fx-background-radius: 14 14 0 0; " +
                "-fx-border-color: " + p().borderSoft + "; -fx-border-width: 0 0 1 0;";
    }

    public static String confirmYesButton() {
        return buyButton().replace("9 28", "9 28");
    }

    public static String confirmCancelButton() {
        return "-fx-background-color: " + p().btnSecondaryBg + "; -fx-text-fill: " + p().textSecondary + "; " +
                "-fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 9 22; " +
                "-fx-border-color: " + p().btnSecondaryBorder + "; -fx-border-width: 1; -fx-cursor: hand;";
    }

    public static String durationLine() {
        return "-fx-background-color: " + p().border + "; -fx-pref-height: 1; -fx-max-height: 1;";
    }

    public static String cardFooter() {
        return "-fx-padding: 12 20; -fx-background-color: " + footerRowBg() + "; " +
                "-fx-background-radius: 0 0 12 12; -fx-border-color: " + p().divider + "; -fx-border-width: 1 0 0 0;";
    }

    public static String listRowBg() {
        return isLight() ? "rgba(2,136,209,0.06)" : "rgba(255,255,255,0.07)";
    }

    public static String listRow() {
        return "-fx-background-color: " + listRowBg() + "; -fx-background-radius: 8; -fx-padding: 10 14; " +
                "-fx-alignment: CENTER_LEFT; -fx-cursor: hand;";
    }

    public static String legCardBg() {
        return isLight() ? "rgba(2,136,209,0.08)" : "rgba(255,255,255,0.1)";
    }

    public static String legCard() {
        return "-fx-background-color: " + legCardBg() + "; -fx-background-radius: 8; -fx-padding: 10; " +
                "-fx-border-color: " + p().borderSoft + "; -fx-border-width: 1; -fx-border-radius: 8;";
    }

    public static String searchPanel() {
        return "-fx-padding: 20 20; -fx-background-color: " + p().bgCard + "; " +
                "-fx-border-color: " + p().divider + "; -fx-border-width: 0 0 1 0;";
    }

    public static String searchBtnRow() {
        return "-fx-padding: 12 20; -fx-background-color: " + p().bgCard + "; " +
                "-fx-border-color: " + p().divider + "; -fx-border-width: 0 0 1 0;";
    }

    public static String leftPanel() {
        return "-fx-border-color: " + p().divider + "; -fx-border-width: 0 1 0 0; -fx-background-color: " + p().bgMain + ";";
    }

    public static String mapPanelBg() {
        return "-fx-background-color: " + p().bgHeroGradientTop + ";";
    }

    public static String panelCard() {
        return "-fx-background-color: " + p().bgCard + "; -fx-background-radius: 12; -fx-padding: 28; " +
                "-fx-border-color: " + p().borderSoft + "; -fx-border-width: 1; -fx-border-radius: 12;";
    }

    public static String screenHeader() {
        return "-fx-background-color: " + p().bgFooter + "; -fx-padding: 0 32; -fx-min-height: 60; " +
                "-fx-border-color: " + p().borderSoft + "; -fx-border-width: 0 0 1 0;";
    }

    public static String screenHeaderBackButton() {
        return "-fx-background-color: transparent; -fx-text-fill: " + p().accent + "; -fx-font-size: 13px; " +
                "-fx-padding: 8 16; -fx-border-color: " + p().border + "; -fx-border-width: 1; " +
                "-fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand;";
    }

    public static String findRouteButton() {
        return "-fx-background-color: " + p().accent + "; -fx-text-fill: white; -fx-font-size: 13px; " +
                "-fx-font-weight: bold; -fx-padding: 9 24; -fx-background-radius: 6; -fx-cursor: hand;";
    }

    /** Цвета маркеров на карте: вылет / пересадка / прилёт */
    public static String mapMarkerColor(int index, int total) {
        if (index == 0) return p().accent;
        if (index == total - 1) return isLight() ? "#c62828" : "#e53935";
        return priceColor();
    }

    public static String tipLabel() {
        return "-fx-text-fill: " + p().textPrimary + "; -fx-font-size: 13px; " +
                "-fx-background-color: " + p().featureCardBg + "; -fx-background-radius: 8; -fx-padding: 10 14;";
    }
}
