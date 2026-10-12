package com.exchange.ui;

import com.exchange.client.ExchangeApiClient;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Trader Authentication & Quick-Switcher Dialog.
 * Allows instant 1-click login into seeded profiles (Alice, Bob, Charlie, Market Maker)
 * or creating a custom trader account.
 */
public class LoginDialog {
    private final ExchangeApiClient apiClient;
    private final Consumer<Map<String, Object>> onLoginSuccess;

    public LoginDialog(ExchangeApiClient apiClient, Consumer<Map<String, Object>> onLoginSuccess) {
        this.apiClient = apiClient;
        this.onLoginSuccess = onLoginSuccess;
    }

    public void show(Stage owner) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(owner);
        stage.setTitle("Stock Exchange Login & Trader Selection");

        VBox root = new VBox(12);
        root.setPadding(new Insets(20));
        root.getStyleClass().add("root");
        root.setMinWidth(420);

        Label title = new Label("SELECT TRADER ACCOUNT");
        title.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #58a6ff;");

        Label desc = new Label("Choose a seeded demo trader or enter custom credentials:");
        desc.setStyle("-fx-font-size: 11px; -fx-text-fill: #8b949e;");

        // Quick trader selector buttons
        VBox quickBox = new VBox(6);
        quickBox.getChildren().addAll(
                createQuickTraderBtn("Alice (Buyer - $50,000 Cash, 150 AAPL)", "alice", "password123", stage),
                createQuickTraderBtn("Bob (Seller - $50,000 Cash, 200 AAPL)", "bob", "password123", stage),
                createQuickTraderBtn("Charlie (Trader - $75,000 Cash, GOOGL/NVDA)", "charlie", "password123", stage),
                createQuickTraderBtn("Market Maker ($1,000,000 Institutional Book)", "market_maker", "password123", stage)
        );

        Separator sep = new Separator();

        // Custom Login form
        Label customTitle = new Label("OR LOG IN WITH CUSTOM CREDENTIALS");
        customTitle.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #8b949e;");

        TextField userField = new TextField();
        userField.setPromptText("Username");
        PasswordField passField = new PasswordField();
        passField.setPromptText("Password");

        Label errLbl = new Label();
        errLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #f85149;");

        HBox btnBox = new HBox(8);
        Button loginBtn = new Button("LOG IN");
        loginBtn.getStyleClass().add("btn-buy");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(loginBtn, Priority.ALWAYS);

        Button regBtn = new Button("REGISTER NEW");
        regBtn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(regBtn, Priority.ALWAYS);

        loginBtn.setOnAction(e -> {
            String u = userField.getText().trim();
            String p = passField.getText().trim();
            if (u.isEmpty() || p.isEmpty()) {
                errLbl.setText("Enter username and password");
                return;
            }
            performLogin(u, p, errLbl, stage);
        });

        regBtn.setOnAction(e -> {
            String u = userField.getText().trim();
            String p = passField.getText().trim();
            if (u.isEmpty() || p.isEmpty()) {
                errLbl.setText("Enter username and password to register");
                return;
            }
            performRegister(u, p, errLbl, stage);
        });

        btnBox.getChildren().addAll(loginBtn, regBtn);

        root.getChildren().addAll(title, desc, quickBox, sep, customTitle, userField, passField, btnBox, errLbl);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/styles/dark-theme.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }

    private Button createQuickTraderBtn(String label, String username, String password, Stage stage) {
        Button btn = new Button(label);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setStyle("-fx-background-color: #161b22; -fx-border-color: #30363d; -fx-padding: 8px 12px; -fx-font-size: 12px;");
        btn.setOnAction(e -> {
            Label err = new Label();
            performLogin(username, password, err, stage);
        });
        return btn;
    }

    private void performLogin(String username, String password, Label errLbl, Stage stage) {
        new Thread(() -> {
            try {
                Map<String, Object> session = apiClient.login(username, password);
                Platform.runLater(() -> {
                    stage.close();
                    if (onLoginSuccess != null) {
                        onLoginSuccess.accept(session);
                    }
                });
            } catch (Exception ex) {
                Platform.runLater(() -> errLbl.setText("Login failed: " + ex.getMessage()));
            }
        }).start();
    }

    private void performRegister(String username, String password, Label errLbl, Stage stage) {
        new Thread(() -> {
            try {
                Map<String, Object> session = apiClient.register(username, password, username, 50000.0);
                Platform.runLater(() -> {
                    stage.close();
                    if (onLoginSuccess != null) {
                        onLoginSuccess.accept(session);
                    }
                });
            } catch (Exception ex) {
                Platform.runLater(() -> errLbl.setText("Registration failed: " + ex.getMessage()));
            }
        }).start();
    }
}
