package edu.itc.salesreport.render;

import edu.itc.salesreport.model.BranchSummary;
import edu.itc.salesreport.model.MonthlyReport;

import java.nio.charset.StandardCharsets;

public final class CsvReportRenderer implements ReportRenderer {

    @Override
    public String extension() {
        return "csv";
    }

    @Override
    public byte[] render(MonthlyReport report) {
        var sb = new StringBuilder("month,branch,revenue,discounts,receipts,average_basket\n");
        for (BranchSummary b : report.branches()) {
            line(sb, report, b);
        }
        line(sb, report, report.chain());
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static void line(StringBuilder sb, MonthlyReport report, BranchSummary b) {
        sb.append(String.join(",",
                report.month().toString(),
                b.branch(),
                b.revenue().toPlainString(),
                b.discounts().toPlainString(),
                String.valueOf(b.receipts()),
                b.averageBasket().toPlainString())).append('\n');
    }
}