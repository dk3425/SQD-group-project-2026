import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import restaurant.common.Rating;
import restaurant.data.RatingReader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class RatingReaderTest {

    private static final String HEADER = "DBA,ZIPCODE,CUISINE DESCRIPTION,RATING,PRICE LEVEL";

    /** JUnit creates a fresh temp folder for each test and deletes it afterwards. */
    @TempDir
    Path tempDir;

    /** Writes the given lines to a CSV file in the temp folder and returns its path. */
    private String writeCsv(String... lines) throws IOException {
        Path file = tempDir.resolve("ratings.csv");
        Files.write(file, Arrays.asList(lines));
        return file.toString();
    }

    // ------------------------------------------------------------------
    // Happy path
    // ------------------------------------------------------------------

    @Test
    void readRatings() throws IOException {
        String file = writeCsv(
                HEADER,
                "JOE'S PIZZA,10027,Pizza,4.5,$$",
                "TACO PLACE,10025,Mexican,4.0,$");

        List<Rating> ratings = new RatingReader(file).readRatings();

        assertEquals(2, ratings.size());

        Rating first = ratings.get(0);
        assertEquals("JOE'S PIZZA", first.restaurant);
        assertEquals("10027", first.zip);
        assertEquals("Pizza", first.cuisine);
        assertEquals("4.5", first.rating);
        assertEquals("$$", first.priceLevel);

        Rating second = ratings.get(1);
        assertEquals("TACO PLACE", second.restaurant);
        assertEquals("10025", second.zip);
        assertEquals("Mexican", second.cuisine);
        assertEquals("4.0", second.rating);
        assertEquals("$", second.priceLevel);
    }

    @Test
    void readRatings_headerOnly_returnsEmptyList() throws IOException {
        String file = writeCsv(HEADER);

        List<Rating> ratings = new RatingReader(file).readRatings();

        assertNotNull(ratings);
        assertTrue(ratings.isEmpty());
    }

    @Test
    void readRatings_trimsWhitespaceAroundValues() throws IOException {
        String file = writeCsv(
                HEADER,
                "  JOE'S PIZZA , 10027 ,  Pizza , 4.5 ,  $$  ");

        Rating r = new RatingReader(file).readRatings().get(0);

        assertEquals("JOE'S PIZZA", r.restaurant);
        assertEquals("10027", r.zip);
        assertEquals("Pizza", r.cuisine);
        assertEquals("4.5", r.rating);
        assertEquals("$$", r.priceLevel);
    }

    @Test
    void readRatings_headerNamesAreTrimmed() throws IOException {
        String file = writeCsv(
                " DBA , ZIPCODE , CUISINE DESCRIPTION , RATING , PRICE LEVEL ",
                "JOE'S PIZZA,10027,Pizza,4.5,$$");

        List<Rating> ratings = new RatingReader(file).readRatings();

        assertEquals(1, ratings.size());
        assertEquals("JOE'S PIZZA", ratings.get(0).restaurant);
    }

    @Test
    void readRatings_columnsInAnyOrder() throws IOException {
        String file = writeCsv(
                "PRICE LEVEL,RATING,CUISINE DESCRIPTION,ZIPCODE,DBA",
                "$$,4.5,Pizza,10027,JOE'S PIZZA");

        Rating r = new RatingReader(file).readRatings().get(0);

        assertEquals("JOE'S PIZZA", r.restaurant);
        assertEquals("10027", r.zip);
        assertEquals("Pizza", r.cuisine);
        assertEquals("4.5", r.rating);
        assertEquals("$$", r.priceLevel);
    }

    @Test
    void readRatings_ignoresExtraColumns() throws IOException {
        String file = writeCsv(
                "ID,DBA,BORO,ZIPCODE,CUISINE DESCRIPTION,RATING,PRICE LEVEL,NOTES",
                "1,JOE'S PIZZA,MANHATTAN,10027,Pizza,4.5,$$,great");

        Rating r = new RatingReader(file).readRatings().get(0);

        assertEquals("JOE'S PIZZA", r.restaurant);
        assertEquals("10027", r.zip);
        assertEquals("Pizza", r.cuisine);
        assertEquals("4.5", r.rating);
        assertEquals("$$", r.priceLevel);
    }

    @Test
    void readRatings_preservesFileOrder() throws IOException {
        String file = writeCsv(
                HEADER,
                "C,10001,Cafe,3.0,$",
                "A,10002,Cafe,4.0,$",
                "B,10003,Cafe,5.0,$");

        List<Rating> ratings = new RatingReader(file).readRatings();

        assertEquals("C", ratings.get(0).restaurant);
        assertEquals("A", ratings.get(1).restaurant);
        assertEquals("B", ratings.get(2).restaurant);
    }

    // ------------------------------------------------------------------
    // Bad / short rows
    // ------------------------------------------------------------------

    @Test
    void readRatings_skipsRowsWithTooFewColumns() throws IOException {
        String file = writeCsv(
                HEADER,
                "GOOD ONE,10027,Pizza,4.5,$$",
                "TOO SHORT,10027,Pizza",
                "GOOD TWO,10025,Thai,4.0,$");

        List<Rating> ratings = new RatingReader(file).readRatings();

        assertEquals(2, ratings.size());
        assertEquals("GOOD ONE", ratings.get(0).restaurant);
        assertEquals("GOOD TWO", ratings.get(1).restaurant);
    }

    @Test
    void readRatings_skipsBlankLines() throws IOException {
        String file = writeCsv(
                HEADER,
                "GOOD ONE,10027,Pizza,4.5,$$",
                "",
                "GOOD TWO,10025,Thai,4.0,$");

        List<Rating> ratings = new RatingReader(file).readRatings();

        assertEquals(2, ratings.size());
    }

    /**
     * Documents current behavior: split(",") drops trailing empty fields, so a row whose
     * LAST column (PRICE LEVEL here) is blank has too few tokens and is skipped.
     * If you change the reader to split(",", -1), update this test to expect 1 row
     * with priceLevel "".
     */
    @Test
    void readRatings_blankTrailingField_rowIsSkipped() throws IOException {
        String file = writeCsv(
                HEADER,
                "NO PRICE,10027,Pizza,4.5,");

        List<Rating> ratings = new RatingReader(file).readRatings();

        assertTrue(ratings.isEmpty());
    }

    @Test
    void readRatings_blankMiddleField_isKeptAsEmptyString() throws IOException {
        String file = writeCsv(
                HEADER,
                "NO RATING,10027,Pizza,,$$");

        List<Rating> ratings = new RatingReader(file).readRatings();

        assertEquals(1, ratings.size());
        assertEquals("", ratings.get(0).rating);
        assertEquals("$$", ratings.get(0).priceLevel);
    }

    // ------------------------------------------------------------------
    // Errors
    // ------------------------------------------------------------------

    @Test
    void readRatings_nullFilename_throwsIllegalArgumentException() {
        RatingReader reader = new RatingReader(null);

        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, reader::readRatings);
        assertEquals("File name is not valid.", ex.getMessage());
    }

    @Test
    void readRatings_blankFilename_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new RatingReader("").readRatings());
        assertThrows(IllegalArgumentException.class, () -> new RatingReader("   ").readRatings());
    }

    @Test
    void readRatings_fileNotFound_throwsIllegalStateException() {
        String missing = tempDir.resolve("does-not-exist.csv").toString();

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> new RatingReader(missing).readRatings());
        assertEquals("File not found: " + missing, ex.getMessage());
    }

    @Test
    void readRatings_missingRequiredColumn_throwsIllegalStateException() throws IOException {
        // No PRICE LEVEL column
        String file = writeCsv(
                "DBA,ZIPCODE,CUISINE DESCRIPTION,RATING",
                "JOE'S PIZZA,10027,Pizza,4.5");

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> new RatingReader(file).readRatings());
        assertEquals("CSV header is missing required columns.", ex.getMessage());
    }

    @Test
    void readRatings_headerIsCaseSensitive() throws IOException {
        String file = writeCsv(
                "dba,zipcode,cuisine description,rating,price level",
                "JOE'S PIZZA,10027,Pizza,4.5,$$");

        assertThrows(IllegalStateException.class,
                () -> new RatingReader(file).readRatings());
    }

    /**
     * Documents current behavior: an completely empty file has no header line, so
     * scanner.nextLine() throws NoSuchElementException (it isn't caught or converted).
     * If you later add a friendlier check, update the expected exception.
     */
    @Test
    void readRatings_emptyFile_throwsNoSuchElementException() throws IOException {
        String file = writeCsv(); // creates an empty file

        assertThrows(NoSuchElementException.class,
                () -> new RatingReader(file).readRatings());
    }
}