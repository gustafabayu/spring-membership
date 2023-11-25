package com.api.membership.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.api.membership.model.LoginUserRequest;
import com.api.membership.model.TokenResponse;
import com.api.membership.model.WebResponse;
import com.api.membership.service.AuthService;

@RestController
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping(path = "/api/auth/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public WebResponse<TokenResponse> login(@RequestBody LoginUserRequest request) {
        TokenResponse tokenResponse = authService.login(request);
        return WebResponse.<TokenResponse>builder().data(tokenResponse).build();
    }
}
