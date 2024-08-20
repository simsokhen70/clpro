package com.example.clpro.service.serviceImpl;

import com.example.clpro.service.interfaces.StaticDeployService;
import com.example.clpro.utils.SSHUtil;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class StaticDeployServiceImpl implements StaticDeployService {

    @Override
    public void buildDockerImage(SseEmitter emitter, String parentDirectory, String branchName, String repoUrl, String imageName, boolean isPrivate, String username, String token, String projectType) {
        new Thread(() -> {
            Map<String, String> response = new HashMap<>();
            try {
//                String repoName = repoUrl.substring(repoUrl.lastIndexOf('/') + 1, repoUrl.lastIndexOf('.'));
//                String gitCloneCommand = String.format("cd %s && git clone --branch %s %s", parentDirectory, branchName, repoUrl);
                String repoName = repoUrl.substring(repoUrl.lastIndexOf('/') + 1, repoUrl.lastIndexOf('.'));
                String gitCloneCommand;

                if (isPrivate) {
                    gitCloneCommand = String.format("expect -c 'spawn git clone --branch %s %s %s/%s; " +
                            "expect \"Username for\"; send \"%s\\r\"; " +
                            "expect \"Password for\"; send \"%s\\r\"; " +
                            "interact'", branchName, repoUrl, parentDirectory, repoName, username, token);
                } else {
                    gitCloneCommand = String.format("cd %s && git clone --branch %s %s", parentDirectory, branchName, repoUrl);
                }

                String createDockerfileCommand;
                String createNginxConf = "";
                if ("nextjs".equalsIgnoreCase(projectType)) {
                    createDockerfileCommand = String.format("cd %s/%s && echo \"%s\" > Dockerfile", parentDirectory, repoName,
                            "FROM node:18-alpine as builder\n\n" +
                                    "WORKDIR /app\n\n" +
                                    "COPY package*.json ./\n\n" +
                                    "RUN npm -f install\n\n" +
                                    "COPY . .\n\n" +
                                    "RUN npm run build\n\n" +
                                    "EXPOSE 3000\n\n" +
                                    "CMD [\"npm\", \"start\"]".replace("\n", "\\n"));
                } else if("reactjs".equalsIgnoreCase(projectType)) {
                    createNginxConf = String.format("cd %s/%s && echo \"%s\" > nginx.conf", parentDirectory, repoName,
                            "server {\n\n" +
                                    "listen 80;\n\n" +
                                    "server_name localhost;\n\n" +
                                    "location / {\n\n" +
                                    "root /usr/share/nginx/html;\n\n" +
                                    "index index.html index.html;\n\n" +
                                    "try_files $uri /index.html;\n\n" +
                                    "}\n\n" +
                                    "error_page 500 502 503 504 /50x.html;\n\n" +
                                    "location = /50x.html {\n\n" +
                                    "root /usr/share/nginx/html;\n\n" +
                                    "}\n\n" +
                                    "}".replace("\n", "\\n"));

                    createDockerfileCommand = String.format("cd %s/%s && echo \"%s\" > Dockerfile", parentDirectory, repoName,
                            "FROM node:14-alpine as build\n\n" +
                                    "WORKDIR /app\n\n" +
                                    "RUN ln -sf /usr/share/zoneinfo/Asia/Bangkok /etc/localtime\n\n" +
                                    "COPY package*.json ./\n\n" +
                                    "RUN npm -f install\n\n" +
                                    "COPY . .\n\n" +
                                    "RUN npm run build\n\n" +
                                    "FROM nginx:alpine\n\n" +
                                    "COPY nginx.conf /etc/nginx/conf.d/default.conf\n\n" +
                                    "COPY --from=build /app/build /usr/share/nginx/html\n\n" +
                                    "EXPOSE 80\n\n" +
                                    "CMD [\"nginx\", \"-g\", \"daemon off\"]".replace("\n", "\\n"));

                } else { // default to static
                    createDockerfileCommand = String.format("cd %s/%s && echo \"%s\" > Dockerfile", parentDirectory, repoName,
                            "# Use an official Nginx runtime as a base image\n" +
                                    "FROM nginx:latest\n\n" +
                                    "# Copy the contents of the local 'html' folder to the default Nginx public directory\n" +
                                    "COPY . /usr/share/nginx/html/\n\n" +
                                    "# Expose port 80 to allow external access\n" +
                                    "EXPOSE 80".replace("\n", "\\n"));
                }
//                String createDockerfileCommand = String.format("cd %s/%s && echo \"%s\" > Dockerfile", parentDirectory, repoName,
//                        "# Use an official Nginx runtime as a base image\n" +
//                                "FROM nginx:latest\n\n" +
//                                "# Copy the contents of the local 'html' folder to the default Nginx public directory\n" +
//                                "COPY . /usr/share/nginx/html/\n\n" +
//                                "# Expose port 80 to allow external access\n" +
//                                "EXPOSE 80".replace("\n", "\\n"));
                String dockerBuildCommand = String.format("cd %s/%s && docker build -t %s .", parentDirectory, repoName, imageName);

                // Execute git clone command
                SSHUtil.executeCommand(response, gitCloneCommand, emitter);

                if ("reactjs".equalsIgnoreCase(projectType)) {
                    if ("success".equals(response.get("status"))) {
                        SSHUtil.executeCommand(response, createNginxConf, emitter);
                    }
                }

                // Execute create Dockerfile command after git clone completes
                if ("success".equals(response.get("status"))) {
                    SSHUtil.executeCommand(response, createDockerfileCommand, emitter);
                }



                // Execute Docker build command after create Dockerfile completes
                if ("success".equals(response.get("status"))) {
                    SSHUtil.executeCommand(response, dockerBuildCommand, emitter);
                }

                emitter.complete();
            } catch (Exception e) {
                try {
                    emitter.send("Error: " + e.getMessage());
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                emitter.completeWithError(e);
            }
        }).start();
    }

    @Override
    public Map<String, String> runDockerContainer(String containerName, String imageName, String port) {
        Map<String, String> response = new HashMap<>();
        String dockerRunCommand = String.format("docker run --name %s -dp %s:80 %s", containerName, port, imageName);
        SSHUtil.executeCommand(response, dockerRunCommand, null);  // No emitter for this method
        return response;
    }

    @Override
    public void configDomain(SseEmitter emitter, String name, String serverName, String port) {
        new Thread(() -> {
            try {
                // Step 1: Create DNS record in Cloudflare
                RestTemplate restTemplate = new RestTemplate();
                String cloudflareUrl = String.format("https://api.cloudflare.com/client/v4/zones/db4cbc26b44f6234d56bcb0db63d507d/dns_records");

                JSONObject requestBody = new JSONObject();
                requestBody.put("type", "A");
                requestBody.put("name", name);
                requestBody.put("content", "47.237.2.156");
                requestBody.put("ttl", 1);
                requestBody.put("proxied", false);

                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", "Bearer 93LiXoKdUPMhmYGrAiAj4OtbhZRER410sZV9X_Az");
                headers.set("Content-Type", "application/json");

                HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);

                ResponseEntity<String> response = restTemplate.exchange(cloudflareUrl, HttpMethod.POST, entity, String.class);

                if (response.getStatusCode().is2xxSuccessful()) {
                    emitter.send("DNS record created successfully.");
                    Thread.sleep(5000);
                    // Step 2: Create Nginx configuration file
                    String configFileName = getRandomString(6);
                    String configContent = String.format("server {\n" +
//                            "    listen 80;\n" +
                            "    server_name %s www.%s;\n" +
                            "    location / {\n" +
                            "        proxy_pass http://0.0.0.0:%s;\n" +
                            "    }\n" +
                            "}", serverName, serverName, port);

                    String createConfigCommand = String.format("cd /etc/nginx/conf.d && echo \"%s\" | sudo tee %s.conf", configContent, configFileName);
                    Map<String, String> sshResponse = new HashMap<>();
                    SSHUtil.executeCommand(sshResponse, createConfigCommand, emitter);

                    if ("success".equals(sshResponse.get("status"))) {
                        emitter.send("Nginx configuration file created successfully.");

                        // Step 3: Run Certbot
                        String certbotCommand = String.format("sudo certbot --nginx -d %s --redirect", serverName);
                        SSHUtil.executeCommand(sshResponse, certbotCommand, emitter);

                        if (!"success".equals(sshResponse.get("status"))) {
                            // Step 4: If there was an error, handle the prompt with expect
                            String expectScript = String.format(
                                    "spawn sudo certbot --nginx -d %s --redirect\n" +
                                            "expect {\n" +
                                            "    \"What would you like to do?\" { send \"1\\r\" }\n" +
                                            "    timeout { }\n" +
                                            "}\n" +
                                            "expect eof", serverName
                            );
                            String expectCommand = String.format("expect -c '%s'", expectScript);

                            SSHUtil.executeCommand(sshResponse, expectCommand, emitter);

                            if ("success".equals(sshResponse.get("status"))) {
                                emitter.send("Certbot ran successfully.");
                            } else {
                                emitter.send("Error running Certbot: " + sshResponse.get("output"));
                            }                    } else {
                            emitter.send("Certbot ran successfully.");
                        }
//                        String certbotCommand = String.format("sudo certbot --nginx -d %s --redirect", serverName);
//                        SSHUtil.executeCommand(sshResponse, certbotCommand, emitter);
//                        String expectScript = String.format(
//                                "spawn sudo certbot --nginx -d %s --redirect\n" +
//                                        "expect \"What would you like to do?\"\n" +
//                                        "send \"1\\r\"\n" +
//                                        "expect eof", serverName
//                        );
//                        String expectCommand = String.format("expect -c \"%s\"", expectScript);
//                        SSHUtil.executeCommand(sshResponse, expectCommand, emitter);

                        if ("success".equals(sshResponse.get("status"))) {
                            emitter.send("Certbot ran successfully.");
                        } else {
                            emitter.send("Error running Certbot: " + sshResponse.get("output"));
                        }
                    } else {
                        emitter.send("Error creating Nginx configuration file: " + sshResponse.get("output"));
                    }
                } else {
                    emitter.send("Error creating DNS record: " + response.getBody());
                }
                emitter.complete();
            } catch (Exception e) {
                try {
                    emitter.send("Error: " + e.getMessage());
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                emitter.completeWithError(e);
            }
        }).start();
    }

    private String getRandomString(int length) {
        String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder result = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            result.append(characters.charAt(random.nextInt(characters.length())));
        }
        return result.toString();
    }
}
