package com.thenuka.socialweb.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * Uploaded avatar images are saved OUTSIDE the compiled project (in an
 * "uploads" folder next to where the app runs), not inside src/main/resources.
 * This tells Spring "when someone requests /uploads/xyz.jpg, fetch it from
 * that external folder" - otherwise the browser would get a 404.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadsPath = Paths.get("uploads").toAbsolutePath().toUri().toString();

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadsPath);
    }
}
