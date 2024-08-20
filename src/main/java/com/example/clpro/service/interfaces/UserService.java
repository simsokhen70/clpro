package com.example.clpro.service.interfaces;

import com.example.clpro.entities.dto.AuthDto;

import java.util.List;

public interface UserService {
    List<AuthDto> getAllUsers();

    AuthDto getUserByUsername(String username);
}
