package com.srs.domain.models.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class AuthResponse {
    String token;
}
