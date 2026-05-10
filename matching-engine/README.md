# Matching Engine

## 📌 Overview

Core component responsible for aggressive matching buy and sell orders with minimal latency.

---

## 🎯 Responsibilities

* Consume New, Cancel or Replace orders
* Maintain order book (bid/ask) per symbol
* Match orders based on price-time priority
* Execute trades
* Executed trades are pushed to Chronicle queue (memory-mapped file), from where other modules picks the executed trade
  for post trade
  processing.

---

## ⚙️ Core Concepts

* Order Book (in-memory)
* Price-Time Priority
* Trade Execution

---

## 🧠 Algorithm

1. Incoming order added to order book per Symbol
2. Match against opposite side
3. Execute trades until exhausted or no match
4. Push executed trades to Chronicle queue.

---

## ⚡ Performance Considerations

* Poolable/reused Order object to minimize GC
* New Order objects taken from L1 cache to improve latency.
* If objects exhaust from L1 cache, objects are taken from RAM in LIFO basic to avoid cache miss.
* No shared object helps to avoid cordination/lock delays between threads.
* In-memory data structures (TreeMap)
* Chronicle queue (memory-mapped file) used for persistence to avoid latency.
* Thread abruptly shutdown will be auto-healed. No external trigger required.

---

## ⚠️ Concurrency Strategy

* Single-threaded matching per symbol. No locks used.
* No shared objects between threads.

---

## 📊 Trade-offs

* In-memory → fast but volatile
* Chronicle queue (memory-mapped file) for persistence/sharing across different modules.
* Single-thread → simpler but needs partitioning for scale

---

## 🔄 Failure Handling

* Thread abruptly shutdown will be auto-healed. No external trigger required.
* Replay from event log (future)
* Snapshotting order book (future)

---

## 🚀 Future Enhancements

* Multi-threaded matching to avoid hotspot
* Distributed order book to server multi-region exchange.

