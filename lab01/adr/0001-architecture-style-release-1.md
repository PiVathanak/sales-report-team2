# ADR-0001: Modular monolith batch application for release 1
Status: Accepted
Date: 2026-09-15 · Deciders: Seavhong CHEA, Pi-sereyvathanak

## Context
Angkor Mart operates three branches (PNH, REP, BTB) generating approximately 33,000 transactions per branch per day (~3 million rows per month). Head office requires a consolidated monthly sales report on the morning of day 2 of month M+1 with revenue totals, category breakdowns, basket statistics, and anomalies computed with high numerical accuracy (BigDecimal, half-up rounding).

Our architectural drivers dictate strict constraints:
- **QA-1 (Performance, H/H)**: The batch run over ~3 million rows must finish in 60 seconds or less on an 8-core server.
- **QA-3 (Availability, H/M)**: Branch WAN links periodically fail; the system must produce partial reports when files are missing and integrate late data without stalling or duplicating figures.
- **QA-5 (Security, H/M)**: Branch managers must only access their own branch's figures, whereas head-office analysts have chain-wide visibility.

Furthermore, we operate with a two-person engineering team with strict semester deadlines. We need an architectural style that eliminates distributed systems complexity, minimizes latency, maximizes testability, and allows rapid iteration.

## Decision
We will build Release 1 as a **modular monolith batch application** running in a single Java 25 virtual machine (JVM).

The application is structured into decoupled, cohesive packages (`model`, `ingest`, `engine`, `render`, `ui`) with strict unidirectional package dependencies verified by automated architecture tests (`DependencyRulesTest`). On day 2 at 06:00, the operating system scheduler (cron or systemd timer) initiates the batch process in-process. The engine parses daily branch CSV files from the local landing zone, performs validations, aggregates totals using multi-core parallel processing in memory, and writes output artifacts to a designated report store.

## Alternatives considered
- **Alternative A: Distributed event-driven microservices.**
  *Why it is attractive:* Decouples ingestion, aggregation, rendering, and notifications into independent deployable microservices communicating via a message broker (Kafka or RabbitMQ). Provides granular horizontal scalability and independent deployment lifecycles.
  *Why we did not choose it:* Adds substantial operational overhead (message broker cluster, schema registries, network hops, distributed tracing). Transferring 3 million individual events over a broker would take >60 seconds in network and serialization overhead alone, directly violating **QA-1**. It also violates **QA-6** by requiring complex integration harnesses for routine testing.
- **Alternative B: Multi-process pipeline.**
  *Why it is attractive:* Uses isolated OS processes (e.g., coordinator and per-branch worker processes connected via Unix domain sockets or anonymous pipes). A crash in one branch parser does not crash the coordinator process.
  *Why we did not choose it:* Inter-process communication (IPC) serialization adds unnecessary CPU overhead compared to direct in-memory JVM structures. Modern Java 25 running on an 8-core host can process 3 million rows in under 10 seconds in-process using parallel streams or virtual threads without IPC complexity.

## Consequences
+ **Performance (QA-1)**: Zero serialization, socket, or network overhead across architectural layers; memory-mapped and in-process stream parsing easily achieves the sub-60-second target on 8 cores.
+ **Testability (QA-6)**: The entire batch pipeline can be exercised deterministically in seconds via automated JUnit tests on local temporary files without mocks or network services.
+ **Operational Simplicity**: Single deployable artifact (`.jar`) executed via system scheduler with zero external broker or database daemon dependencies.
- **Scalability ceiling (QA-2)**: Bound to the vertical CPU and memory limits of a single host; cannot distribute batch processing across multiple server nodes without re-architecting boundaries.
- **Failure blast radius**: Uncaught JVM out-of-memory errors or JVM crashes terminate the entire monthly aggregation rather than halting only the failing branch worker.

Revisit when: Monthly transaction volume exceeds 10 million rows, or business requirements mandate real-time continuous streaming analytics instead of a monthly batch run.
