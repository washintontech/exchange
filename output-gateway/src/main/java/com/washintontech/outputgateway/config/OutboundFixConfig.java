package com.washintontech.outputgateway.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class OutboundFixConfig {

//    private OutboundFixApplication outboundFixApplication;
//
//    @Bean // TODO: Check if bean exposure required
//    public Initiator fixInboundInitiator() throws ConfigError {
//        SessionSettings settings = new SessionSettings("outboundFix.cfg");
//        final var socketInitiator = new SocketInitiator(
//                outboundFixApplication, new FileStoreFactory(settings), settings,
//                new ScreenLogFactory(settings), new DefaultMessageFactory());
//        socketInitiator.start();
//        return socketInitiator;
//    }
}
