package com.api.membership.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.api.membership.entity.User;
import com.api.membership.model.LoginUserRequest;
import com.api.membership.model.TokenResponse;
import com.api.membership.repository.UserRepository;
import com.api.membership.security.BCrypt;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ValidationService validationService;

    @Transactional
    public TokenResponse login(LoginUserRequest request){
        validationService.validate(request);

        User user=userRepository.findById(request.getUsername())
                    .orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Username or password wrong"));

        if(BCrypt.checkpw(request.getPassword(), user.getPassword())){
            user.setToken(UUID.randomUUID().toString());
            user.setTokenExpiredAt(next30days());
            userRepository.save(user);

            return TokenResponse.builder()
                    .token(user.getToken())
                    .expiredAt(user.getTokenExpiredAt())
                    .build();
        }else{
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Username or password wrong");
        }
    }

    private Long next30days() {
        return System.currentTimeMillis() + (1000 * 16 * 24 * 30);
    }
}