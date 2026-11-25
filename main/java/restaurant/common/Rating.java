package restaurant.common;

public class Rating {
    public String restaurant;
    public String zip;
    public String cuisine;
    public String rating;
    public String priceLevel;

    public Rating(String restaurant, String zip, String cuisine, String rating, String priceLevel){
        this.restaurant = restaurant;
        this.zip = zip;
        this.cuisine = cuisine;
        this.rating = rating;
        this.priceLevel = priceLevel;
    }

    public Rating(){}

    @Override
    public String toString(){
        return "restaurant: " + restaurant + " | zip: " + zip + " | cuisine: " + cuisine + " | rating: " + rating +
                " | price level:" + priceLevel;
    }
}
