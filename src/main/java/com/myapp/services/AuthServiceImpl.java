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
        // Fetch user by email and verify password (BCrypt or legacy plain text)
        User existing = userDao.findByEmail(email.trim());
        if (existing == null) {
            return null;
        }
        String stored = existing.getPassword();
        if (stored == null || stored.isBlank()) {
            return null;
        }
        String raw = password.trim();
        boolean ok;
        try {
            if (isBCryptHash(stored)) {
                ok = BCrypt.checkpw(raw, stored);
            } else {
                // Legacy accounts saved as plain text
                ok = stored.equals(raw);
                if (ok) {
                    // Auto-migrate: re-hash and persist
                    String newHash = BCrypt.hashpw(raw, BCrypt.gensalt(12));
                    userDao.updatePasswordHash(existing.getId(), newHash);
                    existing.setPassword(newHash);
                }
            }
        } catch (IllegalArgumentException iae) {
            // e.g., invalid salt version -> treat as mismatch
            ok = false;
        }
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

// Package-private helper to detect BCrypt format
class BCryptUtil {
    static boolean isBCryptHash(String s) {
        if (s == null) return false;
        // Typical BCrypt prefixes and length ~60
        return (s.startsWith("$2a$") || s.startsWith("$2b$") || s.startsWith("$2y$")) && s.length() >= 56;
    }
}

// Allow calling without qualifier inside AuthServiceImpl
private static boolean isBCryptHash(String s) {
    return BCryptUtil.isBCryptHash(s);
}
