package com.bop.mock;

import com.bop.mock.config.LanderAuthProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(LanderAuthProperties.class)
public class MockExternalGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(MockExternalGatewayApplication.class, args);
    }
}
