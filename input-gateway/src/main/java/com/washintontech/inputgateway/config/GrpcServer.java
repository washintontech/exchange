//package com.washintontech.inputgateway.config;
//
//import io.grpc.BindableService;
//import io.grpc.Server;
//import io.grpc.ServerBuilder;
//import io.grpc.ServerServiceDefinition;
//import io.grpc.ServiceDescriptor;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//
//import java.util.stream.Stream;
//
//public class GrpcServer {
//    private static final Logger log = LogManager.getLogger(GrpcServer.class);
//    public static final Integer PORT_NUMBER = 5002;
//
//    private final Server server;
//
//    public GrpcServer(final Server server) {
//        this.server = server;
//    }
//
//    public static GrpcServer create(Stream<BindableService> serviceStream) {
//        var serverBuilder = ServerBuilder.forPort(PORT_NUMBER);
//        serviceStream.forEach(serverBuilder::addService);
//        return new GrpcServer(serverBuilder.build());
//    }
//
//    public GrpcServer start() {
//        var services = server.getServices()
//                .stream()
//                .map(ServerServiceDefinition::getServiceDescriptor)
//                .map(ServiceDescriptor::getName)
//                .toList();
//        try {
//            server.start();
//            log.info("server started. listening on port {}. services: {}", server.getPort(), services);
//            return this;
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    public void stop() {
//        server.shutdownNow();
//        log.debug("server stopped");
//    }
//}
