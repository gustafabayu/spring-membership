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

import com.api.membership.model.Address;
import com.api.membership.model.Contact;
import com.api.membership.model.User;
import com.api.membership.model.request.CreateAddressRequest;
import com.api.membership.model.request.UpdateAddressRequest;
import com.api.membership.model.response.AddressResponse;
import com.api.membership.model.response.WebResponse;
import com.api.membership.repository.AddressRepository;
import com.api.membership.repository.ContactRepository;
import com.api.membership.repository.UserRepository;
import com.api.membership.service.security.BCrypt;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
public class AddressTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ContactRepository contactRepository;

        @Autowired
        private AddressRepository addressRepository;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private ObjectMapper objectMapper;

        @BeforeEach
        void setUp() {
                addressRepository.deleteAll();
                contactRepository.deleteAll();
                userRepository.deleteAll();

                User user = new User();
                user.setUsername("test");
                user.setPassword(BCrypt.hashpw("test1234", BCrypt.gensalt()));
                user.setName("Usertest");
                user.setToken("test");
                user.setTokenExpiredAt(System.currentTimeMillis() + 100000000000L);
                userRepository.save(user);

                Contact contact = new Contact();
                contact.setId("test");
                contact.setUser(user);
                contact.setFirstName("tes");
                contact.setLastName("salah");
                contact.setEmail("salah@co.a");
                contact.setPhone("145622");
                contactRepository.save(contact);
        }

        @Test
        void createAddressBadRequest() throws Exception {
                CreateAddressRequest request = new CreateAddressRequest();
                request.setCountry("");

                mockMvc.perform(
                                post("/api/contacts/test/addresses")
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
        void createAddressSuccess() throws Exception {
                CreateAddressRequest request = new CreateAddressRequest();
                request.setStreet("test");
                request.setCity("test1");
                request.setProvince("test2");
                request.setCountry("test3");
                request.setPostalCode("123456");

                mockMvc.perform(
                                post("/api/contacts/test/addresses")
                                                .accept(MediaType.APPLICATION_JSON)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request))
                                                .header("X-API-TOKEN", "test"))
                                .andExpectAll(
                                                status().isOk())
                                .andDo(result -> {
                                        WebResponse<AddressResponse> response = objectMapper.readValue(
                                                        result.getResponse().getContentAsString(),
                                                        new TypeReference<WebResponse<AddressResponse>>() {

                                                        });
                                        assertNull(response.getErrors());
                                        assertEquals(request.getStreet(), response.getData().getStreet());
                                        assertEquals(request.getCity(), response.getData().getCity());
                                        assertEquals(request.getProvince(), response.getData().getProvince());
                                        assertEquals(request.getCountry(), response.getData().getCountry());
                                        assertEquals(request.getPostalCode(), response.getData().getPostalCode());

                                        assertTrue(addressRepository.existsById(response.getData().getId()));
                                });
        }

        @Test
        void getAddressNotFound() throws Exception {

                mockMvc.perform(
                                get("/api/contacts/test/addresses/test")
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
        void getAddressSuccess() throws Exception {
                Contact contact = contactRepository.findById("test").orElseThrow();

                Address address = new Address();
                address.setId("test");
                address.setContact(contact);
                address.setStreet("jalan");
                address.setCity("gotham");
                address.setProvince("batman");
                address.setCountry("DC");
                address.setPostalCode("123123");
                addressRepository.save(address);

                mockMvc.perform(
                                get("/api/contacts/test/addresses/test")
                                                .accept(MediaType.APPLICATION_JSON)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .header("X-API-TOKEN", "test"))
                                .andExpectAll(
                                                status().isOk())
                                .andDo(result -> {
                                        WebResponse<AddressResponse> response = objectMapper.readValue(
                                                        result.getResponse().getContentAsString(),
                                                        new TypeReference<WebResponse<AddressResponse>>() {

                                                        });
                                        assertNull(response.getErrors());
                                        assertEquals(address.getId(), response.getData().getId());
                                        assertEquals(address.getStreet(), response.getData().getStreet());
                                        assertEquals(address.getCity(), response.getData().getCity());
                                        assertEquals(address.getProvince(), response.getData().getProvince());
                                        assertEquals(address.getCountry(), response.getData().getCountry());
                                        assertEquals(address.getPostalCode(), response.getData().getPostalCode());

                                });
        }

        @Test
        void updateAddressBadRequest() throws Exception {
                UpdateAddressRequest request = new UpdateAddressRequest();
                request.setCountry("");

                mockMvc.perform(
                                put("/api/contacts/test/addresses/test")
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
        void updateAddressSuccess() throws Exception {
                Contact contact = contactRepository.findById("test").orElseThrow();

                Address address = new Address();
                address.setId("test");
                address.setContact(contact);
                address.setStreet("jalan");
                address.setCity("gotham");
                address.setProvince("batman");
                address.setCountry("DC");
                address.setPostalCode("123123");
                addressRepository.save(address);

                UpdateAddressRequest request = new UpdateAddressRequest();
                request.setStreet("test");
                request.setCity("test1");
                request.setProvince("test2");
                request.setCountry("test3");
                request.setPostalCode("123456");

                mockMvc.perform(
                                put("/api/contacts/test/addresses/test")
                                                .accept(MediaType.APPLICATION_JSON)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request))
                                                .header("X-API-TOKEN", "test"))
                                .andExpectAll(
                                                status().isOk())
                                .andDo(result -> {
                                        WebResponse<AddressResponse> response = objectMapper.readValue(
                                                        result.getResponse().getContentAsString(),
                                                        new TypeReference<WebResponse<AddressResponse>>() {

                                                        });
                                        assertNull(response.getErrors());
                                        assertEquals(request.getStreet(), response.getData().getStreet());
                                        assertEquals(request.getCity(), response.getData().getCity());
                                        assertEquals(request.getProvince(), response.getData().getProvince());
                                        assertEquals(request.getCountry(), response.getData().getCountry());
                                        assertEquals(request.getPostalCode(), response.getData().getPostalCode());

                                        assertTrue(addressRepository.existsById(response.getData().getId()));
                                });
        }

        @Test
        void deleteAddressNotFound() throws Exception {

                mockMvc.perform(
                                delete("/api/contacts/test/addresses/test")
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
        void deleteAddressSuccess() throws Exception {
                Contact contact = contactRepository.findById("test").orElseThrow();

                Address address = new Address();
                address.setId("test");
                address.setContact(contact);
                address.setStreet("jalan");
                address.setCity("gotham");
                address.setProvince("batman");
                address.setCountry("DC");
                address.setPostalCode("123123");
                addressRepository.save(address);

                mockMvc.perform(
                                delete("/api/contacts/test/addresses/test")
                                                .accept(MediaType.APPLICATION_JSON)
                                                .contentType(MediaType.APPLICATION_JSON)
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

                                        assertFalse(addressRepository.existsById("test"));
                                });
        }

        @Test
        void listAddressNotFound() throws Exception {

                mockMvc.perform(
                                get("/api/contacts/test1/addresses")
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
        void listAddressSuccess() throws Exception {
                Contact contact = contactRepository.findById("test").orElseThrow();

                for (int i = 0; i < 5; i++) {
                        Address address = new Address();
                        address.setId("test-" + i);
                        address.setContact(contact);
                        address.setStreet("jalan");
                        address.setCity("gotham");
                        address.setProvince("batman");
                        address.setCountry("DC");
                        address.setPostalCode("123123");
                        addressRepository.save(address);
                }

                mockMvc.perform(
                                get("/api/contacts/test/addresses")
                                                .accept(MediaType.APPLICATION_JSON)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .header("X-API-TOKEN", "test"))
                                .andExpectAll(
                                                status().isOk())
                                .andDo(result -> {
                                        WebResponse<List<AddressResponse>> response = objectMapper.readValue(
                                                        result.getResponse().getContentAsString(),
                                                        new TypeReference<WebResponse<List<AddressResponse>>>() {

                                                        });
                                        assertNull(response.getErrors());
                                        assertEquals(5, response.getData().size());

                                });
        }
}
