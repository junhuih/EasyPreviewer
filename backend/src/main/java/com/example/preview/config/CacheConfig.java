package com.example.preview.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
@EnableConfigurationProperties(PreviewProperties.class)
public class CacheConfig {

    @Bean
    CacheManager cacheManager(PreviewProperties previewProperties) {
        CaffeineCacheManager manager = new CaffeineCacheManager("previewSessions");
        manager.setCaffeine(
                Caffeine.newBuilder()
                        .maximumSize(1_000)
                        .expireAfterWrite(previewProperties.getCache().getTtlMinutes(), TimeUnit.MINUTES)
        );
        return manager;
    }
}

