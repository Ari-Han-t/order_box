package com.exchange.server;

import com.exchange.dao.*;
import com.exchange.db.DatabaseManager;
import com.exchange.engine.MatchingEngine;
import com.exchange.service.*;
import com.exchange.servlet.*;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Embedded Servlet Container hosting the Jakarta Servlets REST API layer
 * (Presentation Slide 06 / 12 & Slide 10 / 12).
 */
public class EmbeddedServletContainer {
    private static final Logger logger = LoggerFactory.getLogger(EmbeddedServletContainer.class);

    private final int port;
    private Server server;

    private final DatabaseManager dbManager;
    private final MatchingEngine matchingEngine;
    private final UserDAO userDAO;
    private final StockDAO stockDAO;
    private final OrderDAO orderDAO;
    private final TradeDAO tradeDAO;
    private final PortfolioDAO portfolioDAO;

    private final AuthService authService;
    private final PortfolioService portfolioService;
    private final TradingService tradingService;
    private final DemoScenarioService demoScenarioService;

    public EmbeddedServletContainer(int port) {
        this.port = port;

        this.dbManager = DatabaseManager.getInstance();
        this.matchingEngine = new MatchingEngine();
        this.userDAO = new UserDAO(dbManager);
        this.stockDAO = new StockDAO(dbManager);
        this.orderDAO = new OrderDAO(dbManager);
        this.tradeDAO = new TradeDAO(dbManager);
        this.portfolioDAO = new PortfolioDAO(dbManager);

        this.authService = new AuthService(userDAO);
        this.portfolioService = new PortfolioService(portfolioDAO, userDAO, stockDAO);
        this.tradingService = new TradingService(dbManager, matchingEngine, orderDAO, tradeDAO, userDAO, stockDAO, portfolioDAO);
        this.demoScenarioService = new DemoScenarioService(tradingService, userDAO, stockDAO, portfolioService);

        // Load active orders into memory matching engine
        this.tradingService.rehydrateOrderBooksFromDB();
    }

    public synchronized void start() throws Exception {
        server = new Server(port);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        // Servlets under /api/*
        context.addServlet(new ServletHolder(new AuthServlet(authService)), "/api/auth/*");
        context.addServlet(new ServletHolder(new OrderServlet(tradingService)), "/api/orders/*");
        context.addServlet(new ServletHolder(new OrderBookServlet(tradingService)), "/api/orderbook/*");
        context.addServlet(new ServletHolder(new TradeServlet(tradingService)), "/api/trades/*");
        context.addServlet(new ServletHolder(new PortfolioServlet(portfolioService)), "/api/portfolio/*");
        context.addServlet(new ServletHolder(new StockServlet(stockDAO)), "/api/stocks/*");
        context.addServlet(new ServletHolder(new DemoScenarioServlet(demoScenarioService)), "/api/demo/*");
        context.addServlet(new ServletHolder(new HealthServlet(dbManager, matchingEngine)), "/api/health/*");

        // Presentation Slide 10 Exact Endpoints Aliases
        context.addServlet(new ServletHolder(new AuthServlet(authService)), "/login");
        context.addServlet(new ServletHolder(new OrderServlet(tradingService)), "/orders/*");
        context.addServlet(new ServletHolder(new OrderBookServlet(tradingService)), "/orderbook");
        context.addServlet(new ServletHolder(new TradeServlet(tradingService)), "/trades");
        context.addServlet(new ServletHolder(new PortfolioServlet(portfolioService)), "/portfolio");

        // Web Frontend Dashboard at root / and static assets /css/*, /js/*
        context.addServlet(new ServletHolder(new WebFrontendServlet()), "/");
        context.addServlet(new ServletHolder(new WebFrontendServlet()), "/css/*");
        context.addServlet(new ServletHolder(new WebFrontendServlet()), "/js/*");

        server.setHandler(context);
        server.start();

        logger.info("=================================================================");
        logger.info(" Exchange REST API Server running on port {}", port);
        logger.info(" Endpoints active:");
        logger.info("   POST   /api/auth/login or /login");
        logger.info("   POST   /api/auth/register");
        logger.info("   POST   /api/orders or /orders");
        logger.info("   GET    /api/orders or /orders");
        logger.info("   DELETE /api/orders/{id}");
        logger.info("   GET    /api/orderbook?symbol=AAPL or /orderbook");
        logger.info("   GET    /api/trades?symbol=AAPL or /trades");
        logger.info("   GET    /api/portfolio or /portfolio");
        logger.info("   GET    /api/stocks");
        logger.info("   POST   /api/demo/run-scenario");
        logger.info("   GET    /api/health");
        logger.info("=================================================================");
    }

    public synchronized void stop() throws Exception {
        if (server != null) {
            server.stop();
        }
    }

    public int getPort() {
        return port;
    }

    public TradingService getTradingService() {
        return tradingService;
    }

    public AuthService getAuthService() {
        return authService;
    }

    public PortfolioService getPortfolioService() {
        return portfolioService;
    }

    public DemoScenarioService getDemoScenarioService() {
        return demoScenarioService;
    }
}
