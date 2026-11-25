import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import restaurant.common.Inspection;
import restaurant.data.InspectionReader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class InspectionReaderTest {

    private static final String HEADER =
            "DBA,BORO,STREET,ZIPCODE,VIOLATION DESCRIPTION,CRITICAL FLAG,SCORE,GRADE,GRADE DATE";

    /** JUnit creates a fresh temp folder for each test and deletes it afterwards. */
    @TempDir
    Path tempDir;

    /** Writes the given lines to a CSV file in the temp folder and returns its path. */
    private String writeCsv(String... lines) throws IOException {
        Path file = tempDir.resolve("inspections.csv");
        Files.write(file, Arrays.asList(lines));
        return file.toString();
    }

    // ------------------------------------------------------------------
    // Happy path
    // ------------------------------------------------------------------

    @Test
    void readInspections() throws IOException {
        String file = writeCsv(
                HEADER,
                "JOE'S PIZZA,MANHATTAN,BROADWAY,10027,Food not protected,Y,9,A,2025-01-01",
                "TACO PLACE,BROOKLYN,5TH AVE,11215,Mice present,N,20,B,2025-02-15");

        List<Inspection> inspections = new InspectionReader(file).readInspections();

        assertEquals(2, inspections.size());

        Inspection first = inspections.get(0);
        assertEquals("JOE'S PIZZA", first.restaurant);
        assertEquals("MANHATTAN", first.borough);
        assertEquals("BROADWAY", first.street);
        assertEquals("10027", first.zip);
        assertEquals("Food not protected", first.violation);
        assertEquals("Y", first.flag);
        assertEquals("9", first.score);
        assertEquals("A", first.grade);
        assertEquals("2025-01-01", first.gradeDate);

        Inspection second = inspections.get(1);
        assertEquals("TACO PLACE", second.restaurant);
        assertEquals("BROOKLYN", second.borough);
        assertEquals("5TH AVE", second.street);
        assertEquals("11215", second.zip);
        assertEquals("Mice present", second.violation);
        assertEquals("N", second.flag);
        assertEquals("20", second.score);
        assertEquals("B", second.grade);
        assertEquals("2025-02-15", second.gradeDate);
    }

    @Test
    void readInspections_headerOnly_returnsEmptyList() throws IOException {
        String file = writeCsv(HEADER);

        List<Inspection> inspections = new InspectionReader(file).readInspections();

        assertNotNull(inspections);
        assertTrue(inspections.isEmpty());
    }

    @Test
    void readInspections_trimsWhitespaceAroundValues() throws IOException {
        String file = writeCsv(
                HEADER,
                "  JOE'S PIZZA , MANHATTAN ,  BROADWAY , 10027 , Some violation , Y , 9 , A , 2025-01-01 ");

        Inspection i = new InspectionReader(file).readInspections().get(0);

        assertEquals("JOE'S PIZZA", i.restaurant);
        assertEquals("MANHATTAN", i.borough);
        assertEquals("BROADWAY", i.street);
        assertEquals("10027", i.zip);
        assertEquals("Some violation", i.violation);
        assertEquals("Y", i.flag);
        assertEquals("9", i.score);
        assertEquals("A", i.grade);
        assertEquals("2025-01-01", i.gradeDate);
    }

    @Test
    void readInspections_columnsInAnyOrder() throws IOException {
        String file = writeCsv(
                "GRADE DATE,GRADE,SCORE,CRITICAL FLAG,VIOLATION DESCRIPTION,ZIPCODE,STREET,BORO,DBA",
                "2025-01-01,A,9,Y,Some violation,10027,BROADWAY,MANHATTAN,JOE'S PIZZA");

        Inspection i = new InspectionReader(file).readInspections().get(0);

        assertEquals("JOE'S PIZZA", i.restaurant);
        assertEquals("MANHATTAN", i.borough);
        assertEquals("BROADWAY", i.street);
        assertEquals("10027", i.zip);
        assertEquals("Some violation", i.violation);
        assertEquals("Y", i.flag);
        assertEquals("9", i.score);
        assertEquals("A", i.grade);
        assertEquals("2025-01-01", i.gradeDate);
    }

    @Test
    void readInspections_ignoresExtraColumns() throws IOException {
        String file = writeCsv(
                "CAMIS,DBA,BORO,STREET,ZIPCODE,PHONE,VIOLATION DESCRIPTION,CRITICAL FLAG,SCORE,GRADE,GRADE DATE,RECORD DATE",
                "123,JOE'S PIZZA,MANHATTAN,BROADWAY,10027,5551234,Some violation,Y,9,A,2025-01-01,2025-03-01");

        Inspection i = new InspectionReader(file).readInspections().get(0);

        assertEquals("JOE'S PIZZA", i.restaurant);
        assertEquals("10027", i.zip);
        assertEquals("A", i.grade);
        assertEquals("2025-01-01", i.gradeDate);
    }

    @Test
    void readInspections_preservesFileOrder() throws IOException {
        String file = writeCsv(
                HEADER,
                "C,MANHATTAN,S1,10001,v,N,1,A,2025-01-01",
                "A,MANHATTAN,S2,10002,v,N,2,A,2025-01-01",
                "B,MANHATTAN,S3,10003,v,N,3,A,2025-01-01");

        List<Inspection> inspections = new InspectionReader(file).readInspections();

        assertEquals("C", inspections.get(0).restaurant);
        assertEquals("A", inspections.get(1).restaurant);
        assertEquals("B", inspections.get(2).restaurant);
    }

    @Test
    void readInspections_sameRestaurantOnMultipleRows_keepsEveryRow() throws IOException {
        // The reader does not de-duplicate; that's the processor's concern.
        String file = writeCsv(
                HEADER,
                "JOE'S PIZZA,MANHATTAN,BROADWAY,10027,Violation one,Y,9,A,2025-01-01",
                "JOE'S PIZZA,MANHATTAN,BROADWAY,10027,Violation two,N,9,A,2025-01-01");

        List<Inspection> inspections = new InspectionReader(file).readInspections();

        assertEquals(2, inspections.size());
        assertEquals("Violation one", inspections.get(0).violation);
        assertEquals("Violation two", inspections.get(1).violation);
    }

    // ------------------------------------------------------------------
    // Quoted fields
    // ------------------------------------------------------------------

    @Test
    void readInspections_commaInsideQuotes_staysInOneField() throws IOException {
        String file = writeCsv(
                HEADER,
                "JOE'S PIZZA,MANHATTAN,BROADWAY,10027,\"Cold food, held above 41F, not discarded\",Y,9,A,2025-01-01");

        List<Inspection> inspections = new InspectionReader(file).readInspections();

        assertEquals(1, inspections.size());
        Inspection i = inspections.get(0);
        assertEquals("Cold food, held above 41F, not discarded", i.violation);
        // Fields after the quoted one must line up correctly
        assertEquals("Y", i.flag);
        assertEquals("9", i.score);
        assertEquals("A", i.grade);
        assertEquals("2025-01-01", i.gradeDate);
    }

    @Test
    void readInspections_escapedQuotesInsideQuotedField_areUnescaped() throws IOException {
        String file = writeCsv(
                HEADER,
                "JOE'S PIZZA,MANHATTAN,BROADWAY,10027,\"Food labeled \"\"fresh\"\" but old\",Y,9,A,2025-01-01");

        Inspection i = new InspectionReader(file).readInspections().get(0);

        assertEquals("Food labeled \"fresh\" but old", i.violation);
    }

    @Test
    void readInspections_quotedFieldWithoutComma_hasQuotesRemoved() throws IOException {
        String file = writeCsv(
                HEADER,
                "\"JOE'S PIZZA\",MANHATTAN,BROADWAY,10027,Some violation,Y,9,A,2025-01-01");

        Inspection i = new InspectionReader(file).readInspections().get(0);

        assertEquals("JOE'S PIZZA", i.restaurant);
    }

    @Test
    void readInspections_whitespaceInsideQuotes_isTrimmed() throws IOException {
        String file = writeCsv(
                HEADER,
                "JOE'S PIZZA,MANHATTAN,BROADWAY,10027,\"  Some violation  \",Y,9,A,2025-01-01");

        Inspection i = new InspectionReader(file).readInspections().get(0);

        assertEquals("Some violation", i.violation);
    }

    @Test
    void readInspections_quotedHeaderNames_areAccepted() throws IOException {
        String file = writeCsv(
                "\"DBA\",\"BORO\",\"STREET\",\"ZIPCODE\",\"VIOLATION DESCRIPTION\",\"CRITICAL FLAG\",\"SCORE\",\"GRADE\",\"GRADE DATE\"",
                "JOE'S PIZZA,MANHATTAN,BROADWAY,10027,Some violation,Y,9,A,2025-01-01");

        List<Inspection> inspections = new InspectionReader(file).readInspections();

        assertEquals(1, inspections.size());
        assertEquals("JOE'S PIZZA", inspections.get(0).restaurant);
    }

    // ------------------------------------------------------------------
    // Blank fields and bad rows
    // ------------------------------------------------------------------

    @Test
    void readInspections_blankTrailingGradeAndDate_areKeptAsEmptyStrings() throws IOException {
        // Unlike restaurant.data.RatingReader, this reader splits with limit -1, so trailing blanks survive.
        // This matches real data: pending inspections have no grade or grade date.
        String file = writeCsv(
                HEADER,
                "NEW PLACE,MANHATTAN,W 110TH ST,10025,Some violation,Y,12,,");

        List<Inspection> inspections = new InspectionReader(file).readInspections();

        assertEquals(1, inspections.size());
        assertEquals("12", inspections.get(0).score);
        assertEquals("", inspections.get(0).grade);
        assertEquals("", inspections.get(0).gradeDate);
    }

    @Test
    void readInspections_blankMiddleField_isKeptAsEmptyString() throws IOException {
        String file = writeCsv(
                HEADER,
                "JOE'S PIZZA,MANHATTAN,BROADWAY,10027,,Y,9,A,2025-01-01");

        Inspection i = new InspectionReader(file).readInspections().get(0);

        assertEquals("", i.violation);
        assertEquals("Y", i.flag);
    }

    @Test
    void readInspections_skipsRowsWithTooFewColumns() throws IOException {
        String file = writeCsv(
                HEADER,
                "GOOD ONE,MANHATTAN,S1,10027,v,Y,9,A,2025-01-01",
                "TOO SHORT,MANHATTAN,S2,10027",
                "GOOD TWO,BROOKLYN,S3,11215,v,N,20,B,2025-02-15");

        List<Inspection> inspections = new InspectionReader(file).readInspections();

        assertEquals(2, inspections.size());
        assertEquals("GOOD ONE", inspections.get(0).restaurant);
        assertEquals("GOOD TWO", inspections.get(1).restaurant);
    }

    @Test
    void readInspections_skipsBlankLines() throws IOException {
        String file = writeCsv(
                HEADER,
                "GOOD ONE,MANHATTAN,S1,10027,v,Y,9,A,2025-01-01",
                "",
                "GOOD TWO,BROOKLYN,S3,11215,v,N,20,B,2025-02-15");

        List<Inspection> inspections = new InspectionReader(file).readInspections();

        assertEquals(2, inspections.size());
    }

    // ------------------------------------------------------------------
    // Errors
    // ------------------------------------------------------------------

    @Test
    void readInspections_nullFilename_throwsIllegalArgumentException() {
        InspectionReader reader = new InspectionReader(null);

        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, reader::readInspections);
        assertEquals("File name is not valid.", ex.getMessage());
    }

    @Test
    void readInspections_blankFilename_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> new InspectionReader("").readInspections());
        assertThrows(IllegalArgumentException.class,
                () -> new InspectionReader("   ").readInspections());
    }

    @Test
    void readInspections_fileNotFound_throwsIllegalStateException() {
        String missing = tempDir.resolve("does-not-exist.csv").toString();

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> new InspectionReader(missing).readInspections());
        assertEquals("File not found: " + missing, ex.getMessage());
    }

    @Test
    void readInspections_missingRequiredColumn_throwsIllegalStateException() throws IOException {
        // No GRADE DATE column
        String file = writeCsv(
                "DBA,BORO,STREET,ZIPCODE,VIOLATION DESCRIPTION,CRITICAL FLAG,SCORE,GRADE",
                "JOE'S PIZZA,MANHATTAN,BROADWAY,10027,Some violation,Y,9,A");

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> new InspectionReader(file).readInspections());
        assertEquals("CSV header is missing required columns.", ex.getMessage());
    }

    @Test
    void readInspections_headerIsCaseSensitive() throws IOException {
        String file = writeCsv(
                "dba,boro,street,zipcode,violation description,critical flag,score,grade,grade date",
                "JOE'S PIZZA,MANHATTAN,BROADWAY,10027,Some violation,Y,9,A,2025-01-01");

        assertThrows(IllegalStateException.class,
                () -> new InspectionReader(file).readInspections());
    }

    /**
     * Documents current behavior: a completely empty file has no header line, so
     * scanner.nextLine() throws NoSuchElementException (it isn't caught or converted).
     * If you later add a friendlier check, update the expected exception.
     */
    @Test
    void readInspections_emptyFile_throwsNoSuchElementException() throws IOException {
        String file = writeCsv(); // creates an empty file

        assertThrows(NoSuchElementException.class,
                () -> new InspectionReader(file).readInspections());
    }
}