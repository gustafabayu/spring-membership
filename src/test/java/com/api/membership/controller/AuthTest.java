package com.api.membership.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.api.membership.model.User;
import com.api.membership.model.request.LoginUserRequest;
import com.api.membership.model.response.TokenResponse;
import com.api.membership.model.response.WebResponse;
import com.api.membership.repository.UserRepository;
import com.api.membership.service.security.BCrypt;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private ObjectMapper objectMapper;

        @BeforeEach
        void setUp() {
                userRepository.deleteAll();
        }

        @Test
        void loginFailedUserNotFound() throws Exception {
                LoginUserRequest request = new LoginUserRequest();
                request.setUsername("test");
                request.setPassword("test1234");

                mockMvc.perform(
                                post("/api/auth/login")
                                                .accept(MediaType.APPLICATION_JSON)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request)))
                                .andExpectAll(
                                                status().isUnauthorized())
                                .andDo(result -> {
                                        WebResponse<String> response = objectMapper.readValue(
                                                        result.getResponse().getContentAsString(),
                                                        new TypeReference<WebResponse<String>>() {

                                                        });
                                        assertNotNull(response.getErrors());
                                });
        }

        @Test
        void loginFailedWrongPassword() throws Exception {
                User user = new User();
                user.setUsername("test");
                user.setPassword(BCrypt.hashpw("salah", BCrypt.gensalt()));
                user.setName("Usertest");
                userRepository.save(user);

                LoginUserRequest request = new LoginUserRequest();
                request.setUsername("test");
                request.setPassword("test1234");

                mockMvc.perform(
                                post("/api/auth/login")
                                                .accept(MediaType.APPLICATION_JSON)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request)))
                                .andExpectAll(
                                                status().isUnauthorized())
                                .andDo(result -> {
                                        WebResponse<String> response = objectMapper.readValue(
                                                        result.getResponse().getContentAsString(),
                                                        new TypeReference<WebResponse<String>>() {

                                                        });
                                        assertNotNull(response.getErrors());
                                });
        }

        @Test
        void loginSuccess() throws Exception {
                User user = new User();
                user.setUsername("test");
                user.setPassword(BCrypt.hashpw("test1234", BCrypt.gensalt()));
                user.setName("Usertest");
                userRepository.save(user);

                LoginUserRequest request = new LoginUserRequest();
                request.setUsername("test");
                request.setPassword("test1234");

                mockMvc.perform(
                                post("/api/auth/login")
                                                .accept(MediaType.APPLICATION_JSON)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request)))
                                .andExpectAll(
                                                status().isOk())
                                .andDo(result -> {
                                        WebResponse<TokenResponse> response = objectMapper.readValue(
                                                        result.getResponse().getContentAsString(),
                                                        new TypeReference<>() {

                                                        });
                                        assertNull(response.getErrors());
                                        assertNotNull(response.getData().getToken());
                                        assertNotNull(response.getData().getExpiredAt());

                                        User userDb = userRepository.findById("test").orElse(null);
                                        assertNotNull(userDb);
                                        assertEquals(userDb.getToken(), response.getData().getToken());
                                        assertEquals(userDb.getTokenExpiredAt(), response.getData().getExpiredAt());
                                });
        }

        @Test
        void logoutFailed() throws Exception {
                mockMvc.perform(
                                delete("/api/auth/logout")
                                                .accept(MediaType.APPLICATION_JSON))
                                .andExpectAll(
                                                status().isUnauthorized())
                                .andDo(result -> {
                                        WebResponse<String> response = objectMapper.readValue(
                                                        result.getResponse().getContentAsString(),
                                                        new TypeReference<WebResponse<String>>() {

                                                        });
                                        assertNotNull(response.getErrors());
                                });
        }

        @Test
        void logoutSuccess() throws Exception {
                User user = new User();
                user.setUsername("test");
                user.setPassword(BCrypt.hashpw("test1234", BCrypt.gensalt()));
                user.setName("Usertest");
                user.setToken("test");
                user.setTokenExpiredAt(System.currentTimeMillis() + 100000000000L);
                userRepository.save(user);

                mockMvc.perform(
                                delete("/api/auth/logout")
                                                .accept(MediaType.APPLICATION_JSON)
                                                .header("X-API-TOKEN", "test"))
                                .andExpectAll(
                                                status().isOk())
                                .andDo(result -> {
                                        WebResponse<String> response = objectMapper.readValue(
                                                        result.getResponse().getContentAsString(),
                                                        new TypeReference<WebResponse<String>>() {

                                                        });
                                        assertNull(response.getErrors());
                                        assertEquals("OK", response.getData());

                                        User userDb = userRepository.findById("test").orElse(null);
                                        assertNotNull(userDb);
                                        assertNull(userDb.getToken());
                                        assertNull(userDb.getTokenExpiredAt());
                                });
        }
}
