package com.washintontech.common.quickfix;

import com.washintontech.common.component.FixApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import quickfix.Application;
import quickfix.DataDictionary;
import quickfix.DefaultMessageFactory;
import quickfix.FileLogFactory;
import quickfix.FileStoreFactory;
import quickfix.LogFactory;
import quickfix.MessageFactory;
import quickfix.MessageStoreFactory;
import quickfix.SessionSettings;
import quickfix.SocketAcceptor;

import javax.annotation.PreDestroy;
import java.util.Objects;

@Configuration
public class FixConfig {

    private FixApplication fixApplication;
    private SocketAcceptor acceptor;

    @Bean
    public SessionSettings sessionSettings() throws Exception {
        return new SessionSettings(Objects.requireNonNull(
                getClass().getClassLoader().getResourceAsStream("quickfix/fix-session.cfg")));
    }

    @Bean
    public DataDictionary dataDictionary() throws Exception {
        return new DataDictionary(getClass().getClassLoader().getResourceAsStream("quickfix/FIX44.xml"));
    }

    @Bean
    public MessageStoreFactory messageStoreFactory(SessionSettings settings) {
        return new FileStoreFactory(settings);
    }

    @Bean
    public LogFactory logFactory(SessionSettings settings) {
        return new FileLogFactory(settings);
    }

    @Bean
    public MessageFactory messageFactory() {
        return new DefaultMessageFactory();
    }

    @Bean
    public Application fixApplication(@Lazy FixMessageListener listener) {
        return new FixApplication(listener);
    }

    @Bean
    public SocketAcceptor startFixEngine(@Lazy FixMessageListener listener, SessionSettings sessionSettings) throws Exception {
        final var socketAcceptor = new SocketAcceptor(
                fixApplication(listener),
                messageStoreFactory(sessionSettings),
                sessionSettings,
                logFactory(sessionSettings),
                messageFactory()
        );
        socketAcceptor.start();
        this.acceptor = socketAcceptor;
        return socketAcceptor;
    }

    @PreDestroy
    public void stopFixEngine() {
        if (acceptor != null) {
            acceptor.stop();
        }
    }

}
