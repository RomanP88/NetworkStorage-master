package ru.gb.storage.server.dao;

import org.hibernate.Session;
import org.hibernate.Transaction;
import ru.gb.storage.server.models.User;
import ru.gb.storage.server.utils.HibernateSessionFactoryUtil;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.List;

public class UserDAOImpl implements UserDAO {
    @Override
    public User findById(int id) {
        return HibernateSessionFactoryUtil.getSessionFactory().openSession().get(User.class, id);
    }

    @Override
    public User findByLogin(String login) {
        Query query = HibernateSessionFactoryUtil.getSessionFactory().openSession().createQuery("from User where login = :login");
        query.setParameter("login", login);
        User user = null;
        try {
            user = (User) query.getSingleResult();
        } catch (NoResultException ignored) {
        }
        return user;
    }

    @Override
    public void save(User user) {
        Session session = HibernateSessionFactoryUtil.getSessionFactory().openSession();
        Transaction tx1 = session.beginTransaction();
        session.save(user);
        tx1.commit();
        session.close();
    }

    @Override
    public void update(User user) {
        Session session = HibernateSessionFactoryUtil.getSessionFactory().openSession();
        Transaction tx1 = session.beginTransaction();
        session.update(user);
        tx1.commit();
        session.close();
    }

    @Override
    public void delete(User user) {
        Session session = HibernateSessionFactoryUtil.getSessionFactory().openSession();
        Transaction tx1 = session.beginTransaction();
        session.delete(user);
        tx1.commit();
        session.close();
    }

    @Override
    public List<User> findAll() {
        List<User> users = (List<User>) HibernateSessionFactoryUtil.getSessionFactory().openSession().createQuery("From User").list();
        return users;
    }
}
