import restaurant.common.Inspection;
import restaurant.common.Rating;
import restaurant.common.RestaurantSummary;
import restaurant.common.ZipCode;
import restaurant.data.InspectionReader;
import restaurant.data.RatingReader;
import restaurant.data.ZipCodeReader;
import restaurant.processor.RestaurantProcessor;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class GetRestaurantsInNeighborhoodTest {

    // ------------------------------------------------------------------
    // Data helpers (these only build model objects; the reader stubs are
    // defined as anonymous classes inside each test method)
    // ------------------------------------------------------------------

    private static Rating rating(String name, String zip, String cuisine,
                                 String rating, String price) {
        return new Rating(name, zip, cuisine, rating, price);
    }

    private static Inspection inspection(String name, String street, String score, String grade) {
        return new Inspection(name, "MANHATTAN", street, "10027", "some violation", "Y",
                score, grade, "2025-01-01");
    }

    private static ZipCode zip(String neighborhood, String zip) {
        return new ZipCode("Manhattan", neighborhood, zip);
    }

    // ------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------

    @org.junit.jupiter.api.Test
    void constructor_nullReaders_throwNullPointerException() {
        RatingReader ratingReader = new RatingReader("unused.csv") {
            @Override
            public List<Rating> readRatings() {
                return Collections.emptyList();
            }
        };
        InspectionReader inspectionReader = new InspectionReader("unused.csv") {
            @Override
            public List<Inspection> readInspections() {
                return Collections.emptyList();
            }
        };
        ZipCodeReader zipCodeReader = new ZipCodeReader("unused.csv") {
            @Override
            public List<ZipCode> readZipCodes() {
                return Collections.emptyList();
            }
        };

        assertThrows(NullPointerException.class,
                () -> new RestaurantProcessor(null, inspectionReader, zipCodeReader));
        assertThrows(NullPointerException.class,
                () -> new RestaurantProcessor(ratingReader, null, zipCodeReader));
        assertThrows(NullPointerException.class,
                () -> new RestaurantProcessor(ratingReader, inspectionReader, null));
    }


    @org.junit.jupiter.api.Test
    void getRestaurantsInNeighborhood() {
        RatingReader ratingReader = new RatingReader("unused.csv") {
            @Override
            public List<Rating> readRatings() {
                return Arrays.asList(
                        rating("IN HOOD", "10027", "Pizza", "4.5", "$$"),
                        rating("OUT OF HOOD", "10001", "Thai", "4.0", "$"));
            }
        };
        InspectionReader inspectionReader = new InspectionReader("unused.csv") {
            @Override
            public List<Inspection> readInspections() {
                return Arrays.asList(
                        inspection("IN HOOD", "BROADWAY", "9", "A"),
                        inspection("OUT OF HOOD", "7TH AVE", "20", "B"));
            }
        };
        ZipCodeReader zipCodeReader = new ZipCodeReader("unused.csv") {
            @Override
            public List<ZipCode> readZipCodes() {
                return Arrays.asList(
                        zip("Harlem", "10027"),
                        zip("Chelsea", "10001"));
            }
        };
        RestaurantProcessor p = new RestaurantProcessor(ratingReader, inspectionReader, zipCodeReader);

        List<RestaurantSummary> results = p.getRestaurantsInNeighborhood("Harlem");

        assertEquals(1, results.size());
        assertEquals("IN HOOD", results.get(0).restaurant);
        assertEquals("BROADWAY", results.get(0).street);
        assertEquals("A", results.get(0).inspectionGrade);
    }

    @org.junit.jupiter.api.Test
    void getRestaurantsInNeighborhood_multipleZips_includesAll() {
        RatingReader ratingReader = new RatingReader("unused.csv") {
            @Override
            public List<Rating> readRatings() {
                return Arrays.asList(
                        rating("ONE", "10027", "Pizza", "4.5", "$$"),
                        rating("TWO", "10030", "Thai", "4.0", "$"),
                        rating("THREE", "10001", "Cafe", "3.5", "$"));
            }
        };
        InspectionReader inspectionReader = new InspectionReader("unused.csv") {
            @Override
            public List<Inspection> readInspections() {
                return Arrays.asList(
                        inspection("ONE", "S1", "9", "A"),
                        inspection("TWO", "S2", "10", "A"),
                        inspection("THREE", "S3", "11", "A"));
            }
        };
        ZipCodeReader zipCodeReader = new ZipCodeReader("unused.csv") {
            @Override
            public List<ZipCode> readZipCodes() {
                return Arrays.asList(
                        zip("Harlem", "10027"),
                        zip("Harlem", "10030"),
                        zip("Chelsea", "10001"));
            }
        };
        RestaurantProcessor p = new RestaurantProcessor(ratingReader, inspectionReader, zipCodeReader);

        List<RestaurantSummary> results = p.getRestaurantsInNeighborhood("Harlem");

        assertEquals(2, results.size());
        assertEquals("ONE", results.get(0).restaurant);
        assertEquals("TWO", results.get(1).restaurant);
    }

    @org.junit.jupiter.api.Test
    void getRestaurantsInNeighborhood_sortedAlphabeticallyIgnoringCase() {
        RatingReader ratingReader = new RatingReader("unused.csv") {
            @Override
            public List<Rating> readRatings() {
                return Arrays.asList(
                        rating("zebra grill", "10027", "Grill", "4.0", "$$"),
                        rating("Apple Cafe", "10027", "Cafe", "4.0", "$"),
                        rating("mango tree", "10027", "Thai", "4.0", "$$"));
            }
        };
        InspectionReader inspectionReader = new InspectionReader("unused.csv") {
            @Override
            public List<Inspection> readInspections() {
                return Arrays.asList(
                        inspection("zebra grill", "S1", "9", "A"),
                        inspection("Apple Cafe", "S2", "9", "A"),
                        inspection("mango tree", "S3", "9", "A"));
            }
        };
        ZipCodeReader zipCodeReader = new ZipCodeReader("unused.csv") {
            @Override
            public List<ZipCode> readZipCodes() {
                return Collections.singletonList(zip("Harlem", "10027"));
            }
        };
        RestaurantProcessor p = new RestaurantProcessor(ratingReader, inspectionReader, zipCodeReader);

        List<RestaurantSummary> results = p.getRestaurantsInNeighborhood("Harlem");

        assertEquals(3, results.size());
        assertEquals("Apple Cafe", results.get(0).restaurant);
        assertEquals("mango tree", results.get(1).restaurant);
        assertEquals("zebra grill", results.get(2).restaurant);
    }

    @org.junit.jupiter.api.Test
    void getRestaurantsInNeighborhood_ignoresCaseAndWhitespace() {
        RatingReader ratingReader = new RatingReader("unused.csv") {
            @Override
            public List<Rating> readRatings() {
                return Collections.singletonList(rating("ONE", " 10027 ", "Pizza", "4.5", "$$"));
            }
        };
        InspectionReader inspectionReader = new InspectionReader("unused.csv") {
            @Override
            public List<Inspection> readInspections() {
                return Collections.singletonList(inspection("ONE", "S1", "9", "A"));
            }
        };
        ZipCodeReader zipCodeReader = new ZipCodeReader("unused.csv") {
            @Override
            public List<ZipCode> readZipCodes() {
                return Collections.singletonList(zip("Harlem", "10027 "));
            }
        };
        RestaurantProcessor p = new RestaurantProcessor(ratingReader, inspectionReader, zipCodeReader);

        assertEquals(1, p.getRestaurantsInNeighborhood("  hArLeM ").size());
    }

    @org.junit.jupiter.api.Test
    void getRestaurantsInNeighborhood_unknownNeighborhood_returnsEmptyList() {
        RatingReader ratingReader = new RatingReader("unused.csv") {
            @Override
            public List<Rating> readRatings() {
                return Collections.singletonList(rating("ONE", "10027", "Pizza", "4.5", "$$"));
            }
        };
        InspectionReader inspectionReader = new InspectionReader("unused.csv") {
            @Override
            public List<Inspection> readInspections() {
                return Collections.singletonList(inspection("ONE", "S1", "9", "A"));
            }
        };
        ZipCodeReader zipCodeReader = new ZipCodeReader("unused.csv") {
            @Override
            public List<ZipCode> readZipCodes() {
                return Collections.singletonList(zip("Harlem", "10027"));
            }
        };
        RestaurantProcessor p = new RestaurantProcessor(ratingReader, inspectionReader, zipCodeReader);

        List<RestaurantSummary> results = p.getRestaurantsInNeighborhood("Atlantis");

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @org.junit.jupiter.api.Test
    void getRestaurantsInNeighborhood_noRestaurantsInZip_returnsEmptyList() {
        RatingReader ratingReader = new RatingReader("unused.csv") {
            @Override
            public List<Rating> readRatings() {
                return Collections.singletonList(rating("ONE", "10001", "Pizza", "4.5", "$$"));
            }
        };
        InspectionReader inspectionReader = new InspectionReader("unused.csv") {
            @Override
            public List<Inspection> readInspections() {
                return Collections.singletonList(inspection("ONE", "S1", "9", "A"));
            }
        };
        ZipCodeReader zipCodeReader = new ZipCodeReader("unused.csv") {
            @Override
            public List<ZipCode> readZipCodes() {
                return Collections.singletonList(zip("Harlem", "10027"));
            }
        };
        RestaurantProcessor p = new RestaurantProcessor(ratingReader, inspectionReader, zipCodeReader);

        assertTrue(p.getRestaurantsInNeighborhood("Harlem").isEmpty());
    }

    @org.junit.jupiter.api.Test
    void getRestaurantsInNeighborhood_nullRatingZip_isSkipped() {
        RatingReader ratingReader = new RatingReader("unused.csv") {
            @Override
            public List<Rating> readRatings() {
                return Arrays.asList(
                        rating("NO ZIP", null, "Pizza", "4.5", "$$"),
                        rating("HAS ZIP", "10027", "Thai", "4.0", "$"));
            }
        };
        InspectionReader inspectionReader = new InspectionReader("unused.csv") {
            @Override
            public List<Inspection> readInspections() {
                return Arrays.asList(
                        inspection("NO ZIP", "S1", "9", "A"),
                        inspection("HAS ZIP", "S2", "9", "A"));
            }
        };
        ZipCodeReader zipCodeReader = new ZipCodeReader("unused.csv") {
            @Override
            public List<ZipCode> readZipCodes() {
                return Collections.singletonList(zip("Harlem", "10027"));
            }
        };
        RestaurantProcessor p = new RestaurantProcessor(ratingReader, inspectionReader, zipCodeReader);

        List<RestaurantSummary> results = p.getRestaurantsInNeighborhood("Harlem");

        assertEquals(1, results.size());
        assertEquals("HAS ZIP", results.get(0).restaurant);
    }

    @org.junit.jupiter.api.Test
    void getRestaurantsInNeighborhood_missingInspection_usesNotAvailable() {
        RatingReader ratingReader = new RatingReader("unused.csv") {
            @Override
            public List<Rating> readRatings() {
                return Collections.singletonList(rating("ONE", "10027", "Pizza", "4.5", "$$"));
            }
        };
        InspectionReader inspectionReader = new InspectionReader("unused.csv") {
            @Override
            public List<Inspection> readInspections() {
                return Collections.emptyList();
            }
        };
        ZipCodeReader zipCodeReader = new ZipCodeReader("unused.csv") {
            @Override
            public List<ZipCode> readZipCodes() {
                return Collections.singletonList(zip("Harlem", "10027"));
            }
        };
        RestaurantProcessor p = new RestaurantProcessor(ratingReader, inspectionReader, zipCodeReader);

        List<RestaurantSummary> results = p.getRestaurantsInNeighborhood("Harlem");

        assertEquals(1, results.size());
        assertEquals("N/A", results.get(0).street);
        assertEquals("N/A", results.get(0).inspectionGrade);
        assertEquals("N/A", results.get(0).inspectionScore);
    }

    @org.junit.jupiter.api.Test
    void getRestaurantsInNeighborhood_nullOrBlank_throwsIllegalArgumentException() {
        RatingReader ratingReader = new RatingReader("unused.csv") {
            @Override
            public List<Rating> readRatings() {
                return Collections.emptyList();
            }
        };
        InspectionReader inspectionReader = new InspectionReader("unused.csv") {
            @Override
            public List<Inspection> readInspections() {
                return Collections.emptyList();
            }
        };
        ZipCodeReader zipCodeReader = new ZipCodeReader("unused.csv") {
            @Override
            public List<ZipCode> readZipCodes() {
                return Collections.emptyList();
            }
        };
        RestaurantProcessor p = new RestaurantProcessor(ratingReader, inspectionReader, zipCodeReader);

        assertThrows(IllegalArgumentException.class, () -> p.getRestaurantsInNeighborhood(null));
        assertThrows(IllegalArgumentException.class, () -> p.getRestaurantsInNeighborhood(""));
        assertThrows(IllegalArgumentException.class, () -> p.getRestaurantsInNeighborhood("  "));
    }
}
