
package com.example.user.service;

import java.util.List;

import com.example.user.entity.User;

public interface UserService {

    // Get all users
    List<User> getAll();

    // Get user by ID
    User getById(Long id);

    // Create new user
    User create(User user);

    // Update existing user
    User update(Long id, User user);

    // Delete user
    void delete(Long id);
}
