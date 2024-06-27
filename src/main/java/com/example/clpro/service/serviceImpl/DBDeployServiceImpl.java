package com.example.clpro.service.serviceImpl;

import com.example.clpro.service.interfaces.DBDeployService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static com.example.clpro.utils.SSHUtil.executeCommand;

@Service
public class DBDeployServiceImpl implements DBDeployService {
    @Override
    public Map<String, String> deployDb(String name, String postgresUser, String postgresPassword, String port) {
        Map<String, String> response = new HashMap<>();

        // Docker run command with user-provided parameters
        String runCommand = String.format(
                "docker run -d " +
                        "--name %s " +
                        "--restart always " +
                        "-p %s:5432 " +
                        "-e POSTGRES_USER=%s " +
                        "-e POSTGRES_PASSWORD=%s " +
                        "-e POSTGRES_DB=postgres " +
                        "postgres:latest",
                name, port, postgresUser, postgresPassword
        );
        executeCommand(response, runCommand, null);
        return response;
    }
}
