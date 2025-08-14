//package com.washintontech.inputgateway.config;
//
//import com.washintontech.cache.annotation.ServerService;
//import io.grpc.BindableService;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import java.util.stream.Stream;
//
//@Configuration
//public class ServerConfiguration {
//    @Bean
//    public GrpcServer grpcServer(ApplicationContext applicationContext) {
//        return GrpcServer.create(bindableServices(applicationContext))
//                .start();
//    }
//
//    private Stream<BindableService> bindableServices(ApplicationContext applicationContext) {
//        return applicationContext.getBeansWithAnnotation(ServerService.class)
//                .values().stream()
//                .map(BindableService.class::cast);
//    }
//}
