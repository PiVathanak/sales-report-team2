package edu.itc.salesreport.model;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record MonthlyReport(YearMonth month, List<BranchSummary> branches,
        Map<String, BigDecimal> revenueByCategory) {

    public MonthlyReport {
        Objects.requireNonNull(month, "month");
        Objects.requireNonNull(branches, "branches");
        Objects.requireNonNull(revenueByCategory, "revenueByCategory");
        branches = List.copyOf(branches);
        revenueByCategory = Map.copyOf(revenueByCategory);
    }
}