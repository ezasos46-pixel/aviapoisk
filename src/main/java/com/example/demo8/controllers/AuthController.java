package com.example.demo8.controllers;

import com.example.demo8.models.Flight;
import com.example.demo8.models.RoundTrip;
import com.example.demo8.models.User;
import com.example.demo8.services.SupabaseClient;
import com.example.demo8.utils.SessionManager;
import com.example.demo8.utils.ThemeStyleRemap;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public class AuthController {
    @FXML private Label titleLabel;
    @FXML private VBox loginForm;
    @FXML private VBox registerForm;
    @FXML private Label switchToRegisterLabel;
    @FXML private Label switchToLoginLabel;
    @FXML private TextField loginEmailOrPhone;
    @FXML private PasswordField loginPassword;
    @FXML private TextField loginPasswordVisible;
    @FXML private Button loginPasswordToggle;
    @FXML private HBox loginPasswordToggleBox;
    @FXML private TextField registerEmail;
    @FXML private TextField registerPhone;
    @FXML private PasswordField registerPassword;
    @FXML private TextField registerPasswordVisible;
    @FXML private Button registerPasswordToggle;
    @FXML private HBox registerPasswordToggleBox;
    @FXML private CheckBox privacyConsentCheckbox;

    private boolean loginPasswordVisibleState = false;
    private boolean registerPasswordVisibleState = false;

    private boolean isLoginMode = true;
    private MainController mainController;
    private ResultsController resultsController;
    private Flight selectedFlight;
    private RoundTrip selectedRoundTrip;
    private SupabaseClient supabaseClient;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{11}$");
    private static final Pattern EMAIL_OR_PHONE_PATTERN = Pattern.compile("^([A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}|\\d{11})$");

    public void initialize() {
        supabaseClient = SupabaseClient.getInstance();
        Platform.runLater(() -> {
            if (loginEmailOrPhone != null) ThemeStyleRemap.bindScene(loginEmailOrPhone);
        });
        updateFormVisibility();
        setupValidation();
        setupPasswordToggleButtons();
        setupPasswordToggleMouseTransparency();
    }

    private void setupPasswordToggleMouseTransparency() {
        // Делаем HBox не перехватывающим события, но кнопка остается кликабельной
        if (loginPasswordToggleBox != null) {
            loginPasswordToggleBox.setPickOnBounds(false);
        }
        if (registerPasswordToggleBox != null) {
            registerPasswordToggleBox.setPickOnBounds(false);
        }
    }

    private void setupPasswordToggleButtons() {
        // Эффект при наведении для кнопки входа
        loginPasswordToggle.setOnMouseEntered(e -> {
            loginPasswordToggle.setStyle("-fx-background-color: rgba(0,0,0,0.05); -fx-background-radius: 3; -fx-font-size: 16px; -fx-padding: 0; -fx-cursor: hand; -fx-text-fill: #333; -fx-border-color: transparent;");
        });
        loginPasswordToggle.setOnMouseExited(e -> {
            loginPasswordToggle.setStyle("-fx-background-color: transparent; -fx-background-radius: 3; -fx-font-size: 16px; -fx-padding: 0; -fx-cursor: hand; -fx-text-fill: #666; -fx-border-color: transparent;");
        });

        // Эффект при наведении для кнопки регистрации
        registerPasswordToggle.setOnMouseEntered(e -> {
            registerPasswordToggle.setStyle("-fx-background-color: rgba(0,0,0,0.05); -fx-background-radius: 3; -fx-font-size: 16px; -fx-padding: 0; -fx-cursor: hand; -fx-text-fill: #333; -fx-border-color: transparent;");
        });
        registerPasswordToggle.setOnMouseExited(e -> {
            registerPasswordToggle.setStyle("-fx-background-color: transparent; -fx-background-radius: 3; -fx-font-size: 16px; -fx-padding: 0; -fx-cursor: hand; -fx-text-fill: #666; -fx-border-color: transparent;");
        });
    }

    private void setupValidation() {
        // Валидация email в регистрации - только допустимые символы для email
        UnaryOperator<TextFormatter.Change> emailFilter = change -> {
            String newText = change.getControlNewText();
            if (newText.isEmpty() || newText.matches("^[A-Za-z0-9+_.-@]*$")) {
                return change;
            }
            return null;
        };
        registerEmail.setTextFormatter(new TextFormatter<>(emailFilter));
        registerEmail.textProperty().addListener((obs, oldVal, newVal) -> {
            validateEmailField(registerEmail, newVal);
        });

        // Валидация телефона - только цифры, максимум 11
        UnaryOperator<TextFormatter.Change> phoneFilter = change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d{0,11}")) {
                return change;
            }
            return null;
        };
        registerPhone.setTextFormatter(new TextFormatter<>(phoneFilter));
        registerPhone.textProperty().addListener((obs, oldVal, newVal) -> {
            validatePhoneField(registerPhone, newVal);
        });

        // Валидация поля входа - только цифры (номер телефона)
        UnaryOperator<TextFormatter.Change> loginFilter = change -> {
            String newText = change.getControlNewText();
            // Разрешаем только цифры, максимум 11
            if (newText.matches("\\d{0,11}")) {
                return change;
            }
            return null;
        };
        loginEmailOrPhone.setTextFormatter(new TextFormatter<>(loginFilter));
        loginEmailOrPhone.textProperty().addListener((obs, oldVal, newVal) -> {
            validateLoginField(loginEmailOrPhone, newVal);
        });

        // Ограничение длины пароля - максимум 20 символов
        UnaryOperator<TextFormatter.Change> passwordFilter = change -> {
            String newText = change.getControlNewText();
            if (newText.length() <= 20) {
                return change;
            }
            return null;
        };

        // Валидация пароля при входе
        loginPassword.setTextFormatter(new TextFormatter<>(passwordFilter));
        loginPasswordVisible.setTextFormatter(new TextFormatter<>(passwordFilter));
        loginPassword.textProperty().addListener((obs, oldVal, newVal) -> {
            validatePasswordField(loginPassword, newVal);
            if (!loginPasswordVisibleState) {
                loginPasswordVisible.setText(newVal);
            }
        });
        loginPasswordVisible.textProperty().addListener((obs, oldVal, newVal) -> {
            if (loginPasswordVisibleState) {
                loginPassword.setText(newVal);
            }
            validatePasswordTextField(loginPasswordVisible, newVal);
        });

        // Валидация пароля в регистрации
        registerPassword.setTextFormatter(new TextFormatter<>(passwordFilter));
        registerPasswordVisible.setTextFormatter(new TextFormatter<>(passwordFilter));
        registerPassword.textProperty().addListener((obs, oldVal, newVal) -> {
            validatePasswordField(registerPassword, newVal);
            if (!registerPasswordVisibleState) {
                registerPasswordVisible.setText(newVal);
            }
        });
        registerPasswordVisible.textProperty().addListener((obs, oldVal, newVal) -> {
            if (registerPasswordVisibleState) {
                registerPassword.setText(newVal);
            }
            validatePasswordTextField(registerPasswordVisible, newVal);
        });
    }

    private static final String STYLE_NORMAL  = "-fx-background-color: #1e2d50; -fx-text-fill: white; -fx-control-inner-background: #1e2d50; -fx-font-size: 12px; -fx-padding: 7; -fx-border-color: transparent;";
    private static final String STYLE_VALID   = "-fx-background-color: #1e2d50; -fx-text-fill: white; -fx-control-inner-background: #1e2d50; -fx-font-size: 12px; -fx-padding: 7; -fx-border-color: #4CAF50; -fx-border-width: 2; -fx-border-radius: 3;";
    private static final String STYLE_INVALID = "-fx-background-color: #2d1a1a; -fx-text-fill: white; -fx-control-inner-background: #2d1a1a; -fx-font-size: 12px; -fx-padding: 7; -fx-border-color: #f44336; -fx-border-width: 2; -fx-border-radius: 3;";

    private void validatePasswordField(PasswordField field, String value) {
        if (value.isEmpty()) field.setStyle(STYLE_NORMAL);
        else if (value.length() >= 6) field.setStyle(STYLE_VALID);
        else field.setStyle(STYLE_INVALID);
    }

    private void validatePasswordTextField(TextField field, String value) {
        if (value.isEmpty()) field.setStyle(STYLE_NORMAL);
        else if (value.length() >= 6) field.setStyle(STYLE_VALID);
        else field.setStyle(STYLE_INVALID);
    }

    private void validateEmailField(TextField field, String value) {
        if (value.isEmpty()) field.setStyle(STYLE_NORMAL);
        else if (EMAIL_PATTERN.matcher(value).matches()) field.setStyle(STYLE_VALID);
        else field.setStyle(STYLE_INVALID);
    }

    private void validatePhoneField(TextField field, String value) {
        if (value.isEmpty()) field.setStyle(STYLE_NORMAL);
        else if (PHONE_PATTERN.matcher(value).matches()) field.setStyle(STYLE_VALID);
        else field.setStyle(STYLE_INVALID);
    }

    private void validateLoginField(TextField field, String value) {
        if (value.isEmpty()) field.setStyle(STYLE_NORMAL);
        else if (PHONE_PATTERN.matcher(value).matches()) field.setStyle(STYLE_VALID);
        else field.setStyle(STYLE_INVALID);
    }

    public void setLoginMode(boolean isLogin) {
        this.isLoginMode = isLogin;
        updateFormVisibility();
    }

    public void setMainController(MainController controller) {
        this.mainController = controller;
    }

    public void setResultsController(ResultsController controller) {
        this.resultsController = controller;
    }

    public void setSelectedFlight(Flight flight) {
        this.selectedFlight = flight;
    }

    public void setSelectedRoundTrip(RoundTrip roundTrip) {
        this.selectedRoundTrip = roundTrip;
    }

    private void updateFormVisibility() {
        if (isLoginMode) {
            // Показываем форму входа
            loginForm.setVisible(true);
            loginForm.setManaged(true);
            // Поднимаем форму входа на передний план
            if (loginForm.getParent() != null) {
                loginForm.toFront();
            }

            // Скрываем форму регистрации
            registerForm.setVisible(false);
            registerForm.setManaged(false);
        } else {
            // Показываем форму регистрации
            registerForm.setVisible(true);
            registerForm.setManaged(true);
            // Поднимаем форму регистрации на передний план
            if (registerForm.getParent() != null) {
                registerForm.toFront();
            }

            // Скрываем форму входа
            loginForm.setVisible(false);
            loginForm.setManaged(false);
        }
        titleLabel.setText(isLoginMode ? "Вход" : "Регистрация");
    }

    @FXML
    private void switchToRegister(MouseEvent event) {
        isLoginMode = false;
        updateFormVisibility();
    }

    @FXML
    private void switchToLogin(MouseEvent event) {
        isLoginMode = true;
        updateFormVisibility();
    }

    @FXML
    private void handleLogin() {
        String emailOrPhone = loginEmailOrPhone.getText().trim();
        String password = loginPasswordVisibleState ? loginPasswordVisible.getText() : loginPassword.getText();

        if (emailOrPhone.isEmpty() || password.isEmpty()) {
            showError("Заполните все поля");
            return;
        }

        // Валидация формата телефона
        if (!PHONE_PATTERN.matcher(emailOrPhone).matches()) {
            showError("Номер телефона должен содержать 11 цифр");
            loginEmailOrPhone.setStyle(STYLE_INVALID);
            return;
        }

        new Thread(() -> {
            try {
                User user = supabaseClient.loginUser(emailOrPhone, password);
                Platform.runLater(() -> {
                    if (user != null) {
                        SessionManager.getInstance().setCurrentUser(user);
                        if (mainController != null) {
                            mainController.updateUI();
                        }
                        Stage stage = (Stage) loginForm.getScene().getWindow();
                        stage.close();

                        if (resultsController != null) {
                            if (selectedRoundTrip != null) {
                                resultsController.openBookingWindow(selectedRoundTrip);
                            } else if (selectedFlight != null) {
                                // Для обратной совместимости
                                RoundTrip rt = new RoundTrip(selectedFlight, null);
                                resultsController.openBookingWindow(rt);
                            }
                        }
                    } else {
                        showError("Неверный email/телефон или пароль");
                    }
                });
            } catch (IOException e) {
                Platform.runLater(() -> showError("Ошибка входа: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    private void handleRegister() {
        String email = registerEmail.getText().trim();
        String phone = registerPhone.getText().trim();
        String password = registerPasswordVisibleState ? registerPasswordVisible.getText() : registerPassword.getText();

        if (email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
            showError("Заполните все поля");
            return;
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            showError("Неверный формат email");
            return;
        }

        if (!PHONE_PATTERN.matcher(phone).matches()) {
            showError("Номер телефона должен содержать 11 цифр");
            return;
        }

        if (password.length() < 6) {
            showError("Пароль должен содержать минимум 6 символов");
            return;
        }

        if (privacyConsentCheckbox == null || !privacyConsentCheckbox.isSelected()) {
            showError("Необходимо дать согласие на обработку персональных данных");
            return;
        }

        new Thread(() -> {
            try {
                User user = supabaseClient.registerUser(email, phone, password);
                Platform.runLater(() -> {
                    if (user != null) {
                        SessionManager.getInstance().setCurrentUser(user);
                        if (mainController != null) {
                            mainController.updateUI();
                        }
                        Stage stage = (Stage) registerForm.getScene().getWindow();
                        stage.close();

                        if (resultsController != null) {
                            if (selectedRoundTrip != null) {
                                resultsController.openBookingWindow(selectedRoundTrip);
                            } else if (selectedFlight != null) {
                                // Для обратной совместимости
                                RoundTrip rt = new RoundTrip(selectedFlight, null);
                                resultsController.openBookingWindow(rt);
                            }
                        }
                    } else {
                        showError("Ошибка регистрации. Возможно, пользователь с таким email или телефоном уже существует");
                    }
                });
            } catch (IOException e) {
                Platform.runLater(() -> showError("Ошибка регистрации: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    private void toggleLoginPasswordVisibility() {
        loginPasswordVisibleState = !loginPasswordVisibleState;
        if (loginPasswordVisibleState) {
            loginPasswordVisible.setText(loginPassword.getText());
            loginPasswordVisible.setVisible(true);
            loginPasswordVisible.setManaged(true);
            loginPassword.setVisible(false);
            loginPassword.setManaged(false);
            loginPasswordToggle.setText("🙈");
        } else {
            loginPassword.setText(loginPasswordVisible.getText());
            loginPassword.setVisible(true);
            loginPassword.setManaged(true);
            loginPasswordVisible.setVisible(false);
            loginPasswordVisible.setManaged(false);
            loginPasswordToggle.setText("👁");
        }
        // Обновляем стиль после изменения текста
        String baseStyle = "-fx-background-color: transparent; -fx-background-radius: 3; -fx-font-size: 16px; -fx-padding: 0; -fx-cursor: hand; -fx-text-fill: #666; -fx-border-color: transparent;";
        loginPasswordToggle.setStyle(baseStyle);
    }

    @FXML
    private void toggleRegisterPasswordVisibility() {
        registerPasswordVisibleState = !registerPasswordVisibleState;
        if (registerPasswordVisibleState) {
            registerPasswordVisible.setText(registerPassword.getText());
            registerPasswordVisible.setVisible(true);
            registerPasswordVisible.setManaged(true);
            registerPassword.setVisible(false);
            registerPassword.setManaged(false);
            registerPasswordToggle.setText("🙈");
        } else {
            registerPassword.setText(registerPasswordVisible.getText());
            registerPassword.setVisible(true);
            registerPassword.setManaged(true);
            registerPasswordVisible.setVisible(false);
            registerPasswordVisible.setManaged(false);
            registerPasswordToggle.setText("👁");
        }
        // Обновляем стиль после изменения текста
        String baseStyle = "-fx-background-color: transparent; -fx-background-radius: 3; -fx-font-size: 16px; -fx-padding: 0; -fx-cursor: hand; -fx-text-fill: #666; -fx-border-color: transparent;";
        registerPasswordToggle.setStyle(baseStyle);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}