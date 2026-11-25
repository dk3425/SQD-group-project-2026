import restaurant.common.Inspection;
import restaurant.common.Rating;
import restaurant.common.ZipCode;
import restaurant.data.InspectionReader;
import restaurant.data.RatingReader;
import restaurant.data.ZipCodeReader;
import restaurant.processor.RestaurantProcessor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class GetInspectionTest {

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
    void getInspectionForRestaurant() {
        RatingReader ratingReader = new RatingReader("unused.csv") {
            @Override
            public List<Rating> readRatings() {
                return Collections.emptyList();
            }
        };
        InspectionReader inspectionReader = new InspectionReader("unused.csv") {
            @Override
            public List<Inspection> readInspections() {
                return Arrays.asList(
                        inspection("JOE'S PIZZA", "BROADWAY", "9", "A"),
                        inspection("TACO PLACE", "AMSTERDAM AVE", "20", "B"));
            }
        };
        ZipCodeReader zipCodeReader = new ZipCodeReader("unused.csv") {
            @Override
            public List<ZipCode> readZipCodes() {
                return Collections.emptyList();
            }
        };
        RestaurantProcessor p = new RestaurantProcessor(ratingReader, inspectionReader, zipCodeReader);

        Optional<Inspection> result = p.getInspectionForRestaurant("TACO PLACE");

        assertTrue(result.isPresent());
        assertEquals("TACO PLACE", result.get().restaurant);
        assertEquals("AMSTERDAM AVE", result.get().street);
        assertEquals("20", result.get().score);
        assertEquals("B", result.get().grade);
    }

    @org.junit.jupiter.api.Test
    void getInspectionForRestaurant_ignoresCaseAndWhitespace() {
        RatingReader ratingReader = new RatingReader("unused.csv") {
            @Override
            public List<Rating> readRatings() {
                return Collections.emptyList();
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

        assertTrue(p.getInspectionForRestaurant("joe's pizza").isPresent());
        assertTrue(p.getInspectionForRestaurant("  Joe's Pizza  ").isPresent());
    }

    @org.junit.jupiter.api.Test
    void getInspectionForRestaurant_notFound_returnsEmpty() {
        RatingReader ratingReader = new RatingReader("unused.csv") {
            @Override
            public List<Rating> readRatings() {
                return Collections.emptyList();
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

        assertTrue(p.getInspectionForRestaurant("DOES NOT EXIST").isEmpty());
        assertTrue(p.getInspectionForRestaurant("JOE").isEmpty()); // partial name
    }

    @org.junit.jupiter.api.Test
    void getInspectionForRestaurant_emptyData_returnsEmpty() {
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

        assertTrue(p.getInspectionForRestaurant("ANYTHING").isEmpty());
    }

    @org.junit.jupiter.api.Test
    void getInspectionForRestaurant_nullOrBlank_throwsIllegalArgumentException() {
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

        assertThrows(IllegalArgumentException.class, () -> p.getInspectionForRestaurant(null));
        assertThrows(IllegalArgumentException.class, () -> p.getInspectionForRestaurant(""));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> p.getInspectionForRestaurant("   "));
        assertEquals("Restaurant name must not be empty.", ex.getMessage());
    }

}
