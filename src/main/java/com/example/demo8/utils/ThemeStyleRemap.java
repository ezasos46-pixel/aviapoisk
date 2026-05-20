package com.example.demo8.utils;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

/**
 * Перекрашивает inline-стили, заданные под тёмную тему, под текущую палитру.
 */
public final class ThemeStyleRemap {

    private ThemeStyleRemap() {}

    public static void bindScene(Node anchor) {
        bindScene(anchor, 0);
    }

    private static void bindScene(Node anchor, int attempt) {
        if (anchor == null || attempt > 8) return;
        javafx.application.Platform.runLater(() -> {
            Scene scene = anchor.getScene();
            if (scene == null) {
                bindScene(anchor, attempt + 1);
                return;
            }
            SceneThemeSupport.install(scene);
        });
    }

    public static void applyTree(Node root) {
        if (root == null) return;
        applyNodeStyle(root);

        if (root instanceof TabPane tabPane) {
            for (Tab tab : tabPane.getTabs()) {
                applyTree(tab.getContent());
            }
        }
        if (root instanceof ScrollPane scrollPane && scrollPane.getContent() != null) {
            applyTree(scrollPane.getContent());
        }
        if (root instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                applyTree(child);
            }
        }
    }

    /** Перекрашивает TabPane и содержимое всех вкладок. */
    public static void applyTabPane(TabPane tabPane) {
        if (tabPane == null) return;
        applyNodeStyle(tabPane);
        for (Tab tab : tabPane.getTabs()) {
            applyTree(tab.getContent());
        }
    }

    private static void applyNodeStyle(Node root) {
        String style = root.getStyle();
        if (style != null && !style.isBlank()) {
            root.setStyle(remap(style));
        }
    }

    public static String remap(String style) {
        if (style == null || style.isBlank()) return style;
        ThemeManager.Palette p = ThemeStyles.p();
        String s = style;

        if (ThemeStyles.isLight()) {
            s = s.replace("-fx-text-fill: #0b1628", "-fx-text-fill: white");
        }

        s = s.replace("-fx-background-color: #0b1628", "-fx-background-color: " + p.bgMain);
        s = s.replace("-fx-background-color: #131f3e", "-fx-background-color: " + p.bgCard);
        s = s.replace("-fx-background-color: #080f1e", "-fx-background-color: " + p.bgFooter);
        s = s.replace("-fx-background-color: #0d1b3e", "-fx-background-color: " + p.bgHeroGradientTop);
        s = s.replace("#0b1628", p.bgMain);
        s = s.replace("#131f3e", p.bgCard);
        s = s.replace("#080f1e", p.bgFooter);
        s = s.replace("#0d1b3e", p.bgHeroGradientTop);
        s = s.replace("#1e2d50", ThemeStyles.inputBg());
        s = s.replace("#1e3a50", ThemeStyles.inputBgAccent());
        s = s.replace("#4fc3f7", p.accent);
        s = s.replace("#0288d1", p.accent);
        s = s.replace("#ffe066", ThemeStyles.priceColor());
        s = s.replace("#e67e22", ThemeStyles.priceColor());
        s = s.replace("#e53935", p.btnBuy);
        s = s.replace("#00897b", ThemeStyles.isLight() ? p.accent : "#00897b");
        s = s.replace("linear-gradient(to bottom, #0d1b3e, #0b1628)",
                "linear-gradient(to bottom, " + p.bgHeroGradientTop + ", " + p.bgHeroGradientBottom + ")");
        s = s.replace("linear-gradient(to bottom right, #0d1b3e, #131f3e)",
                "linear-gradient(to bottom right, " + p.bgHeroGradientTop + ", " + p.bgHeroGradientBottom + ")");

        s = s.replace("rgba(180,200,255,0.7)", p.textSecondary);
        s = s.replace("rgba(180,200,255,0.55)", p.textMuted);
        s = s.replace("rgba(180,200,255,0.6)", p.textSecondary);
        s = s.replace("rgba(180,200,255,0.5)", p.textMuted);
        s = s.replace("rgba(180,200,255,0.45)", p.textMuted);
        s = s.replace("rgba(180,200,255,0.4)", p.textMuted);
        s = s.replace("rgba(220,230,255,0.9)", p.textPrimary);

        s = s.replace("rgba(79,195,247,0.7)", p.accentSoft);
        s = s.replace("rgba(79,195,247,0.6)", p.accentSoft);
        s = s.replace("rgba(79,195,247,0.4)", p.borderSoft);
        s = s.replace("rgba(79,195,247,0.35)", p.border);
        s = s.replace("rgba(79,195,247,0.3)", p.border);
        s = s.replace("rgba(79,195,247,0.25)", p.border);
        s = s.replace("rgba(79,195,247,0.2)", p.borderSoft);
        s = s.replace("rgba(79,195,247,0.18)", p.borderSoft);
        s = s.replace("rgba(79,195,247,0.15)", p.borderSoft);
        s = s.replace("rgba(79,195,247,0.12)", p.borderSoft);
        s = s.replace("rgba(79,195,247,0.1)", p.divider);
        s = s.replace("rgba(79,195,247,0.08)", p.divider);

        s = s.replace("rgba(255,255,255,0.15)", p.btnSecondaryBorder);
        s = s.replace("rgba(255,255,255,0.3)", p.border);

        s = s.replace("rgba(255,255,255,0.85)", p.textPrimary);
        s = s.replace("rgba(255,255,255,0.8)", p.textSecondary);
        s = s.replace("rgba(255,255,255,0.75)", p.textSecondary);
        s = s.replace("rgba(255,255,255,0.7)", p.textSecondary);
        s = s.replace("rgba(255,255,255,0.6)", p.textSecondary);
        s = s.replace("rgba(255,255,255,0.5)", p.textMuted);
        s = s.replace("rgba(255,255,255,0.4)", p.textMuted);
        s = s.replace("rgba(255,255,255,0.3)", p.border);
        s = s.replace("rgba(255,255,255,0.15)", p.btnSecondaryBorder);
        s = s.replace("rgba(255,255,255,0.08)", p.featureCardBg);
        s = s.replace("rgba(255,255,255,0.07)", ThemeStyles.listRowBg());
        s = s.replace("rgba(255,255,255,0.1)", ThemeStyles.legCardBg());
        s = s.replace("rgba(255,255,255,0.04)", p.featureCardBg);
        s = s.replace("rgba(200,220,255,0.85)", p.textPrimary);

        if (ThemeStyles.isLight()) {
            s = s.replace("-fx-text-fill: white", "-fx-text-fill: " + p.textPrimary);
            s = s.replace("-fx-text-fill: #c8e6ff", "-fx-text-fill: " + p.textSecondary);
            s = s.replace("-fx-background-color: " + p.accent + "; -fx-text-fill: " + p.textPrimary,
                    "-fx-background-color: " + p.accent + "; -fx-text-fill: white");
            s = s.replace("-fx-text-fill: " + p.textPrimary + "; -fx-background-color: " + p.accent,
                    "-fx-text-fill: white; -fx-background-color: " + p.accent);
            s = s.replace("linear-gradient(to bottom right, " + p.bgHeroGradientTop + ", " + p.bgCard + ")",
                    "linear-gradient(to bottom right, " + p.bgHeroGradientTop + ", " + p.bgHeroGradientBottom + ")");
        }

        return s;
    }
}
