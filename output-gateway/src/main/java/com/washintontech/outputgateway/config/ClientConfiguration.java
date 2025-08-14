package com.washintontech.outputgateway.config;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClientConfiguration {
    public static final Integer PORT_NUMBER = 5001;

    @Bean
    public ManagedChannel managedChannel() {
        return ManagedChannelBuilder.forAddress("localhost", PORT_NUMBER)
                .usePlaintext()
                .build();
    }
}
