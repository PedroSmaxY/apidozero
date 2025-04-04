package com.pedrosmaxy.apidozero.controllers;

import com.pedrosmaxy.apidozero.dao.UserDAO;
import com.pedrosmaxy.apidozero.entities.User;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class UserControllerTest {

    @Mock
    private UserDAO userDAO;

    @Mock
    private HttpExchange exchange;

    private UserController controller;
    private ByteArrayOutputStream responseStream;
    private Headers headers;

    @Before
    public void setUp() {
        try (AutoCloseable mocks = MockitoAnnotations.openMocks(this)) {
            controller = new UserController(userDAO);

            responseStream = new ByteArrayOutputStream();
            headers = new Headers();

            when(exchange.getResponseBody()).thenReturn(responseStream);
            when(exchange.getResponseHeaders()).thenReturn(headers);
            when(exchange.getRemoteAddress()).thenReturn(new InetSocketAddress("localhost", 8080));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void getAllUsersReturnsListOfUsers() throws IOException, URISyntaxException {
        when(exchange.getRequestMethod()).thenReturn("GET");
        when(exchange.getRequestURI()).thenReturn(new URI("/api/user"));

        List<User> users = Arrays.asList(
            createUser(1, "John Doe", "john@example.com"),
            createUser(2, "Jane Smith", "jane@example.com")
        );
        when(userDAO.getAll()).thenReturn(users);

        controller.handle(exchange);

        verify(exchange).sendResponseHeaders(eq(200), anyLong());
        String response = responseStream.toString();
        assertTrue(response.contains("John Doe"));
        assertTrue(response.contains("jane@example.com"));
    }

    @Test
    public void getUserByIdReturnsUser() throws IOException, URISyntaxException {
        when(exchange.getRequestMethod()).thenReturn("GET");
        when(exchange.getRequestURI()).thenReturn(new URI("/api/user/1"));

        User user = createUser(1, "John Doe", "john@example.com");
        when(userDAO.findById(1)).thenReturn(Optional.of(user));

        controller.handle(exchange);

        verify(exchange).sendResponseHeaders(eq(200), anyLong());
        String response = responseStream.toString();
        assertTrue(response.contains("John Doe"));
        assertTrue(response.contains("john@example.com"));
    }

    @Test
    public void getUserByIdReturns404WhenUserNotFound() throws IOException, URISyntaxException {
        when(exchange.getRequestMethod()).thenReturn("GET");
        when(exchange.getRequestURI()).thenReturn(new URI("/api/user/999"));

        when(userDAO.findById(999)).thenReturn(Optional.empty());

        controller.handle(exchange);

        verify(exchange).sendResponseHeaders(eq(404), anyLong());
        assertTrue(responseStream.toString().contains("User not found"));
    }

    @Test
    public void createUserSuccessfully() throws IOException, URISyntaxException {
        when(exchange.getRequestMethod()).thenReturn("POST");
        when(exchange.getRequestURI()).thenReturn(new URI("/api/user"));

        String requestBody = "{\"name\":\"New User\",\"email\":\"new@example.com\"}";
        when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));

        createUser(0, "New User", "new@example.com");
        User createdUser = createUser(3, "New User", "new@example.com");
        when(userDAO.create(any(User.class))).thenReturn(createdUser);

        controller.handle(exchange);

        verify(exchange).sendResponseHeaders(eq(201), anyLong());
        String response = responseStream.toString();
        assertTrue(response.contains("New User"));
        assertTrue(response.contains("new@example.com"));
    }

    @Test
    public void createUserReturns400WhenMissingRequiredFields() throws IOException, URISyntaxException {
        when(exchange.getRequestMethod()).thenReturn("POST");
        when(exchange.getRequestURI()).thenReturn(new URI("/api/user"));

        String requestBody = "{\"name\":\"New User\"}"; // missing email
        when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));

        controller.handle(exchange);

        verify(exchange).sendResponseHeaders(eq(400), anyLong());
        assertTrue(responseStream.toString().contains("Name and email are required"));
    }

    @Test
    public void updateUserSuccessfully() throws IOException, URISyntaxException {
        when(exchange.getRequestMethod()).thenReturn("PUT");
        when(exchange.getRequestURI()).thenReturn(new URI("/api/user/1"));

        User existingUser = createUser(1, "Old Name", "old@example.com");
        when(userDAO.findById(1)).thenReturn(Optional.of(existingUser));

        String requestBody = "{\"name\":\"Updated Name\",\"email\":\"updated@example.com\"}";
        when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));

        controller.handle(exchange);

        verify(userDAO).change(any(User.class), eq(1));
        verify(exchange).sendResponseHeaders(eq(200), anyLong());
        String response = responseStream.toString();
        assertTrue(response.contains("Updated Name"));
        assertTrue(response.contains("updated@example.com"));
    }

    @Test
    public void updateUserReturns404WhenUserNotFound() throws IOException, URISyntaxException {
        when(exchange.getRequestMethod()).thenReturn("PUT");
        when(exchange.getRequestURI()).thenReturn(new URI("/api/user/999"));

        when(userDAO.findById(999)).thenReturn(Optional.empty());

        String requestBody = "{\"name\":\"Updated Name\",\"email\":\"updated@example.com\"}";
        when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));

        controller.handle(exchange);

        verify(exchange).sendResponseHeaders(eq(404), anyLong());
        assertTrue(responseStream.toString().contains("User not found"));
    }

    @Test
    public void deleteUserSuccessfully() throws IOException, URISyntaxException {
        when(exchange.getRequestMethod()).thenReturn("DELETE");
        when(exchange.getRequestURI()).thenReturn(new URI("/api/user/1"));

        User existingUser = createUser(1, "User To Delete", "delete@example.com");
        when(userDAO.findById(1)).thenReturn(Optional.of(existingUser));

        controller.handle(exchange);

        verify(userDAO).delete(1);
        verify(exchange).sendResponseHeaders(eq(200), anyLong());
        assertTrue(responseStream.toString().contains("successfully deleted"));
    }

    @Test
    public void deleteUserReturns404WhenUserNotFound() throws IOException, URISyntaxException {
        when(exchange.getRequestMethod()).thenReturn("DELETE");
        when(exchange.getRequestURI()).thenReturn(new URI("/api/user/999"));

        when(userDAO.findById(999)).thenReturn(Optional.empty());

        controller.handle(exchange);

        verify(exchange).sendResponseHeaders(eq(404), anyLong());
        assertTrue(responseStream.toString().contains("User not found"));
    }

    @Test
    public void optionsRequestReturnsCorrectHeaders() throws IOException, URISyntaxException {
        when(exchange.getRequestMethod()).thenReturn("OPTIONS");
        when(exchange.getRequestURI()).thenReturn(new URI("/api/user"));

        controller.handle(exchange);

        verify(exchange).sendResponseHeaders(eq(204), eq(-1L));
        assertEquals("GET, POST, PUT, DELETE, OPTIONS", headers.getFirst("Access-Control-Allow-Methods"));
        assertEquals("Content-Type", headers.getFirst("Access-Control-Allow-Headers"));
    }

    @Test
    public void unsupportedMethodReturns405() throws IOException, URISyntaxException {
        when(exchange.getRequestMethod()).thenReturn("PATCH");
        when(exchange.getRequestURI()).thenReturn(new URI("/api/user"));

        controller.handle(exchange);

        verify(exchange).sendResponseHeaders(eq(405), anyLong());
        assertTrue(responseStream.toString().contains("Method not allowed"));
    }

    @Test
    public void invalidEndpointReturns404() throws IOException, URISyntaxException {
        when(exchange.getRequestMethod()).thenReturn("GET");
        when(exchange.getRequestURI()).thenReturn(new URI("/api/invalid"));

        controller.handle(exchange);

        verify(exchange).sendResponseHeaders(eq(404), anyLong());
        assertTrue(responseStream.toString().contains("Endpoint not found"));
    }

    private User createUser(int id, String name, String email) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        return user;
    }
}