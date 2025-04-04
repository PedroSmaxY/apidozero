package com.pedrosmaxy.apidozero.controllers;

import com.pedrosmaxy.apidozero.Main;
import com.pedrosmaxy.apidozero.dao.UserDAO;
import com.pedrosmaxy.apidozero.entities.User;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserController implements HttpHandler {

    private static final Pattern USER_ID_PATTERN = Pattern.compile("/api/user/(\\d+)");
    private static final Pattern NAME_PATTERN = Pattern.compile("\"name\"\\s*:\\s*\"([^\"]*)\"");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("\"email\"\\s*:\\s*\"([^\"]*)\"");
    private final UserDAO userDAO;

    public UserController(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        Main.log(method + " " + path + " - " + exchange.getRemoteAddress().toString());

        try {
            if ("/api/user".equals(path)) {
                switch (method) {
                    case "GET" -> handleGetAllUsers(exchange);
                    case "POST" -> handleCreateUser(exchange);
                    case "OPTIONS" -> handleOptionsRequest(exchange);
                    default -> handleMethodNotAllowed(exchange);
                }
                return;
            }

            Matcher matcher = USER_ID_PATTERN.matcher(path);
            if (matcher.matches()) {
                int userId = Integer.parseInt(matcher.group(1));

                switch (method) {
                    case "GET" -> handleGetUserById(exchange, userId);
                    case "PUT" -> handleUpdateUser(exchange, userId);
                    case "DELETE" -> handleDeleteUser(exchange, userId);
                    case "OPTIONS" -> handleOptionsRequest(exchange);
                    default -> handleMethodNotAllowed(exchange);
                }
                return;
            }

            sendJsonResponse(exchange, 404, "{\"error\": \"Endpoint not found\"}");
        } catch (Exception e) {
            Main.log("Error during processing: " + e.getMessage());
            sendJsonResponse(exchange, 500, "{\"error\": \"Internal server error\"}");
        } finally {
            exchange.close();
        }
    }

    private void handleGetAllUsers(HttpExchange exchange) throws IOException {
        Main.log("Fetching all users");
        List<User> users = userDAO.getAll();
        String response = userListToJson(users);
        sendJsonResponse(exchange, 200, response);
    }

    private void handleGetUserById(HttpExchange exchange, int id) throws IOException {
        Main.log("Fetching user with ID: " + id);
        var user = userDAO.findById(id);

        if (user.isEmpty()) {
            sendJsonResponse(exchange, 404, "{\"error\": \"User not found\"}");
            return;
        }

        sendJsonResponse(exchange, 200, userToJson(user.get()));
    }

    private void handleCreateUser(HttpExchange exchange) throws IOException {
        Main.log("Creating new user");
        try {
            String requestBody = readRequestBody(exchange.getRequestBody());
            Main.log("Request body: " + requestBody);

            var newUser = parseUserJson(requestBody);

            if (newUser.getName() == null || newUser.getEmail() == null) {
                sendJsonResponse(exchange, 400, "{\"error\": \"Name and email are required\"}");
                return;
            }

            User createdUser = userDAO.create(newUser);
            Main.log("User created with ID: " + createdUser.getId());
            sendJsonResponse(exchange, 201, userToJson(createdUser));
        } catch (RuntimeException e) {
            Main.log("Error creating user: " + e.getMessage());
            sendJsonResponse(exchange, 400, "{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    private void handleUpdateUser(HttpExchange exchange, int id) throws IOException {
        Main.log("Updating user with ID: " + id);
        try {
            var existingUser = userDAO.findById(id);
            if (existingUser.isEmpty()) {
                sendJsonResponse(exchange, 404, "{\"error\": \"User not found\"}");
                return;
            }

            String requestBody = readRequestBody(exchange.getRequestBody());
            var updatedUser = parseUserJson(requestBody);
            updatedUser.setId(id);

            if (updatedUser.getName() == null || updatedUser.getEmail() == null) {
                sendJsonResponse(exchange, 400, "{\"error\": \"Name and email are required\"}");
                return;
            }

            userDAO.change(updatedUser, id);
            sendJsonResponse(exchange, 200, userToJson(updatedUser));
        } catch (RuntimeException e) {
            sendJsonResponse(exchange, 400, "{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    private void handleDeleteUser(HttpExchange exchange, int id) throws IOException {
        Main.log("Deleting user with ID: " + id);
        var user = userDAO.findById(id);

        if (user.isEmpty()) {
            sendJsonResponse(exchange, 404, "{\"error\": \"User not found\"}");
            return;
        }

        userDAO.delete(id);
        sendJsonResponse(exchange, 200, "{\"message\": \"User successfully deleted\"}");
    }

    private void handleOptionsRequest(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
        sendJsonResponse(exchange, 204, "");
    }

    private void handleMethodNotAllowed(HttpExchange exchange) throws IOException {
        sendJsonResponse(exchange, 405, "{\"error\": \"Method not allowed\"}");
    }

    private User parseUserJson(String json) {
        User user = new User();

        Matcher nameMatcher = NAME_PATTERN.matcher(json);
        if (nameMatcher.find()) {
            user.setName(nameMatcher.group(1));
        }

        Matcher emailMatcher = EMAIL_PATTERN.matcher(json);
        if (emailMatcher.find()) {
            user.setEmail(emailMatcher.group(1));
        }

        return user;
    }

    private String readRequestBody(InputStream inputStream) {
        try (Scanner s = new Scanner(inputStream).useDelimiter("\\A")) {
            return s.hasNext() ? s.next() : "";
        }
    }

    private String userToJson(User user) {
        return "{\"id\": " + user.getId() +
                ", \"name\": \"" + user.getName() + "\"" +
                ", \"email\": \"" + user.getEmail() + "\"}";
    }

    private String userListToJson(List<User> users) {
        var sb = new StringBuilder("[");
        for (var i = 0; i < users.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(userToJson(users.get(i)));
        }
        sb.append("]");
        return sb.toString();
    }

    private void sendJsonResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(statusCode, !response.isEmpty() ? response.getBytes().length : -1);

        if (!response.isEmpty()) {
            try (OutputStream output = exchange.getResponseBody()) {
                output.write(response.getBytes());
                output.flush();
            }
        }
    }
}