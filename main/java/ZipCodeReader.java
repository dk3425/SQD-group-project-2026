import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ZipCodeReader {
    protected String filename;

    public ZipCodeReader(String filename){
        this.filename = filename;
    }

    public List<ZipCode> readZipCodes(){
        if (filename == null || filename.isBlank())
            throw new IllegalArgumentException("File name is not valid.");

        List<ZipCode> zipCodes = new ArrayList<>();
        try (Scanner scanner = new Scanner(new File(filename))) {

            String header = scanner.nextLine();
            String[] tok = header.split(",");

            int boroughIndex = -1;
            int neighborhoodIndex = -1;
            int zipIndex = -1;

            for (int i = 0; i < tok.length; i++) {
                if (tok[i].equals("Borough")) {
                    boroughIndex = i;
                } else if (tok[i].equals("Neighborhood")) {
                    neighborhoodIndex = i;
                } else if (tok[i].equals("ZipCode")) {
                    zipIndex = i;
                }
            }

            if (boroughIndex == -1 || neighborhoodIndex == -1 || zipIndex == -1) {
                throw new IllegalStateException("CSV header is missing required columns.");
            }

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                tok = line.split(",");

                if (tok.length <= boroughIndex || tok.length <= neighborhoodIndex || tok.length <= zipIndex) {
                    continue;
                }

                ZipCode zipCode = new ZipCode(
                        tok[boroughIndex],
                        tok[neighborhoodIndex],
                        tok[zipIndex]
                );
                zipCodes.add(zipCode);
            }

        } catch (FileNotFoundException e){
            throw new IllegalStateException("File not found: " + filename);
        }

        return zipCodes;
    }
}
