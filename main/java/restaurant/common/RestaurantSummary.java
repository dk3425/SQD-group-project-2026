package restaurant.common;

/** What the restaurant.ui.UI receives for features 2 and 3: one restaurant with both of its ratings. */
public class RestaurantSummary {
    public final String restaurant;
    public final String street;
    public final String zip;
    public final String cuisine;
    public final String rating;          // review rating (ratings.csv)
    public final String priceLevel;      // ratings.csv
    public final String inspectionGrade; // inspections.csv
    public final String inspectionScore; // inspections.csv

    public RestaurantSummary(String restaurant, String street, String zip, String cuisine,
                             String rating, String priceLevel,
                             String inspectionGrade, String inspectionScore) {
        this.restaurant = restaurant;
        this.street = street;
        this.zip = zip;
        this.cuisine = cuisine;
        this.rating = rating;
        this.priceLevel = priceLevel;
        this.inspectionGrade = inspectionGrade;
        this.inspectionScore = inspectionScore;
    }

    @Override
    public String toString() {
        return restaurant + " (" + street + ", " + zip + ") | " + cuisine
                + " | review rating: " + rating
                + " | price level: " + priceLevel
                + " | inspection grade: " + inspectionGrade
                + " (score " + inspectionScore + ")";
    }
}
