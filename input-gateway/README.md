# Input Gateway

## 📌 Overview

Handles incoming client/broker requests and forwards them to the matching engine.

---

## 🎯 Responsibilities

* Accept orders via FIX44
* Validate requests
* Compliance and Risk assessment
* Send requested order to Cache module to maintain record.
* Forward requested order to matching engine module.
* Create execution report for requested order and send it to chache module to maintain execution report.

---

## 🔌 APIs

* Accepts FIX44 messages:
    * NewOrderSingle
    * OrderCancelRequest
    * OrderCancelReplaceRequest

---

## ⚙️ Flow
                            
Client → Input Gateway → Matching Engine
AND
CACHE (Maintaining order requested/execution report)


---

## ⚠️ Failure Handling

* Input validation errors → rejected immediately, maintains requested Order and execution report.

---

## 🧠 Design Decisions

* Stateless service → easy scaling

---

## 🚀 Scalability

* Can scale horizontally behind load balancer
