package cas.thomas.Sudoku;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class SudokuConstraintConverter {

    private static final String FIRST_LINE_STRING = "[1-9]+";

    public static void main(String[] args) throws IOException {

        if (args.length != 1) {
            System.err.println("Your input contains more than one file or folder!");
            System.exit(-1);
        }

        Path filePath = Paths.get(args[0]);

        if (Files.isDirectory(Paths.get(args[0]))) {

        } else {
            List<String> lines = Files.readAllLines(filePath);

            if (lines.size() == 0) {
                System.err.println("Your input has no lines!");
                System.exit(-1);
            }

            if (!lines.get(0).matches(FIRST_LINE_STRING)) {
                System.err.println("Your first line doesn't specify the sudoku game size!");
                System.exit(-1);
            }

            BigInteger initialSize = new BigInteger(lines.get(0).split("\\n")[0]);
            BigInteger size = initialSize.pow(2);

            if (lines.size() - 1 != size.intValue()) {
                System.err.println("The number of rows does not match your specified size!");
                System.exit(-1);
            }

            List<String> linesToPrint = new ArrayList<>();

            int clauseCounter = 0;

            final String linePattern = "([0-9]+\\s){" + (size.intValue() - 1) + "}[0-9]+";

            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i);

                if (!line.matches(linePattern)) {
                    System.err.println("Row " + i + " doesn't match the specified size or format");
                    System.exit(-1);
                }

                String[] lineParts = line.split("\\s");

                for (int a = 0; a < lineParts.length; a++) {
                    if (!lineParts[a].equals("0")) {
                        linesToPrint.add(createUniqueIdentifier((a+1), i, Integer.parseInt(lineParts[a])) + " 0");
                        clauseCounter++;
                    }
                }

            }


            for (int x = 1; x <= size.intValue(); x++) {
                for (int y = 1; y <= size.intValue(); y++) {
                    String line = "";
                    String AMOrowLine = "AMO";
                    String AMOcolumnLine = "AMO";
                    for (int z = 1; z <= size.intValue(); z++) {
                        line += line.equals("") ?  createUniqueIdentifier(x,y,z) : " " + createUniqueIdentifier(x,y,z);
                        AMOrowLine += " " + createUniqueIdentifier(z, y, x);
                        AMOcolumnLine += " " + createUniqueIdentifier(x, z,y);
                    }
                    linesToPrint.add(line + " 0");
                    linesToPrint.add(AMOrowLine + " 0");
                    linesToPrint.add(AMOcolumnLine + " 0");
                    clauseCounter += 3;
                }
            }

            for (int gridFactorX = 0; gridFactorX <= size.intValue() - initialSize.intValue(); gridFactorX+=initialSize.intValue()) {
                for (int gridFactorY = 0; gridFactorY <= size.intValue() - initialSize.intValue(); gridFactorY+=initialSize.intValue()) {
                    for (int z = 1; z < size.intValue(); z++) {
                        String line = "AMO";
                        for (int x = 1 + gridFactorX; x <= initialSize.intValue() + gridFactorX; x++) {
                            for (int y = 1 + gridFactorY; y <= initialSize.intValue() + gridFactorY; y++) {
                                line += " " + createUniqueIdentifier(x, y, z);
                            }
                        }
                        linesToPrint.add(line + " 0");
                        clauseCounter++;
                    }
                }
            }

            PrintWriter writer = new PrintWriter(new FileWriter(args[0] + "_AMO.cnf"));

            writer.println("p cnf " + size.pow(3) + " " + clauseCounter);
            for (String line : linesToPrint) {
                writer.println(line);
            }

            writer.close();



        }
    }

    private static String createUniqueIdentifier(int x, int y, int z) {
        return String.valueOf(cantoirPairing(cantoirPairing(x,y), z));
    }

    private static int cantoirPairing(int x, int y) {
        return ((x + y) * (x + y + 1)) / 2 + y;
    }

    public static List<Integer> reverseUniqueIdentifier(String identifier) {
        int identifierVariable = Integer.parseInt(identifier);
        List<Integer> split = reverseCantorPairing(identifierVariable);

        int tmp = split.get(0);
        int z = split.get(1);

        split = reverseCantorPairing(tmp);

        return Arrays.asList(split.get(0), split.get(1), z);
    }

    private static List<Integer> reverseCantorPairing(int z) {
        double w = Math.floor((Math.sqrt(8*z + 1) - 1) / 2);
        double t = ((w * w) + w) / 2;
        int y = (int) (z - t);
        int x = (int) (w - y);

        return Arrays.asList(x, y);
    }
}
