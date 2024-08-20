package com.example.clpro.entities.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthDto {
    private String id;
    private String username;
    private String profile;
    private String email;
    private String telegramId;
    private String role;
}
