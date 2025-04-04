package com.pedrosmaxy.apidozero.dao.impl;

import com.pedrosmaxy.apidozero.dao.UserDAO;
import com.pedrosmaxy.apidozero.entities.User;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class UserDAOTextFileImpl implements UserDAO {

    private int idIncremental;
    private final String filePath;

    public UserDAOTextFileImpl(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            throw new IllegalArgumentException("Caminho do arquivo não pode ser nulo ou vazio");
        }
        this.filePath = filePath;
        this.idIncremental = getLastId();
    }

    public UserDAOTextFileImpl() {
        this("./base.txt");
    }

    private int getLastId() {
        assert filePath != null;
        File file = new File(filePath);

        if (!file.exists()) {
            return 0;
        }

        try (Scanner input = new Scanner(file)) {
            input.useDelimiter("-|\n");

            int maxId = 0;
            while (input.hasNext()) {
                int id = input.nextInt();
                input.next(); // nome
                input.next(); // email

                if (id > maxId) {
                    maxId = id;
                }
            }

            return maxId;
        } catch (FileNotFoundException e) {
            return 0;
        }
    }

    @Override
    public List<User> getAll() {
        List<User> users = new ArrayList<>();
        File file = new File(filePath);

        if (!file.exists()) {
            return users;
        }

        try (Scanner input = new Scanner(file)) {
            input.useDelimiter("-|\n");

            while (input.hasNext()) {
                var user = new User();
                user.setId(input.nextInt());
                user.setName(input.next());
                user.setEmail(input.next());

                users.add(user);
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Erro ao ler arquivo de usuários", e);
        }

        return users;
    }

    @Override
    public Optional<User> findById(int id) {
        return getAll().stream()
                .filter(user -> user.getId() == id)
                .findFirst();
    }

    private boolean isEmailUnique(String email, int excludeId) {
        return getAll().stream()
                .filter(user -> user.getId() != excludeId)
                .anyMatch(user -> user.getEmail().equals(email));
    }

    @Override
    public User create(User newUser) {
        if (isEmailUnique(newUser.getEmail(), -1)) {
            throw new RuntimeException("Email já está em uso");
        }

        idIncremental++;
        newUser.setId(idIncremental);

        List<User> users = getAll();
        users.add(newUser);
        saveAllUsers(users);

        return newUser;
    }

    @Override
    public Optional<User> change(User updateUser, int id) {
        if (findById(id).isEmpty()) {
            throw new RuntimeException("Usuário não existe");
        }

        if (isEmailUnique(updateUser.getEmail(), id)) {
            throw new RuntimeException("Email já está em uso por outro usuário");
        }

        List<User> users = getAll();
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId() == id) {
                updateUser.setId(id);
                users.set(i, updateUser);
                break;
            }
        }

        saveAllUsers(users);
        return Optional.of(updateUser);
    }

    @Override
    public void delete(int id) {
        if (findById(id).isEmpty()) {
            throw new RuntimeException("Usuário não existe");
        }

        List<User> users = getAll();
        users.removeIf(user -> user.getId() == id);
        saveAllUsers(users);
    }

    private void saveAllUsers(List<User> users) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (User user : users) {
                writer.write(user.getId() + "-" + user.getName() + "-" + user.getEmail() + "\n");
            }
        } catch (IOException e) {
            throw new RuntimeException("Erro ao salvar usuários no arquivo", e);
        }
    }
}