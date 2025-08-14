package com.washintontech.matchingEngine.util;

import reactor.core.publisher.Signal;

import java.util.function.Consumer;

public class LoggerUtil {
    public static <T> Consumer<Signal<T>> logOnNext(Consumer<T> logStatement) {
        return signal -> {
            if (signal.isOnNext()) {
                log(signal, s -> logStatement.accept(s.get()));
            }
        };
    }

    private static <T extends Signal<U>, U> void log(T signal, Consumer<T> consumer) {
        consumer.accept(signal);

//        List<MDC.MDCCloseable> fields = new ArrayList<>();
//        try {
//            for (DiagnosticHeaderName diagnosticHeader : DiagnosticHeaderName.values()) {
//                signal.getContext().getOrEmpty(diagnosticHeader.getName()).
//                        ifPresent(value -> fields.add(MDC.putCloseable(diagnosticHeader.getMdcName(),
//                                value.toString())));
//            }
//            consumer.accept(signal);
//        } catch (Exception exception) {
//            for (MDC.MDCCloseable field : fields) {
//                field.close();
//            }
//        }
//        fields.forEach(MDC.MDCCloseable::close);
    }
}
