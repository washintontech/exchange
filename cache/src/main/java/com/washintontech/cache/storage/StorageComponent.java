//package com.washintontech.cache.storage;
//
//import com.washintontech.updater.ResponsePayLoad;
//import com.washintontech.updater.SubscribedScriptsRequest;
//import org.springframework.stereotype.Component;
//
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.stream.Collectors;
//
//@Component
//public class StorageComponent {
//    private final Map<Integer, ResponsePayLoad> scriptResponsePayLoadMap; // Key = script, Value = Response
//
//    public StorageComponent() {
//        this.scriptResponsePayLoadMap = new ConcurrentHashMap<>();
//        // Dummy data
//        setDummyValues();
//    }
//
////    public ResponsePayLoad update(SubscribedScriptsRequest responsePayLoad) {
////        return scriptResponsePayLoadMap.put(responsePayLoad.getScriptValue(), responsePayLoad);
////    }
////
////    public ResponsePayLoad getUpdatedValues(final Integer scriptValue) {
////        return scriptResponsePayLoadMap.get(scriptValue);
////    }
//
//    public Map<String, ResponsePayLoad> getUpdatedValues(final SubscribedScriptsRequest subscribedScriptsRequest) {
//        return subscribedScriptsRequest.getUpdateRequestList()
//                .stream()
//                .collect(Collectors.toMap(
//                        scriptRequest -> scriptRequest.getScript().name(),
//                        scriptRequest -> this.scriptResponsePayLoadMap.get(scriptRequest.getScriptValue())));
//    }
//
//    private void setDummyValues() {
//        addKeyValueInScriptResponsePayLoadMap(1, 9999);
//        addKeyValueInScriptResponsePayLoadMap(2, 8888);
//        addKeyValueInScriptResponsePayLoadMap(3, 7777);
//        addKeyValueInScriptResponsePayLoadMap(4, 6666);
//    }
//
//    private void addKeyValueInScriptResponsePayLoadMap(final int scriptKey, final int price) {
//        scriptResponsePayLoadMap.put(scriptKey, ResponsePayLoad.newBuilder()
//                .setScriptValue(scriptKey)
//                .setCurrentPrice(price)
//                .build());
//    }
//}
