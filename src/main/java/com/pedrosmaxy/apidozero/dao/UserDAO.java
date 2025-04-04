package com.pedrosmaxy.apidozero.dao;

import com.pedrosmaxy.apidozero.entities.User;

import java.util.List;
import java.util.Optional;

public interface UserDAO {
    List<User> getAll();

    Optional<User> findById(int id);

    User create(User newUser);

    Optional<User> change(User updateUser, int id);

    void delete(int id);
}
