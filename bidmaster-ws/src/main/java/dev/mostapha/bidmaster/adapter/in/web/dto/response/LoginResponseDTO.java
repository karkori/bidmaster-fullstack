package dev.mostapha.bidmaster.adapter.in.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la respuesta de autenticación que incluye datos del usuario y el token JWT
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {
    private String id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private String token;
    private String tokenType;
}
