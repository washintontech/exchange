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

        //        var loader = getClass().getClassLoader();
//        var url = loader.getResource("quickfix");
//        if (url == null) {
//            System.out.println("quickfix folder not found in classpath");
//        } else {
//            System.out.println("quickfix folder found: " + url);
//        }

//        InputStream is = getClass().getClassLoader().getResourceAsStream("quickfix/fix-session.cfg");
//        System.out.println("InputStream is null? " + (is == null));
//        if (is == null) throw new RuntimeException("fix-session.cfg not found");

//        return new SessionSettings(is);
//        System.out.println("===== Loaded FIX Sessions =====");
//        settings.sectionIterator().forEachRemaining(System.out::println);
//
//        System.out.println("===== All Config Settings =====");
//        settings.getDefaultProperties().forEach((k, v) -> System.out.println("[DEFAULT] " + k + "=" + v));
//
//        settings.sectionIterator().forEachRemaining(section -> {
//            System.out.println("Section: " + section);
//            try {
//                settings.getSessionProperties(section).forEach((k, v) ->
//                        System.out.println("    " + k + " = " + v)
//                );
//            } catch (Exception e) {
//                System.err.println("    Failed to read section: " + section);
//            }
//        });

//        return settings;
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
