# Cache Module

## Overview

Provides in-memory storage for order record and execution record.

## Responsibilities

* Fast read/write access
* Maintain requested orders before sending to Matching engine.
* Asynchronously consume post trade transactions report and store it in-memory

## Tech

* In-memory structures

## Trade-offs

* Faster access vs persistence risk

## Future

* Support read query from Broker/Client
