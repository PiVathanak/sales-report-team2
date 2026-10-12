package edu.itc.salesreport.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SaleTransactionTest {

    private static SaleTransaction sale(int quantity, String unitPrice, String discount) {
        return new SaleTransaction("PNH", LocalDate.of(2026, 9, 1), "PNH-000001",
                "SKU-1001", "Jasmine Rice 5kg", "Grocery", quantity,
                new BigDecimal(unitPrice), new BigDecimal(discount), PaymentMethod.CASH);
    }

    @Test
    void revenueRoundsHalfUp() {
        // 3 x 0.335 = 1.005 -> 1.01
        assertEquals(new BigDecimal("1.01"), sale(3, "0.335", "0").revenue());
    }

    @Test
    void rejectsInvalidValues() {
        assertAll(
            () -> assertThrows(IllegalArgumentException.class, () -> sale(0, "1.00", "0")),
            () -> assertThrows(IllegalArgumentException.class, () -> sale(1, "-1.00", "0")),
            () -> assertThrows(IllegalArgumentException.class, () -> sale(1, "1.00", "-0.10")),
            () -> assertThrows(IllegalArgumentException.class, () -> sale(2, "1.00", "2.01")),
            () -> assertThrows(NullPointerException.class, () -> new SaleTransaction(null,
                    LocalDate.of(2026, 9, 1), "r", "s", "p", "Grocery", 1,
                    BigDecimal.ONE, BigDecimal.ZERO, PaymentMethod.CASH)));
    }

    @Test
    void topProductsIsUnmodifiable() {
        List<ProductTotal> source = new ArrayList<>();
        source.add(new ProductTotal("SKU-1001", "Jasmine Rice 5kg", 2, new BigDecimal("13.00")));
        var summary = new BranchSummary("PNH", new BigDecimal("13.00"), 1, source);

        assertThrows(UnsupportedOperationException.class, () -> summary.topProducts().add(
                new ProductTotal("SKU-2001", "Mineral Water 1.5L", 1, new BigDecimal("0.45"))));

        source.clear(); // the summary keeps its own copy
        assertEquals(1, summary.topProducts().size());
    }
}