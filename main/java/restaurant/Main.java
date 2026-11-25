package restaurant;

import restaurant.processor.RestaurantProcessor;
import restaurant.data.InspectionReader;
import restaurant.data.RatingReader;
import restaurant.data.ZipCodeReader;
import restaurant.ui.UI;

public class Main {
    public static void main(String[] args){
        InspectionReader ir = new InspectionReader("inspections.csv");
        RatingReader rr = new RatingReader("ratings.csv");
        ZipCodeReader zcr = new ZipCodeReader("nyc-zip-codes.csv");

        RestaurantProcessor processor = new RestaurantProcessor(rr, ir, zcr);

        UI ui = new UI(processor);
        ui.start();
    }
}
