package com.example.authdemo.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@Slf4j
@Component
public class BrowserOpener {
    @Value("${server.port}")
    private String serverPort;

    @EventListener(ApplicationReadyEvent.class)
    public void openBrowser() {
        String url = "http://localhost:" + serverPort;
        String os = System.getProperty("os.name").toLowerCase();

        try {
            if (os.contains("win")) {
                // For Windows
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
            } else if (os.contains("mac")) {
                // For MacOS
                Runtime.getRuntime().exec("open " + url);
            } else if (os.contains("nix") || os.contains("nux")) {
                // For Linux
                Runtime.getRuntime().exec("xdg-open " + url);
            } else {
                // Fallback to Desktop API
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(new URI(url));
                } else {
                    log.warn("Could not detect supported browser launching system");
                }
            }
            log.info("Opening browser to {}", url);
        } catch (IOException | URISyntaxException e) {
            log.error("Failed to open browser: {}", e.getMessage());
        }
    }
}