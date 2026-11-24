
package com.example.user.test.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.example.user.entity.User;
import com.example.user.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired
    private UserRepository repository;

    private User user;
    private String uniqueEmail;

    @BeforeEach
    void setUp() {
        uniqueEmail = "chandu-" + UUID.randomUUID() + "@gmail.com";
        user = new User("Chandu", uniqueEmail, "9876543210");
    }

    @Test
    void testSaveUser() {
        User savedUser = repository.save(user);

        assertNotNull(savedUser.getId());
        assertEquals("Chandu", savedUser.getName());
        assertEquals(uniqueEmail, savedUser.getEmail());
        assertEquals("9876543210", savedUser.getPhone());
    }

    @Test
    void testFindById() {
        User savedUser = repository.save(user);

        Optional<User> result = repository.findById(savedUser.getId());

        assertTrue(result.isPresent());
        assertEquals("Chandu", result.get().getName());
        assertEquals(uniqueEmail, result.get().getEmail());
    }

    @Test
    void testFindAll() {
        repository.save(user);

        List<User> users = repository.findAll();

        assertFalse(users.isEmpty());
        assertTrue(users.stream()
                .anyMatch(saved -> uniqueEmail.equals(saved.getEmail())));
    }

    @Test
    void testFindByEmail() {
        repository.save(user);

        Optional<User> result = repository.findByEmail(uniqueEmail);

        assertTrue(result.isPresent());
        assertEquals("Chandu", result.get().getName());
    }

    @Test
    void testFindByEmailNotFound() {
        Optional<User> result = repository.findByEmail("missing-" + UUID.randomUUID() + "@gmail.com");

        assertTrue(result.isEmpty());
    }

    @Test
    void testExistsByEmail() {
        repository.save(user);

        assertTrue(repository.existsByEmail(uniqueEmail));
    }

    @Test
    void testExistsByEmailFalse() {
        assertFalse(repository.existsByEmail("missing-" + UUID.randomUUID() + "@gmail.com"));
    }

    @Test
    void testUpdateUser() {
        User savedUser = repository.save(user);

        savedUser.setName("Updated Chandu");
        savedUser.setEmail("updated-" + UUID.randomUUID() + "@gmail.com");
        savedUser.setPhone("9876543211");

        User updatedUser = repository.saveAndFlush(savedUser);

        assertEquals(savedUser.getId(), updatedUser.getId());
        assertEquals("Updated Chandu", updatedUser.getName());
        assertEquals("9876543211", updatedUser.getPhone());
    }

    @Test
    void testDeleteUser() {
        User savedUser = repository.saveAndFlush(user);
        Long id = savedUser.getId();

        repository.deleteById(id);
        repository.flush();

        assertFalse(repository.findById(id).isPresent());
    }
}
