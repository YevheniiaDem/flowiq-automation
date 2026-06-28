package com.flowiq.models.response;

import lombok.Data;

@Data
public class ProfileResponse {
    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private String name;
    private String phone;
    private String avatar;
    private String company;
    private String role;
    private String createdAt;
    private String updatedAt;
}
