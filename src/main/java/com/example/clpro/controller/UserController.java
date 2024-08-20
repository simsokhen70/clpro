package com.example.clpro.controller;

import com.example.clpro.entities.dto.AuthDto;
import com.example.clpro.entities.model.Auth;
import com.example.clpro.entities.response.ApiResponse;
import com.example.clpro.service.interfaces.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("api/v1/user")
@CrossOrigin
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/getAllUsers")
    public ResponseEntity<?> getAllUsers() {
        List<AuthDto> users = userService.getAllUsers();
        return new ResponseEntity<>(new ApiResponse<>(
                "User registered successfully",
                users,
                LocalDateTime.now()
        ), HttpStatus.OK);
    }

    @GetMapping("/getUserByUsername")
    public ResponseEntity<?> getUserByUsername(@RequestParam String username) {
        AuthDto user = userService.getUserByUsername(username);
        return new ResponseEntity<>(new ApiResponse<>(
                "User retrieved successfully",
                user,
                LocalDateTime.now()
        ), HttpStatus.OK);
    }


}
