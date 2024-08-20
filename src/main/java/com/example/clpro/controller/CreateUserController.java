package com.example.clpro.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import static com.example.clpro.utils.SSHUtil.executeCommand;

@RestController
@RequestMapping("api/v1/user")
@SecurityRequirement(name = "bearerAuth")
public class CreateUserController {

    @PostMapping("/create-user")
    public Map<String, String> createUser(@RequestParam String username, @RequestParam String password) {
        Map<String, String> response = new HashMap<>();
        String command = String.format("sudo adduser --disabled-password --gecos \"\" %s && echo '%s:%s' | sudo chpasswd && sudo usermod -aG sudo %s", username, username, password, username);
        executeCommand(response, command, null);
        return response;
    }

    @PostMapping("/ssh")
    public Map<String, String> sshLogin(@RequestParam String command) {
        Map<String, String> response = new HashMap<>();
        executeCommand(response, command, null);
        return response;
    }

    @GetMapping("/test")
    public String test() {
        return "Hello, World!";
    }
}
