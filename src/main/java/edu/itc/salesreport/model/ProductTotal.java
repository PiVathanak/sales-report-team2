package edu.itc.salesreport.model;

import java.math.BigDecimal;
import java.util.Objects;

public record ProductTotal(String sku, String productName, int quantity, BigDecimal revenue) {

    public ProductTotal {
        Objects.requireNonNull(sku, "sku");
        Objects.requireNonNull(productName, "productName");
        Objects.requireNonNull(revenue, "revenue");
    }
}