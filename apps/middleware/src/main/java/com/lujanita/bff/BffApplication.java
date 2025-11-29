package com.lujanita.bff;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.annotation.PostConstruct;

@SpringBootApplication
public class BffApplication {

    @Autowired
    private com.lujanita.bff.ollama.OllamaClientService ollamaClientService;

    public static void main(String[] args) {
        SpringApplication.run(BffApplication.class, args);
    }

    @PostConstruct
    public void validateOllamaModelOnStartup() {
        ollamaClientService.validateModelAvailable();
    }
}
