package ru.gb.storage.server.services;

import org.mindrot.jbcrypt.BCrypt;
import ru.gb.storage.server.dao.UserDAO;
import ru.gb.storage.server.dao.UserDAOImpl;
import ru.gb.storage.server.models.User;
import ru.gb.storage.server.services.interfaces.AuthService;

public class AuthServiceImpl implements AuthService {
    private UserDAO userDAO = new UserDAOImpl();

    @Override
    public boolean authenticate(String login, String password) {
        User user = userDAO.findByLogin(login);
        if (user == null) {
            return false;
        }
        return BCrypt.checkpw(password, user.getPassword());
    }
}
