import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class GetRestaurantInfoTest {

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


    @org.junit.jupiter.api.Test
    void getRestaurantInfo() {
        RatingReader ratingReader = new RatingReader("unused.csv") {
            @Override
            public List<Rating> readRatings() {
                return Collections.singletonList(
                        rating("JOE'S PIZZA", "10027", "Pizza", "4.5", "$$"));
            }
        };
        InspectionReader inspectionReader = new InspectionReader("unused.csv") {
            @Override
            public List<Inspection> readInspections() {
                return Collections.singletonList(inspection("JOE'S PIZZA", "BROADWAY", "9", "A"));
            }
        };
        ZipCodeReader zipCodeReader = new ZipCodeReader("unused.csv") {
            @Override
            public List<ZipCode> readZipCodes() {
                return Collections.emptyList();
            }
        };
        RestaurantProcessor p = new RestaurantProcessor(ratingReader, inspectionReader, zipCodeReader);

        Optional<RestaurantSummary> result = p.getRestaurantInfo("JOE'S PIZZA");

        assertTrue(result.isPresent());
        RestaurantSummary s = result.get();
        assertEquals("JOE'S PIZZA", s.restaurant);
        assertEquals("BROADWAY", s.street);          // from inspections.csv
        assertEquals("10027", s.zip);                // from ratings.csv
        assertEquals("Pizza", s.cuisine);
        assertEquals("4.5", s.rating);
        assertEquals("$$", s.priceLevel);
        assertEquals("A", s.inspectionGrade);
        assertEquals("9", s.inspectionScore);
    }

    @org.junit.jupiter.api.Test
    void getRestaurantInfo_ignoresCaseAndWhitespace() {
        RatingReader ratingReader = new RatingReader("unused.csv") {
            @Override
            public List<Rating> readRatings() {
                return Collections.singletonList(
                        rating("JOE'S PIZZA", "10027", "Pizza", "4.5", "$$"));
            }
        };
        InspectionReader inspectionReader = new InspectionReader("unused.csv") {
            @Override
            public List<Inspection> readInspections() {
                return Collections.singletonList(inspection("JOE'S PIZZA", "BROADWAY", "9", "A"));
            }
        };
        ZipCodeReader zipCodeReader = new ZipCodeReader("unused.csv") {
            @Override
            public List<ZipCode> readZipCodes() {
                return Collections.emptyList();
            }
        };
        RestaurantProcessor p = new RestaurantProcessor(ratingReader, inspectionReader, zipCodeReader);

        assertTrue(p.getRestaurantInfo("  joe's pizza ").isPresent());
    }

    @org.junit.jupiter.api.Test
    void getRestaurantInfo_missingInspection_usesNotAvailable() {
        RatingReader ratingReader = new RatingReader("unused.csv") {
            @Override
            public List<Rating> readRatings() {
                return Collections.singletonList(
                        rating("JOE'S PIZZA", "10027", "Pizza", "4.5", "$$"));
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

        Optional<RestaurantSummary> result = p.getRestaurantInfo("JOE'S PIZZA");

        assertTrue(result.isPresent());
        assertEquals("N/A", result.get().street);
        assertEquals("N/A", result.get().inspectionGrade);
        assertEquals("N/A", result.get().inspectionScore);
        assertEquals("Pizza", result.get().cuisine); // rating data still present
    }

    @org.junit.jupiter.api.Test
    void getRestaurantInfo_notInRatings_returnsEmpty() {
        RatingReader ratingReader = new RatingReader("unused.csv") {
            @Override
            public List<Rating> readRatings() {
                return Collections.singletonList(
                        rating("JOE'S PIZZA", "10027", "Pizza", "4.5", "$$"));
            }
        };
        InspectionReader inspectionReader = new InspectionReader("unused.csv") {
            @Override
            public List<Inspection> readInspections() {
                return Arrays.asList(
                        inspection("JOE'S PIZZA", "BROADWAY", "9", "A"),
                        inspection("INSPECTED ONLY", "1ST AVE", "5", "A"));
            }
        };
        ZipCodeReader zipCodeReader = new ZipCodeReader("unused.csv") {
            @Override
            public List<ZipCode> readZipCodes() {
                return Collections.emptyList();
            }
        };
        RestaurantProcessor p = new RestaurantProcessor(ratingReader, inspectionReader, zipCodeReader);

        assertTrue(p.getRestaurantInfo("INSPECTED ONLY").isEmpty());
        assertTrue(p.getRestaurantInfo("NOPE").isEmpty());
    }

    @org.junit.jupiter.api.Test
    void getRestaurantInfo_pendingGrade_passesThrough() {
        RatingReader ratingReader = new RatingReader("unused.csv") {
            @Override
            public List<Rating> readRatings() {
                return Collections.singletonList(
                        rating("NEW PLACE", "10025", "Thai", "4.0", "$"));
            }
        };
        InspectionReader inspectionReader = new InspectionReader("unused.csv") {
            @Override
            public List<Inspection> readInspections() {
                return Collections.singletonList(inspection("NEW PLACE", "W 110TH ST", "", "Z"));
            }
        };
        ZipCodeReader zipCodeReader = new ZipCodeReader("unused.csv") {
            @Override
            public List<ZipCode> readZipCodes() {
                return Collections.emptyList();
            }
        };
        RestaurantProcessor p = new RestaurantProcessor(ratingReader, inspectionReader, zipCodeReader);

        assertEquals("Z", p.getRestaurantInfo("NEW PLACE").get().inspectionGrade);
    }

    @org.junit.jupiter.api.Test
    void getRestaurantInfo_picksCorrectRestaurantAmongMany() {
        RatingReader ratingReader = new RatingReader("unused.csv") {
            @Override
            public List<Rating> readRatings() {
                return Arrays.asList(
                        rating("A CAFE", "10027", "Cafe", "4.0", "$"),
                        rating("B BISTRO", "10025", "French", "4.8", "$$$"),
                        rating("C DINER", "10026", "American", "3.9", "$$"));
            }
        };
        InspectionReader inspectionReader = new InspectionReader("unused.csv") {
            @Override
            public List<Inspection> readInspections() {
                return Arrays.asList(
                        inspection("A CAFE", "1ST AVE", "5", "A"),
                        inspection("B BISTRO", "2ND AVE", "18", "B"),
                        inspection("C DINER", "3RD AVE", "40", "C"));
            }
        };
        ZipCodeReader zipCodeReader = new ZipCodeReader("unused.csv") {
            @Override
            public List<ZipCode> readZipCodes() {
                return Collections.emptyList();
            }
        };
        RestaurantProcessor p = new RestaurantProcessor(ratingReader, inspectionReader, zipCodeReader);

        RestaurantSummary s = p.getRestaurantInfo("B BISTRO").get();

        assertEquals("2ND AVE", s.street);
        assertEquals("French", s.cuisine);
        assertEquals("B", s.inspectionGrade);
    }

    @org.junit.jupiter.api.Test
    void getRestaurantInfo_nullOrBlank_throwsIllegalArgumentException() {
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

        assertThrows(IllegalArgumentException.class, () -> p.getRestaurantInfo(null));
        assertThrows(IllegalArgumentException.class, () -> p.getRestaurantInfo(""));
        assertThrows(IllegalArgumentException.class, () -> p.getRestaurantInfo("  "));
    }
}
