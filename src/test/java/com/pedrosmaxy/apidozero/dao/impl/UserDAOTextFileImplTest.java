// UserDAOTextFileImplTest.java
            package com.pedrosmaxy.apidozero.dao.impl;

            import com.pedrosmaxy.apidozero.entities.User;
            import com.pedrosmaxy.apidozero.dao.UserDAO;
            import org.junit.Before;
            import org.junit.Rule;
            import org.junit.Test;
            import org.junit.rules.TemporaryFolder;

            import java.io.File;
            import java.io.IOException;
            import java.util.List;
            import java.util.Optional;

            import static org.junit.Assert.*;

            public class UserDAOTextFileImplTest {
                @Rule
                public TemporaryFolder folder = new TemporaryFolder();

                private UserDAO userDAO;

                @Before
                public void setup() throws IOException {
                    File tempFile = folder.newFile("test_base.txt");
                    userDAO = new UserDAOTextFileImpl(tempFile.getAbsolutePath());
                }

                @Test
                public void getAllReturnsEmptyListWhenNoUsers() {
                    List<User> users = userDAO.getAll();
                    assertTrue("Expected empty list", users.isEmpty());
                }

                @Test
                public void getAllReturnsAllCreatedUsers() {
                    long timestamp = System.currentTimeMillis();
                    User user1 = userDAO.create(new User("User One", "user1" + timestamp + "@example.com"));
                    User user2 = userDAO.create(new User("User Two", "user2" + timestamp + "@example.com"));

                    List<User> users = userDAO.getAll();
                    assertEquals(2, users.size());
                    assertTrue(users.contains(user1));
                    assertTrue(users.contains(user2));
                }

                @Test
                public void findByIdReturnsUserWhenExists() {
                    long timestamp = System.currentTimeMillis();
                    User user = userDAO.create(new User("Test User", "test" + timestamp + "@example.com"));

                    Optional<User> retrieved = userDAO.findById(user.getId());
                    assertTrue("User should be found", retrieved.isPresent());
                    assertEquals(user.getId(), retrieved.get().getId());
                }

                @Test
                public void findByIdReturnsEmptyWhenUserDoesNotExist() {
                    Optional<User> retrieved = userDAO.findById(9999);
                    assertFalse("User should not be found", retrieved.isPresent());
                }

                @Test(expected = RuntimeException.class)
                public void createUserWithDuplicateEmailThrowsException() {
                    long timestamp = System.currentTimeMillis();
                    String email = "duplicate" + timestamp + "@example.com";
                    userDAO.create(new User("User One", email));
                    userDAO.create(new User("User Two", email));
                }

                @Test(expected = RuntimeException.class)
                public void deleteNonExistentUserThrowsException() {
                    userDAO.delete(9999);
                }

                @Test
                public void deleteUser() {
                    long timestamp = System.currentTimeMillis();
                    User user = userDAO.create(new User("Delete Test", "delete" + timestamp + "@example.com"));
                    userDAO.delete(user.getId());

                    Optional<User> retrieved = userDAO.findById(user.getId());
                    assertFalse("User should be deleted", retrieved.isPresent());
                }
            }