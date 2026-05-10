# Output Gateway

## Overview

Post trade activities.
Consume transactions and send execution report to Broker.

## Responsibilities

* Asynchronously consume traded transaction from matching engine on Chronicle queue.
* Build execution report and sent it to Cache module.
* Send execution report to Broker on FIX protocol.

## Flow

Matching Engine → Output Gateway → Cache Module(Execution report maintenance) & Common Module(FIX communication)

## Scalability

* Async processing
* Queue processing
