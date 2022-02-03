package cas.thomas.SolutionChecker;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.List;
import java.util.ListIterator;

public class SolutionCheckerConjunctiveFormula extends SolutionCheckerFormula {

    public SolutionCheckerConjunctiveFormula(SolutionCheckerConstraint[] constraints) {
        super(constraints);
    }

    @Override
    public boolean isTrue(List<Integer> variables) {
        for (int i = 0; i < constraints.length; i++) {
            if (!constraints[i].isTrue(variables)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void toDimacsFile(Path filePath, int variables) throws IOException {
        String dimacsString = "p cnf " + variables + " " + constraints.length + "\n";

        for (int i = 0; i < constraints.length; i++) {
            dimacsString += constraints[i].toDimacsString() + "\n";
        }

        PrintWriter writer = new PrintWriter(new FileWriter(filePath.toFile()));

        writer.write(dimacsString);

        writer.close();
    }

    @Override
    public void toDimacsCNFFile(Path filePath, int variables) throws IOException {
        String dimacsString = "p cnf " + variables + " " + constraints.length + "\n";

        for (int i = 0; i < constraints.length; i++) {
            dimacsString += constraints[i].toDimacsCNFString();
        }

        PrintWriter writer = new PrintWriter(new FileWriter(filePath.toFile()));

        writer.write(dimacsString);

        writer.close();
    }


}
