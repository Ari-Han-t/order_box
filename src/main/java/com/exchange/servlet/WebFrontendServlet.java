package com.exchange.servlet;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Serves the full-stack Web Trading Dashboard assets directly from the embedded server.
 * Enables zero-config browser access at the root URL (e.g. http://localhost:8085/ or cloud URL).
 */
public class WebFrontendServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getRequestURI();
        if (req.getContextPath() != null && !req.getContextPath().isEmpty() && path.startsWith(req.getContextPath())) {
            path = path.substring(req.getContextPath().length());
        }
        if (path == null || path.isEmpty() || path.equals("/")) {
            path = "/index.html";
        }

        // Clean path to prevent directory traversal
        if (path.contains("..")) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid path");
            return;
        }

        String resourcePath = "/web" + path;
        InputStream in = getClass().getResourceAsStream(resourcePath);

        // Fallback: if not found, serve index.html (SPA fallback)
        if (in == null) {
            if (path.endsWith(".css") || path.endsWith(".js") || path.endsWith(".png") || path.endsWith(".ico") || path.endsWith(".svg")) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Resource not found: " + path);
                return;
            }
            resourcePath = "/web/index.html";
            in = getClass().getResourceAsStream(resourcePath);
        }

        if (in == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Web frontend not found.");
            return;
        }

        // Determine MIME type
        String contentType = "text/plain";
        if (resourcePath.endsWith(".html")) {
            contentType = "text/html; charset=UTF-8";
        } else if (resourcePath.endsWith(".css")) {
            contentType = "text/css; charset=UTF-8";
        } else if (resourcePath.endsWith(".js")) {
            contentType = "application/javascript; charset=UTF-8";
        } else if (resourcePath.endsWith(".json")) {
            contentType = "application/json; charset=UTF-8";
        } else if (resourcePath.endsWith(".svg")) {
            contentType = "image/svg+xml";
        } else if (resourcePath.endsWith(".png")) {
            contentType = "image/png";
        } else if (resourcePath.endsWith(".ico")) {
            contentType = "image/x-icon";
        }

        resp.setContentType(contentType);
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");

        try (InputStream stream = in; OutputStream out = resp.getOutputStream()) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = stream.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            out.flush();
        }
    }
}
