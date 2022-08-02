package ru.gb.storage.server.services.interfaces;

public interface RegisterService {
    void register(String login, String password);
    boolean checkExistLogin(String login);
}
