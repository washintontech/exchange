package com.washintontech.inputgateway.config;

import com.washintontech.cache.config.CacheConfig;
import com.washintontech.common.config.CommonConfig;
import com.washintontech.matchingEngine.config.MatchingEngineConfig;
import com.washintontech.outputgateway.config.OutputGatewayConfig;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({CommonConfig.class,
        CacheConfig.class,
        OutputGatewayConfig.class,
        MatchingEngineConfig.class})
@ComponentScan(basePackages = {"com.washintontech.inputgateway", "com.washintontech.common"})
public class ApplicationConfig {
}
