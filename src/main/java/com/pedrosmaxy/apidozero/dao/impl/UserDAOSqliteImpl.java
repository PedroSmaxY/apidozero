package com.pedrosmaxy.apidozero.dao.impl;

import com.pedrosmaxy.apidozero.dao.UserDAO;
import com.pedrosmaxy.apidozero.entities.User;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDAOSqliteImpl implements UserDAO {

    private final String connectionUrl;

    public UserDAOSqliteImpl() {
        this("jdbc:sqlite:base.db");
    }

    public UserDAOSqliteImpl(String connectionUrl) {
        this.connectionUrl = connectionUrl;
        try (var connection = this.newConnection();
             var statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS users(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name VARCHAR(100), " +
                    "email VARCHAR(100) UNIQUE" +
                    ")");
        } catch (SQLException e) {
            throw new RuntimeException("Error creating table", e);
        }
    }

    private Connection newConnection() {
        try {
            return DriverManager.getConnection(this.connectionUrl);
        } catch (SQLException e) {
            throw new RuntimeException("Error connecting to the database", e);
        }
    }

    @Override
    public List<User> getAll() {
        List<User> users = new ArrayList<>();
        try (var connection = this.newConnection();
             var statement = connection.createStatement();
             var result = statement.executeQuery("SELECT * FROM users")) {

            while (result.next()) {
                var user = new User();
                user.setId(result.getInt("id"));
                user.setName(result.getString("name"));
                user.setEmail(result.getString("email"));
                users.add(user);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching users", e);
        }
        return users;
    }

    @Override
    public Optional<User> findById(int id) {
        try (var connection = this.newConnection();
             var statement = connection.prepareStatement("SELECT * FROM users WHERE id = ?")) {
            statement.setInt(1, id);
            try (var result = statement.executeQuery()) {
                if (result.next()) {
                    var user = new User();
                    user.setId(result.getInt("id"));
                    user.setName(result.getString("name"));
                    user.setEmail(result.getString("email"));
                    return Optional.of(user);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user by ID", e);
        }
        return Optional.empty();
    }

    @Override
    public User create(User newUser) {
        try (Connection connection = this.newConnection()) {
            connection.setAutoCommit(false);

            try (var statement = connection.prepareStatement(
                    "INSERT INTO users (name, email) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, newUser.getName());
                statement.setString(2, newUser.getEmail());
                statement.executeUpdate();

                try (var generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        newUser.setId(generatedKeys.getInt(1));
                    }
                }

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new RuntimeException("Error creating user", e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error in transaction", e);
        }
        return newUser;
    }

    @Override
    public Optional<User> change(User updateUser, int id) {
        if (this.findById(id).isEmpty()) {
            throw new RuntimeException("User not exists");
        }

        try (Connection connection = this.newConnection()) {
            connection.setAutoCommit(false);

            try (var statement = connection.prepareStatement("UPDATE users SET name = ?, email = ? WHERE id = ?")) {
                statement.setString(1, updateUser.getName());
                statement.setString(2, updateUser.getEmail());
                statement.setInt(3, id);
                statement.executeUpdate();

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new RuntimeException("Error updating user", e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error in transaction", e);
        }

        return this.findById(id);
    }

    @Override
    public void delete(int id) {
        if (this.findById(id).isEmpty()) {
            throw new RuntimeException("User not exists");
        }

        try (Connection connection = this.newConnection()) {
            connection.setAutoCommit(false);

            try (var statement = connection.prepareStatement("DELETE FROM users WHERE id = ?")) {
                statement.setInt(1, id);
                statement.executeUpdate();

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new RuntimeException("Error deleting user", e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error in transaction", e);
        }
    }
}