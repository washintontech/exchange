//package com.washintontech.cache.service.grpc;
//
//import com.washintontech.cache.annotation.ServerService;
//import com.washintontech.cache.requestHandler.UpdateRequestHandler;
//import com.washintontech.cache.storage.StorageComponent;
//import com.washintontech.updater.SubscribedScriptsRequest;
//import com.washintontech.updater.UpdateResponse;
//import com.washintontech.updater.UpdaterServiceGrpc;
//import io.grpc.stub.StreamObserver;
//import org.springframework.stereotype.Service;
//
//@ServerService
//@Service
//public class UpdaterServiceExt extends UpdaterServiceGrpc.UpdaterServiceImplBase {
//
//    private final StorageComponent storageComponent;
//
//    public UpdaterServiceExt(final StorageComponent storageComponent) {
//        this.storageComponent = storageComponent;
//    }
//
//    @Override
//    public StreamObserver<SubscribedScriptsRequest> rate(StreamObserver<UpdateResponse> observer) {
//        return new UpdateRequestHandler(observer, storageComponent);
//    }
//}
