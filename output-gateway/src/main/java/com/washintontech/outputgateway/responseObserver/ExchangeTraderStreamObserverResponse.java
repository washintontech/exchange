package com.washintontech.outputgateway.responseObserver;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;

public class ExchangeTraderStreamObserverResponse implements StreamObserver<Empty> {
    @Override
    public void onNext(final Empty empty) {

    }

    @Override
    public void onError(final Throwable throwable) {

    }

    @Override
    public void onCompleted() {

    }
}
