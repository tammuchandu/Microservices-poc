
package com.example.user.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.user.entity.User;
import com.example.user.repository.UserRepository;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository repository;

    public UserServiceImpl(UserRepository repository) {
        this.repository = repository;
    }

    // Get all users
    @Override
    public List<User> getAll() {
        return repository.findAll();
    }

    // Get user by ID
    @Override
    public User getById(Long id) {

        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found with ID: " + id
                ));
    }

    // Create new user
    @Override
    public User create(User user) {

        if (repository.existsByEmail(user.getEmail())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email already exists"
            );
        }

        user.setId(null);

        return repository.save(user);
    }

    // Update existing user
    @Override
    public User update(Long id, User user) {

        User existingUser = getById(id);

        if (repository.findByEmail(user.getEmail())
                .filter(existing -> !existing.getId().equals(id))
                .isPresent()) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email already belongs to another user"
            );
        }

        existingUser.setName(user.getName());
        existingUser.setEmail(user.getEmail());
        existingUser.setPhone(user.getPhone());

        return repository.save(existingUser);
    }

    // Delete user
    @Override
    public void delete(Long id) {

        User existingUser = getById(id);

        repository.delete(existingUser);
    }
}
