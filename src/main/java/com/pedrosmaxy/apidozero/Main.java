package com.pedrosmaxy.apidozero;

import com.pedrosmaxy.apidozero.controllers.UserController;
import com.pedrosmaxy.apidozero.dao.UserDAO;
import com.pedrosmaxy.apidozero.dao.impl.UserDAOSqliteImpl;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Main {

    private final static UserDAO userDAO = new UserDAOSqliteImpl();
    private static final int SERVER_PORT = 3000;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(SERVER_PORT), 0);
            server.createContext("/api", (exchange) -> {
                var method = exchange.getRequestMethod();
                var path = exchange.getRequestURI().getPath();

                Main.log(method + " " + path + " - " + exchange.getRemoteAddress().toString());


                if ("GET".equals(method)) {
                    String responseText = "Hello from API REST Vanilla!\n";
                    exchange.sendResponseHeaders(200, responseText.getBytes().length);
                    OutputStream output = exchange.getResponseBody();
                    output.write(responseText.getBytes());
                    output.flush();
                } else {
                    exchange.sendResponseHeaders(405, -1);
                }

                exchange.close();
            });

            server.createContext("/api/user", new UserController(userDAO));

            server.setExecutor(null);
            server.start();

            System.out.println("=================================================");
            System.out.println("Server started on port: " + SERVER_PORT);
            System.out.println("=================================================");

        } catch (IOException e) {
            System.err.println("Error to start server: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static void log(String message) {
        String timestamp = LocalDateTime.now().format(formatter);
        System.out.println("[" + timestamp + "] " + message);
    }
}