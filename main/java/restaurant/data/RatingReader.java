package restaurant.data;

import restaurant.common.Rating;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class RatingReader {
    protected String filename;

    public RatingReader(String filename){
        this.filename = filename;
    }

    public List<Rating> readRatings(){
        // Part 1.1 bug fixes
        if (filename == null || filename.isBlank())
            throw new IllegalArgumentException("File name is not valid.");

        List<Rating> ratings = new ArrayList<>();
        try (Scanner scanner = new Scanner(new File(filename))) {

            String header = scanner.nextLine();
            String[] tok = header.split(",");

            int restaurantIndex = -1;
            int zipIndex = -1;
            int cuisineIndex = -1;
            int ratingIndex = -1;
            int priceLevelIndex = -1;

            for (int i = 0; i < tok.length; i++) {
                if (tok[i].trim().equals("DBA")) {
                    restaurantIndex = i;
                } else if (tok[i].trim().equals("ZIPCODE")) {
                    zipIndex = i;
                } else if (tok[i].trim().equals("CUISINE DESCRIPTION")) {
                    cuisineIndex = i;
                } else if (tok[i].trim().equals("RATING")) {
                    ratingIndex = i;
                } else if (tok[i].trim().equals("PRICE LEVEL")) {
                    priceLevelIndex = i;
                }
            }

            if (restaurantIndex == -1 || zipIndex == -1 || cuisineIndex == -1 || ratingIndex == -1 || priceLevelIndex == -1) {
                throw new IllegalStateException("CSV header is missing required columns.");
            }

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                tok = line.split(",");
                // Part 1.1 bug fixes
                if (tok.length <= restaurantIndex || tok.length <= zipIndex || tok.length <= cuisineIndex ||
                        tok.length <= ratingIndex || tok.length <= priceLevelIndex) {
                    continue;
                }

                Rating r = new Rating();
                r.restaurant = tok[restaurantIndex].trim();
                r.zip = tok[zipIndex].trim();
                r.cuisine = tok[cuisineIndex].trim();
                r.rating = tok[ratingIndex].trim();
                r.priceLevel = tok[priceLevelIndex].trim();

                ratings.add(r);
            }

        } catch (FileNotFoundException e){
            throw new IllegalStateException("File not found: " + filename);
        }

        return ratings;
    }
}
