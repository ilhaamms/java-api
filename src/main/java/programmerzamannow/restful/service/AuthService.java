package programmerzamannow.restful.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import programmerzamannow.restful.entity.User;
import programmerzamannow.restful.model.LoginUserRequest;
import programmerzamannow.restful.model.TokenResponse;
import programmerzamannow.restful.repository.UserRepository;
import programmerzamannow.restful.security.BCrypt;

import java.util.UUID;

@Service
public class AuthService {

    private UserRepository userRepository;

    private ValidationService validationService;

    public AuthService(UserRepository userRepository, ValidationService validationService) {
        this.userRepository = userRepository;
        this.validationService = validationService;
    }

    @Transactional
    public TokenResponse login(LoginUserRequest request) {
        validationService.validate(request);

        User user = userRepository.findById(request.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Username or Password wrong"));

        if (BCrypt.checkpw(request.getPassword(), user.getPassword())){
            user.setToken(UUID.randomUUID().toString());
            user.setTokenExpiredAt(next30Day());

            userRepository.save(user);

            return TokenResponse.builder()
                    .token(user.getToken())
                    .expiredAt(user.getTokenExpiredAt())
                    .build();
        }

        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Username or Password wrong");
    }

    public Long next30Day(){
        return System.currentTimeMillis() + 1000 * 16 * 24 * 30;
    }

    @Transactional
    public void logout(User user) {
        user.setToken(null);
        user.setTokenExpiredAt(null);

        userRepository.save(user);
    }

}
