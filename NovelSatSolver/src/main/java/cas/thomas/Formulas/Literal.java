package cas.thomas.Formulas;

import java.util.ArrayList;
import java.util.List;

public class Literal {

    private Integer value;
    private List<Constraint> containedIn;

    public Literal(int value) {
        this.value = value;
        this.containedIn = new ArrayList<>();
    }

    public void condition() {
        for (int i = 0; i < containedIn.size(); i++) {
            containedIn.get(i).condition(value);
        }
    }
}
