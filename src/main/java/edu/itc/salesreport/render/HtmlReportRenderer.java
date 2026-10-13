package edu.itc.salesreport.render;

import edu.itc.salesreport.model.BranchSummary;
import edu.itc.salesreport.model.MonthlyReport;

import java.nio.charset.StandardCharsets;

public final class HtmlReportRenderer implements ReportRenderer {

    @Override
    public String extension() {
        return "html";
    }

    @Override
    public byte[] render(MonthlyReport report) {
        String title = "Angkor Mart monthly sales " + escape(report.month().toString());
        var sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n")
          .append("<meta charset=\"UTF-8\">\n")
          .append("<title>").append(title).append("</title>\n</head>\n<body>\n")
          .append("<h1>").append(title).append("</h1>\n")
          .append("<table>\n<thead><tr><th>Branch</th><th>Revenue</th>")
          .append("<th>Receipts</th><th>Average basket</th></tr></thead>\n<tbody>\n");
        for (BranchSummary b : report.branches()) {
            row(sb, b);
        }
        row(sb, report.chain());
        sb.append("</tbody>\n</table>\n</body>\n</html>\n");
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static void row(StringBuilder sb, BranchSummary b) {
        sb.append("<tr><td>").append(escape(b.branch())).append("</td><td>")
          .append(b.revenue().toPlainString()).append("</td><td>")
          .append(b.receipts()).append("</td><td>")
          .append(b.averageBasket().toPlainString()).append("</td></tr>\n");
    }

    /** Escapes &, < and >. The ampersand goes first so it is not escaped twice. */
    private static String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}