package com.thenuka.socialweb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the application.
 * Running this class starts an embedded Tomcat server on http://localhost:8080
 */
@SpringBootApplication
public class SocialWebFoundationApplication {

    public static void main(String[] args) {
        SpringApplication.run(SocialWebFoundationApplication.class, args);
    }

}
