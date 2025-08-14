package com.washintontech.outputgateway.config;

import com.washintontech.common.config.CommonConfig;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({CommonConfig.class, OutboundFixConfig.class})
@ComponentScan(basePackages = {"com.washintontech.common", "com.washintontech.outputgateway"})
public class OutputGatewayConfig {
}
