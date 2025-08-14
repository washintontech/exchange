package com.washintontech.matchingEngine.service;

import org.springframework.stereotype.Service;

@Service
public class TradeService {

    // shared map across JVMs/processes
//    private final Map<Script, Map<Action, PriorityQueue<Order>>> orderBook;
//
//    private final Map<Script, Map<Action, PriorityQueue<Order>>> orderBookCronicleMap;
//
//    // shared map across JVMs/processes
//    private final Map<Script, Map<Action, List<Transaction>>> tradeBook;
//    private final List<Transaction> transactions;
//
//    private final TradeRepository tradeRepository;
//
//
//    public TradeService(final TradeRepository tradeRepository) {
//        this.transactions = new ArrayList<>();
//        this.tradeBook = new HashMap<>();
//        this.tradeRepository = tradeRepository;
//        this.orderBook = new HashMap<>();
//        this.orderBookCronicleMap = getCronicalMap();
//    }
//
//    private Map<Script, Map<Action, PriorityQueue<Order>>> getCronicalMap() {
//        File file = new File("my-map.dat");
//        try (ChronicleMap<String, String> map = ChronicleMap
//                .of(String.class, String.class)
//                .name("my-map")
//                .entries(1_000_000)
//                .averageKey("averageKeyLength")
//                .averageValue("averageValueLength")
//                .createPersistedTo(file)) {
//
//        } catch (IOException ioException) {
//
//        }
//
//
//        return null;
//    }
//
//    void bid() {
//        final var order = new Order("orderID", 10, 100.00f, Script.AMAZON,
//                TimeUtils.generateInstantEpochNanoSec());
//        orderBook.computeIfAbsent(order.getScript(), script -> new HashMap<>());
//        orderBook.compute(order.getScript(), actionPriorityQueueMapByActionOrder().apply(Action.BID, order));
//    }
//
//    void ask() {
//        final var order = new Order("orderID", 10, 100.00f, Script.AMAZON,
//                TimeUtils.generateInstantEpochNanoSec());
//        orderBook.computeIfAbsent(order.getScript(), script -> new HashMap<>());
//        orderBook.compute(order.getScript(), actionPriorityQueueMapByActionOrder().apply(Action.ASK, order));
//    }
//
//    public void transact() {
//        orderBook.forEach((script, actionPriorityQueueMap) -> {
//            final var bidOrders = actionPriorityQueueMap.get(Action.BID);
//            final var askOrders = actionPriorityQueueMap.get(Action.ASK);
//            while (bidOrders.peek().getPrice() >= askOrders.peek().getPrice()) {
//                final var bidOrder = bidOrders.peek();
//                final var askOrder = askOrders.peek();
//                final var transactionId = StringUtils.generateRandomUUID();
//                final var transactionTime = TimeUtils.generateInstantEpochNanoSec();
//                final var transactionPrice = NumberUtils.midValue(
//                        bidOrder.getPrice(), askOrder.getPrice(), 2, 0.05f);
//
//                final Transaction bidTransaction;
//                final Transaction askTransaction;
//                final var pendingBidQty = bidOrder.pendingQty();
//                final var pendingAskQty = askOrder.pendingQty();
//                if (pendingBidQty <= pendingAskQty) {
//                    bidOrders.remove(bidOrder);
//                    if (pendingBidQty == pendingAskQty) {
//                        askOrders.remove(askOrder);
//                    } else {
//                        askOrder.updateExecutedQuantity(pendingBidQty);
//                    }
//
//                    bidTransaction = new Transaction(transactionId, transactionTime, bidOrder.getOrderId(),
//                            pendingBidQty, transactionPrice, bidOrder.getScript());
//                    askTransaction = new Transaction(transactionId, transactionTime, askOrder.getOrderId(),
//                            pendingBidQty, transactionPrice, askOrder.getScript());
//                } else {
//                    askOrders.remove(askOrder);
//                    bidOrder.updateExecutedQuantity(pendingAskQty);
//
//                    bidTransaction = new Transaction(transactionId, transactionTime, bidOrder.getOrderId(),
//                            askOrder.getTotalQuantity(), transactionPrice, bidOrder.getScript());
//                    askTransaction = new Transaction(transactionId, transactionTime, askOrder.getOrderId(),
//                            askOrder.getTotalQuantity(), transactionPrice, askOrder.getScript());
//                }
//                addTransactionsInTradeBook(bidTransaction, askTransaction, bidOrder);
//            }
//        });
//    }
//
//    private void addTransactionsInTradeBook(final Transaction bidTransaction, final Transaction askTransaction, final Order bidOrder) {
//        transactions.add(bidTransaction);
//        transactions.add(askTransaction);
//
//        tradeBook.computeIfAbsent(bidOrder.getScript(), script -> new HashMap<>());
//        tradeBook.compute(bidOrder.getScript(), (script, actionTransactionsMap) -> {
//            addTransaction(actionTransactionsMap, Action.BID, bidTransaction);
//            addTransaction(actionTransactionsMap, Action.ASK, askTransaction);
//            return actionTransactionsMap;
//        });
//    }
//
//    private static void addTransaction(final Map<Action, List<Transaction>> actionTransactionsMap, final Action act, final Transaction transaction) {
//        actionTransactionsMap.computeIfAbsent(act, action -> new ArrayList<>())
//                .add(transaction);
//    }

//    private static float midPrice(final float price, final float price1) {
//        BigDecimal bd1 = BigDecimal.valueOf(price);
//        BigDecimal bd2 = BigDecimal.valueOf(price1);
//        BigDecimal multiple = BigDecimal.valueOf(0.05);
//        return bd1.add(bd2)
//                .divide(BigDecimal.valueOf(2), 10, RoundingMode.HALF_UP)
//                .divide(multiple, 0, RoundingMode.HALF_UP).multiply(multiple)
//                .setScale(2, RoundingMode.HALF_UP).floatValue();
//    }


//    private static BiFunction<Action, Order, BiFunction<Script, Map<Action, PriorityQueue<Order>>, Map<Action, PriorityQueue<Order>>>> actionPriorityQueueMapByActionOrder() {
//        return (action, order) -> (script, priorityQueueMap) -> {
//            priorityQueueMap.computeIfAbsent(action, act -> new PriorityQueue<>(getOrderComparator(act)));
//            priorityQueueMap.compute(action, addOrderInPriorityQueue().apply(order));
//            return priorityQueueMap;
//        };
//    }
//
//    private static Function<Order, BiFunction<Action, PriorityQueue<Order>, PriorityQueue<Order>>> addOrderInPriorityQueue() {
//        return (order) -> (action, queue) -> {
//            queue.add(order);
//            return queue;
//        };
//    }
//
//    private static Comparator<Order> getOrderComparator(final Action act) {
//        return Action.BID.equals(act)
//                ? getBidComparator()
//                : getAskComparator();
//    }
//
//    private static Comparator<Order> getBidComparator() {
//        return (order1, order2) -> {
//            if (order1.getPrice() > order2.getPrice()) {
//                return -1;
//            } else if (order1.getPrice() < order2.getPrice()) {
//                return 1;
//            }
//
//            return tieBreaker(order1.getInstantEpochNanoSec(), order2.getInstantEpochNanoSec());
//        };
//    }
//
//    private static Comparator<Order> getAskComparator() {
//        return (order1, order2) -> {
//            if (order1.getPrice() < order2.getPrice()) {
//                return -1;
//            } else if (order1.getPrice() > order2.getPrice()) {
//                return 1;
//            }
//
//            return tieBreaker(order1.getInstantEpochNanoSec(), order2.getInstantEpochNanoSec());
//        };
//    }
//
//    private static int tieBreaker(long orderTime1, long orderTime2) {
//        return orderTime1 <= orderTime2
//                ? -1
//                : 1;
//    }





/*    void ask() {
        final var order = new Order("orderID", 10, 100.00f, Script.AMAZON,
                TimeUtils.generateInstantEpochNanoSec());
        orderBook.compute(order.getOrderId(), (orderId, priorityQueueMap) -> {
            priorityQueueMap = priorityQueueMap == null
                    ? new HashMap<>()
                    : priorityQueueMap;
            priorityQueueMap.compute(ASK, (action, queue) -> {
                queue = queue == null
                        ? new PriorityQueue<>(getAskComparator())
                        : queue;
                queue.add(order);
                return queue;
            });
            return priorityQueueMap;
        });
    }*/


    //        return new Supplier<Comparator<Order>>() {
//            @Override
//            public Comparator<Order> get() {
//                return (order1, order2) -> {
//                    if (order1.getPrice() > order2.getPrice()) {
//                        return -1;
//                    } else if (order1.getPrice() < order2.getPrice()) {
//                        return 1;
//                    }
//
//                    return tieBreaker(order1.getInstantEpochNanoSec(), order2.getInstantEpochNanoSec());
//                };
//            }
//        };

//        return (order1, order2) -> {
//            if (order1.getPrice() > order2.getPrice()) {
//                return -1;
//            } else if (order1.getPrice() < order2.getPrice()) {
//                return 1;
//            }
//
//            return tieBreaker(order1.getInstantEpochNanoSec(), order2.getInstantEpochNanoSec());
//        };

}
