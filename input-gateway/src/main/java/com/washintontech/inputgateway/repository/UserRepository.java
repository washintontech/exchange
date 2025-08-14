//package com.washintontech.inputgateway.repository;
//
//import com.washintontech.inputgateway.entity.User;
//import jakarta.validation.constraints.NotNull;
//import org.springframework.stereotype.Repository;
//
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
//@Repository
//public class UserRepository {
//
//    private final Map<String, User> userMap;
//
//    public UserRepository() {
//        userMap = new ConcurrentHashMap<>();
//    }
//
//    @NotNull
//    public User save(final User user) {
//        userMap.put(user.clientId(), user);
//        return userMap.get(user.clientId());
//    }
//
//}
