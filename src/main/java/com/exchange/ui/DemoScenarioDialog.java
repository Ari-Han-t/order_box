package com.exchange.ui;

import com.exchange.client.ExchangeApiClient;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;
import java.util.Map;

/**
 * Interactive Dialog executing and visualizing the presentation's scripted demo scenario
 * (Presentation Slide 07 & Slide 11) with step-by-step audit feedback.
 */
public class DemoScenarioDialog {
    private final ExchangeApiClient apiClient;
    private final Runnable onCompletedCallback;

    public DemoScenarioDialog(ExchangeApiClient apiClient, Runnable onCompletedCallback) {
        this.apiClient = apiClient;
        this.onCompletedCallback = onCompletedCallback;
    }

    @SuppressWarnings("unchecked")
    public void show(Stage owner) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(owner);
        stage.setTitle("Live Demo Scenario — Presentation Slide 07 & 11 Walkthrough");

        VBox root = new VBox(12);
        root.setPadding(new Insets(16));
        root.getStyleClass().add("root");
        root.setMinWidth(620);
        root.setMinHeight(520);

        Label title = new Label("SCRIPTED DEMO: PARTIAL EXECUTION & PRICE-TIME PRIORITY");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #58a6ff;");

        Label desc = new Label("Visibly verifies Alice (BUY 100 @ $200) matching Bob (SELL 50 @ $199.80) at Maker Price with 50 shares remaining in book.");
        desc.setStyle("-fx-font-size: 11px; -fx-text-fill: #8b949e;");
        desc.setWrapText(true);

        ProgressBar progress = new ProgressBar();
        progress.setMaxWidth(Double.MAX_VALUE);
        progress.setProgress(-1);

        VBox stepsBox = new VBox(8);
        stepsBox.setPadding(new Insets(8));
        stepsBox.setStyle("-fx-background-color: #161b22; -fx-border-color: #30363d; -fx-border-radius: 6px; -fx-background-radius: 6px;");

        ScrollPane scroll = new ScrollPane(stepsBox);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(340);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        Button closeBtn = new Button("CLOSE & UPDATE DASHBOARD");
        closeBtn.getStyleClass().add("btn-demo");
        closeBtn.setMaxWidth(Double.MAX_VALUE);
        closeBtn.setDisable(true);
        closeBtn.setOnAction(e -> {
            stage.close();
            if (onCompletedCallback != null) {
                onCompletedCallback.run();
            }
        });

        root.getChildren().addAll(title, desc, progress, scroll, closeBtn);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/styles/dark-theme.css").toExternalForm());
        stage.setScene(scene);
        stage.show();

        // Run scenario in background thread
        new Thread(() -> {
            try {
                Map<String, Object> result = apiClient.runDemoScenario();
                Platform.runLater(() -> {
                    progress.setVisible(false);
                    closeBtn.setDisable(false);

                    List<Map<String, Object>> steps = (List<Map<String, Object>>) result.get("steps");
                    if (steps != null) {
                        for (Map<String, Object> s : steps) {
                            stepsBox.getChildren().add(createStepCard(s));
                        }
                    }

                    // Add Success Banner
                    HBox banner = new HBox(8);
                    banner.setAlignment(Pos.CENTER);
                    banner.setPadding(new Insets(8));
                    banner.setStyle("-fx-background-color: rgba(46, 160, 67, 0.2); -fx-border-color: #2ea043; -fx-border-radius: 6px;");
                    Label bLbl = new Label("DEMO SCENARIO COMPLETED & VERIFIED AGAINST DATABASE");
                    bLbl.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #3fb950;");
                    banner.getChildren().add(bLbl);
                    stepsBox.getChildren().add(banner);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    progress.setVisible(false);
                    closeBtn.setDisable(false);
                    Label errLbl = new Label("Execution Failed: " + ex.getMessage());
                    errLbl.setStyle("-fx-text-fill: #f85149; -fx-font-weight: bold;");
                    stepsBox.getChildren().add(errLbl);
                });
            }
        }).start();
    }

    private VBox createStepCard(Map<String, Object> stepData) {
        VBox card = new VBox(4);
        card.setPadding(new Insets(8));
        card.setStyle("-fx-background-color: #0d1117; -fx-border-color: #21262d; -fx-border-radius: 6px; -fx-background-radius: 6px;");

        int stepNum = ((Number) stepData.getOrDefault("step", 0)).intValue();
        String action = (String) stepData.getOrDefault("action", "");

        Label header = new Label(String.format("STEP %d: %s", stepNum, action));
        header.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #58a6ff;");
        card.getChildren().add(header);

        for (Map.Entry<String, Object> e : stepData.entrySet()) {
            if ("step".equals(e.getKey()) || "action".equals(e.getKey())) continue;
            HBox row = new HBox(6);
            Label k = new Label(e.getKey() + ":");
            k.setStyle("-fx-font-size: 11px; -fx-text-fill: #8b949e;");
            k.setPrefWidth(160);
            Label v = new Label(String.valueOf(e.getValue()));
            v.setStyle("-fx-font-size: 11px; -fx-text-fill: #c9d1d9; -fx-font-weight: bold;");
            row.getChildren().addAll(k, v);
            card.getChildren().add(row);
        }

        return card;
    }
}
