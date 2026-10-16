# Lab-01: architecture foundations of the sales report system

Team: Team 2 · Members: Seavhong CHEA (cseavhong22), Pi-sereyvathanak (PiVathanak)

## Files produced in this lab

| File | Purpose | Author(s) |
|------|---------|-----------|
| `lab01/quality-scenarios.md` | QA-1..QA-6, utility tree, architectural drivers | Pi-sereyvathanak, Seavhong CHEA |
| `lab01/c4-context.mmd`, `.png` | C4 level 1 context diagram (source & exported PNG) | Pi-sereyvathanak |
| `lab01/c4-container.mmd`, `.png` | C4 level 2 container diagram for release 1 (source & exported PNG) | Pi-sereyvathanak |
| `lab01/c4-notes.md` | Notes on C4 diagrams, container-to-QA mappings, client questions | Pi-sereyvathanak |
| `lab01/design-review.md` | Package responsibilities, coupling (Ca, Ce, I), and renderer evaluation | Seavhong CHEA |
| `lab01/adr/0001-architecture-style-release-1.md` | ADR-0001: Modular monolith batch application for release 1 | Seavhong CHEA |
| `lab01/adr/0002-branch-to-head-office-data-flow.md` | ADR-0002: Branch-initiated daily push with partial-failure policy | Pi-sereyvathanak, Seavhong CHEA |
| `lab01/atam.md` | ATAM-style trade-off analysis of three candidate architectures (Challenge) | Seavhong CHEA, Pi-sereyvathanak |
| `pom.xml`, `src/**`, `tools/SampleData.java` | sales-report 0.1.0-SNAPSHOT: canonical model, CSV parser, renderers, loader, tests | Pi-sereyvathanak, Seavhong CHEA |

## Inputs reused from earlier labs

None (first lab).

## Change log

| Date | Lab | Change | Commit or tag |
|------|-----|--------|---------------|
| 2026-10-16 | Lab-01 | Project created: model, parser, renderers, loader; 2 ADRs; ATAM analysis | `lab-01` |
