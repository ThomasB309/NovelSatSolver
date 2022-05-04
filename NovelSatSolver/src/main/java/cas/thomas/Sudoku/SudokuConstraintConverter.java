package cas.thomas.Sudoku;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class SudokuConstraintConverter {

    private static final String FIRST_LINE_STRING = "[1-9]+";

    public static void main(String[] args) throws IOException {

        checkIfSizesMatch(args.length, 1, "Your input contains more than one file or folder!");

        Path filePath = Paths.get(args[0]);

        if (Files.isDirectory(Paths.get(args[0]))) {

        } else {
            List<String> lines = Files.readAllLines(filePath);

            checkForMissingInput(lines);

            BigInteger initialSize = new BigInteger(lines.get(0).split("\\n")[0]);
            BigInteger size = initialSize.pow(2);

            checkIfSizesMatch(lines.size() - 1, size.intValue(), "The number of rows does not match your specified size!");

            List<String> linesToPrint = new ArrayList<>();

            int clauseCounter = 0;

            final String linePattern = "([0-9]+\\s){" + (size.intValue() - 1) + "}[0-9]+";

            clauseCounter = addUnitLiteralsForAlreadyFilledSquares(lines, size, linesToPrint, clauseCounter, linePattern);


            clauseCounter = createAMOConstraintsForRowsAndColumns(size, linesToPrint, clauseCounter);

            clauseCounter = createAmoConstraintsForSquares(initialSize, size, linesToPrint, clauseCounter);

            PrintWriter writer = new PrintWriter(new FileWriter(args[0] + "_AMO.cnf"));

            writer.println("p cnf " + size.pow(3) + " " + clauseCounter);
            for (String line : linesToPrint) {
                writer.println(line);
            }

            writer.close();


        }
    }

    private static void checkIfSizesMatch(int i2, int i3, String s) {
        if (i2 != i3) {
            System.err.println(s);
            System.exit(-1);
        }
    }

    private static void checkForMissingInput(List<String> lines) {
        if (lines.size() == 0) {
            System.err.println("Your input has no lines!");
            System.exit(-1);
        }

        if (!lines.get(0).matches(FIRST_LINE_STRING)) {
            System.err.println("Your first line doesn't specify the sudoku game size!");
            System.exit(-1);
        }
    }

    private static int addUnitLiteralsForAlreadyFilledSquares(List<String> lines, BigInteger size, List<String> linesToPrint, int clauseCounter, String linePattern) {
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);

            if (!line.matches(linePattern)) {
                System.err.println("Row " + i + " doesn't match the specified size or format");
                System.exit(-1);
            }

            String[] lineParts = line.split("\\s");

            for (int a = 0; a < lineParts.length; a++) {
                if (!lineParts[a].equals("0")) {
                    linesToPrint.add(createUniqueIdentifier((a + 1), i, Integer.parseInt(lineParts[a]), size.intValue()) + " " +
                            "0");
                    clauseCounter++;
                }
            }

        }
        return clauseCounter;
    }

    private static int createAMOConstraintsForRowsAndColumns(BigInteger size, List<String> linesToPrint, int clauseCounter) {
        for (int x = 1; x <= size.intValue(); x++) {
            for (int y = 1; y <= size.intValue(); y++) {
                String line = "";
                String AMOrowLine = "AMO";
                String AMOcolumnLine = "AMO";
                for (int z = 1; z <= size.intValue(); z++) {
                    line += line.equals("") ? createUniqueIdentifier(x, y, z, size.intValue()) :
                            " " + createUniqueIdentifier(x, y, z, size.intValue());
                    AMOrowLine += " " + createUniqueIdentifier(z, y, x, size.intValue());
                    AMOcolumnLine += " " + createUniqueIdentifier(x, z, y, size.intValue());
                }
                linesToPrint.add(line + " 0");
                linesToPrint.add(AMOrowLine + " 0");
                linesToPrint.add(AMOcolumnLine + " 0");
                clauseCounter += 3;
            }
        }
        return clauseCounter;
    }

    private static int createAmoConstraintsForSquares(BigInteger initialSize, BigInteger size, List<String> linesToPrint, int clauseCounter) {
        for (int gridFactorX = 0; gridFactorX <= size.intValue() - initialSize.intValue(); gridFactorX += initialSize.intValue()) {
            for (int gridFactorY = 0; gridFactorY <= size.intValue() - initialSize.intValue(); gridFactorY += initialSize.intValue()) {
                for (int z = 1; z < size.intValue(); z++) {
                    String line = "AMO";
                    for (int x = 1 + gridFactorX; x <= initialSize.intValue() + gridFactorX; x++) {
                        for (int y = 1 + gridFactorY; y <= initialSize.intValue() + gridFactorY; y++) {
                            line += " " + createUniqueIdentifier(x, y, z, size.intValue());
                        }
                    }
                    linesToPrint.add(line + " 0");
                    clauseCounter++;
                }
            }
        }
        return clauseCounter;
    }

    private static String createUniqueIdentifier(int x, int y, int z, int fieldSize) {
        return String.valueOf((x - 1) * fieldSize * fieldSize + (y - 1) * fieldSize + z);
    }

}
