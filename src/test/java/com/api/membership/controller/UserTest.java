package com.api.membership.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

import com.api.membership.model.User;
import com.api.membership.model.request.RegisterUserRequest;
import com.api.membership.model.request.UpdateUserRequest;
import com.api.membership.model.response.UserResponse;
import com.api.membership.model.response.WebResponse;
import com.api.membership.repository.UserRepository;
import com.api.membership.service.security.BCrypt;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
public class UserTest {

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
        void testRegisterSuccess() throws Exception {
                RegisterUserRequest request = new RegisterUserRequest();
                request.setUsername("test");
                request.setPassword("test1234");
                request.setName("Usertest");

                mockMvc.perform(
                                post("/api/users")
                                                .accept(MediaType.APPLICATION_JSON)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request)))
                                .andExpectAll(
                                                status().isOk())
                                .andDo(result -> {
                                        WebResponse<String> response = objectMapper.readValue(
                                                        result.getResponse().getContentAsString(),
                                                        new TypeReference<WebResponse<String>>() {

                                                        });
                                        assertEquals("OK", response.getData());
                                });
        }

        @Test
        void testRegisterBadRequest() throws Exception {
                RegisterUserRequest request = new RegisterUserRequest();
                request.setUsername("");
                request.setPassword("");
                request.setName("");

                mockMvc.perform(
                                post("/api/users")
                                                .accept(MediaType.APPLICATION_JSON)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request)))
                                .andExpectAll(
                                                status().isBadRequest())
                                .andDo(result -> {
                                        WebResponse<String> response = objectMapper.readValue(
                                                        result.getResponse().getContentAsString(),
                                                        new TypeReference<WebResponse<String>>() {

                                                        });
                                        assertNotNull(response.getErrors());
                                });
        }

        @Test
        void testRegisterDuplicate() throws Exception {
                User user = new User();
                user.setUsername("test");
                user.setPassword(BCrypt.hashpw("test1234", BCrypt.gensalt()));
                user.setName("Usertest");
                userRepository.save(user);

                RegisterUserRequest request = new RegisterUserRequest();
                request.setUsername("test");
                request.setPassword("test1234");
                request.setName("Usertest");

                mockMvc.perform(
                                post("/api/users")
                                                .accept(MediaType.APPLICATION_JSON)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request)))
                                .andExpectAll(
                                                status().isBadRequest())
                                .andDo(result -> {
                                        WebResponse<String> response = objectMapper.readValue(
                                                        result.getResponse().getContentAsString(),
                                                        new TypeReference<WebResponse<String>>() {

                                                        });
                                        assertNotNull(response.getErrors());
                                });
        }

        @Test
        void getUserUnauthorized() throws Exception {
                mockMvc.perform(
                                get("/api/users/current")
                                                .accept(MediaType.APPLICATION_JSON)
                                                .header("X-API-TOKEN", "notfound"))
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
        void getUserUnauthorizedTokenNotSent() throws Exception {
                mockMvc.perform(
                                get("/api/users/current")
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
        void getUserSuccess() throws Exception {
                User user = new User();
                user.setUsername("test");
                user.setPassword(BCrypt.hashpw("test1234", BCrypt.gensalt()));
                user.setName("Usertest");
                user.setToken("test");
                user.setTokenExpiredAt(System.currentTimeMillis() + 100000000000L);
                userRepository.save(user);

                mockMvc.perform(
                                get("/api/users/current")
                                                .accept(MediaType.APPLICATION_JSON)
                                                .header("X-API-TOKEN", "test"))
                                .andExpectAll(
                                                status().isOk())
                                .andDo(result -> {
                                        WebResponse<UserResponse> response = objectMapper.readValue(
                                                        result.getResponse().getContentAsString(),
                                                        new TypeReference<>() {

                                                        });
                                        assertNull(response.getErrors());
                                        assertEquals("test", response.getData().getUsername());
                                        assertEquals("Usertest", response.getData().getName());
                                });
        }

        @Test
        void getUserTokenExpired() throws Exception {
                User user = new User();
                user.setUsername("test");
                user.setPassword(BCrypt.hashpw("test1234", BCrypt.gensalt()));
                user.setName("Usertest");
                user.setToken("test");
                user.setTokenExpiredAt(System.currentTimeMillis() - 100000000000L);
                userRepository.save(user);

                mockMvc.perform(
                                get("/api/users/current")
                                                .accept(MediaType.APPLICATION_JSON)
                                                .header("X-API-TOKEN", "test"))
                                .andExpectAll(
                                                status().isUnauthorized())
                                .andDo(result -> {
                                        WebResponse<String> response = objectMapper.readValue(
                                                        result.getResponse().getContentAsString(),
                                                        new TypeReference<>() {

                                                        });
                                        assertNotNull(response.getErrors());
                                });
        }

        @Test
        void updateUserUnauthorized() throws Exception {
                UpdateUserRequest request = new UpdateUserRequest();

                mockMvc.perform(
                                patch("/api/users/current")
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
        void updateUserSuccess() throws Exception {
                User user = new User();
                user.setUsername("test");
                user.setPassword(BCrypt.hashpw("test1234", BCrypt.gensalt()));
                user.setName("Usertest");
                user.setToken("test");
                user.setTokenExpiredAt(System.currentTimeMillis() + 100000000000L);
                userRepository.save(user);

                UpdateUserRequest request = new UpdateUserRequest();
                request.setName("coba");
                request.setPassword("coba123");

                mockMvc.perform(
                                patch("/api/users/current")
                                                .accept(MediaType.APPLICATION_JSON)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request))
                                                .header("X-API-TOKEN", "test"))
                                .andExpectAll(
                                                status().isOk())
                                .andDo(result -> {
                                        WebResponse<UserResponse> response = objectMapper.readValue(
                                                        result.getResponse().getContentAsString(),
                                                        new TypeReference<>() {

                                                        });
                                        assertNull(response.getErrors());
                                        assertEquals("test", response.getData().getUsername());
                                        assertEquals("coba", response.getData().getName());

                                        User userDb = userRepository.findById("test").orElse(null);
                                        assertNotNull(userDb);
                                        assertTrue(BCrypt.checkpw("coba123", userDb.getPassword()));
                                });
        }
}
