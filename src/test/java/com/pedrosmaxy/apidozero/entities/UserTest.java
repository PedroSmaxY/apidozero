package com.pedrosmaxy.apidozero.entities;

import org.junit.Test;

import static org.junit.Assert.*;

public class UserTest {

    @Test
    public void userConstructorSetsFieldsCorrectly() {
        User user = new User("John Doe", "john@example.com");
        assertNull(user.getId());
        assertEquals("John Doe", user.getName());
        assertEquals("john@example.com", user.getEmail());
    }

    @Test
    public void userSettersAndGettersWorkCorrectly() {
        User user = new User();
        user.setId(1);
        user.setName("Jane Doe");
        user.setEmail("jane@example.com");

        assertEquals(Integer.valueOf(1), user.getId());
        assertEquals("Jane Doe", user.getName());
        assertEquals("jane@example.com", user.getEmail());
    }

    @Test
    public void userEqualsReturnsTrueForSameNameAndEmail() {
        User user1 = new User("John Doe", "john@example.com");
        User user2 = new User("John Doe", "john@example.com");

        assertEquals(user1, user2);
    }

    @Test
    public void userEqualsReturnsFalseForDifferentNameOrEmail() {
        User user1 = new User("John Doe", "john@example.com");
        User user2 = new User("Jane Doe", "jane@example.com");

        assertNotEquals(user1, user2);
    }

    @Test
    public void userHashCodeIsConsistentWithEquals() {
        User user1 = new User("John Doe", "john@example.com");
        User user2 = new User("John Doe", "john@example.com");

        assertEquals(user1.hashCode(), user2.hashCode());
    }

    @Test
    public void userToStringReturnsCorrectFormat() {
        User user = new User("John Doe", "john@example.com");
        user.setId(1);

        String expected = "User{id=1, name='John Doe', email='john@example.com'}";
        assertEquals(expected, user.toString());
    }
}