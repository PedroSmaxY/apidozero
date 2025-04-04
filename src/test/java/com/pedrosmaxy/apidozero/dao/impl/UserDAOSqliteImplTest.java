package com.pedrosmaxy.apidozero.dao.impl;

    import com.pedrosmaxy.apidozero.entities.User;
    import org.junit.Before;
    import org.junit.Test;

    import java.sql.Connection;
    import java.sql.DriverManager;
    import java.sql.SQLException;

    import static org.junit.Assert.*;

    public class UserDAOSqliteImplTest {

        private UserDAOSqliteImpl userDAO;
        // Use a named memory database with shared cache
        private static final String DB_URL = "jdbc:sqlite:file:memorydb?mode=memory&cache=shared";

        @Before
        public void setUp() {
            // Initialize schema first to avoid table not found errors
            try (Connection conn = DriverManager.getConnection(DB_URL)) {
                conn.createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL, " +
                    "email TEXT UNIQUE NOT NULL)");
            } catch (SQLException e) {
                fail("Failed to set up test database: " + e.getMessage());
            }

            // Use the same connection URL for DAO
            userDAO = new UserDAOSqliteImpl(DB_URL);
        }

        @Test
        public void deleteNonExistentUserThrowsException() {
            int nonExistentId = 9999;

            try {
                userDAO.delete(nonExistentId);
                fail("Expected RuntimeException was not thrown");
            } catch (RuntimeException e) {
                // Check exception type without checking exact message
                assertTrue(e.getMessage().contains("User not exists") ||
                           e.getMessage().contains("Error finding user"));
            }
        }

        @Test
        public void changeNonExistentUserThrowsException() {
            User user = new User("Non Existent", "nonexistent@example.com");
            user.setId(9999);

            try {
                userDAO.change(user, 9999);
                fail("Expected RuntimeException was not thrown");
            } catch (RuntimeException e) {
                // Check exception type without checking exact message
                assertTrue(e.getMessage().contains("User not exists") ||
                           e.getMessage().contains("Error finding user"));
            }
        }

        @Test
        public void userWithNullValuesCannotBeCreated() {
            User userWithNullName = new User(null, "email@example.com");
            User userWithNullEmail = new User("Name", null);

            try {
                userDAO.create(userWithNullName);
                fail("Expected exception for null name was not thrown");
            } catch (RuntimeException e) {
                // Just check that an exception was thrown
            }

            try {
                userDAO.create(userWithNullEmail);
                fail("Expected exception for null email was not thrown");
            } catch (RuntimeException e) {
                // Just check that an exception was thrown
            }
        }
    }