package edu.itc.salesreport.ingest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MonthLoaderTest {

    private static final String GOOD =
            "PNH,2026-09-01,PNH-000001,SKU-1001,Jasmine Rice 5kg,Grocery,2,6.50,0.00,CASH";
    private static final String BAD =
            "PNH,2026-09-01,PNH-000002,SKU-1001,Jasmine Rice 5kg,Grocery,two,6.50,0.00,CASH";

    @TempDir
    Path dataDir;

    private void writeFiles() throws IOException {
        Path month = Files.createDirectories(dataDir.resolve("2026-09"));
        Files.write(month.resolve("A.csv"),
                List.of(CsvTransactionParser.HEADER, GOOD, GOOD));
        Files.write(month.resolve("B.csv"),
                List.of(CsvTransactionParser.HEADER, GOOD, BAD));
    }

    @Test
    void publishesOneEventPerFileThenFinished() throws IOException {
        writeFiles();
        var loader = new MonthLoader(new CsvTransactionParser());
        List<LoadEvent> events = new ArrayList<>();
        loader.subscribe(events::add);

        loader.load(YearMonth.of(2026, 9), dataDir);

        assertEquals(3, events.size());
        var first = assertInstanceOf(LoadEvent.FileLoaded.class, events.get(0));
        assertEquals("A.csv", first.file().getFileName().toString());
        assertEquals(2, first.accepted());
        assertEquals(0, first.rejected());
        assertEquals(1, first.filesDone());
        assertEquals(2, first.filesTotal());

        var second = assertInstanceOf(LoadEvent.FileLoaded.class, events.get(1));
        assertEquals("B.csv", second.file().getFileName().toString());
        assertEquals(1, second.accepted());
        assertEquals(1, second.rejected());

        var finished = assertInstanceOf(LoadEvent.LoadFinished.class, events.get(2));
        assertEquals(3, finished.accepted());
        assertEquals(1, finished.rejected());
        assertEquals(List.of("B.csv:3 bad quantity 'two'"), loader.rejectedRows());
    }

    @Test
    void unsubscribedListenerReceivesNothing() throws IOException {
        writeFiles();
        var loader = new MonthLoader(new CsvTransactionParser());
        List<LoadEvent> events = new ArrayList<>();
        Runnable unsubscribe = loader.subscribe(events::add);
        unsubscribe.run();

        loader.load(YearMonth.of(2026, 9), dataDir);

        assertTrue(events.isEmpty());
    }
}