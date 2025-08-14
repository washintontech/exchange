//package com.washintontech.app;
//
//import com.washintontech.inputgateway.ShutDownListener;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//
//import java.time.ZoneId;
//import java.util.Locale;
//import java.util.TimeZone;
//
//@Slf4j
//@SpringBootApplication
//public class AppApplication {
//
//    public static void main(String[] args) {
//        //SpringApplication.run(AppApplication.class, args);
//
//        try {
//            log.info("""
//
//                        Starting Matching Engine ....        \s
//                    """);
//
//            final TimeZone jvmTimeZone = TimeZone.getTimeZone(ZoneId.of("UTC"));
//            TimeZone.setDefault(jvmTimeZone);
//            Locale.setDefault(Locale.US);
//
//            final var applicationContext = SpringApplication.run(AppApplication.class, args);
//            applicationContext.addApplicationListener(new ShutDownListener());
//
//            log.info("""
//
//                        Matching Engine Started....        \s
//                    """);
//
//        } catch (Exception exception) {
//            log.error("Failed to start Matching Engine: ", exception);
//            System.exit(1);
//        }
//    }
//
//}
