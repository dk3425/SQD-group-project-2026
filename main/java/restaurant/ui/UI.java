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

        // Part 1.5, added for blank name case. Processor throws error on blank name case.
        if (name.isBlank()) {
            System.out.println("No data found");
            return;
        }

        Optional<Inspection> inspection = processor.getInspectionForRestaurant(name.toUpperCase());

        // Part 1.5, add check to print No data found if inspection does not exist. 
        if (!inspection.isPresent()) {
            System.out.println("No data found");
        } else {
            System.out.println(inspection.get());
        }
    }

    //Edited for Part 1.2
    public void doRestaurantInfo(Scanner scanner){
        System.out.println("Enter a restaurant name: ");
        String name = scanner.nextLine().trim();

        // Part 1.5, added for blank name case. Processor throws error on blank name case.
        if (name.isBlank()) {
            System.out.println("No data found");
            return;
        }

        //Optional<restaurant.common.RestaurantSummary> summary = Optional.empty();
        //for (restaurant.common.Rating r : processor.ratings) {
        //    if (RestaurantProcessor.sameText(r.restaurant, name)) {
        //        summary = Optional.of(processor.buildSummary(r));
        //        break;
        //    }
        //}
        Optional<RestaurantSummary> summary = processor.getRestaurantInfo(name);

        // Part 1.5, add check to print No data found if summary does not exist. 
        if (!summary.isPresent()) {
            System.out.println("No data found");
        } else {
            System.out.println(summary.get());
        }
    }

    public void doRestaurantsInNeighborhood(Scanner scanner){
        System.out.println("Enter a neighborhood name: ");
        String name = scanner.nextLine().trim();

        // Part 1.5, added for blank name case. Processor throws error on blank name case.
        if (name.isBlank()) {
            System.out.println("No data found");
            return;
        }

        List<RestaurantSummary> list = processor.getRestaurantsInNeighborhood(name);
        // Part 1.5, add check to print No data found if list is empty.
        if (list.isEmpty()) {
            System.out.println("No data found");
        } else {
            list.forEach(r -> System.out.println(r));
        }
    }

    public void showMenu(){
        System.out.println("1: Show inspection for restaurant\n2: Show restaurant info\n3: Get restaurants in neighborhood\n0: Exit");
    }

    public void start() {

        Scanner scanner = new Scanner(System.in);
        while (true){
            showMenu();
            System.out.println("Hello! Please select an option: ");

            // Updated for part 1.5: handle invalid inputs (wrap the parse int in a try/catch)
            try {
                int option = Integer.parseInt(scanner.nextLine().trim());
                if (option == 1){
                    doInspectionForRestaurant(scanner);
                } else if (option == 2){
                    doRestaurantInfo(scanner);
                } else if (option == 3){
                    doRestaurantsInNeighborhood(scanner);
                } else if (option == 0){
                    break;
                } else {
                    System.out.println("Invalid selection");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid selection");
            }
        }

    }
}

