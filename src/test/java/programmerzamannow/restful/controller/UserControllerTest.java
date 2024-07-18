package programmerzamannow.restful.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import programmerzamannow.restful.entity.User;
import programmerzamannow.restful.model.*;
import programmerzamannow.restful.repository.ContactRepository;
import programmerzamannow.restful.repository.UserRepository;
import programmerzamannow.restful.security.BCrypt;

import javax.validation.ConstraintViolationException;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ContactRepository contactRepository;

    @BeforeEach
    void setUp() {
        contactRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @SneakyThrows
    void testRegisterSuccess() {

        User user = new User();
        user.setUsername("ilhaam.ms");
        user.setPassword("rahasia");
        user.setName("Ilham");

        mockMvc.perform(
                post("/api/users")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user))
        ).andExpectAll(
                content().contentType(MediaType.APPLICATION_JSON),
                status().isOk()
        ).andDo(result -> {
            WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals("OK", response.getData());
            assertNull(response.getErrors());
        });
    }


    @Test
    @SneakyThrows
    void testRegisterNotBlank() {

        User user = new User();
        user.setUsername("");
        user.setPassword("");
        user.setName("");

        mockMvc.perform(
                post("/api/users")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user))
        ).andExpectAll(
                content().contentType(MediaType.APPLICATION_JSON),
                status().isBadRequest()
        ).andDo(result -> {
            WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertNotNull(response.getErrors());
            assertThrows(ConstraintViolationException.class, () -> {
                throw new ConstraintViolationException(null);
            });
        });
    }

    @Test
    @SneakyThrows
    void testRegisterUsernameDuplicate() {
        User user1 = new User();
        user1.setUsername("ilhaam.ms");
        user1.setPassword(BCrypt.hashpw("rahasia", BCrypt.gensalt()));
        user1.setName("Ilham");
        userRepository.save(user1);


        User user2 = new User();
        user2.setUsername("ilhaam.ms");
        user2.setPassword("123");
        user2.setName("Ucup");

        mockMvc.perform(
                post("/api/users")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user2))
        ).andExpectAll(
                content().contentType(MediaType.APPLICATION_JSON),
                status().isBadRequest()
        ).andDo(result -> {
            WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertNotNull(response.getErrors());
            assertEquals("Username already registered", response.getErrors());
            assertThrows(ResponseStatusException.class, () -> {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, response.getErrors());
            });
        });
    }

    @Test
    @SneakyThrows
    void getUserInvalidToken() {
        mockMvc.perform(
                get("/api/users/current")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-API-Token", "INVALID-TOKEN")
        ).andExpect(
                status().isUnauthorized()
        ).andDo(result -> {
            WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertNotNull(response.getErrors());
            assertEquals("Silahkan login terlebih dahulu", response.getErrors());
            assertThrows(ResponseStatusException.class, () -> {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, response.getErrors());
            });
        });
    }

    @Test
    @SneakyThrows
    void getUserTokenHeaderNotSet() {
        mockMvc.perform(
                get("/api/users/current")
                        .accept(MediaType.APPLICATION_JSON)
        ).andExpect(
                status().isUnauthorized()
        ).andDo(result -> {
            WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertNotNull(response.getErrors());
            assertEquals("Silahkan login terlebih dahulu", response.getErrors());
            assertThrows(ResponseStatusException.class, () -> {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, response.getErrors());
            });
        });
    }

    @Test
    @SneakyThrows
    void getUserTokenValid() {

        User user = new User();
        user.setUsername("ilhaam.ms");
        user.setPassword(BCrypt.hashpw("rahasia", BCrypt.gensalt()));
        user.setName("Ilham");
        user.setToken("VALID-TOKEN");
        user.setTokenExpiredAt(System.currentTimeMillis() + 1000 * 60 * 60);

        userRepository.save(user);

        mockMvc.perform(
                get("/api/users/current")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-API-Token", user.getToken())
        ).andExpect(
                status().isOk()
        ).andDo(result -> {
            WebResponse<UserResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });


            assertNull(response.getErrors());
            assertEquals("ilhaam.ms", response.getData().getUsername());
            assertEquals("Ilham", response.getData().getName());
        });
    }

    @Test
    @SneakyThrows
    void getUserTokenExpired() {

        User user = new User();
        user.setUsername("ilhaam.ms");
        user.setPassword(BCrypt.hashpw("rahasia", BCrypt.gensalt()));
        user.setName("Ilham");
        user.setToken("VALID-TOKEN");
        user.setTokenExpiredAt(System.currentTimeMillis() - 1000 * 60 * 60);

        userRepository.save(user);

        mockMvc.perform(
                get("/api/users/current")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-API-Token", user.getToken())
        ).andExpect(
                status().isUnauthorized()
        ).andDo(result -> {
            WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertNotNull(response.getErrors());
            assertEquals("Token expired", response.getErrors());
            assertThrows(ResponseStatusException.class, () -> {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, response.getErrors());
            });
        });
    }

    @Test
    @SneakyThrows
    void updateUserInvalidToken() {

        UpdateUserRequest user = new UpdateUserRequest();

        mockMvc.perform(
                patch("/api/users/current")
                        .header("X-API-Token", "INVALID-TOKEN")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user))
        ).andExpect(
                status().isUnauthorized()
        ).andDo(result -> {
            WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertNotNull(response.getErrors());
            assertEquals("Silahkan login terlebih dahulu", response.getErrors());
            assertThrows(ResponseStatusException.class, () -> {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, response.getErrors());
            });
        });
    }

    @Test
    @SneakyThrows
    void updateUserTokenHeaderNotSet() {
        mockMvc.perform(
                patch("/api/users/current")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateUserRequest()))
        ).andExpect(
                status().isUnauthorized()
        ).andDo(result -> {
            WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertNotNull(response.getErrors());
            assertEquals("Silahkan login terlebih dahulu", response.getErrors());
            assertThrows(ResponseStatusException.class, () -> {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, response.getErrors());
            });
        });
    }

    @Test
    @SneakyThrows
    void updateNameAndPasswordUserSuccess() {

        User user = new User();
        user.setUsername("ilhaam.msss");
        user.setPassword(BCrypt.hashpw("rahasia", BCrypt.gensalt()));
        user.setName("Ilham");
        user.setToken("VALID-TOKEN");
        user.setTokenExpiredAt(System.currentTimeMillis() + 1000 * 60 * 60);

        userRepository.save(user);

        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("Ilham Muhammad Sidiq");
        request.setPassword("rahasia123");

        mockMvc.perform(
                patch("/api/users/current")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-API-Token", user.getToken())
        ).andExpectAll(
                content().contentType(MediaType.APPLICATION_JSON),
                status().isOk()
        ).andDo(result -> {
            WebResponse<UserResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertNull(response.getErrors());
            assertEquals("ilhaam.msss", response.getData().getUsername());
            assertEquals("Ilham Muhammad Sidiq", response.getData().getName());

            var userDB = userRepository.findById("ilhaam.msss").orElse(null);
            assertNotNull(userDB);
            assertTrue(BCrypt.checkpw("rahasia123", userDB.getPassword()));
        });
    }

    @Test
    @SneakyThrows
    void updateNameUserSuccess() {

        User user = new User();
        user.setUsername("ilhaam.msss");
        user.setPassword(BCrypt.hashpw("rahasia", BCrypt.gensalt()));
        user.setName("Ilham");
        user.setToken("VALID-TOKEN");
        user.setTokenExpiredAt(System.currentTimeMillis() + 1000 * 60 * 60);

        userRepository.save(user);

        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("Hamzah");

        mockMvc.perform(
                patch("/api/users/current")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-API-Token", user.getToken())
        ).andExpectAll(
                content().contentType(MediaType.APPLICATION_JSON),
                status().isOk()
        ).andDo(result -> {
            WebResponse<UserResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertNull(response.getErrors());
            assertEquals("ilhaam.msss", response.getData().getUsername());
            assertEquals("Hamzah", response.getData().getName());

            var userDB = userRepository.findById("ilhaam.msss").orElse(null);
            assertNotNull(userDB);
            assertEquals("Hamzah", userDB.getName());

        });
    }

    @Test
    @SneakyThrows
    void updatePasswordUserSuccess() {

        User user = new User();
        user.setUsername("ilhaam.msss");
        user.setPassword(BCrypt.hashpw("rahasia", BCrypt.gensalt()));
        user.setName("Ilham");
        user.setToken("VALID-TOKEN");
        user.setTokenExpiredAt(System.currentTimeMillis() + 1000 * 60 * 60);

        userRepository.save(user);

        UpdateUserRequest request = new UpdateUserRequest();
//        request.setName("Hamzah");
        request.setPassword("rahasia123823723");
        mockMvc.perform(
                patch("/api/users/current")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-API-Token", user.getToken())
        ).andExpectAll(
                content().contentType(MediaType.APPLICATION_JSON),
                status().isOk()
        ).andDo(result -> {
            WebResponse<UserResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertNull(response.getErrors());
            assertEquals("ilhaam.msss", response.getData().getUsername());
            assertEquals("Ilham", response.getData().getName());

            var userDB = userRepository.findById("ilhaam.msss").orElse(null);
            assertNotNull(userDB);
            assertTrue(BCrypt.checkpw("rahasia123823723", userDB.getPassword()));
        });
    }

    @Test
    @SneakyThrows
    void logoutFailed() {

        mockMvc.perform(
                delete("/api/auth/logout")
                        .accept(MediaType.APPLICATION_JSON)
        ).andExpect(
                status().isUnauthorized()
        ).andDo(result -> {
            WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertNotNull(response.getErrors());
            assertEquals("Silahkan login terlebih dahulu", response.getErrors());
            assertThrows(ResponseStatusException.class, () -> {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, response.getErrors());
            });
        });

    }

    @Test
    @SneakyThrows
    void logoutSuccess() {

        User user = new User();
        user.setUsername("ilhaam.msss");
        user.setPassword(BCrypt.hashpw("rahasia", BCrypt.gensalt()));
        user.setName("Ilham");
        user.setToken("VALID-TOKEN");
        user.setTokenExpiredAt(System.currentTimeMillis() + 1000 * 60 * 60);

        userRepository.save(user);


        mockMvc.perform(
                delete("/api/auth/logout")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-API-Token", "VALID-TOKEN")
        ).andExpectAll(
                content().contentType(MediaType.APPLICATION_JSON),
                status().isOk()
        ).andDo(result -> {
            WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertNull(response.getErrors());
            assertEquals("OK", response.getData());

            var userDB = userRepository.findById("ilhaam.msss").orElse(null);
            assertNotNull(userDB);
            assertNull(userDB.getToken());
            assertNull(userDB.getTokenExpiredAt());
        });
    }

}