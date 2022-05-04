package cas.thomas.SolutionChecker;

import cas.thomas.utils.Pair;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.List;

public class SolutionCheckerConjunctiveFormula extends SolutionCheckerFormula {

    public SolutionCheckerConjunctiveFormula(SolutionCheckerConstraint[] constraints, int variables) {
        super(constraints, variables);
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
    public void toDimacsFile(Path filePath) throws IOException {
        String dimacsString = "p cnf " + variables + " " + constraints.length + "\n";

        for (int i = 0; i < constraints.length; i++) {
            dimacsString += constraints[i].toDimacsString();
        }

        PrintWriter writer = new PrintWriter(new FileWriter(filePath.toFile()));

        writer.write(dimacsString);

        writer.close();
    }

    @Override
    public void toDimacsCNFFile(Path filePath) throws IOException {
        int maxVariables = variables;
        int constraintCounter = 0;

        StringBuilder dimacsString = new StringBuilder();

        for (int i = 0; i < constraints.length; i++) {
            Pair<Integer, Integer> intPair = constraints[i].toDimacsCNFString(dimacsString, maxVariables);
            maxVariables = intPair.getFirstPairPart();
            constraintCounter += intPair.getSecondPairPart();
        }

        StringBuilder finalDimacsString = new StringBuilder();

        finalDimacsString.append("p cnf " + maxVariables + " " + constraintCounter + "\n");

        finalDimacsString.append(dimacsString);

        PrintWriter writer = new PrintWriter(new FileWriter(filePath.toFile()));

        writer.write(finalDimacsString.toString());

        writer.close();
    }


}
