package cas.thomas.SolutionChecker;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public abstract class SolutionCheckerFormula {

    protected SolutionCheckerConstraint[] constraints;
    protected int variables;

    public SolutionCheckerFormula(SolutionCheckerConstraint[] constraints, int variables) {
        this.constraints = constraints;
        this.variables = variables;
    }

    public abstract boolean isTrue(List<Integer> variables);

    public abstract void toDimacsFile(Path filePath) throws IOException;

    public abstract void toDimacsCNFFile(Path filePath) throws IOException;
}
