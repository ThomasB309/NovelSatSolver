package cas.thomas.SolutionChecker;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public abstract class SolutionCheckerFormula {

    protected SolutionCheckerConstraint[] constraints;

    public SolutionCheckerFormula(SolutionCheckerConstraint[] constraints) {
        this.constraints = constraints;
    }

    public abstract boolean isTrue(List<Integer> variables);

    public abstract void toDimacsFile(Path filePath, int variables) throws IOException;

    public abstract void toDimacsCNFFile(Path filePath, int variables) throws IOException;
}
