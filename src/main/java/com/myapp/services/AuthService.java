package com.myapp.services;

import com.myapp.models.User;

public interface AuthService {
    User login(String email, String password);
    User register(String name, String email, String password);
}
