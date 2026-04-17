package com.kyotocuisine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;

@SpringBootApplication
public class KyotoCuisineApplication {
    public static void main(String[] args) {
        // Load .env BEFORE Spring starts so that property placeholders
        // like ${GMAIL_USERNAME} resolve correctly when Spring creates beans.
        loadEnvFile();
        SpringApplication.run(KyotoCuisineApplication.class, args);
    }

    /** Reads a local .env file and copies each KEY=VALUE pair into System properties. */
    private static void loadEnvFile() {
        Path envFile = Path.of(".env");
        if (!Files.exists(envFile)) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(envFile.toFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                int eq = line.indexOf('=');
                if (eq <= 0) continue;
                String key = line.substring(0, eq).trim();
                String value = line.substring(eq + 1).trim();
                if (System.getenv(key) == null && System.getProperty(key) == null) {
                    System.setProperty(key, value);
                }
            }
        } catch (Exception e) {
            System.err.println("Could not load .env file: " + e.getMessage());
        }
    }
}
