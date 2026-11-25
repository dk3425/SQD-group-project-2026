package restaurant.common;

public class ZipCode {
    // nyc-zip-codes.csv columns: borough, neighborhood, zipcode

    public String borough;
    public String neighborhood;
    public String zip;

    public ZipCode(String borough, String neighborhood, String zip){
        this.borough = borough;
        this.neighborhood = neighborhood;
        this.zip = zip;
    }

    @Override
    public String toString() {
        return neighborhood + ", " + borough + " (" + zip + ")";
    }
}
