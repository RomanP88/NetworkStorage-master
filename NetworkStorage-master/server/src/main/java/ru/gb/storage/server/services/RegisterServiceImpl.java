package ru.gb.storage.server.services;

import org.mindrot.jbcrypt.BCrypt;
import ru.gb.storage.server.dao.UserDAO;
import ru.gb.storage.server.dao.UserDAOImpl;
import ru.gb.storage.server.models.User;
import ru.gb.storage.server.services.interfaces.RegisterService;

public class RegisterServiceImpl implements RegisterService {
    private UserDAO userDAO = new UserDAOImpl();

    @Override
    public void register(String login, String password) {
        String hashPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        User user = new User();
        user.setLogin(login);
        user.setPassword(hashPassword);
        userDAO.save(user);
    }

    @Override
    public boolean checkExistLogin(String login) {
        User user = userDAO.findByLogin(login);
        return user != null;
    }
}
