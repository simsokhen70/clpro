package com.example.clpro.repository;

import com.example.clpro.entities.model.Auth;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface UserRepository extends MongoRepository<Auth, String> {
    @Query("{ 'username' : ?0 }")
    Auth findUserByUsername(String username);

    @Query("{ 'email' : ?0 }")
    Auth findUserByEmail(String email);
    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);
}
