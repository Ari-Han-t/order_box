package com.exchange.ui;

import com.exchange.client.ExchangeApiClient;
import com.exchange.model.Order;
import com.exchange.model.OrderSide;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

import java.util.List;
import java.util.function.Consumer;

/**
 * Open Orders management component allowing traders to view active resting orders
 * and cancel them with a single click (Presentation Slide 10 / 12).
 */
public class OpenOrdersComponent extends VBox {
    private final ExchangeApiClient apiClient;
    private final Consumer<Void> onOrderCancelledCallback;
    private final VBox ordersList = new VBox(4);

    public OpenOrdersComponent(ExchangeApiClient apiClient, Consumer<Void> onOrderCancelledCallback) {
        this.apiClient = apiClient;
        this.onOrderCancelledCallback = onOrderCancelledCallback;

        setSpacing(8);
        getStyleClass().add("card-panel");
        setMinWidth(360);
        setPrefWidth(420);

        Label header = new Label("OPEN ORDERS & ACTIVE CANCELLATION");
        header.getStyleClass().add("panel-header");

        // Table Header
        HBox tblHeader = new HBox();
        tblHeader.setPadding(new Insets(2, 6, 2, 6));
        Label hSym = new Label("ORDER ID / SYM");
        Label hSide = new Label("SIDE");
        Label hPrice = new Label("PRICE");
        Label hQty = new Label("REMAINING");
        Label hAction = new Label("ACTION");

        hSym.setStyle("-fx-font-size: 10px; -fx-text-fill: #8b949e; -fx-font-weight: bold;");
        hSide.setStyle("-fx-font-size: 10px; -fx-text-fill: #8b949e; -fx-font-weight: bold;");
        hPrice.setStyle("-fx-font-size: 10px; -fx-text-fill: #8b949e; -fx-font-weight: bold;");
        hQty.setStyle("-fx-font-size: 10px; -fx-text-fill: #8b949e; -fx-font-weight: bold;");
        hAction.setStyle("-fx-font-size: 10px; -fx-text-fill: #8b949e; -fx-font-weight: bold;");

        Region r1 = new Region();
        Region r2 = new Region();
        Region r3 = new Region();
        Region r4 = new Region();
        HBox.setHgrow(r1, Priority.ALWAYS);
        HBox.setHgrow(r2, Priority.ALWAYS);
        HBox.setHgrow(r3, Priority.ALWAYS);
        HBox.setHgrow(r4, Priority.ALWAYS);
        tblHeader.getChildren().addAll(hSym, r1, hSide, r2, hPrice, r3, hQty, r4, hAction);

        ScrollPane scroll = new ScrollPane(ordersList);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(150);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        getChildren().addAll(header, tblHeader, scroll);
    }

    public void update(List<Order> orders) {
        ordersList.getChildren().clear();
        if (orders == null || orders.isEmpty()) {
            Label emptyLbl = new Label("No active open orders.");
            emptyLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #8b949e; -fx-padding: 10px;");
            ordersList.getChildren().add(emptyLbl);
            return;
        }

        for (Order o : orders) {
            HBox row = new HBox();
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(3, 6, 3, 6));

            VBox idSymBox = new VBox(1);
            Label symLbl = new Label(o.getSymbol());
            symLbl.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #58a6ff;");
            Label idLbl = new Label(o.getOrderId());
            idLbl.setStyle("-fx-font-size: 9px; -fx-text-fill: #8b949e;");
            idSymBox.getChildren().addAll(symLbl, idLbl);
            idSymBox.setPrefWidth(90);

            Region r1 = new Region();
            HBox.setHgrow(r1, Priority.ALWAYS);

            Label sideLbl = new Label(o.getSide().name());
            sideLbl.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;");
            sideLbl.getStyleClass().add(o.getSide() == OrderSide.BUY ? "bid-green" : "ask-red");
            sideLbl.setPrefWidth(45);

            Region r2 = new Region();
            HBox.setHgrow(r2, Priority.ALWAYS);

            Label priceLbl = new Label(String.format("$%.2f", o.getPrice()));
            priceLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #c9d1d9;");
            priceLbl.setPrefWidth(60);

            Region r3 = new Region();
            HBox.setHgrow(r3, Priority.ALWAYS);

            Label qtyLbl = new Label(String.format("%d / %d", o.getRemainingQuantity(), o.getQuantity()));
            qtyLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #c9d1d9;");
            qtyLbl.setPrefWidth(65);

            Region r4 = new Region();
            HBox.setHgrow(r4, Priority.ALWAYS);

            Button cancelBtn = new Button("CANCEL");
            cancelBtn.getStyleClass().add("btn-cancel");
            cancelBtn.setOnAction(e -> handleCancel(o.getOrderId()));

            row.getChildren().addAll(idSymBox, r1, sideLbl, r2, priceLbl, r3, qtyLbl, r4, cancelBtn);
            ordersList.getChildren().add(row);
        }
    }

    private void handleCancel(String orderId) {
        new Thread(() -> {
            try {
                apiClient.cancelOrder(orderId);
                Platform.runLater(() -> {
                    if (onOrderCancelledCallback != null) {
                        onOrderCancelledCallback.accept(null);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to cancel order: " + e.getMessage());
                    alert.showAndWait();
                });
            }
        }).start();
    }
}
