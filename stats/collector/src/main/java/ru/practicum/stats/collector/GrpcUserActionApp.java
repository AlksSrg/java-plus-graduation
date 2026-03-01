package ru.practicum.stats.collector;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import com.netflix.discovery.EurekaClient;
import org.springframework.beans.factory.annotation.Autowired;

@SpringBootApplication
@EnableDiscoveryClient
public class GrpcUserActionApp {

    public static void main(String[] args) {
        SpringApplication.run(GrpcUserActionApp.class, args);
    }

    @Component
    public static class EurekaDiagnostic implements ApplicationListener<ContextRefreshedEvent> {

        @Autowired(required = false)
        private EurekaClient eurekaClient;

        @Override
        public void onApplicationEvent(@NonNull ContextRefreshedEvent event) {
            System.out.println("========================================");
            if (eurekaClient == null) {
                System.err.println("❌ EUREKA CLIENT IS NULL");
            } else {
                System.out.println("✅ EUREKA CLIENT INITIALIZED");
                System.out.println("Eureka client applications: " + eurekaClient.getApplications());
            }
            System.out.println("========================================");
        }
    }
}