
package com.example.user.test.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import com.example.user.controller.UserController;
import com.example.user.entity.User;
import com.example.user.service.UserService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService service;

    @Test
    void testGetAllUsers() throws Exception {
        User user = new User("Chandu", "chandu@gmail.com", "9876543210");
        user.setId(1L);

        when(service.getAll()).thenReturn(List.of(user));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Chandu"))
                .andExpect(jsonPath("$[0].email").value("chandu@gmail.com"))
                .andExpect(jsonPath("$[0].phone").value("9876543210"));

        verify(service).getAll();
    }

    @Test
    void testGetUserById() throws Exception {
        User user = new User("Chandu", "chandu@gmail.com", "9876543210");
        user.setId(1L);

        when(service.getById(1L)).thenReturn(user);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Chandu"));

        verify(service).getById(1L);
    }

    @Test
    void testCreateUser() throws Exception {
        User user = new User("Chandu", "chandu@gmail.com", "9876543210");
        user.setId(1L);

        when(service.create(any(User.class))).thenReturn(user);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Chandu",
                                    "email": "chandu@gmail.com",
                                    "phone": "9876543210"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Chandu"));

        verify(service).create(any(User.class));
    }

    @Test
    void testCreateUserWithInvalidName() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "",
                                    "email": "chandu@gmail.com",
                                    "phone": "9876543210"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(service, never()).create(any(User.class));
    }

    @Test
    void testCreateUserWithInvalidEmail() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Chandu",
                                    "email": "invalid-email",
                                    "phone": "9876543210"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(service, never()).create(any(User.class));
    }

    @Test
    void testCreateUserWithInvalidPhone() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Chandu",
                                    "email": "chandu@gmail.com",
                                    "phone": "12345"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(service, never()).create(any(User.class));
    }

    @Test
    void testUpdateUser() throws Exception {
        User user = new User("Updated Chandu", "updated@gmail.com", "9876543210");
        user.setId(1L);

        when(service.update(eq(1L), any(User.class))).thenReturn(user);

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Updated Chandu",
                                    "email": "updated@gmail.com",
                                    "phone": "9876543210"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Chandu"))
                .andExpect(jsonPath("$.email").value("updated@gmail.com"));

        verify(service).update(eq(1L), any(User.class));
    }

    @Test
    void testDeleteUser() throws Exception {
        doNothing().when(service).delete(1L);

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());

        verify(service).delete(1L);
    }
}
