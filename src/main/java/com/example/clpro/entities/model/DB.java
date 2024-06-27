package com.example.clpro.entities.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DB {
    private Long id;
    private String database_name;
    private String connection_host;
    private String port;
    private String type;
    private String username;
    private String password;
    private String project_name;
    private LocalDateTime create_at;
}
