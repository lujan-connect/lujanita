package com.lujanita.bff.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private BffProperties bffProperties;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        BffProperties.Cors cors = bffProperties.getCors();
        if (cors != null) {
            String[] origins = cors.getAllowedOrigins() != null ? cors.getAllowedOrigins().split(",") : new String[]{"*"};
            registry.addMapping("/**")
                    .allowedOrigins(origins)
                    .allowedMethods(cors.getAllowedMethods() != null ? cors.getAllowedMethods() : "*")
                    .allowedHeaders(cors.getAllowedHeaders() != null ? cors.getAllowedHeaders() : "*")
                    .allowCredentials(cors.isAllowCredentials())
                    .maxAge(cors.getMaxAge() > 0 ? cors.getMaxAge() : 3600);
        } else {
            // Fallback
            registry.addMapping("/**")
                    .allowedOrigins("*")
                    .allowedMethods("*")
                    .allowedHeaders("*")
                    .allowCredentials(true);
        }
    }
}
