# 🏦 Low-Latency Stock Exchange System

## 📌 Overview

A high-performance, low-latency stock exchange system designed to handle order ingestion, matching, and trade execution.

The system simulates real-world exchange behavior including:

* Order matching (bid/ask)
* Trade execution
* FIX-based communication from/to Broker
* Internal communication on Chronicle
  queue [ memory-mapped file which stores data directly to off-heap memory, making it free of GC overhead ]
* High concurrency and thread-safe processing
* Order report & Execution report generation.

---

## 🎯 Problem Statement

Design a scalable and low-latency system capable of:

* Handling high-frequency order submissions
* Matching orders in real-time
* Ensuring correctness under concurrency
* Supporting broker ↔ exchange communication

---

## 🏗️ Architecture

```
Client/Broker
     ↓
Input Gateway → Matching Engine 
     ↓                ↓
     ↓                ↓
    Cache    <-  Output Gateway                
     ^                ↓
     |                ↓ 
   Audit          Client/Broker
    
```

---

## 🧩 Modules

* **input-gateway** → Accepts incoming orders (FIX)
* **matching-engine** → Core engine for order matching
* **cache** → In-memory data (order book, state, execution report). Serves Read query from Client/Broker
* **output-gateway** → Generate post trade execution report, Publishes (FIX) execution reports to Client/Broker
* **audit** → Stores trades and events for reconciliation
* **common** → FIX configuration, Chronicle queue config, Shared models, utilities

---

## ⚙️ Tech Stack

* Java 21
* Spring Boot
* Kafka (event streaming)
* Redis / In-memory structures
* FIX Protocol (QuickFIX/J)

---

## 🚀 Key Features

* Low-latency order matching
* Thread-safe matching engine
* Low memory footprints
* Pluggable input/output gateways
* Audit trail for all trades

---

## 🧠 System Design Highlights

* Lock-free concurrency in matching engine
* In-memory order book for fast lookup
* Memory Map based communication between modules
* Separation of concerns via modular architecture

---

## 🔄 Order Flow (Simplified)

1. Order received via Input Gateway on FIX message
2. Validated, ensure compliance and risk assessment.
3. Requested order send to Cache to maintain records.
4. Requested forwarded to Matching Engine.
5. Matching Engine matches bid/ask order and publish transaction on Chronicle queue.
6. Cache module read the queue's message and stores execution orders
7. Output Gateway read the queue's message and send it to broker.
8. Audit module do reconciliation between accepted orders and execution report.

---

## ⚠️ Failure Handling

* Graceful degradation if downstream fails

---

## 📈 Scalability Considerations

* Horizontal scaling of gateways
* Partitioned order books (by symbol)

---

## ⚖️ Design Decisions

* Why in-memory matching?
  → Low latency requirement

* Why separated module connected with chronicle queue?
  → Loose coupling and scalability

* Why not DB for matching?
  → High latency, not suitable for trading

---

## 🛠️ How to Run

```bash
# Example
mvn clean install
```

---

## 🔮 Future Improvements

* Multi-asset support
* Distributed matching engine
* Advanced order types (IOC, FOK)
* Risk management system
* Latency benchmarking
* Fault tolerance

---

## 📊 Performance Goals

* Sub-millisecond matching latency (target)
* High throughput (10K+ orders/sec)
