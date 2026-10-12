package com.exchange.servlet;

import com.exchange.service.DemoScenarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

/**
 * Servlet providing a one-click endpoint to execute the scripted demo scenario
 * (Presentation Slide 07 & Slide 11) end-to-end against the live backend.
 */
public class DemoScenarioServlet extends BaseServlet {
    private final DemoScenarioService demoScenarioService;

    public DemoScenarioServlet(DemoScenarioService demoScenarioService) {
        this.demoScenarioService = demoScenarioService;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handleDemoExecution(resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handleDemoExecution(resp);
    }

    private void handleDemoExecution(HttpServletResponse resp) throws IOException {
        try {
            Map<String, Object> result = demoScenarioService.runPresentationScenario();
            sendJson(resp, HttpServletResponse.SC_OK, result);
        } catch (Exception ex) {
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Demo execution failed: " + ex.getMessage());
        }
    }
}
