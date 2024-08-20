package com.example.clpro.controller;

import com.example.clpro.service.interfaces.DBDeployService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("api/v1/db")
@CrossOrigin
@SecurityRequirement(name = "bearerAuth")
public class DBDeployController {
    private final DBDeployService dbDeployService;

    public DBDeployController(DBDeployService dbDeployService) {
        this.dbDeployService = dbDeployService;
    }

    @PostMapping("/deploy-db")
    public Map<String, String> deployDb(
            @RequestParam String name,
            @RequestParam String postgresUser,
            @RequestParam String postgresPassword,
            @RequestParam String port
    ) {
        return dbDeployService.deployDb(name, postgresUser, postgresPassword, port);
    }
}
