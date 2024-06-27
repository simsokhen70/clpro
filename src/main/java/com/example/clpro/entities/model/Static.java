package com.example.clpro.entities.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Static {
    private Long id;
    private String appName;
    private String appVersion;
}
