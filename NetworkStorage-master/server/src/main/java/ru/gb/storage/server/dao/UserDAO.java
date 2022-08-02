package ru.gb.storage.server.dao;

import ru.gb.storage.server.models.User;

import java.util.List;

public interface UserDAO {
    User findById(int id);
    User findByLogin(String login);
    void save(User user);
    void update(User user);
    void delete(User user);
    List<User> findAll();
}
