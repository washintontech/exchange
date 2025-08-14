//package com.washintontech.cache.requestHandler;
//
//import com.washintontech.cache.storage.StorageComponent;
//import com.washintontech.updater.SubscribedScriptsRequest;
//import com.washintontech.updater.UpdateResponse;
//import io.grpc.stub.StreamObserver;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//
//import java.util.concurrent.Executors;
//import java.util.concurrent.ScheduledExecutorService;
//import java.util.concurrent.TimeUnit;
//
//public class UpdateRequestHandler implements StreamObserver<SubscribedScriptsRequest> {
//    private static final Logger log = LogManager.getLogger(UpdateRequestHandler.class);
//    private final StreamObserver<UpdateResponse> responseObserver;
//    private final StorageComponent storageComponent;
//    private final ScheduledExecutorService scheduledExecutorService;
//
//    public UpdateRequestHandler(final StreamObserver<UpdateResponse> responseObserver,
//                                final StorageComponent storageComponent) {
//        this.responseObserver = responseObserver;
//        this.storageComponent = storageComponent;
//        this.scheduledExecutorService = Executors.newScheduledThreadPool(1);
//    }
//
//    @Override
//    public void onNext(final SubscribedScriptsRequest subscribedScriptsRequest) {
//        scheduledExecutorService.scheduleAtFixedRate(
//                () -> responseObserver.onNext(UpdateResponse.newBuilder()
//                        .putAllScriptPriceMap(storageComponent.getUpdatedValues(subscribedScriptsRequest))
//                        .build()), 0, 5, TimeUnit.SECONDS);
//    }
//
//    @Override
//    public void onError(final Throwable throwable) {
//        log.error(throwable);
//    }
//
//    @Override
//    public void onCompleted() {
//        responseObserver.onCompleted();
//        log.info("Completed");
//    }
//}
