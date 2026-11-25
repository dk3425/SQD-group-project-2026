package restaurant.processor;

import restaurant.common.Inspection;
import restaurant.common.Rating;
import restaurant.common.RestaurantSummary;
import restaurant.common.ZipCode;
import restaurant.data.InspectionReader;
import restaurant.data.RatingReader;
import restaurant.data.ZipCodeReader;

import java.util.*;


public class RestaurantProcessor {

    protected final List<Rating> ratings;
    protected final List<Inspection> inspections;
    protected final List<ZipCode> zipCodes;

    public RestaurantProcessor(RatingReader ratingReader,
                               InspectionReader inspectionReader,
                               ZipCodeReader zipCodeReader) {
        Objects.requireNonNull(ratingReader, "ratingReader must not be null");
        Objects.requireNonNull(inspectionReader, "inspectionReader must not be null");
        Objects.requireNonNull(zipCodeReader, "zipCodeReader must not be null");

        // Load each file once; unmodifiable so callers can't change our data.
        this.ratings = Collections.unmodifiableList(ratingReader.readRatings());
        this.inspections = Collections.unmodifiableList(inspectionReader.readInspections());
        this.zipCodes = Collections.unmodifiableList(zipCodeReader.readZipCodes());
    }



    public Optional<Inspection> getInspectionForRestaurant(String dba) {
        requireNonBlank(dba, "Restaurant name");
        for (Inspection inspection : inspections) {
            if (sameText(inspection.restaurant, dba)) {
                return Optional.of(inspection);
            }
        }
        return Optional.empty();
    }


    public List<RestaurantSummary> getRestaurantsInNeighborhood(String neighborhood) {
        requireNonBlank(neighborhood, "Neighborhood");

        Set<String> zipsInNeighborhood = new HashSet<>();
        for (ZipCode z : zipCodes) {
            // System.out.println(z.neighborhood);
            if (sameText(z.neighborhood, neighborhood)) {
                zipsInNeighborhood.add(z.zip.trim());
            }
        }

        List<RestaurantSummary> results = new ArrayList<>();
        for (Rating r : ratings) {
            if (r.zip != null && zipsInNeighborhood.contains(r.zip.trim())) {
                results.add(buildSummary(r));
            }
        }

        results.sort((a, b) -> a.restaurant.compareToIgnoreCase(b.restaurant));
        return results;
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    /** Joins a rating with its inspection (matched by restaurant name). */
    RestaurantSummary buildSummary(Rating r) {
        Optional<Inspection> inspection = Optional.empty();//findInspection(r.restaurant);
        for (Inspection in : inspections) {
            if (sameText(in.restaurant, r.restaurant)) {
                inspection = Optional.of(in);
                break;
            }
        }

        // A restaurant in ratings.csv might still be missing from inspections.csv.
        String street = inspection.map(i -> i.street).orElse("N/A");
        String grade = inspection.map(i -> i.grade).orElse("N/A");
        String score = inspection.map(i -> i.score).orElse("N/A");

        return new RestaurantSummary(
                r.restaurant, street, r.zip, r.cuisine, r.rating, r.priceLevel, grade, score);
    }

    static boolean sameText(String a, String b) {
        // Part 1.1 bug fixes
        return a != null && b != null && a.trim().equalsIgnoreCase(b.trim());
    }

    private static void requireNonBlank(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(label + " must not be empty.");
        }
    }

    //Part 1.2 getRestaurantInfo method
    public Optional<RestaurantSummary> getRestaurantInfo(String name) {
        requireNonBlank(name, "Restaurant name");

        for (Rating r: ratings){
            if (sameText(r.restaurant, name)){
                return Optional.of(buildSummary(r));
            }
        }
        return Optional.empty();

    }


}
