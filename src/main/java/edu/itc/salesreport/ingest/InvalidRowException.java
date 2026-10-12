package edu.itc.salesreport.ingest;

/** Checked exception: a CSV line cannot become a valid SaleTransaction. */
public class InvalidRowException extends Exception {

    public InvalidRowException(String message) {
        super(message);
    }

    public InvalidRowException(String message, Throwable cause) {
        super(message, cause);
    }
}