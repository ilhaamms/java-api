package programmerzamannow.restful.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import programmerzamannow.restful.entity.Contact;
import programmerzamannow.restful.entity.User;
import programmerzamannow.restful.model.ContactResponse;
import programmerzamannow.restful.model.CreateContactRequest;
import programmerzamannow.restful.model.UpdateContactRequest;
import programmerzamannow.restful.model.WebResponse;
import programmerzamannow.restful.repository.ContactRepository;
import programmerzamannow.restful.repository.UserRepository;
import programmerzamannow.restful.security.BCrypt;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.MockMvcBuilder.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ContactControllerTest {

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
        user.setUsername("ilhaam.ms");
        user.setPassword(BCrypt.hashpw("test", BCrypt.gensalt()));
        user.setName("Test");
        user.setToken("test");
        user.setTokenExpiredAt(System.currentTimeMillis() + 1000000);

        userRepository.save(user);

    }

    @Test
    @SneakyThrows
    void createContactInvalid() {

        CreateContactRequest request = new CreateContactRequest();
        request.setFirstName("");
        request.setEmail("salah");

        mockMvc.perform(
                post("/api/contacts")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-API-Token", "test")
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
            WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertNotNull(response.getErrors());
        });
    }

    @Test
    @SneakyThrows
    void createContactSuccess() {

        CreateContactRequest request = new CreateContactRequest();
        request.setFirstName("Hamzah");
        request.setLastName("Muhammad Ramadhan");
        request.setEmail("hamzah@example.com");
        request.setPhone("081234567890");

        mockMvc.perform(
                post("/api/contacts")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-API-Token", "test")
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
            WebResponse<ContactResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertNull(response.getErrors());

            assertEquals("Hamzah", response.getData().getFirstName());
            assertEquals("Muhammad Ramadhan", response.getData().getLastName());
            assertEquals("hamzah@example.com", response.getData().getEmail());
            assertEquals("081234567890", response.getData().getPhone());

            assertTrue(contactRepository.existsById(response.getData().getId()));

        });
    }

    @Test
    @SneakyThrows
    void getContactNotFound() {
        mockMvc.perform(
                get("/api/contacts/152365136")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-API-Token", "test")
        ).andExpectAll(
                status().isNotFound()
        ).andDo(result -> {
            WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertNotNull(response.getErrors());
            assertEquals("Contact not found", response.getErrors());
            assertThrows(ResponseStatusException.class, () -> {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Contact not found");
            });
        });
    }

    @Test
    @SneakyThrows
    void getContactSuccess() {

        var user = userRepository.findById("ilhaam.ms").orElse(null);

        Contact contact = new Contact();
        contact.setUser(user);
        contact.setId(UUID.randomUUID().toString());
        contact.setFirstName("Hamzah");
        contact.setLastName("Muhammad Ramadhan");
        contact.setEmail("hamzah@gmail.com");
        contact.setPhone("081234567890");

        contactRepository.save(contact);

        mockMvc.perform(
                get("/api/contacts/" + contact.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-API-Token", "test")
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
            WebResponse<ContactResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertNull(response.getErrors());
            assertEquals(contact.getId(), response.getData().getId());
            assertEquals("Hamzah", response.getData().getFirstName());
            assertEquals("Muhammad Ramadhan", response.getData().getLastName());
            assertEquals("hamzah@gmail.com", response.getData().getEmail());
            assertEquals("081234567890", response.getData().getPhone());

        });
    }

    @Test
    @SneakyThrows
    void updateContactInvalid() {

        UpdateContactRequest request = new UpdateContactRequest();
        request.setFirstName("");
        request.setEmail("salah");

        mockMvc.perform(
                put("/api/contacts/123456")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-API-Token", "test")
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
            WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertNotNull(response.getErrors());
        });
    }

    @Test
    @SneakyThrows
    void updateContactSuccess() {

        var user = userRepository.findById("ilhaam.ms").orElse(null);

        Contact contact = new Contact();
        contact.setUser(user);
        contact.setId(UUID.randomUUID().toString());
        contact.setFirstName("Hamzah");
        contact.setLastName("Muhammad Ramadhan");
        contact.setEmail("hamzah@gmail.com");
        contact.setPhone("081234567890");

        contactRepository.save(contact);

        UpdateContactRequest request = new UpdateContactRequest();
        request.setFirstName("Ilham");
        request.setLastName("Muhammad Sidiq");
        request.setEmail("ilham@example.com");
        request.setPhone("167237123");

        mockMvc.perform(
                put("/api/contacts/" + contact.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-API-Token", "test")
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
            WebResponse<ContactResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertNull(response.getErrors());

            assertEquals(request.getFirstName(), response.getData().getFirstName());
            assertEquals(request.getLastName(), response.getData().getLastName());
            assertEquals(request.getEmail(), response.getData().getEmail());
            assertEquals(request.getPhone(), response.getData().getPhone());

            assertTrue(contactRepository.existsById(response.getData().getId()));

        });
    }


    @Test
    @SneakyThrows
    void deleteContactNotFound() {
        mockMvc.perform(
                delete("/api/contacts/152365136")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-API-Token", "test")
        ).andExpectAll(
                status().isNotFound()
        ).andDo(result -> {
            WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertNotNull(response.getErrors());
            assertEquals("Contact not found", response.getErrors());
            assertThrows(ResponseStatusException.class, () -> {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Contact not found");
            });
        });
    }

    @Test
    @SneakyThrows
    void deleteContactSuccess() {

        var user = userRepository.findById("ilhaam.ms").orElseThrow();

        Contact contact = new Contact();
        contact.setId(UUID.randomUUID().toString());
        contact.setUser(user);
        contact.setFirstName("Hamzah");
        contact.setLastName("Muhammad Ramadhan");
        contact.setEmail("hamzah@gmail.com");
        contact.setPhone("081234567890");

        contactRepository.save(contact);

        mockMvc.perform(
                delete("/api/contacts/" + contact.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-API-Token", "test")
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
            WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertNull(response.getErrors());
            assertEquals("OK", response.getData());

        });
    }

}