package com.flowiq.models.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateProfileRequest {
    private String firstName;
    private String lastName;
    private String phone;
    private String company;
}
