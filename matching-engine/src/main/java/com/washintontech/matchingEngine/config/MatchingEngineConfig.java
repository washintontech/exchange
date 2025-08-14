package com.washintontech.matchingEngine.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {"com.washintontech.common", "com.washintontech.matchingEngine"})
public class MatchingEngineConfig {
}
