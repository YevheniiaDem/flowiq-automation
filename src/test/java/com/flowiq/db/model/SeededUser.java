package com.flowiq.db.model;

import com.flowiq.models.request.LoginRequest;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SeededUser {
    long id;
    String email;
    String plainPassword;
    String name;
    String company;

    public LoginRequest toLoginRequest() {
        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword(plainPassword);
        return request;
    }
}
