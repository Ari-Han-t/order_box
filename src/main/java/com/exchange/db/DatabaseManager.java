package com.exchange.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Manages JDBC connection pooling with HikariCP.
 * Features intelligent auto-detection: attempts production MySQL connection first,
 * with automatic, zero-config fallback to an embedded in-memory H2 database
 * configured in MySQL compatibility mode if MySQL is not running locally.
 */
public class DatabaseManager {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    private static volatile DatabaseManager instance;

    private final HikariDataSource dataSource;
    private final boolean isFallbackH2;

    private DatabaseManager() {
        HikariDataSource ds = null;
        boolean fallback = false;

        // Read environment variables or fallback to defaults
        String envDbUrl = System.getenv("DB_URL");
        String envDbUser = System.getenv("DB_USER");
        String envDbPassword = System.getenv("DB_PASSWORD");

        String mysqlUrl = envDbUrl != null ? envDbUrl :
                "jdbc:mysql://localhost:3306/stock_exchange?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        String mysqlUser = envDbUser != null ? envDbUser : "root";
        String mysqlPass = envDbPassword != null ? envDbPassword : "root";

        logger.info("Attempting connection to Primary Database (MySQL) at: {}", mysqlUrl);

        try {
            // Test if MySQL is reachable with a short timeout
            DriverManager.setLoginTimeout(3);
            try (Connection testConn = DriverManager.getConnection(mysqlUrl, mysqlUser, mysqlPass)) {
                logger.info("Successfully established connection to MySQL Database!");
            }

            HikariConfig config = new HikariConfig();
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
            config.setJdbcUrl(mysqlUrl);
            config.setUsername(mysqlUser);
            config.setPassword(mysqlPass);
            config.setMaximumPoolSize(15);
            config.setMinimumIdle(3);
            config.setConnectionTimeout(5000);
            config.setPoolName("ExchangeMySQLPool");
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

            ds = new HikariDataSource(config);
            fallback = false;
        } catch (Exception e) {
            logger.warn("Primary MySQL database unavailable ({}); falling back to Embedded In-Memory Engine (MySQL Mode)", e.getMessage());
            fallback = true;

            HikariConfig h2Config = new HikariConfig();
            h2Config.setDriverClassName("org.h2.Driver");
            h2Config.setJdbcUrl("jdbc:h2:mem:stock_exchange;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1");
            h2Config.setUsername("sa");
            h2Config.setPassword("");
            h2Config.setMaximumPoolSize(15);
            h2Config.setMinimumIdle(3);
            h2Config.setConnectionTimeout(5000);
            h2Config.setPoolName("ExchangeH2Pool");

            ds = new HikariDataSource(h2Config);
        }

        this.dataSource = ds;
        this.isFallbackH2 = fallback;

        // Auto-run schema migrations and initial seed data
        initializeDatabaseSchema();
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            synchronized (DatabaseManager.class) {
                if (instance == null) {
                    instance = new DatabaseManager();
                }
            }
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public boolean isFallbackH2() {
        return isFallbackH2;
    }

    public void initializeDatabaseSchema() {
        logger.info("Executing Schema Initializer and Data Seeders...");
        executeSqlScript("/schema.sql");
        executeSqlScript("/seed.sql");
        logger.info("Database Schema and Seed Data initialization completed successfully!");
    }

    private void executeSqlScript(String resourcePath) {
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is == null) {
                logger.warn("SQL script not found in classpath: {}", resourcePath);
                return;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                 Connection conn = getConnection();
                 Statement stmt = conn.createStatement()) {

                StringBuilder sqlBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (trimmed.isEmpty() || trimmed.startsWith("--") || trimmed.startsWith("//")) {
                        continue;
                    }
                    sqlBuilder.append(line).append("\n");
                    if (trimmed.endsWith(";")) {
                        String statementSql = sqlBuilder.toString().trim();
                        // Remove trailing semicolon
                        if (statementSql.endsWith(";")) {
                            statementSql = statementSql.substring(0, statementSql.length() - 1);
                        }
                        if (!statementSql.isEmpty()) {
                            try {
                                stmt.execute(statementSql);
                            } catch (SQLException ex) {
                                // Ignore duplicate key errors if already seeded
                                if (!ex.getMessage().toLowerCase().contains("duplicate") &&
                                    !ex.getMessage().toLowerCase().contains("already exists")) {
                                    logger.debug("SQL execution notice for script {}: {}", resourcePath, ex.getMessage());
                                }
                            }
                        }
                        sqlBuilder.setLength(0);
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("Failed to execute SQL script {}: {}", resourcePath, ex.getMessage(), ex);
        }
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
