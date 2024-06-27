package com.example.clpro.service.interfaces;

import java.util.Map;

public interface DBDeployService {
    Map<String, String> deployDb(String name, String postgresUser, String postgresPassword, String port);
}
