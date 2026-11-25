package restaurant.ui;

import restaurant.processor.RestaurantProcessor;
import restaurant.common.Inspection;
import restaurant.common.RestaurantSummary;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class UI {
    private RestaurantProcessor processor;

    public UI(RestaurantProcessor rp){
        this.processor = rp;
    }

    public void doInspectionForRestaurant(Scanner scanner){
        System.out.println("Enter a restaurant name: ");
        String name = scanner.nextLine().trim();
        Optional<Inspection> inspection = processor.getInspectionForRestaurant(name.toUpperCase());
        System.out.println(inspection.get());
    }

    //Edited for Part 1.2
    public void doRestaurantInfo(Scanner scanner){
        System.out.println("Enter a restaurant name: ");
        String name = scanner.nextLine().trim();
        //Optional<restaurant.common.RestaurantSummary> summary = Optional.empty();
        //for (restaurant.common.Rating r : processor.ratings) {
        //    if (RestaurantProcessor.sameText(r.restaurant, name)) {
        //        summary = Optional.of(processor.buildSummary(r));
        //        break;
        //    }
        //}
        Optional<RestaurantSummary> summary = processor.getRestaurantInfo(name);
        System.out.println(summary.get());
    }

    public void doRestaurantsInNeighborhood(Scanner scanner){
        System.out.println("Enter a neighborhood name: ");
        String name = scanner.nextLine().trim();
        List<RestaurantSummary> list = processor.getRestaurantsInNeighborhood(name);
        list.forEach(r -> System.out.println(r));
    }

    public void showMenu(){
        System.out.println("1: Show inspection for restaurant\n2: Show restaurant info\n3: Get restaurants in neighborhood\n0: Exit");
    }

    public void start() {

        Scanner scanner = new Scanner(System.in);
        while (true){
            showMenu();
            System.out.println("Hello! Please select an option: ");
            int option = Integer.parseInt(scanner.nextLine().trim());
            if (option == 1){
                doInspectionForRestaurant(scanner);
            } else if (option == 2){
                doRestaurantInfo(scanner);
            } else if (option == 3){
                doRestaurantsInNeighborhood(scanner);
            } else if (option == 0){
                break;
            }
        }

    }
}

