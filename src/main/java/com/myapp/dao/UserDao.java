package com.myapp.dao;

import com.myapp.models.User;

public interface UserDao {
    User login(String email, String password);
    User create(User user);
    User findByEmail(String email);
    void updatePasswordHash(int userId, String hashedPassword);
}
