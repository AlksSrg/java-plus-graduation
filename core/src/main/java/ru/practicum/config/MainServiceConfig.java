package ru.practicum.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.support.RetryTemplate;
import ru.practicum.StatsClient;

@Configuration
public class MainServiceConfig {
    @Value("${stats-client.id:stats-server}")
    private String statsClientId;

    private FixedBackOffPolicy fixedBackOffPolicy(long backOffPeriod) {
        FixedBackOffPolicy policy = new FixedBackOffPolicy();
        policy.setBackOffPeriod(backOffPeriod);
        return policy;
    }

    @Bean
    public RetryTemplate retryTemplate() {
        return RetryTemplate.builder()
                .maxAttempts(3)
                .customBackoff(fixedBackOffPolicy(2000L))
                .build();
    }

    @Bean
    public StatsClient getStatClient(DiscoveryClient discoveryClient,
                                     RetryTemplate retryTemplate) {
        return new StatsClient(statsClientId, discoveryClient, retryTemplate);
    }
}