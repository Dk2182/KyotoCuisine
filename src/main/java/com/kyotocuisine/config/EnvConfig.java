package com.kyotocuisine.config;

import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;

@Configuration
public class EnvConfig {

    @PostConstruct
    public void loadEnv() {
        Path envFile = Path.of(".env");
        if (Files.exists(envFile)) {
            try (BufferedReader reader = new BufferedReader(new FileReader(envFile.toFile()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) continue;
                    int eq = line.indexOf('=');
                    if (eq > 0) {
                        String key = line.substring(0, eq).trim();
                        String value = line.substring(eq + 1).trim();
                        if (System.getenv(key) == null && System.getProperty(key) == null) {
                            System.setProperty(key, value);
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Could not load .env file: " + e.getMessage());
            }
        }
    }
}
