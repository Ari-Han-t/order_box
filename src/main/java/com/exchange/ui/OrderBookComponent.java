package com.exchange.ui;

import com.exchange.model.OrderBookSnapshot;
import com.exchange.model.PriceLevel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * High-performance JavaFX component rendering the real-time Order Book depth ladder
 * with visual depth bars for Asks (crimson) and Bids (emerald) (Presentation Slide 06 / 12).
 */
public class OrderBookComponent extends VBox {
    private final VBox asksContainer = new VBox(2);
    private final VBox bidsContainer = new VBox(2);
    private final Label spreadLabel = new Label("SPREAD: -- | MID: --");

    public OrderBookComponent() {
        setSpacing(6);
        getStyleClass().add("card-panel");
        setMinWidth(280);
        setPrefWidth(320);

        Label header = new Label("LIVE ORDER BOOK");
        header.getStyleClass().add("panel-header");

        // Column Header
        HBox colHeader = new HBox();
        colHeader.setPadding(new Insets(4, 8, 4, 8));
        Label colPrice = new Label("PRICE ($)");
        Label colSize = new Label("SIZE");
        Label colTotal = new Label("TOTAL");
        colPrice.setStyle("-fx-font-size: 10px; -fx-text-fill: #8b949e; -fx-font-weight: bold;");
        colSize.setStyle("-fx-font-size: 10px; -fx-text-fill: #8b949e; -fx-font-weight: bold;");
        colTotal.setStyle("-fx-font-size: 10px; -fx-text-fill: #8b949e; -fx-font-weight: bold;");

        Region r1 = new Region();
        Region r2 = new Region();
        HBox.setHgrow(r1, Priority.ALWAYS);
        HBox.setHgrow(r2, Priority.ALWAYS);
        colHeader.getChildren().addAll(colPrice, r1, colSize, r2, colTotal);

        // Spread indicator banner
        HBox spreadBox = new HBox(spreadLabel);
        spreadBox.setAlignment(Pos.CENTER);
        spreadBox.setPadding(new Insets(4, 0, 4, 0));
        spreadLabel.getStyleClass().add("spread-badge");

        getChildren().addAll(header, colHeader, asksContainer, spreadBox, bidsContainer);
    }

    public void update(OrderBookSnapshot snapshot) {
        if (snapshot == null) return;

        // Calculate max volume for relative depth bars
        int maxQty = 1;
        for (PriceLevel pl : snapshot.getAsks()) maxQty = Math.max(maxQty, pl.getQuantity());
        for (PriceLevel pl : snapshot.getBids()) maxQty = Math.max(maxQty, pl.getQuantity());

        // Update Asks (render in reverse order so lowest ask is closest to spread at bottom)
        asksContainer.getChildren().clear();
        List<PriceLevel> asksReversed = new ArrayList<>(snapshot.getAsks());
        Collections.reverse(asksReversed);
        for (PriceLevel pl : asksReversed) {
            asksContainer.getChildren().add(createPriceLevelRow(pl, false, maxQty));
        }

        // Update Spread
        if (snapshot.getBestBid() > 0 && snapshot.getBestAsk() > 0) {
            double spread = snapshot.getSpread();
            double mid = (snapshot.getBestBid() + snapshot.getBestAsk()) / 2.0;
            spreadLabel.setText(String.format("SPREAD: $%.2f  |  MID: $%.2f", spread, mid));
        } else {
            spreadLabel.setText(String.format("LAST: $%.2f  |  SPREAD: --", snapshot.getLastPrice()));
        }

        // Update Bids (highest bid at top closest to spread)
        bidsContainer.getChildren().clear();
        for (PriceLevel pl : snapshot.getBids()) {
            bidsContainer.getChildren().add(createPriceLevelRow(pl, true, maxQty));
        }
    }

    private StackPane createPriceLevelRow(PriceLevel pl, boolean isBid, int maxQty) {
        StackPane row = new StackPane();
        row.setPrefHeight(22);

        // Visual depth bar background
        Region depthBar = new Region();
        double pct = Math.min(1.0, (double) pl.getQuantity() / maxQty);
        depthBar.setMaxWidth(Double.MAX_VALUE);
        depthBar.setPrefHeight(20);

        String barColor = isBid ? "rgba(46, 160, 67, 0.18)" : "rgba(218, 54, 51, 0.18)";
        depthBar.setStyle(String.format("-fx-background-color: %s; -fx-background-radius: 3px;", barColor));
        depthBar.scaleXProperty().setValue(pct);
        StackPane.setAlignment(depthBar, isBid ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        // Text content
        HBox content = new HBox();
        content.setAlignment(Pos.CENTER_LEFT);
        content.setPadding(new Insets(2, 8, 2, 8));

        Label priceLbl = new Label(String.format("%.2f", pl.getPrice()));
        priceLbl.getStyleClass().add(isBid ? "bid-green" : "ask-red");
        priceLbl.setPrefWidth(75);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label sizeLbl = new Label(String.format("%,d", pl.getQuantity()));
        sizeLbl.setStyle("-fx-text-fill: #c9d1d9; -fx-font-size: 11px;");
        sizeLbl.setPrefWidth(60);
        sizeLbl.setAlignment(Pos.CENTER_RIGHT);

        Label totalLbl = new Label(String.format("%,d", pl.getCumulativeQuantity()));
        totalLbl.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 11px;");
        totalLbl.setPrefWidth(60);
        totalLbl.setAlignment(Pos.CENTER_RIGHT);

        content.getChildren().addAll(priceLbl, spacer, sizeLbl, totalLbl);

        row.getChildren().addAll(depthBar, content);
        return row;
    }
}
