package com.exchange.ui;

import com.exchange.model.Trade;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Live Market Trades Feed component (Tape).
 */
public class TradesComponent extends VBox {
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    private final VBox tradesList = new VBox(2);
    private double lastSeenPrice = 0.0;

    public TradesComponent() {
        setSpacing(6);
        getStyleClass().add("card-panel");
        setMinWidth(260);
        setPrefWidth(280);

        Label header = new Label("RECENT TRADES");
        header.getStyleClass().add("panel-header");

        // Column header
        HBox colHeader = new HBox();
        colHeader.setPadding(new Insets(2, 6, 2, 6));
        Label colTime = new Label("TIME");
        Label colPrice = new Label("PRICE");
        Label colSize = new Label("QTY");

        colTime.setStyle("-fx-font-size: 10px; -fx-text-fill: #8b949e; -fx-font-weight: bold;");
        colPrice.setStyle("-fx-font-size: 10px; -fx-text-fill: #8b949e; -fx-font-weight: bold;");
        colSize.setStyle("-fx-font-size: 10px; -fx-text-fill: #8b949e; -fx-font-weight: bold;");

        Region r1 = new Region();
        Region r2 = new Region();
        HBox.setHgrow(r1, Priority.ALWAYS);
        HBox.setHgrow(r2, Priority.ALWAYS);
        colHeader.getChildren().addAll(colTime, r1, colPrice, r2, colSize);

        ScrollPane scroll = new ScrollPane(tradesList);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(200);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        getChildren().addAll(header, colHeader, scroll);
    }

    public void update(List<Trade> trades) {
        if (trades == null) return;
        tradesList.getChildren().clear();

        for (Trade t : trades) {
            HBox row = new HBox();
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(3, 6, 3, 6));

            String timeStr = TIME_FMT.format(Instant.ofEpochMilli(t.getExecutedAtEpoch()));
            Label timeLbl = new Label(timeStr);
            timeLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #8b949e;");
            timeLbl.setPrefWidth(60);

            Region r1 = new Region();
            HBox.setHgrow(r1, Priority.ALWAYS);

            Label priceLbl = new Label(String.format("$%.2f", t.getPrice()));
            priceLbl.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;");
            if (t.getPrice() >= lastSeenPrice) {
                priceLbl.getStyleClass().add("bid-green");
            } else {
                priceLbl.getStyleClass().add("ask-red");
            }
            priceLbl.setPrefWidth(70);
            priceLbl.setAlignment(Pos.CENTER_RIGHT);

            Region r2 = new Region();
            HBox.setHgrow(r2, Priority.ALWAYS);

            Label qtyLbl = new Label(String.format("%,d", t.getQuantity()));
            qtyLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #c9d1d9;");
            qtyLbl.setPrefWidth(50);
            qtyLbl.setAlignment(Pos.CENTER_RIGHT);

            row.getChildren().addAll(timeLbl, r1, priceLbl, r2, qtyLbl);
            tradesList.getChildren().add(row);
        }

        if (!trades.isEmpty()) {
            lastSeenPrice = trades.get(0).getPrice();
        }
    }
}
