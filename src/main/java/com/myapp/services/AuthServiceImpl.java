package com.myapp.services;

import com.myapp.dao.UserDao;
import com.myapp.dao.UserDaoImpl;
import com.myapp.models.User;
import org.mindrot.jbcrypt.BCrypt;

public class AuthServiceImpl implements AuthService {
    private final UserDao userDao = new UserDaoImpl();

    @Override
    public User login(String email, String password) {
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            return null;
        }
        // Fetch user by email and verify hashed password
        User existing = userDao.findByEmail(email.trim());
        if (existing == null || existing.getPassword() == null) {
            return null;
        }
        boolean ok = BCrypt.checkpw(password.trim(), existing.getPassword());
        return ok ? existing : null;
    }

    @Override
    public User register(String name, String email, String password) {
        if (name == null || name.isBlank() || email == null || email.isBlank() || password == null || password.isBlank()) {
            throw new IllegalArgumentException("Invalid registration data");
        }
        // Hash the password before storing
        String hashed = BCrypt.hashpw(password.trim(), BCrypt.gensalt(12));
        User user = new User(name.trim(), email.trim(), hashed);
        // Rely on DB unique constraint; DAO will throw IllegalArgumentException for duplicates
        return userDao.create(user);
    }
}
