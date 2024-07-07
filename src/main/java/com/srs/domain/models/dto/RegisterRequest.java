package com.srs.domain.models.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    String fullname;
    String username;
    String country;
    String password;
    String role;
}
