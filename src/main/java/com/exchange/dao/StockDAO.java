package com.exchange.dao;

import com.exchange.db.DatabaseManager;
import com.exchange.model.Stock;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Traded Stocks and securities metadata.
 */
public class StockDAO {
    private final DatabaseManager dbManager;

    public StockDAO(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public Stock findBySymbol(String symbol) throws SQLException {
        String sql = "SELECT symbol, company_name, current_price, previous_close, total_volume FROM stocks WHERE symbol = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, symbol.toUpperCase().trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToStock(rs);
                }
            }
        }
        return null;
    }

    public List<Stock> findAllStocks() throws SQLException {
        List<Stock> list = new ArrayList<>();
        String sql = "SELECT symbol, company_name, current_price, previous_close, total_volume FROM stocks ORDER BY symbol ASC";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRowToStock(rs));
            }
        }
        return list;
    }

    public void updatePriceAndVolume(Connection conn, String symbol, double newPrice, long deltaVolume) throws SQLException {
        String sql = "UPDATE stocks SET current_price = ?, total_volume = total_volume + ? WHERE symbol = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, newPrice);
            ps.setLong(2, deltaVolume);
            ps.setString(3, symbol.toUpperCase().trim());
            ps.executeUpdate();
        }
    }

    private Stock mapRowToStock(ResultSet rs) throws SQLException {
        return new Stock(
                rs.getString("symbol"),
                rs.getString("company_name"),
                rs.getDouble("current_price"),
                rs.getDouble("previous_close"),
                rs.getLong("total_volume")
        );
    }
}
