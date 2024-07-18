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
import programmerzamannow.restful.entity.User;
import programmerzamannow.restful.model.LoginUserRequest;
import programmerzamannow.restful.model.TokenResponse;
import programmerzamannow.restful.model.WebResponse;
import programmerzamannow.restful.repository.ContactRepository;
import programmerzamannow.restful.repository.UserRepository;
import programmerzamannow.restful.security.BCrypt;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.MockMvcBuilder.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

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
    void loginFailedUserNotFound() {

        LoginUserRequest loginRequest = new LoginUserRequest();
        loginRequest.setUsername("eko");
        loginRequest.setPassword("rahasia");

        mockMvc.perform(
                post("/api/auth/login")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest))
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
            WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertNotNull(response.getErrors());
            assertThrows(ResponseStatusException.class, () -> {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, response.getErrors());
            });
        });

    }


    @Test
    @SneakyThrows
    void loginFailedUserWrongPasswordOrUsername() {

        User user = new User();
        user.setUsername("eko");
        user.setPassword(BCrypt.hashpw("rahasia", BCrypt.gensalt()));
        user.setName("Eko");

        userRepository.save(user);

        LoginUserRequest loginRequest = new LoginUserRequest();
        loginRequest.setUsername("eko");
        loginRequest.setPassword("salah");

        mockMvc.perform(
                post("/api/auth/login")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest))
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
            WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertNotNull(response.getErrors());
            assertThrows(ResponseStatusException.class, () -> {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, response.getErrors());
            });
        });

    }

    @Test
    @SneakyThrows
    void loginFailedUserSuccess() {

        User user = new User();
        user.setUsername("eko");
        user.setPassword(BCrypt.hashpw("rahasia", BCrypt.gensalt()));
        user.setName("Eko");

        userRepository.save(user);

        LoginUserRequest loginRequest = new LoginUserRequest();
        loginRequest.setUsername("eko");
        loginRequest.setPassword("rahasia");

        mockMvc.perform(
                post("/api/auth/login")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest))
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {

            WebResponse<TokenResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertNull(response.getErrors());
            assertNotNull(response.getData().getToken());
            assertNotNull(response.getData().getExpiredAt());

            User userDB = userRepository.findById("eko").orElse(null);
            assertEquals(response.getData().getToken(), userDB.getToken());
            assertEquals(response.getData().getExpiredAt(), userDB.getTokenExpiredAt());
        });
    }

}