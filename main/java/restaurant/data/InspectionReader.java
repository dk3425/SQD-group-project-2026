package restaurant.data;

import restaurant.common.Inspection;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class InspectionReader {
    protected String filename;

    public InspectionReader(String filename){
        this.filename = filename;
    }

    public List<Inspection> readInspections(){
        if (filename == null || filename.isBlank())
            throw new IllegalArgumentException("File name is not valid.");

        List<Inspection> inspections = new ArrayList<>();
        try (Scanner scanner = new Scanner(new File(filename))) {

            String header = scanner.nextLine();
            String[] tok = splitCsvLine(header);

            int restaurantIndex = -1;
            int boroughIndex = -1;
            int streetIndex = -1;
            int zipIndex = -1;
            int violationIndex = -1;
            int flagIndex = -1;
            int scoreIndex = -1;
            int gradeIndex = -1;
            int gradeDateIndex = -1;

            for (int i = 0; i < tok.length; i++) {
                if (tok[i].equals("DBA")) {
                    restaurantIndex = i;
                } else if (tok[i].equals("BORO")) {
                    boroughIndex = i;
                } else if (tok[i].equals("STREET")) {
                    streetIndex = i;
                } else if (tok[i].equals("ZIPCODE")) {
                    zipIndex = i;
                } else if (tok[i].equals("VIOLATION DESCRIPTION")) {
                    violationIndex = i;
                } else if (tok[i].equals("CRITICAL FLAG")) {
                    flagIndex = i;
                } else if (tok[i].equals("SCORE")) {
                    scoreIndex = i;
                } else if (tok[i].equals("GRADE")) {
                    gradeIndex = i;
                } else if (tok[i].equals("GRADE DATE")) {
                    gradeDateIndex = i;
                }
            }

            // Part 1.1 bug fixes
            if (
                restaurantIndex == -1 ||
                boroughIndex == -1 ||
                streetIndex == -1 ||
                zipIndex == -1 ||
                violationIndex == -1 ||
                flagIndex == -1 ||
                scoreIndex == -1 ||
                gradeIndex == -1 ||
                gradeDateIndex == -1
            ) {
                throw new IllegalStateException("CSV header is missing required columns.");
            }



            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                tok = splitCsvLine(line);

                if (tok.length <= restaurantIndex || tok.length <= boroughIndex || tok.length <= streetIndex || tok.length <= zipIndex ||
                        tok.length <= violationIndex || tok.length <= flagIndex || tok.length <= scoreIndex ||
                        tok.length <= gradeIndex || tok.length <= gradeDateIndex) {
                    continue;
                }

                Inspection inspection = new Inspection(
                        tok[restaurantIndex],
                        tok[boroughIndex],
                        tok[streetIndex],
                        tok[zipIndex],
                        tok[violationIndex],
                        tok[flagIndex],
                        tok[scoreIndex],
                        tok[gradeIndex],
                        tok[gradeDateIndex]
                );

                inspections.add(inspection);
            }

        } catch (FileNotFoundException e){
            // Part 1.1 bug fixes
            throw new IllegalStateException("File not found: " + filename);
        }

        return inspections;
    }

    // Splits on commas that are not inside double quotes (violation descriptions often contain commas).
    // The -1 limit keeps trailing empty fields (e.g. a blank grade or grade date).
    // Each field is trimmed and has its surrounding quotes removed.
    private static String[] splitCsvLine(String line) {
        String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
        for (int i = 0; i < parts.length; i++) {
            String p = parts[i].trim();
            if (p.length() >= 2 && p.startsWith("\"") && p.endsWith("\"")) {
                p = p.substring(1, p.length() - 1).replace("\"\"", "\"").trim();
            }
            parts[i] = p;
        }
        return parts;
    }
}
