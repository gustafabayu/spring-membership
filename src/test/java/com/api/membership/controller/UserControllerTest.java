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

import com.api.membership.entity.User;
import com.api.membership.model.RegisterUserRequest;
import com.api.membership.model.WebResponse;
import com.api.membership.repository.UserRepository;
import com.api.membership.security.BCrypt;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

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
}
