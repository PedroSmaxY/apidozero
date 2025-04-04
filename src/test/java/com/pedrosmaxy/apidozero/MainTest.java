package com.pedrosmaxy.apidozero;

import com.pedrosmaxy.apidozero.controllers.UserController;
import com.pedrosmaxy.apidozero.dao.impl.UserDAOSqliteImpl;
import com.sun.net.httpserver.HttpServer;
import org.junit.After;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.*;

import static org.junit.Assert.*;

public class MainTest {

    private HttpServer server;

    @Before
    public void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress(3000), 0);

        server.createContext("/api", exchange -> {
            if ("GET".equals(exchange.getRequestMethod())) {
                String response = "API is running";
                exchange.sendResponseHeaders(200, response.length());
                exchange.getResponseBody().write(response.getBytes());
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
            exchange.getResponseBody().close();
        });

        server.createContext("/api/user", new UserController(new UserDAOSqliteImpl()));
        server.setExecutor(null);
        server.start();
    }

    @After
    public void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    public void serverStartsSuccessfully() {
        assertNotNull(server);
        assertEquals(3000, server.getAddress().getPort());
    }

    @Test
    public void getRequestToApiReturns200() throws IOException, URISyntaxException {
        URL url = new URI("http://localhost:3000/api").toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(3000);
        int responseCode = connection.getResponseCode();
        assertEquals(200, responseCode);
    }

    @Test
    public void serverHandlesMultipleEndpoints() throws IOException, URISyntaxException {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        var url = new URI("http://localhost:3000/api/user").toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(3000);
        int responseCode = connection.getResponseCode();
        assertEquals(200, responseCode);
    }

    @Test
    public void serverHandlesMethodNotAllowed() throws IOException, URISyntaxException {
        URL url = new URI("http://localhost:3000/api").toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setConnectTimeout(3000);

        try {
            int responseCode = connection.getResponseCode();
            assertEquals(405, responseCode);
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("Server returned HTTP response code"));
        }
    }
}