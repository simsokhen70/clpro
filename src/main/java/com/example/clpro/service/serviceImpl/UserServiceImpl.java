package com.example.clpro.service.serviceImpl;

import com.example.clpro.entities.dto.AuthDto;
import com.example.clpro.entities.model.Auth;
import com.example.clpro.exception.NotFoundExceptionClass;
import com.example.clpro.repository.UserRepository;
import com.example.clpro.service.interfaces.UserService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<AuthDto> getAllUsers() {
        return userRepository.findAll().stream().map(Auth::toDto).collect(Collectors.toList());
    }

    @Override
    public AuthDto getUserByUsername(String username) {

        Boolean user = userRepository.existsByUsername(username);
        System.out.println("user: " + user);
        if (user) {
            return userRepository.findUserByUsername(username).toDto();
        } else {
            throw new NotFoundExceptionClass("User not found");
        }
    }
}
