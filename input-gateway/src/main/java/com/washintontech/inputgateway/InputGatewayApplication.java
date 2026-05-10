package com.washintontech.inputgateway;

import com.washintontech.inputgateway.config.ApplicationConfig;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import java.time.ZoneId;
import java.util.Locale;
import java.util.TimeZone;

@SpringBootApplication
@Import({ApplicationConfig.class})
@Log4j2
public class InputGatewayApplication {
    public static void main(String[] args) {
        try {
            final TimeZone jvmTimeZone = TimeZone.getTimeZone(ZoneId.of("UTC"));
            TimeZone.setDefault(jvmTimeZone);
            Locale.setDefault(Locale.US);

            final var applicationContext = SpringApplication.run(InputGatewayApplication.class, args);
            applicationContext.addApplicationListener(new ShutDownListener());
            log.info("Matching Engine Started.... ");

        } catch (Exception exception) {
            log.error("Failed to start Matching Engine: ", exception);
            System.exit(1);
        }
    }

}
