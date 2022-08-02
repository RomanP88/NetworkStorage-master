package ru.gb.storage.server.services.interfaces;

public interface AuthService {
    boolean authenticate(String login, String password);
}
