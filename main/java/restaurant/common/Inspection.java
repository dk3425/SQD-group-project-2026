package restaurant.common;

public class Inspection {
    public String restaurant;
    public String borough;
    public String street;
    public String zip;
    public String violation;
    public String flag;
    public String score;
    public String grade;
    public String gradeDate;

    public Inspection(String restaurant, String borough, String street, String zip, String violation, String flag, String score, String grade, String gradeDate){
        this.restaurant = restaurant;
        this.borough = borough;
        this.street = street;
        this.zip = zip;
        this.violation = violation;
        this.flag = flag;
        this.score = score;
        this.grade = grade;
        this.gradeDate = gradeDate;
    }

    @Override
    public String toString() {
        return restaurant + " (" + street + ", " + borough + " " + zip + ")"
                + " | grade: " + grade
                + " | score: " + score
                + " | grade date: " + gradeDate
                + " | critical flag: " + flag
                + " | violation: " + violation;
    }

}
