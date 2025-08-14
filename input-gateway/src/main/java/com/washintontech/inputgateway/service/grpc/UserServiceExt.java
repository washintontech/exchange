//package com.washintontech.inputgateway.service.grpc;
//
//import com.washintontech.cache.annotation.ServerService;
//import com.washintontech.common.ResponseStatus;
//import com.washintontech.inputgateway.entity.User;
//import com.washintontech.inputgateway.service.UserService;
//import com.washintontech.matchingEngine.util.StringUtils;
//import com.washintontech.user.UserRequest;
//import com.washintontech.user.UserResponse;
//import com.washintontech.user.UserServiceGrpc;
//import io.grpc.stub.StreamObserver;
//import org.springframework.stereotype.Service;
//
//@ServerService
//@Service
////@RequiredArgsConstructor
//public class UserServiceExt extends UserServiceGrpc.UserServiceImplBase {
//
//    private final UserService userService;
//
//    public UserServiceExt(final UserService userService) {
//        this.userService = userService;
//    }
//
//    @Override
//    public void register(UserRequest request, StreamObserver<UserResponse> responseObserver) {
//        final var clientId = StringUtils.generateRandomUUID();
//        final var user = new User(clientId, request.getName(), request.getGovtId());
//        final var savedUser = userService.save(user);
//
//        final var userResponse = UserResponse.newBuilder()
//                .setStatus(ResponseStatus.ACCEPTED)
//                .setClientId(savedUser.clientId())
//                .setName(savedUser.name())
//                .setGovtId(savedUser.govtId())
//                .build();
//        responseObserver.onNext(userResponse);
//        responseObserver.onCompleted();
//    }
//}
