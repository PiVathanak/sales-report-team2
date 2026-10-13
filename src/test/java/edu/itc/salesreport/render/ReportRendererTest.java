package edu.itc.salesreport.render;

import edu.itc.salesreport.model.MonthlyReport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReportRendererTest {

    @ParameterizedTest
    @ValueSource(strings = {"txt", "html", "csv"})
    void everyFormatShowsAllBranchesAndTheChainTotal(String ext) {
        String out = new String(ReportRenderer.forExtension(ext)
                .render(TestReports.september()), StandardCharsets.UTF_8);
        assertAll(
            () -> assertTrue(out.contains("PNH"), "PNH in " + ext),
            () -> assertTrue(out.contains("REP"), "REP in " + ext),
            () -> assertTrue(out.contains("BTB"), "BTB in " + ext),
            () -> assertTrue(out.contains("2300.50"), "chain total in " + ext));
    }

    @Test
    void csvHasTheExactBranchLine() {
        String csv = new String(new CsvReportRenderer().render(TestReports.september()),
                StandardCharsets.UTF_8);
        assertTrue(csv.lines().toList().contains("2026-09,REP,800.50,0.00,50,16.01"));
    }

    @Test
    void htmlEscapesSpecialCharacters() {
        var report = new MonthlyReport(YearMonth.of(2026, 9),
                List.of(TestReports.branch("A&<B>", "10.00", 1)),
                TestReports.branch("ALL", "10.00", 1), List.of());
        String html = new String(new HtmlReportRenderer().render(report), StandardCharsets.UTF_8);
        assertTrue(html.contains("A&amp;&lt;B&gt;"));
        assertFalse(html.contains("A&<B>"));
    }

    @Test
    void unknownExtensionIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> ReportRenderer.forExtension("pdf"));
    }

    @Test
    void switchOverRendererIsExhaustiveWithoutDefault() {
        ReportRenderer renderer = ReportRenderer.forExtension("csv");
        String name = switch (renderer) {
            case TextReportRenderer t -> "text";
            case HtmlReportRenderer h -> "html";
            case CsvReportRenderer c -> "csv";
        };
        assertEquals("csv", name);
    }
}