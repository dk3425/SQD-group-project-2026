import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import restaurant.common.ZipCode;
import restaurant.data.ZipCodeReader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class ZipCodeReaderTest {

    private static final String HEADER = "Borough,Neighborhood,ZipCode";

    /** JUnit creates a fresh temp folder for each test and deletes it afterwards. */
    @TempDir
    Path tempDir;

    /** Writes the given lines to a CSV file in the temp folder and returns its path. */
    private String writeCsv(String... lines) throws IOException {
        Path file = tempDir.resolve("zip-codes.csv");
        Files.write(file, Arrays.asList(lines));
        return file.toString();
    }

    // ------------------------------------------------------------------
    // Happy path
    // ------------------------------------------------------------------

    @Test
    void readZipCodes() throws IOException {
        String file = writeCsv(
                HEADER,
                "Manhattan,Harlem,10027",
                "Brooklyn,Park Slope,11215");

        List<ZipCode> zipCodes = new ZipCodeReader(file).readZipCodes();

        assertEquals(2, zipCodes.size());

        ZipCode first = zipCodes.get(0);
        assertEquals("Manhattan", first.borough);
        assertEquals("Harlem", first.neighborhood);
        assertEquals("10027", first.zip);

        ZipCode second = zipCodes.get(1);
        assertEquals("Brooklyn", second.borough);
        assertEquals("Park Slope", second.neighborhood);
        assertEquals("11215", second.zip);
    }

    @Test
    void readZipCodes_headerOnly_returnsEmptyList() throws IOException {
        String file = writeCsv(HEADER);

        List<ZipCode> zipCodes = new ZipCodeReader(file).readZipCodes();

        assertNotNull(zipCodes);
        assertTrue(zipCodes.isEmpty());
    }

    @Test
    void readZipCodes_columnsInAnyOrder() throws IOException {
        String file = writeCsv(
                "ZipCode,Borough,Neighborhood",
                "10027,Manhattan,Harlem");

        ZipCode z = new ZipCodeReader(file).readZipCodes().get(0);

        assertEquals("Manhattan", z.borough);
        assertEquals("Harlem", z.neighborhood);
        assertEquals("10027", z.zip);
    }

    @Test
    void readZipCodes_ignoresExtraColumns() throws IOException {
        String file = writeCsv(
                "ID,Borough,Population,Neighborhood,ZipCode,Notes",
                "1,Manhattan,100000,Harlem,10027,busy");

        ZipCode z = new ZipCodeReader(file).readZipCodes().get(0);

        assertEquals("Manhattan", z.borough);
        assertEquals("Harlem", z.neighborhood);
        assertEquals("10027", z.zip);
    }

    @Test
    void readZipCodes_preservesFileOrder() throws IOException {
        String file = writeCsv(
                HEADER,
                "Manhattan,C Hood,10003",
                "Manhattan,A Hood,10001",
                "Manhattan,B Hood,10002");

        List<ZipCode> zipCodes = new ZipCodeReader(file).readZipCodes();

        assertEquals("C Hood", zipCodes.get(0).neighborhood);
        assertEquals("A Hood", zipCodes.get(1).neighborhood);
        assertEquals("B Hood", zipCodes.get(2).neighborhood);
    }

    @Test
    void readZipCodes_neighborhoodWithMultipleZips_keepsOneRowPerZip() throws IOException {
        String file = writeCsv(
                HEADER,
                "Manhattan,Harlem,10027",
                "Manhattan,Harlem,10030",
                "Manhattan,Harlem,10035");

        List<ZipCode> zipCodes = new ZipCodeReader(file).readZipCodes();

        assertEquals(3, zipCodes.size());
        assertEquals("Harlem", zipCodes.get(0).neighborhood);
        assertEquals("Harlem", zipCodes.get(1).neighborhood);
        assertEquals("Harlem", zipCodes.get(2).neighborhood);
        assertEquals("10027", zipCodes.get(0).zip);
        assertEquals("10030", zipCodes.get(1).zip);
        assertEquals("10035", zipCodes.get(2).zip);
    }

    // ------------------------------------------------------------------
    // Whitespace behavior (this reader does not trim)
    // ------------------------------------------------------------------

    /**
     * Documents current behavior: unlike restaurant.data.RatingReader and restaurant.data.InspectionReader, this reader
     * does NOT trim values, so surrounding whitespace is kept. RestaurantProcessor trims
     * zips and ignores case/whitespace on neighborhoods, so this is harmless downstream.
     * If you add trim() to the reader, update this test.
     */
    @Test
    void readZipCodes_doesNotTrimValues() throws IOException {
        String file = writeCsv(
                HEADER,
                " Manhattan , Harlem , 10027 ");

        ZipCode z = new ZipCodeReader(file).readZipCodes().get(0);

        assertEquals(" Manhattan ", z.borough);
        assertEquals(" Harlem ", z.neighborhood);
        assertEquals(" 10027 ", z.zip);
    }

    /** Documents current behavior: header names are compared exactly, without trimming. */
    @Test
    void readZipCodes_headerNamesAreNotTrimmed_throwsIllegalStateException() throws IOException {
        String file = writeCsv(
                " Borough , Neighborhood , ZipCode ",
                "Manhattan,Harlem,10027");

        assertThrows(IllegalStateException.class,
                () -> new ZipCodeReader(file).readZipCodes());
    }

    // ------------------------------------------------------------------
    // Blank fields and bad rows
    // ------------------------------------------------------------------

    @Test
    void readZipCodes_skipsRowsWithTooFewColumns() throws IOException {
        String file = writeCsv(
                HEADER,
                "Manhattan,Harlem,10027",
                "Manhattan,Chelsea",
                "Brooklyn,Park Slope,11215");

        List<ZipCode> zipCodes = new ZipCodeReader(file).readZipCodes();

        assertEquals(2, zipCodes.size());
        assertEquals("Harlem", zipCodes.get(0).neighborhood);
        assertEquals("Park Slope", zipCodes.get(1).neighborhood);
    }

    @Test
    void readZipCodes_skipsBlankLines() throws IOException {
        String file = writeCsv(
                HEADER,
                "Manhattan,Harlem,10027",
                "",
                "Brooklyn,Park Slope,11215");

        List<ZipCode> zipCodes = new ZipCodeReader(file).readZipCodes();

        assertEquals(2, zipCodes.size());
    }

    /**
     * Documents current behavior: split(",") drops trailing empty fields, so a row whose
     * LAST column (ZipCode here) is blank has too few tokens and is skipped.
     * If you change the reader to split(",", -1), update this test.
     */
    @Test
    void readZipCodes_blankTrailingField_rowIsSkipped() throws IOException {
        String file = writeCsv(
                HEADER,
                "Manhattan,Harlem,");

        List<ZipCode> zipCodes = new ZipCodeReader(file).readZipCodes();

        assertTrue(zipCodes.isEmpty());
    }

    @Test
    void readZipCodes_blankMiddleField_isKeptAsEmptyString() throws IOException {
        String file = writeCsv(
                HEADER,
                "Manhattan,,10027");

        List<ZipCode> zipCodes = new ZipCodeReader(file).readZipCodes();

        assertEquals(1, zipCodes.size());
        assertEquals("", zipCodes.get(0).neighborhood);
        assertEquals("10027", zipCodes.get(0).zip);
    }

    // ------------------------------------------------------------------
    // Errors
    // ------------------------------------------------------------------

    @Test
    void readZipCodes_nullFilename_throwsIllegalArgumentException() {
        ZipCodeReader reader = new ZipCodeReader(null);

        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, reader::readZipCodes);
        assertEquals("File name is not valid.", ex.getMessage());
    }

    @Test
    void readZipCodes_blankFilename_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> new ZipCodeReader("").readZipCodes());
        assertThrows(IllegalArgumentException.class,
                () -> new ZipCodeReader("   ").readZipCodes());
    }

    @Test
    void readZipCodes_fileNotFound_throwsIllegalStateException() {
        String missing = tempDir.resolve("does-not-exist.csv").toString();

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> new ZipCodeReader(missing).readZipCodes());
        assertEquals("File not found: " + missing, ex.getMessage());
    }

    @Test
    void readZipCodes_missingRequiredColumn_throwsIllegalStateException() throws IOException {
        // No ZipCode column
        String file = writeCsv(
                "Borough,Neighborhood",
                "Manhattan,Harlem");

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> new ZipCodeReader(file).readZipCodes());
        assertEquals("CSV header is missing required columns.", ex.getMessage());
    }

    @Test
    void readZipCodes_headerIsCaseSensitive() throws IOException {
        // The expected headers are "Borough", "Neighborhood", "ZipCode"
        String file = writeCsv(
                "BOROUGH,NEIGHBORHOOD,ZIPCODE",
                "Manhattan,Harlem,10027");

        assertThrows(IllegalStateException.class,
                () -> new ZipCodeReader(file).readZipCodes());
    }

    /**
     * Documents current behavior: a completely empty file has no header line, so
     * scanner.nextLine() throws NoSuchElementException (it isn't caught or converted).
     * If you later add a friendlier check, update the expected exception.
     */
    @Test
    void readZipCodes_emptyFile_throwsNoSuchElementException() throws IOException {
        String file = writeCsv(); // creates an empty file

        assertThrows(NoSuchElementException.class,
                () -> new ZipCodeReader(file).readZipCodes());
    }
}