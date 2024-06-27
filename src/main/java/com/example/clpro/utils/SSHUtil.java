package com.example.clpro.utils;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;

public class SSHUtil {
    private static final String remoteHost = "47.237.2.156";
    private static final String remoteUser = "root";
    private static final String remotePassword = "Sokhen1645@$";

    public static void executeCommand(Map<String, String> response, String command, SseEmitter emitter) {
        Session session = null;
        ChannelExec channel = null;

        try {
            JSch jsch = new JSch();
            session = jsch.getSession(remoteUser, remoteHost, 22);
            session.setPassword(remotePassword);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);

            channel.setOutputStream(null);
            channel.setErrStream(null);

            channel.connect();

            // Capture output streams
            BufferedReader in = new BufferedReader(new InputStreamReader(channel.getInputStream()));
            BufferedReader err = new BufferedReader(new InputStreamReader(channel.getErrStream()));

            String msg;
            while ((msg = in.readLine()) != null) {
                emitter.send(SseEmitter.event().data(msg));
            }

            while ((msg = err.readLine()) != null) {
                emitter.send(SseEmitter.event().data(msg));
//                emitter.send(SseEmitter.event().data( "Error:" +msg);
            }

            // Wait for the command to complete
            while (!channel.isClosed()) {
                Thread.sleep(100);
            }

            // Check for success or failure
            if (channel.getExitStatus() == 0) {
                response.put("status", "success");
                response.put("output", "Command executed successfully");
            } else {
                response.put("status", "error");
                response.put("message", "Command execution failed");
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            e.printStackTrace(); // Print stack trace for debugging
        } finally {
            if (channel != null) {
                channel.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
        }
    }
}
