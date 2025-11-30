package com.lujanita.bff.prompt;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.yaml.snakeyaml.Yaml;
import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

@Service
public class PromptConfigService {
    @Value("${bff.prompts.path:src/main/resources/prompts.yml}")
    private String promptsPath;
    private Map<String, Object> config;

    @PostConstruct
    public void loadPrompts() {
        try (InputStream in = Files.newInputStream(Paths.get(promptsPath))) {
            Yaml yaml = new Yaml();
            config = yaml.load(in);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo cargar prompts: " + promptsPath, e);
        }
    }

    public String getSystemPrompt(String role, String profile) {
        String prompt = getFromConfig("systemPrompt", role, profile);
        if (prompt == null) {
            prompt = (String) ((Map<?,?>)config.get("default")).get("systemPrompt");
        }
        return prompt;
    }

    public String getAssistantGuidelines(String role, String profile) {
        String prompt = getFromConfig("assistantGuidelines", role, profile);
        if (prompt == null) {
            prompt = (String) ((Map<?,?>)config.get("default")).get("assistantGuidelines");
        }
        return prompt;
    }

    private String getFromConfig(String key, String role, String profile) {
        if (role != null && config.containsKey("role")) {
            Map<?,?> roles = (Map<?,?>) config.get("role");
            if (roles.containsKey(role)) {
                Map<?,?> roleMap = (Map<?,?>) roles.get(role);
                if (roleMap.containsKey(key)) return (String) roleMap.get(key);
            }
        }
        if (profile != null && config.containsKey("profile")) {
            Map<?,?> profiles = (Map<?,?>) config.get("profile");
            if (profiles.containsKey(profile)) {
                Map<?,?> profileMap = (Map<?,?>) profiles.get(profile);
                if (profileMap.containsKey(key)) return (String) profileMap.get(key);
            }
        }
        return null;
    }

    public boolean isValidRole(String role) {
        if (role == null || role.isBlank()) return false;
        if (config == null || !config.containsKey("role")) return false;
        Map<?,?> roles = (Map<?,?>) config.get("role");
        return roles.containsKey(role);
    }

    public boolean isValidProfile(String profile) {
        if (profile == null || profile.isBlank()) return false;
        if (config == null || !config.containsKey("profile")) return false;
        Map<?,?> profiles = (Map<?,?>) config.get("profile");
        return profiles.containsKey(profile);
    }

    // Método para recargar prompts manualmente
    public void reload() { loadPrompts(); }
}
