//package com.washintontech.inputgateway.config;
//
//import com.washintontech.common.component.FixApplication;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import quickfix.ConfigError;
//import quickfix.DefaultMessageFactory;
//import quickfix.FileStoreFactory;
//import quickfix.Initiator;
//import quickfix.ScreenLogFactory;
//import quickfix.SessionSettings;
//import quickfix.SocketInitiator;
//
//@Configuration
//public class InboundFixConfig {
//
//    private FixApplication fixApplication;
//
//    @Bean // TODO: Check if bean exposure required
//    public Initiator fixInboundInitiator() throws ConfigError {
//        SessionSettings settings = new SessionSettings("inboundFix.cfg");
//        final var socketInitiator = new SocketInitiator(
//                fixApplication, new FileStoreFactory(settings), settings,
//                new ScreenLogFactory(settings), new DefaultMessageFactory());
//        socketInitiator.start();
//        return socketInitiator;
//    }
//}
