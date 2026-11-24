
package com.example.user.test.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import com.example.user.entity.User;
import com.example.user.repository.UserRepository;
import com.example.user.service.UserServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository repository;

    @InjectMocks
    private UserServiceImpl service;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("Chandu", "chandu@gmail.com", "9876543210");
        user.setId(1L);
    }

    @Test
    void testGetAllUsers() {
        when(repository.findAll()).thenReturn(List.of(user));

        List<User> result = service.getAll();

        assertEquals(1, result.size());
        assertEquals("Chandu", result.get(0).getName());
        verify(repository).findAll();
    }

    @Test
    void testGetUserById() {
        when(repository.findById(1L)).thenReturn(Optional.of(user));

        User result = service.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Chandu", result.getName());
        verify(repository).findById(1L);
    }

    @Test
    void testGetUserByIdNotFound() {
        when(repository.findById(100L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.getById(100L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("User not found with ID: 100", exception.getReason());
    }

    @Test
    void testCreateUser() {
        when(repository.existsByEmail("chandu@gmail.com")).thenReturn(false);
        when(repository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(1L);
            return savedUser;
        });

        User result = service.create(user);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Chandu", result.getName());
        verify(repository).existsByEmail("chandu@gmail.com");
        verify(repository).save(user);
    }

    @Test
    void testCreateUserWithDuplicateEmail() {
        when(repository.existsByEmail("chandu@gmail.com")).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.create(user)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("Email already exists", exception.getReason());
        verify(repository, never()).save(any(User.class));
    }

    @Test
    void testUpdateUser() {
        User updatedUser = new User(
                "Updated Chandu",
                "updated@gmail.com",
                "9876543211"
        );

        when(repository.findById(1L)).thenReturn(Optional.of(user));
        when(repository.findByEmail("updated@gmail.com")).thenReturn(Optional.empty());
        when(repository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = service.update(1L, updatedUser);

        assertEquals("Updated Chandu", result.getName());
        assertEquals("updated@gmail.com", result.getEmail());
        assertEquals("9876543211", result.getPhone());

        verify(repository).findById(1L);
        verify(repository).findByEmail("updated@gmail.com");
        verify(repository).save(user);
    }

    @Test
    void testUpdateUserWithAnotherUsersEmail() {
        User anotherUser = new User(
                "Another User",
                "another@gmail.com",
                "9876543211"
        );
        anotherUser.setId(2L);

        User updateRequest = new User(
                "Updated Chandu",
                "another@gmail.com",
                "9876543210"
        );

        when(repository.findById(1L)).thenReturn(Optional.of(user));
        when(repository.findByEmail("another@gmail.com"))
                .thenReturn(Optional.of(anotherUser));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.update(1L, updateRequest)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("Email already belongs to another user", exception.getReason());

        verify(repository, never()).save(any(User.class));
    }

    @Test
    void testUpdateUserWithSameEmail() {
        User updateRequest = new User(
                "Updated Chandu",
                "chandu@gmail.com",
                "9876543211"
        );

        when(repository.findById(1L)).thenReturn(Optional.of(user));
        when(repository.findByEmail("chandu@gmail.com")).thenReturn(Optional.of(user));
        when(repository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = service.update(1L, updateRequest);

        assertEquals("Updated Chandu", result.getName());
        assertEquals("chandu@gmail.com", result.getEmail());
        assertEquals("9876543211", result.getPhone());

        verify(repository).save(user);
    }

    @Test
    void testUpdateUserNotFound() {
        User updateRequest = new User(
                "Updated Chandu",
                "updated@gmail.com",
                "9876543211"
        );

        when(repository.findById(100L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.update(100L, updateRequest)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(repository, never()).save(any(User.class));
    }

    @Test
    void testDeleteUser() {
        when(repository.findById(1L)).thenReturn(Optional.of(user));
        doNothing().when(repository).delete(user);

        assertDoesNotThrow(() -> service.delete(1L));

        verify(repository).findById(1L);
        verify(repository).delete(user);
    }

    @Test
    void testDeleteUserNotFound() {
        when(repository.findById(100L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.delete(100L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(repository, never()).delete(any(User.class));
    }
}
