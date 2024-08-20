package com.example.clpro.entities.request;

import com.example.clpro.entities.model.Auth;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonPropertyOrder({"username","password"})
public class LoginRequest{
    @NotBlank(message = "Username is required")
    private String username ;
    @NotBlank(message = "Password is required")
    private String password ;
}
