package com.api.membership.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.api.membership.model.Contact;
import com.api.membership.model.User;
import com.api.membership.model.request.CreateContactRequest;
import com.api.membership.model.request.UpdateContactRequest;
import com.api.membership.model.response.ContactResponse;
import com.api.membership.model.response.WebResponse;
import com.api.membership.repository.ContactRepository;
import com.api.membership.repository.UserRepository;
import com.api.membership.service.security.BCrypt;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
public class ContactTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private ContactRepository contactRepository;

        @Autowired
        private ObjectMapper objectMapper;

        @BeforeEach
        void setUp() {
                contactRepository.deleteAll();
                userRepository.deleteAll();

                User user = new User();
                user.setUsername("test");
                user.setPassword(BCrypt.hashpw("test1234", BCrypt.gensalt()));
                user.setName("Usertest");
                user.setToken("test");
                user.setTokenExpiredAt(System.currentTimeMillis() + 100000000000L);
                userRepository.save(user);
        }

        @Test
        void createContactBadRequest() throws Exception {
                CreateContactRequest request = new CreateContactRequest();
                request.setFirstName("");
                request.setEmail("salah");

                mockMvc.perform(
                                post("/api/contacts")
                                                .accept(MediaType.APPLICATION_JSON)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request))
                                                .header("X-API-TOKEN", "test"))
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
        void createContactSuccess() throws Exception {
                CreateContactRequest request = new CreateContactRequest();
                request.setFirstName("tes");
                request.setLastName("coba");
                request.setPhone("21453");
                request.setEmail("salah@a.co");

                mockMvc.perform(
                                post("/api/contacts")
                                                .accept(MediaType.APPLICATION_JSON)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request))
                                                .header("X-API-TOKEN", "test"))
                                .andExpectAll(
                                                status().isOk())
                                .andDo(result -> {
                                        WebResponse<ContactResponse> response = objectMapper.readValue(
                                                        result.getResponse().getContentAsString(),
                                                        new TypeReference<WebResponse<ContactResponse>>() {

                                                        });
                                        assertNull(response.getErrors());
                                        assertEquals("tes", response.getData().getFirstName());
                                        assertEquals("coba", response.getData().getLastName());
                                        assertEquals("21453", response.getData().getPhone());
                                        assertEquals("salah@a.co", response.getData().getEmail());

                                        assertTrue(contactRepository.existsById(response.getData().getId()));
                                });
        }

        @Test
        void getContactNotFound() throws Exception {
                mockMvc.perform(
                                get("/api/contacts/122345")
                                                .accept(MediaType.APPLICATION_JSON)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .header("X-API-TOKEN", "test"))
                                .andExpectAll(
                                                status().isNotFound())
                                .andDo(result -> {
                                        WebResponse<String> response = objectMapper.readValue(
                                                        result.getResponse().getContentAsString(),
                                                        new TypeReference<WebResponse<String>>() {

                                                        });
                                        assertNotNull(response.getErrors());
                                });
        }

        @Test
        void getContactSuccess() throws Exception {
                User user = userRepository.findById("test").orElseThrow();

                Contact contact = new Contact();
                contact.setId(UUID.randomUUID().toString());
                contact.setUser(user);
                contact.setFirstName("tes");
                contact.setLastName("salah");
                contact.setEmail("salah@co.a");
                contact.setPhone("145622");
                contactRepository.save(contact);

                mockMvc.perform(
                                get("/api/contacts/" + contact.getId())
                                                .accept(MediaType.APPLICATION_JSON)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .header("X-API-TOKEN", "test"))
                                .andExpectAll(
                                                status().isOk())
                                .andDo(result -> {
                                        WebResponse<ContactResponse> response = objectMapper.readValue(
                                                        result.getResponse().getContentAsString(),
                                                        new TypeReference<>() {

                                                        });
                                        assertNull(response.getErrors());
                                        assertEquals(contact.getId(), response.getData().getId());
                                        assertEquals(contact.getFirstName(), response.getData().getFirstName());
                                        assertEquals(contact.getLastName(), response.getData().getLastName());
                                        assertEquals(contact.getPhone(), response.getData().getPhone());
                                        assertEquals(contact.getEmail(), response.getData().getEmail());

                                });
        }

        @Test
        void updateContactBadRequest() throws Exception {
                UpdateContactRequest request = new UpdateContactRequest();
                request.setFirstName("");
                request.setEmail("salah");

                mockMvc.perform(
                                put("/api/contacts/1234")
                                                .accept(MediaType.APPLICATION_JSON)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request))
                                                .header("X-API-TOKEN", "test"))
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
        void updateContactSuccess() throws Exception {
                User user = userRepository.findById("test").orElseThrow();

                Contact contact = new Contact();
                contact.setId(UUID.randomUUID().toString());
                contact.setUser(user);
                contact.setFirstName("tes");
                contact.setLastName("salah");
                contact.setEmail("salah@co.a");
                contact.setPhone("145622");
                contactRepository.save(contact);

                UpdateContactRequest request = new UpdateContactRequest();
                request.setFirstName("toby");
                request.setLastName("kage");
                request.setPhone("77584");
                request.setEmail("toby@a.co");

                mockMvc.perform(
                                put("/api/contacts/" + contact.getId())
                                                .accept(MediaType.APPLICATION_JSON)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request))
                                                .header("X-API-TOKEN", "test"))
                                .andExpectAll(
                                                status().isOk())
                                .andDo(result -> {
                                        WebResponse<ContactResponse> response = objectMapper.readValue(
                                                        result.getResponse().getContentAsString(),
                                                        new TypeReference<WebResponse<ContactResponse>>() {

                                                        });
                                        assertNull(response.getErrors());
                                        assertEquals(request.getFirstName(), response.getData().getFirstName());
                                        assertEquals(request.getLastName(), response.getData().getLastName());
                                        assertEquals(request.getPhone(), response.getData().getPhone());
                                        assertEquals(request.getEmail(), response.getData().getEmail());

                                        assertTrue(contactRepository.existsById(response.getData().getId()));
                                });
        }

        @Test
        void deleteContactNotFound() throws Exception {
                mockMvc.perform(
                                delete("/api/contacts/122345")
                                                .accept(MediaType.APPLICATION_JSON)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .header("X-API-TOKEN", "test"))
                                .andExpectAll(
                                                status().isNotFound())
                                .andDo(result -> {
                                        WebResponse<String> response = objectMapper.readValue(
                                                        result.getResponse().getContentAsString(),
                                                        new TypeReference<WebResponse<String>>() {

                                                        });
                                        assertNotNull(response.getErrors());
                                });
        }

        @Test
        void deleteContactSuccess() throws Exception {
                User user = userRepository.findById("test").orElseThrow();

                Contact contact = new Contact();
                contact.setId(UUID.randomUUID().toString());
                contact.setUser(user);
                contact.setFirstName("tes");
                contact.setLastName("salah");
                contact.setEmail("salah@co.a");
                contact.setPhone("145622");
                contactRepository.save(contact);

                mockMvc.perform(
                                delete("/api/contacts/" + contact.getId())
                                                .accept(MediaType.APPLICATION_JSON)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .header("X-API-TOKEN", "test"))
                                .andExpectAll(
                                                status().isOk())
                                .andDo(result -> {
                                        WebResponse<String> response = objectMapper.readValue(
                                                        result.getResponse().getContentAsString(),
                                                        new TypeReference<>() {

                                                        });
                                        assertNull(response.getErrors());
                                        assertEquals("OK", response.getData());

                                });
        }

        @Test
        void searchNotFound() throws Exception {

                mockMvc.perform(
                                get("/api/contacts")
                                                .accept(MediaType.APPLICATION_JSON)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .header("X-API-TOKEN", "test"))
                                .andExpectAll(
                                                status().isOk())
                                .andDo(result -> {
                                        WebResponse<List<ContactResponse>> response = objectMapper.readValue(
                                                        result.getResponse().getContentAsString(),
                                                        new TypeReference<>() {

                                                        });
                                        assertNull(response.getErrors());
                                        assertEquals(0, response.getData().size());
                                        assertEquals(0, response.getPaging().getTotalPage());
                                        assertEquals(0, response.getPaging().getCurrentPage());
                                        assertEquals(10, response.getPaging().getSize());

                                });
        }

        @Test
        void searchSuccess() throws Exception {
                User user = userRepository.findById("test").orElseThrow();

                for (int i = 0; i < 100; i++) {
                        Contact contact = new Contact();
                        contact.setId(UUID.randomUUID().toString());
                        contact.setUser(user);
                        contact.setFirstName("tes " + i);
                        contact.setLastName("salah");
                        contact.setEmail("salah@co.a");
                        contact.setPhone("145622");
                        contactRepository.save(contact);
                }

                mockMvc.perform(
                                get("/api/contacts")
                                                .queryParam("name", "tes")
                                                .accept(MediaType.APPLICATION_JSON)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .header("X-API-TOKEN", "test"))
                                .andExpectAll(
                                                status().isOk())
                                .andDo(result -> {
                                        WebResponse<List<ContactResponse>> response = objectMapper.readValue(
                                                        result.getResponse().getContentAsString(),
                                                        new TypeReference<>() {

                                                        });
                                        assertNull(response.getErrors());
                                        assertEquals(10, response.getData().size());
                                        assertEquals(10, response.getPaging().getTotalPage());
                                        assertEquals(0, response.getPaging().getCurrentPage());
                                        assertEquals(10, response.getPaging().getSize());

                                });
        }
}
