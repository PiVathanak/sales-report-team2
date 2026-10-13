package edu.itc.salesreport.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record BranchSummary(String branch, BigDecimal revenue,
        BigDecimal discounts, long receipts,
        Map<String, BigDecimal> revenueByCategory,
        List<ProductTotal> topProducts,
        Map<PaymentMethod, BigDecimal> revenueByPayment) {

    public BranchSummary {
        Objects.requireNonNull(branch, "branch");
        Objects.requireNonNull(revenue, "revenue");
        Objects.requireNonNull(discounts, "discounts");
        revenueByCategory = Map.copyOf(revenueByCategory);
        topProducts = List.copyOf(topProducts);
        revenueByPayment = Map.copyOf(revenueByPayment);
    }

    /** revenue / receipts, scale 2, HALF_UP; 0.00 when there are no receipts. */
    public BigDecimal averageBasket() {
        if (receipts == 0) {
            return new BigDecimal("0.00");
        }
        return revenue.divide(BigDecimal.valueOf(receipts), 2, RoundingMode.HALF_UP);
    }
}   