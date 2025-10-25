package com.appointment.system.dto.response;

import com.appointment.system.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;

    private String refreshToken;

    private String tokenType = "Bearer";

    private Long expiresIn;

    private Long userId;

    private String username;

    private String email;

    private Role role;
}
