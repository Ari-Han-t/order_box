package com.exchange.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

import java.util.List;
import java.util.Map;

/**
 * Portfolio Dashboard component displaying cash balances, total net worth,
 * position valuations, and unrealized profit/loss (Presentation Slide 10 / 12).
 */
public class PortfolioComponent extends VBox {
    private final Label cashBalanceLbl = new Label("$0.00");
    private final Label holdingsValueLbl = new Label("$0.00");
    private final Label netWorthLbl = new Label("$0.00");
    private final Label pnlLbl = new Label("+$0.00 (0.0%)");
    private final VBox holdingsList = new VBox(2);

    public PortfolioComponent() {
        setSpacing(10);
        getStyleClass().add("card-panel");
        setMinWidth(360);
        setPrefWidth(420);

        Label header = new Label("PORTFOLIO & ASSET SUMMARY");
        header.getStyleClass().add("panel-header");

        // Stat Cards Grid
        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(8);
        statsGrid.setVgap(8);

        statsGrid.add(createStatCard("CASH BALANCE", cashBalanceLbl, "#e6edf3"), 0, 0);
        statsGrid.add(createStatCard("HOLDINGS VALUE", holdingsValueLbl, "#58a6ff"), 1, 0);
        statsGrid.add(createStatCard("TOTAL NET WORTH", netWorthLbl, "#e6edf3"), 0, 1);
        statsGrid.add(createStatCard("UNREALIZED P&L", pnlLbl, "#3fb950"), 1, 1);

        ColumnConstraints cc1 = new ColumnConstraints();
        cc1.setPercentWidth(50);
        ColumnConstraints cc2 = new ColumnConstraints();
        cc2.setPercentWidth(50);
        statsGrid.getColumnConstraints().addAll(cc1, cc2);

        // Holdings Table Header
        Label holdingsTitle = new Label("ACTIVE POSITIONS");
        holdingsTitle.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #8b949e;");

        HBox tblHeader = new HBox();
        tblHeader.setPadding(new Insets(2, 6, 2, 6));
        Label hSym = new Label("SYMBOL");
        Label hQty = new Label("SHARES");
        Label hAvg = new Label("AVG BUY");
        Label hVal = new Label("VALUE");
        Label hPnl = new Label("P&L");

        hSym.setStyle("-fx-font-size: 10px; -fx-text-fill: #8b949e; -fx-font-weight: bold;");
        hQty.setStyle("-fx-font-size: 10px; -fx-text-fill: #8b949e; -fx-font-weight: bold;");
        hAvg.setStyle("-fx-font-size: 10px; -fx-text-fill: #8b949e; -fx-font-weight: bold;");
        hVal.setStyle("-fx-font-size: 10px; -fx-text-fill: #8b949e; -fx-font-weight: bold;");
        hPnl.setStyle("-fx-font-size: 10px; -fx-text-fill: #8b949e; -fx-font-weight: bold;");

        Region r1 = new Region();
        Region r2 = new Region();
        Region r3 = new Region();
        Region r4 = new Region();
        HBox.setHgrow(r1, Priority.ALWAYS);
        HBox.setHgrow(r2, Priority.ALWAYS);
        HBox.setHgrow(r3, Priority.ALWAYS);
        HBox.setHgrow(r4, Priority.ALWAYS);
        tblHeader.getChildren().addAll(hSym, r1, hQty, r2, hAvg, r3, hVal, r4, hPnl);

        ScrollPane scroll = new ScrollPane(holdingsList);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(180);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        getChildren().addAll(header, statsGrid, holdingsTitle, tblHeader, scroll);
    }

    private VBox createStatCard(String title, Label valueLabel, String colorHex) {
        VBox card = new VBox(3);
        card.setStyle("-fx-background-color: #0d1117; -fx-padding: 8px; -fx-background-radius: 6px; -fx-border-color: #21262d; -fx-border-radius: 6px;");

        Label t = new Label(title);
        t.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #8b949e;");

        valueLabel.setStyle(String.format("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: %s;", colorHex));
        card.getChildren().addAll(t, valueLabel);
        return card;
    }

    @SuppressWarnings("unchecked")
    public void update(Map<String, Object> portfolioSummary) {
        if (portfolioSummary == null) return;

        double cash = ((Number) portfolioSummary.getOrDefault("cashBalance", 0.0)).doubleValue();
        double holdingsVal = ((Number) portfolioSummary.getOrDefault("holdingsMarketValue", 0.0)).doubleValue();
        double netWorth = ((Number) portfolioSummary.getOrDefault("totalNetWorth", 0.0)).doubleValue();
        double pnl = ((Number) portfolioSummary.getOrDefault("totalPnL", 0.0)).doubleValue();

        cashBalanceLbl.setText(String.format("$%,.2f", cash));
        holdingsValueLbl.setText(String.format("$%,.2f", holdingsVal));
        netWorthLbl.setText(String.format("$%,.2f", netWorth));

        if (pnl >= 0) {
            pnlLbl.setText(String.format("+$%,.2f", pnl));
            pnlLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #3fb950;");
        } else {
            pnlLbl.setText(String.format("-$%,.2f", Math.abs(pnl)));
            pnlLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #f85149;");
        }

        holdingsList.getChildren().clear();
        List<Map<String, Object>> holdings = (List<Map<String, Object>>) portfolioSummary.get("holdings");
        if (holdings != null) {
            for (Map<String, Object> h : holdings) {
                String sym = (String) h.get("symbol");
                int qty = ((Number) h.get("quantity")).intValue();
                double avg = ((Number) h.get("averageBuyPrice")).doubleValue();
                double mVal = ((Number) h.get("marketValue")).doubleValue();
                double uPnl = ((Number) h.get("unrealizedPnL")).doubleValue();

                HBox row = new HBox();
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(3, 6, 3, 6));

                Label sLbl = new Label(sym);
                sLbl.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #58a6ff;");
                sLbl.setPrefWidth(55);

                Region r1 = new Region();
                HBox.setHgrow(r1, Priority.ALWAYS);

                Label qLbl = new Label(String.format("%,d", qty));
                qLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #c9d1d9;");
                qLbl.setPrefWidth(50);
                qLbl.setAlignment(Pos.CENTER_RIGHT);

                Region r2 = new Region();
                HBox.setHgrow(r2, Priority.ALWAYS);

                Label aLbl = new Label(String.format("$%.2f", avg));
                aLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #8b949e;");
                aLbl.setPrefWidth(60);
                aLbl.setAlignment(Pos.CENTER_RIGHT);

                Region r3 = new Region();
                HBox.setHgrow(r3, Priority.ALWAYS);

                Label vLbl = new Label(String.format("$%,.2f", mVal));
                vLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #c9d1d9; -fx-font-weight: bold;");
                vLbl.setPrefWidth(70);
                vLbl.setAlignment(Pos.CENTER_RIGHT);

                Region r4 = new Region();
                HBox.setHgrow(r4, Priority.ALWAYS);

                Label pLbl = new Label(String.format("%s$%,.2f", uPnl >= 0 ? "+" : "-", Math.abs(uPnl)));
                pLbl.setStyle(String.format("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: %s;", uPnl >= 0 ? "#3fb950" : "#f85149"));
                pLbl.setPrefWidth(70);
                pLbl.setAlignment(Pos.CENTER_RIGHT);

                row.getChildren().addAll(sLbl, r1, qLbl, r2, aLbl, r3, vLbl, r4, pLbl);
                holdingsList.getChildren().add(row);
            }
        }
    }
}
