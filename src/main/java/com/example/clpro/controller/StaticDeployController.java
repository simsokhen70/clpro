package com.example.clpro.controller;

import com.example.clpro.service.interfaces.StaticDeployService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

@RestController
@RequestMapping("api/v1/web")
@CrossOrigin
//@SecurityRequirement(name = "bearerAuth")
public class StaticDeployController {

    private final StaticDeployService htmlDepService;

    public StaticDeployController(StaticDeployService htmlDepService) {
        this.htmlDepService = htmlDepService;
    }

    @GetMapping(value = "/build-docker-image", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter buildDockerImage(
            @RequestParam String parentDirectory,
            @RequestParam String branchName,
            @RequestParam String repoUrl,
            @RequestParam String imageName,
            @RequestParam boolean isPrivate,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String token,
            @RequestParam String projectType
    ) {
        SseEmitter emitter = new SseEmitter();
        htmlDepService.buildDockerImage(emitter, parentDirectory, branchName, repoUrl, imageName, isPrivate, username, token, projectType);
        return emitter;
    }


    @PostMapping("/run-docker-container")
    public Map<String, String> runDockerContainer(
            @RequestParam String containerName,
            @RequestParam String imageName,
            @RequestParam String port
    ) {
        return htmlDepService.runDockerContainer(containerName, imageName, port);
    }

    @GetMapping("/config-domain")
    public SseEmitter configDomain(
            @RequestParam String name,
            @RequestParam String serverName,
            @RequestParam String port
    ) {
        SseEmitter emitter = new SseEmitter();
        htmlDepService.configDomain(emitter, name, serverName, port);
        return emitter;
    }
}
