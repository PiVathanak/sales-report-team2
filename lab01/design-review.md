# Design review

## Package responsibilities and coupling

Coupling counts below are **package-level**: each distinct imported application
package counts once, while `java.*` imports do not count. `Ce` is the number
of packages this package depends on; `Ca` is the number of packages that
depend on it. Instability is `I = Ce / (Ca + Ce)`.

| Package | Responsibility | Ce | Ca | Instability |
|---|---|---:|---:|---:|
| `edu.itc.salesreport.model` | Defines the sales transactions and report value objects used by the rest of the application. | 0 | 2 (`ingest`, `render`) | `0 / (2 + 0) = 0.00` |
| `edu.itc.salesreport.ingest` | Reads monthly CSV files, parses transactions, records rejected rows, and publishes load progress. | 1 (`model`) | 1 (root) | `1 / (1 + 1) = 0.50` |
| `edu.itc.salesreport.render` | Converts a `MonthlyReport` into a selected text, HTML, or CSV representation. | 1 (`model`) | 0 | `1 / (0 + 1) = 1.00` |
| `edu.itc.salesreport` (root) | Composes the application and presents load progress at the console. | 1 (`ingest`) | 0 | `1 / (0 + 1) = 1.00` |

The accepted coupling is `render -> model`: renderers must read the report
model to produce their output, and keeping that dependency one-way preserves
the model's independence. The `ingest -> model` dependency is similarly
necessary because parsed rows are domain transactions.

## QA-4 and the renderer hierarchy

The Strategy separates output-format decisions from report data and makes
adding a renderer localized. The current **sealed** `ReportRenderer` is useful
for the small, controlled set of formats: it documents the supported
strategies and enables the exhaustive `switch` without a `default`. However,
QA-4 asks for modifiability, and the planned PDF/XLSX additions require
editing the sealed interface's `permits` clause (and typically its factory).
An **open interface** would better serve that quality attribute because a new
format could be added as a separate implementation without changing the
existing abstraction. Therefore, sealed is a good safety choice for this lab's
closed set of formats, but an open interface is the better production choice
if output formats are expected to evolve independently.