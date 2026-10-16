# ADR-0002: Branch-initiated daily push with partial-failure policy
Status: Accepted
Date: 2026-09-18 · Deciders: Seavhong CHEA, Pi-sereyvathanak

## Context
Angkor Mart's three branches (PNH, REP, BTB) export one daily CSV file (`data/<yyyy-MM>/<BRANCH>-<yyyy-MM-dd>.csv`) of ~33,000 rows at store closing. Head office in Phnom Penh requires all files for the month to build the consolidated sales report by the morning of day 2.

Several operational and architectural forces drive this data flow:
- **Unreliable WAN links**: As identified in the brief, branch internet connections "fail now and then". A network disruption in Battambang (BTB) or Siem Reap (REP) at 23:00 must not prevent store closing or compromise data integrity.
- **QA-3 (Availability, H/M)**: If a branch file is delayed (e.g., REP link down on the last day of the month), head office must deliver a partial report by 07:00 on day 2 and subsequently merge late files without duplicating records.
- **QA-1 (Performance, H/H)**: Moving data across the WAN must be complete before the morning batch run so that the report engine can execute in <= 60 seconds against local storage.
- **Branch Security & Network Topology (QA-5)**: Branch store networks sit behind ISP carrier-grade NATs and firewalls. Opening inbound ports into store POS machines poses a severe security risk.

## Decision
We will use a **branch-initiated daily push over SFTP** (with TLS/SSH) combined with an explicit **partial-failure and idempotent late-merge policy**.

At store closing (approx. 22:00–22:30), each branch POS exports its daily transactions to a CSV file and calculates a SHA-256 checksum sidecar (`<BRANCH>-<yyyy-MM-dd>.csv.sha256`). A local script pushes both files to the head-office landing zone (`data/<yyyy-MM>/`) via outbound SFTP by 23:00.

### Failure and Idempotent Late-Merge Policy (QA-3)
1. **Detection & Grace Period**: At 06:00 on day 2 of month M+1, the head-office scheduler verifies file completeness (expected: 3 branches × 30 days = 90 files). If a file is missing (e.g., BTB's link failed at 23:30), the system enters a 30-minute grace period waiting until 06:30 for late transfer.
2. **Partial Report Publication**: If files remain missing at 06:30, operations is immediately paged via automated alert. The batch job executes on all available files and publishes a report before 07:00 clearly stamped with status: `PARTIAL (BTB missing for 2026-09-30)`. Analysts arriving at 08:00 see valid numbers for PNH and REP, with chain totals explicitly flagged as preliminary.
3. **Idempotent Late Merge**: When the branch link recovers (e.g., at 10:00), the branch push script resumes and completes the SFTP transfer. The landing zone file-watcher validates the SHA-256 checksum and immediately triggers a report rebuild.
4. **Deduplication / Idempotence**: Ingestion is keyed by file identity (`<BRANCH>-<date>.csv`) and validated against row `receipt_no`. Re-processing replaces the affected branch partition completely rather than appending rows. The rebuilt report replaces the partial report in `reports/` within 15 minutes, and a "Report Finalized" notification is dispatched.

## Alternatives considered
- **Alternative A: Head-office pull via scheduled polling.**
  *Why it is attractive:* Head office directly controls ingest scheduling and immediately senses unreachable branches.
  *Why we did not choose it:* Requires store networks to open inbound firewall ports and host public listening endpoints on branch POS systems, exposing retail counters to remote vulnerability attacks and conflicting with strict retail store security practices (**QA-5**).
- **Alternative B: Transactional event streaming via message broker (e.g., Kafka).**
  *Why it is attractive:* Immediate real-time streaming of receipts as sales occur throughout the trading day.
  *Why we did not choose it:* Retail POS availability cannot depend on continuous external WAN connectivity. When internet links drop, offline local queuing and broker buffering at three retail sites add excessive software complexity, operational maintenance, and higher infrastructure costs without addressing the monthly batch reporting requirement.

## Consequences
+ **Store security (QA-5)**: Branch stores make only outbound connections to head office over standard encrypted ports; no external listening services are exposed at branch stores.
+ **WAN resilience (QA-3)**: Network dropouts during retail trading or overnight upload do not stall local store cash registers; files buffer locally and push automatically upon link recovery.
+ **Deterministic idempotence**: Daily file naming with SHA-256 validation guarantees that re-runs and late arrivals overwrite existing daily slices without duplicate receipts or corrupted revenue figures.
- **Alerting overhead**: Operations staff must monitor missing-file notifications and manually follow up with branch personnel if links stay down past 06:30.
- **Transient downstream inconsistency**: Analysts and managers accessing reports between 07:00 and the late file arrival see partial figures, requiring downstream awareness that figures are subject to final revision.

Revisit when: Angkor Mart introduces real-time inventory management, online order fulfilment from store shelves, or dynamic pricing requiring live intraday streaming.
