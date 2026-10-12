package edu.itc.salesreport.ingest;

import edu.itc.salesreport.model.PaymentMethod;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class CsvTransactionParserTest {

    private final TransactionParser parser = new CsvTransactionParser();

    @Test
    void parsesAValidRow() throws InvalidRowException {
        var t = parser.parse("PNH,2026-09-01,PNH-000123,"
                + "SKU-1001,Jasmine Rice 5kg,Grocery,2,6.50,0.00,KHQR");
        assertEquals("PNH", t.branch());
        assertEquals(LocalDate.of(2026, 9, 1), t.date());
        assertEquals("PNH-000123", t.receiptNo());
        assertEquals("SKU-1001", t.sku());
        assertEquals("Jasmine Rice 5kg", t.productName());
        assertEquals("Grocery", t.category());
        assertEquals(2, t.quantity());
        assertEquals(new BigDecimal("6.50"), t.unitPrice());
        assertEquals(new BigDecimal("0.00"), t.discount());
        assertEquals(PaymentMethod.KHQR, t.paymentMethod());
        assertEquals(new BigDecimal("13.00"), t.revenue());
    }

    @Test
    void parsesARowWithADiscount() throws InvalidRowException {
        var t = parser.parse("BTB,2026-09-04,BTB-000007,"
                + "SKU-3001,Dish Soap 500ml,Household,3,1.25,0.10,CARD");
        assertEquals(new BigDecimal("0.10"), t.discount());
        assertEquals(PaymentMethod.CARD, t.paymentMethod());
        assertEquals(new BigDecimal("3.65"), t.revenue());
    }

    static Stream<Arguments> invalidRows() {
        return Stream.of(
            Arguments.of("", "empty line"),
            Arguments.of("PNH,2026-09-01,PNH-1,SKU-1,Rice,Grocery,2,6.50,0.00", "10 fields"),
            Arguments.of("XXX,2026-09-01,PNH-1,SKU-1,Rice,Grocery,2,6.50,0.00,CASH", "branch"),
            Arguments.of("PNH,2026-09-31,PNH-1,SKU-1,Rice,Grocery,2,6.50,0.00,CASH", "date"),
            Arguments.of("PNH,2026-09-01,PNH-1,SKU-1,Rice,Toys,2,6.50,0.00,CASH", "category"),
            Arguments.of("PNH,2026-09-01,PNH-1,SKU-1,Rice,Grocery,two,6.50,0.00,CASH", "quantity"),
            Arguments.of("PNH,2026-09-01,PNH-1,SKU-1,Rice,Grocery,0,6.50,0.00,CASH", "quantity"),
            Arguments.of("PNH,2026-09-01,PNH-1,SKU-1,Rice,Grocery,2,-6.50,0.00,CASH", "unitPrice"),
            Arguments.of("PNH,2026-09-01,PNH-1,SKU-1,Rice,Grocery,2,6.50,-0.10,CASH", "discount"),
            Arguments.of("PNH,2026-09-01,PNH-1,SKU-6002,Pen,Stationery,1,0.30,0.50,CASH", "discount"),
            Arguments.of("PNH,2026-09-01,PNH-1,SKU-1,Rice,Grocery,2,6.50,0.00,PAYPAL", "payment_method")
        );
    }

    @ParameterizedTest
    @MethodSource("invalidRows")
    void rejectsInvalidRows(String line, String fieldName) {
        var e = assertThrows(InvalidRowException.class, () -> parser.parse(line));
        assertTrue(e.getMessage().contains(fieldName),
                "message should mention '" + fieldName + "': " + e.getMessage());
    }
}