package com.example.clpro.service.interfaces;


import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

public interface StaticDeployService {
    Map<String, String> runDockerContainer(String containerName, String imageName, String port);

    void buildDockerImage(SseEmitter emitter, String parentDirectory, String branchName, String repoUrl, String imageName, boolean isPrivate,String username, String token, String projectType);

    void configDomain(SseEmitter emitter, String name, String serverName, String port);
}
