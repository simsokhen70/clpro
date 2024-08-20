package com.example.clpro.repository;

import com.example.clpro.entities.model.Auth;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

public interface AuthRepository extends MongoRepository<Auth, String> {
    @Query(value = "{'username': ?0}")
    UserDetails findByUsername(String username);

    @Query(value = "{'username' :  ?0}")
    Auth findByUsernameReturnAuth(String username);
}
