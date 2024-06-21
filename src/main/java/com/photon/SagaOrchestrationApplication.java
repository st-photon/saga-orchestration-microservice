package com.photon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class SagaOrchestrationApplication {

    public static void main(String... args){
        SpringApplication.run(SagaOrchestrationApplication.class, args);
    }
}
