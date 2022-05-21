package cas.thomas.Formulas;

import cas.thomas.SolutionChecker.SolutionCheckerConstraint;
import cas.thomas.SolutionChecker.SolutionCheckerDNFConstraint;
import cas.thomas.utils.IntegerArrayQueue;
import cas.thomas.utils.IntegerStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DNFConstraint extends Constraint {

    protected int[][] terms;
    protected Set<Integer> unitLiteralsPropagatedDuringInitialization;
    private int[] falseTermCache;
    private Set<Integer> visitedLiterals;
    private HashMap<Integer, Integer> literalsMapping;
    private List<Integer>[] positiveLiteralsIntersectionList;
    protected Set<Integer> literalSet;
    private int falseCounter;
    private List<Integer> firstWatchedTermSet;
    private List<Integer> secondWatchedTermSet;
    private int firstWatchedNonFalseTerm;
    private int secondWatchedNonFalseTerm;
    private int[] watchedLiterals;
    private int[] watchedTermsArray;
    private int counter = 0;
    private List<Integer>[] intersectionIndeces;
    private int testCounter;
    private int firstWatchedTermTurnedFalseFirst;
    private int[] addedLiterals;
    protected int[][] solutionCheckerCopy;

    public DNFConstraint(final int[][] terms, final List<Constraint>[] positivelyWatchedList,
                         final List<Constraint>[] negativelyWatchedList, int numberOfVariables) {

        assert (terms.length > 0);

        this.terms = terms;
        this.falseTermCache = new int[terms.length];
        this.visitedLiterals = new HashSet<>();
        this.unitLiteralsPropagatedDuringInitialization = new HashSet<>();
        this.literalSet = new HashSet<>();
        this.falseCounter = 0;
        this.intersectionIndeces = new ArrayList[terms.length];

        Arrays.sort(terms, Comparator.comparing(term -> term.length));

        if (terms.length == 1) {
            for (int i = 0; i < terms[0].length; i++) {
                this.unitLiteralsPropagatedDuringInitialization.add(terms[0][i]);
            }
            return;
        }

        this.unitLiteralsPropagatedDuringInitialization = new HashSet<>();

        final Set<Integer> literalsInDNF = new HashSet<>();

        int nullCounter = termSubsumption(terms);

        this.terms = new int[terms.length - nullCounter][];

        int counter = 0;
        for (int i = 0; i < terms.length; i++){
            if (terms[i] != null) {
                this.terms[counter] = terms[i];
                counter++;
            }
        }

        firstWatchedNonFalseTerm = -1;
        secondWatchedNonFalseTerm = -1;


        this.calculateTermIntersectionsAndUnitLiterals(this.terms, literalsInDNF);

        watchedLiterals = new int[literalSet.size()];
        addedLiterals = new int[literalSet.size()];
        watchedTermsArray = new int[terms.length];

        /*if (terms.length == 5 && Arrays.equals(terms[0], new int[]{-62,-48,-39,58,84}) && Arrays.equals(terms[1],
                new int[]{-76,-63,-62,-44,35})) {
            System.out.println("hello");
        }*/


        findWatchedTermSets(new int[numberOfVariables], new IntegerStack());

        assignWatchedLiterals(positivelyWatchedList, negativelyWatchedList);
    }

    public DNFConstraint(final int[][] terms, final List<Constraint>[] positivelyWatchedList,
                         final List<Constraint>[] negativelyWatchedList, int numberOfVariables,
                         int[][] solutionCheckerCopy) {
        this(terms,positivelyWatchedList,negativelyWatchedList,numberOfVariables);
        this.solutionCheckerCopy = solutionCheckerCopy;
    }

    public void removeAddedLiteral(int literal) {
        int literalMapped = literalsMapping.get(-literal);
        addedLiterals[literalMapped] = 0;
    }

    private void propagateWatchedTermSet(int[] variables, IntegerStack literalsToPropagate) {

    }

    private void secondWatchedNonIntersectingTermTurnedFalse(int[] variables, IntegerStack literalsToPropagate) {
        boolean foundSecondWatchedNonIntersectingTerm = false;
        for (int i = 0; i < terms.length; i++) {
            if (!firstWatchedTermSet.contains(i) && !checkIfTermIsFalse(variables, i, falseTermCache)) {
                secondWatchedNonFalseTerm = i;
                foundSecondWatchedNonIntersectingTerm = true;
                break;
            }
        }

        if (!foundSecondWatchedNonIntersectingTerm) {

        }
    }

    public boolean containsLiteral(int literal) {
        return literalsMapping.containsKey(literal);
    }

    public void findWatchedTermSets(int[] variables, IntegerStack literalsToPropagate) {

        for (int i = 0; i < terms.length; i++) {
            if (!checkIfTermIsFalse(variables, i, falseTermCache)) {
                firstWatchedTermSet = getIntersectionIndeces(i, variables);
                firstWatchedNonFalseTerm = i;
                break;
            }
        }

        assert(firstWatchedTermSet != null);

        for (int i = 0; i < terms.length; i++) {
            if (!firstWatchedTermSet.contains(i) && !checkIfTermIsFalse(variables, i, falseTermCache)) {
                secondWatchedTermSet = getIntersectionIndeces(i, variables);
                secondWatchedNonFalseTerm = i;
                return;
            }
        }

        secondWatchedTermSet = null;
        secondWatchedNonFalseTerm = -1;
        firstWatchedTermTurnedFalseFirst = -1;





        /*boolean firstWatchedTermSetContainsOneNonFalseTerm = false;
        for (Integer termIndex : firstWatchedTermSet) {
            if (!checkIfTermIsFalse(variables, termIndex, falseTermCache)) {
                firstWatchedTermSetContainsOneNonFalseTerm = true;
                break;
            }
        }

        boolean foundFirstWatchedTerm = false;
        if (!firstWatchedTermSetContainsOneNonFalseTerm) {
            for (int i = 0; i < terms.length; i++) {
                if (i != secondWatchedNonFalseTerm && !checkIfTermIsFalse(variables, i, falseTermCache)) {
                    firstWatchedTermSet = new HashSet<>(getIntersectionIndeces(i, variables));
                    foundFirstWatchedTerm = true;
                    break;
                }
            }
        }

        if (!foundFirstWatchedTerm) {
            // propagate secondWatchedTerm and return
        }


        /*boolean foundFirstWatchedTermSet = false;
        boolean foundSecondWatchedTermSet = false;
        int firstWatchedTermCache = firstWatchedTerm;
        for (int i = 0; i < terms.length; i++) {
            if (!checkIfTermIsFalse(variables, i, falseTermCache)) {
                if (!foundFirstWatchedTermSet) {
                    watchedTermSet = new HashSet<>(getIntersectionIndeces(i, variables));
                    firstWatchedTerm = i;
                    foundFirstWatchedTermSet = true;
                } else if (!watchedTermSet.contains(i)) {
                    watchedTermSet.add(i);
                    secondWatchedNonIntersectingTerm = i;
                    foundSecondWatchedTermSet = true;
                    break;
                }

            }
        }

        if (secondWatchedNonIntersectingTerm == firstWatchedTerm) {
            secondWatchedNonIntersectingTerm = firstWatchedTermCache;
        }


        if (!foundFirstWatchedTermSet) {
            System.out.println(testCounter);
            return;
        }

        if (!foundSecondWatchedTermSet) {
            if (watchedTermSet.contains(secondWatchedNonIntersectingTerm)) {
                for (int i = 0; i < terms.length; i++) {
                    if (!watchedTermSet.contains(i)) {
                        secondWatchedNonIntersectingTerm = i;
                        break;
                    }
                }
            }
        }

        if (foundFirstWatchedTermSet && foundSecondWatchedTermSet) {
            //watchedTermSet = new HashSet<>(Arrays.asList(firstWatchedTerm,secondWatchedNonIntersectingTerm));
            return;
        }


        int[] firstTerm = terms[firstWatchedTerm];

        for (int i = 0; i < firstTerm.length; i++) {
            int currentLiteral = firstTerm[i];
            boolean propagateCurrentLiteral = true;
            for (Integer index : watchedTermSet) {
                int[] currentTerm = terms[index];

                if (!checkIfTermIsFalse(variables, index, falseTermCache) && Arrays.binarySearch(currentTerm,
                        currentLiteral) < 0) {
                    propagateCurrentLiteral = false;
                    break;
                }
            }

            if (propagateCurrentLiteral && !unitLiteralsPropagatedDuringInitialization.contains(currentLiteral)) {
                literalsToPropagate.push(currentLiteral);
            }
        }

       if (secondWatchedNonIntersectingTerm != -1) {
           watchedTermSet.add(secondWatchedNonIntersectingTerm);
       }*/


    }

    private void propagateIfNoSecondaryWatchedTermSet(int[] variables, IntegerStack literalsToPropagate) {
        propagateSharedLiteralsInTermSet(variables, literalsToPropagate, firstWatchedTermSet);
    }

    private boolean propagateWatchedTermSet(boolean firstTermSetToReplace, int[] variables,
                                         IntegerStack literalsToPropagate, List<Constraint>[] positivelyWatched,
                                         List<Constraint>[] negativelyWatched) {

        List<Integer> termSetToPropagate;
        List<Integer> termSetToReplace;
        int termToPropagate;
        int termtoReplace;


        if (firstTermSetToReplace) {
            termSetToPropagate = secondWatchedTermSet;
            termSetToReplace = firstWatchedTermSet;
            termToPropagate = secondWatchedNonFalseTerm;
            termtoReplace = firstWatchedNonFalseTerm;
        } else {
            termSetToPropagate = firstWatchedTermSet;
            termSetToReplace = secondWatchedTermSet;
            termToPropagate = firstWatchedNonFalseTerm;
            termtoReplace = secondWatchedNonFalseTerm;
        }



        if (checkIfTermIsFalse(variables, termToPropagate, falseTermCache)) {
            propagateSharedLiteralsInTermSet(variables, literalsToPropagate, termSetToReplace);
            return false;
        }

        for (int i = 0; i < terms.length; i++) {
            if (!termSetToPropagate.contains(i) && !checkIfTermIsFalse(variables, i, falseTermCache)) {
                if (firstTermSetToReplace) {
                    firstWatchedTermSet = getIntersectionIndeces(i, variables);
                    firstWatchedNonFalseTerm = i;
                } else {
                    secondWatchedTermSet = getIntersectionIndeces(i, variables);
                    secondWatchedNonFalseTerm = i;
                }


                return true;
            }
        }

        if (firstTermSetToReplace) {
            firstWatchedTermTurnedFalseFirst = 1;
        } else {
            firstWatchedTermTurnedFalseFirst = -1;
        }

        propagateSharedLiteralsInTermSet(variables, literalsToPropagate, termSetToPropagate);

        return false;

    }

    private void propagateSharedLiteralsInTermSet(int[] variables, IntegerStack literalsToPropagate, List<Integer> termSetToPropagate) {
        int[] firstTerm = null;

        for (Integer termIndex : termSetToPropagate) {
            if (!checkIfTermIsFalse(variables, termIndex, falseTermCache)) {
                firstTerm = terms[termIndex];
                break;
            }
        }

        if (firstTerm == null) {
            if (testCounter != 0) {
                System.out.println(testCounter);
            }
            return;
        }


        for (int i = 0; i < firstTerm.length; i++) {
            int currentLiteral = firstTerm[i];
            boolean propagateCurrentLiteral = true;
            for (Integer index : termSetToPropagate) {
                int[] currentTerm = terms[index];

                if (!checkIfTermIsFalse(variables, index, falseTermCache) && Arrays.binarySearch(currentTerm,
                        currentLiteral) < 0) {
                    propagateCurrentLiteral = false;
                    break;
                }
            }

            if (propagateCurrentLiteral && !unitLiteralsPropagatedDuringInitialization.contains(currentLiteral)) {
                literalsToPropagate.push(currentLiteral);
            }
        }
    }

    private void assignWatchedLiterals(List<Constraint>[] positivelyWatchedList,
                                       List<Constraint>[] negativelyWatchedList) {



        int[] newWatchedLiterals = new int[literalSet.size()];
        for (Integer termIndex : firstWatchedTermSet) {
            int[] term = terms[termIndex];

            for (int i = 0; i < term.length; i++){
                int currentLiteral = term[i];
                int currentLiteralMapped = literalsMapping.get(currentLiteral);

                if (watchedLiterals[currentLiteralMapped] == 0) {
                    newWatchedLiterals[currentLiteralMapped] = 1;
                    watchedLiterals[currentLiteralMapped] = 1;

                    if (addedLiterals[currentLiteralMapped] != 1) {
                        addedLiterals[currentLiteralMapped] = 1;
                        if (currentLiteral < 0) {
                            negativelyWatchedList[Math.abs(currentLiteral)].add(this);
                        } else {
                            positivelyWatchedList[Math.abs(currentLiteral)].add(this);
                        }
                    }
                } else {
                    newWatchedLiterals[currentLiteralMapped] = 1;
                }

            }
        }

        if (secondWatchedTermSet != null) {
            for (Integer termIndex : secondWatchedTermSet) {
                int[] term = terms[termIndex];
                for (int i = 0; i < term.length; i++) {
                    int currentLiteral = term[i];
                    int currentLiteralAbsoluteValue = Math.abs(currentLiteral);
                    int currentLiteralMapped = literalsMapping.get(currentLiteral);

                    if (watchedLiterals[currentLiteralMapped] == 0) {
                        newWatchedLiterals[currentLiteralMapped] = 1;
                        watchedLiterals[currentLiteralMapped] = 1;

                        if (addedLiterals[currentLiteralMapped] != 1) {
                            addedLiterals[currentLiteralMapped] = 1;
                            if (currentLiteral < 0) {
                                negativelyWatchedList[currentLiteralAbsoluteValue].add(this);
                            } else {
                                positivelyWatchedList[currentLiteralAbsoluteValue].add(this);
                            }
                        }
                    } else {
                        newWatchedLiterals[currentLiteralMapped] = 1;
                    }
                }
            }
        }
        watchedLiterals = newWatchedLiterals;
    }

    private int getIntersectionIndeces(int[] term) {
        ArrayList<Integer> intersectionIndeces = new ArrayList<>();
        int[] termAlreadyInIntersection = new int[terms.length];

        for (int i = 0; i < term.length; i++) {
            int currentLiteral = term[i];

            if (unitLiteralsPropagatedDuringInitialization.contains(currentLiteral)) {
                continue;
            }


            for (Integer termIndexIntersection : positiveLiteralsIntersectionList[literalsMapping.get(currentLiteral)]) {

                if (termAlreadyInIntersection[termIndexIntersection] == 0) {
                    termAlreadyInIntersection[termIndexIntersection] = 1;
                    intersectionIndeces.add(termIndexIntersection);
                }
            }
        }

        return intersectionIndeces.size();
    }

    private List<Integer> getIntersectionIndeces(int termIndex, int[] variables) {

        if (intersectionIndeces[termIndex] != null) {
            return intersectionIndeces[termIndex];
        }

        ArrayList<Integer> intersectionIndeces = new ArrayList<>();
        int[] termAlreadyInIntersection = new int[terms.length];
        int[] term = terms[termIndex];

        for (int i = 0; i < term.length; i++) {
            int currentLiteral = term[i];

            if (unitLiteralsPropagatedDuringInitialization.contains(currentLiteral)) {
                continue;
            }


            for (Integer termIndexIntersection : positiveLiteralsIntersectionList[literalsMapping.get(currentLiteral)]) {

                if (termAlreadyInIntersection[termIndexIntersection] == 0) {
                    termAlreadyInIntersection[termIndexIntersection] = 1;
                    intersectionIndeces.add(termIndexIntersection);
                }
            }
        }

        /*for (Iterator<Integer> iterator = intersectionIndeces.iterator(); iterator.hasNext();) {
            int index = iterator.next();
            if (checkIfTermIsFalse(variables, index, falseTermCache)) {
                iterator.remove();
            }
        }*/

        this.intersectionIndeces[termIndex] = intersectionIndeces;

        return intersectionIndeces;

    }

    private int termSubsumption(int[][] terms) {

        int nullCounter = 0;

        for (int i = 0; i < terms.length; i++) {
            int[] term = terms[i];

            if (term == null) {
                continue;
            }

            Arrays.sort(term);

            for (int a = i + 1; a < terms.length; a++) {
                int[] secondTerm = terms[a];

                if (secondTerm == null || secondTerm.length < term.length) {
                    continue;
                }

                boolean subset = true;
                for (int k = 0; k < term.length; k++) {
                    if (Arrays.binarySearch(secondTerm, term[k]) < 0) {
                        subset = false;
                        break;
                    }
                }

                if (subset) {
                    terms[a] = null;
                    nullCounter++;
                }
            }
        }

        return nullCounter;
    }

    private void calculateTermIntersectionsAndUnitLiterals(final int[][] terms, final Set<Integer> literalsInDNF) {
        int literalCounter = 0;
        Map<Integer, List<Integer>> literalIntersections = new HashMap<>();

        Set<Integer> variableSet = new HashSet<>();

        for (int i = 0; i < terms.length; i++) {
            assert (terms[i].length > 0);

            this.terms[i] = terms[i];
            final List<Integer> termSet = new LinkedList<>();

            for (int j = 0; j < terms[i].length; j++) {
                final int currentLiteral = terms[i][j];

                if (!this.literalSet.contains(currentLiteral)) {
                    literalCounter++;
                    this.literalSet.add(currentLiteral);
                }

                if (!variableSet.contains(Math.abs(currentLiteral))) {
                    variableSet.add(Math.abs(currentLiteral));
                    this.variableCounter++;
                }


                literalsInDNF.add(currentLiteral);
                termSet.add(currentLiteral);
                if (literalIntersections.containsKey(currentLiteral)) {
                    final List<Integer> intersectionList = literalIntersections.get(currentLiteral);
                    intersectionList.add(i);

                    if (intersectionList.size() == terms.length) {
                        this.unitLiteralsPropagatedDuringInitialization.add(currentLiteral);
                    }

                } else {
                    final List<Integer> newList = new ArrayList<>();
                    newList.add(i);
                    literalIntersections.put(currentLiteral, newList);
                }
            }

        }

        positiveLiteralsIntersectionList = new ArrayList[literalCounter];
        literalsMapping = new HashMap<>();

        int counter = 0;
        for (Integer key : literalSet) {
            literalsMapping.put(key, counter);
            positiveLiteralsIntersectionList[counter] = literalIntersections.get(key);
            counter++;
        }
    }

    private void assignWatchedLiteralsToWatchList(final Set<Integer> literalsInDNF,
                                                  final List<Constraint>[] positivelyWatchedList,
                                                  final List<Constraint>[] negativelyWatchedList) {


        for (final Integer literal : literalsInDNF) {
            final int literalAbsoluteValue = Math.abs(literal);
            if (literal < 0) {
                negativelyWatchedList[literalAbsoluteValue].add(this);
            } else {
                positivelyWatchedList[literalAbsoluteValue].add(this);
            }
        }


    }

    protected DNFConstraint() {

    }

    @Override
    public boolean propagate(final int propagatedLiteral, final int[] variableAssignments, final int[] unitLiteralState, final IntegerArrayQueue unitLiterals, final List<Constraint>[] positivelyWatched, final List<Constraint>[] negativelyWatched, final Constraint[] reasonClauses) {

        //this.visitedLiterals.add(-propagatedLiteral);
        if (this.terms.length == 1) {
            this.propagateIfConstraintHasOnlyOneTerm(0, variableAssignments, unitLiteralState, unitLiterals, reasonClauses);
            return true;
        }

        final IntegerStack literalsToPropagate = new IntegerStack();

        falseTermCache = new int[terms.length];
        falseCounter = 0;

        if (secondWatchedTermSet == null) {
            propagateIfNoSecondaryWatchedTermSet(variableAssignments, literalsToPropagate);

            final boolean propagate = literalsToPropagate.size() > 0;

            if (this.addUnitLiterals(variableAssignments, unitLiteralState, unitLiterals, reasonClauses, literalsToPropagate))
                return true;

            return propagate;

        }

        List<Integer> falseTerms = positiveLiteralsIntersectionList[literalsMapping.get(-propagatedLiteral)];

        boolean propagateInFirstWatchedTerm = false;
        boolean propagateInSecondWatchedTerm = false;
        int propagateInFirstWatchedTermIfFirstAndSecondWatchedLiteralAreFalseCounter = 0;
        int propagateInSecondWatchedTermIfFirstAndSecondWatchedLiteralAreFalseCounter = 0;
        int counter = 0;
        for (Integer termIndex : falseTerms) {
            falseTermCache[termIndex] = -1;

            if (checkIfTermIsFalseExludingLiteral(terms[termIndex], variableAssignments, -propagatedLiteral)) {

                counter++;
                continue;

            }

            if (termIndex == firstWatchedNonFalseTerm) {
                propagateInFirstWatchedTerm = true;
            }

            if (termIndex == secondWatchedNonFalseTerm) {
                propagateInSecondWatchedTerm = true;
            }

            if (firstWatchedTermSet.contains(termIndex)) {
                propagateInFirstWatchedTermIfFirstAndSecondWatchedLiteralAreFalseCounter++;
            }

            if (secondWatchedTermSet.contains(termIndex)) {
                propagateInSecondWatchedTermIfFirstAndSecondWatchedLiteralAreFalseCounter++;
            }

        }

        if (counter == falseTerms.size()) {
            return false;
        }

        if (!(propagateInFirstWatchedTerm || propagateInSecondWatchedTerm)) {
            boolean firstWatchedTermTrue = !checkIfTermIsFalse(variableAssignments, firstWatchedNonFalseTerm,
                    falseTermCache);
            boolean secondWatchedTermTrue = !checkIfTermIsFalse(variableAssignments, secondWatchedNonFalseTerm,
                    falseTermCache);

            if (firstWatchedTermTrue && secondWatchedTermTrue) {
                return false;
            }

            assert (firstWatchedTermTurnedFalseFirst != 0);

            if (firstWatchedTermTurnedFalseFirst == -1) {
                propagateWatchedTermSet(true, variableAssignments, literalsToPropagate,
                        positivelyWatched,negativelyWatched);
            } else {
                propagateWatchedTermSet(false, variableAssignments, literalsToPropagate,
                        positivelyWatched,negativelyWatched);
            }

            final boolean propagate = literalsToPropagate.size() > 0;

            if (this.addUnitLiterals(variableAssignments, unitLiteralState, unitLiterals, reasonClauses, literalsToPropagate))
                return true;

            return propagate;


        }


        if (propagateWatchedTermSet(propagateInFirstWatchedTerm, variableAssignments, literalsToPropagate,
                positivelyWatched, negativelyWatched)) {
            assignWatchedLiterals(positivelyWatched, negativelyWatched);
        }

        final boolean propagate = literalsToPropagate.size() > 0;

        if (this.addUnitLiterals(variableAssignments, unitLiteralState, unitLiterals, reasonClauses, literalsToPropagate))
            return true;

        return propagate;
    }

    @Override
    public SolutionCheckerConstraint getSolutionCheckerConstraint() {
        return new SolutionCheckerDNFConstraint(solutionCheckerCopy);
    }

    public int calculateLBDScore(final int[] variableDecisionLevels) {
        final Set<Integer> distinctDecisionLevels = new HashSet<>();

        for (int i = 0; i < this.terms.length; i++) {
            for (int j = 0; j < this.terms[i].length; j++) {
                distinctDecisionLevels.add(variableDecisionLevels[Math.abs(this.terms[i][j])]);
            }
        }

        return distinctDecisionLevels.size();
    }

    @Override
    public List<Constraint> handleConflict(final int numberOfVariables, final IntegerStack trail, final int[] variableDecisionLevel, final int[] variablesInvolvedInConflict, final Formula formula) {
        final int[] stateOfResolvedVariables = new int[numberOfVariables];

        for (int i = 0; i < this.terms.length; i++) {
            for (int j = 0; j < this.terms[i].length; j++) {
                final int currentLiteralAbsoluteValue = Math.abs(this.terms[i][j]);
                stateOfResolvedVariables[currentLiteralAbsoluteValue] = this.terms[i][j];
                variablesInvolvedInConflict[currentLiteralAbsoluteValue] = 1;
            }
        }

        return this.findReasonConstraintAndResolveConflict(trail, variablesInvolvedInConflict, formula, stateOfResolvedVariables);
    }

    private List<Constraint> findReasonConstraintAndResolveConflict(final IntegerStack trail, final int[] variablesInvolvedInConflict, final Formula formula, final int[] stateOfResolvedVariables) {
        trail.prepareIterationWithoutPop();
        while (trail.hasNextWithoutPop()) {
            final int literal = trail.peekNextWithoutPop();
            final Constraint reasonConstraint = formula.getReasonClauses(literal);

            if (reasonConstraint == null) {
                continue;
            }

            return reasonConstraint.resolveConflict(this, trail, stateOfResolvedVariables, formula, variablesInvolvedInConflict);
        }

        return Arrays.asList();
    }

    @Override
    public List<Constraint> resolveConflict(final Constraint conflictConstraint, final IntegerStack trail,
                                            final int[] stateOfResolvedVariables, final Formula formula,
                                            final int[] variablesInvolvedInConflict) {
        /*final int[] conflictLiterals = conflictConstraint.getLiterals();
        final int[][] reasonTerms = terms;
        final int conflictLiteral = formula.getConflictLiteral();

        final ArrayList<int[]> resolutionTerms = new ArrayList<>();

        this.getResolutionTermsFromConflictingDisjunctiveConstraint(conflictLiterals, conflictLiteral, resolutionTerms);

        this.getResolutionTermsFromReasonDNFConstraint(variablesInvolvedInConflict, reasonTerms, conflictLiteral, resolutionTerms);


        return Arrays.asList(formula.addDNFConstraints(resolutionTerms.toArray(int[][]::new)));*/


        int conflictLiteral = formula.getConflictLiteral();
        int[] conflictLiterals = conflictConstraint.getLiterals();
        int[] variableAssignments = formula.getVariables();

        Set<Integer> resolutionLiterals = new HashSet<>();

        getConflictResolutionLiterals(-conflictLiteral, variableAssignments, resolutionLiterals);

        for (int i = 0; i < conflictLiterals.length; i++) {
            if (conflictLiterals[i] != conflictLiteral) {
                resolutionLiterals.add(conflictLiterals[i]);
            }
        }

        Integer[] literals = new Integer[resolutionLiterals.size()];

        int pointer = 0;
        for (Integer literal : resolutionLiterals) {
            variablesInvolvedInConflict[Math.abs(literal)] = 1;
            literals[pointer] = literal;
            pointer++;
        }

        Arrays.sort(literals, Comparator.comparingInt(a -> formula.getDecisionLevelOfVariables()[Math.abs(a)] * -1));

        int[] literalsPrimitive = new int[literals.length];

        for (int i = 0 ; i < literals.length; i++) {
            literalsPrimitive[i] = literals[i];
        }

        return Arrays.asList(formula.addDisjunctiveConstraint(literalsPrimitive));

    }

    private void getResolutionTermsFromConflictingDisjunctiveConstraint(final int[] conflictLiterals, final int conflictLiteral, final ArrayList<int[]> resolutionTerms) {
        for (int i = 0; i < conflictLiterals.length; i++) {
            final int currentLiteral = conflictLiterals[i];
            if (currentLiteral != conflictLiteral) {
                resolutionTerms.add(new int[]{currentLiteral});
            }
        }
    }

    private void getResolutionTermsFromReasonDNFConstraint(final int[] variablesInvolvedInConflict, final int[][] reasonTerms, final int conflictLiteral, final ArrayList<int[]> resolutionTerms) {
        for (int i = 0; i < reasonTerms.length; i++) {
            boolean containsConflictLiteral = false;
            for (int j = 0; j < reasonTerms[i].length; j++) {
                variablesInvolvedInConflict[Math.abs(reasonTerms[i][j])] = 1;
                if (reasonTerms[i][j] == -conflictLiteral) {
                    containsConflictLiteral = true;
                    break;
                }
            }

            if (!containsConflictLiteral) {
                resolutionTerms.add(reasonTerms[i]);
            }
        }
    }

    @Override
    public List<Constraint> resolveConflict(final AMOConstraint conflictConstraint, final IntegerStack trail,
                                            final int[] stateOfResolvedvariables, final Formula formula,
                                            final int[] variablesInvolvedInConflict) {
        /*final int[] conflictLiterals = conflictConstraint.getLiterals();
        final int[][] reasonTerms = terms;
        final int conflictLiteral = formula.getConflictLiteral();

        final ArrayList<int[]> resolutionTerms = new ArrayList<>();
        final int[] termFromAmoLiterals = this.getResolutionTermsFromConflictingAMOConstraint(conflictLiterals, conflictLiteral);

        resolutionTerms.add(termFromAmoLiterals);

        this.getResolutionTermsFromReasonDNFConstraint(variablesInvolvedInConflict, reasonTerms, conflictLiteral, resolutionTerms);

        return Arrays.asList(formula.addDNFConstraints(resolutionTerms.toArray(int[][]::new)));*/

        int conflictLiteral = formula.getConflictLiteral();
        int[] conflictLiterals = conflictConstraint.getLiterals();
        int[] variableAssignments = formula.getVariables();

        Set<Integer> resolutionLiterals = new HashSet<>();

        getConflictResolutionLiterals(-conflictLiteral, variableAssignments, resolutionLiterals);

        for (int i = 0; i < conflictLiterals.length; i++) {
            int currentLiteral = conflictLiterals[i];
            int currentLiteralAbsoluteValue = Math.abs(currentLiteral);
            if (currentLiteral != -conflictLiteral && variableAssignments[currentLiteralAbsoluteValue] * currentLiteral > 0) {
                resolutionLiterals.add(-conflictLiterals[i]);
            }
        }

        Integer[] literals = new Integer[resolutionLiterals.size()];

        int pointer = 0;
        for (Integer literal : resolutionLiterals) {
            variablesInvolvedInConflict[Math.abs(literal)] = 1;
            literals[pointer] = literal;
            pointer++;
        }

        Arrays.sort(literals, Comparator.comparingInt(a -> formula.getDecisionLevelOfVariables()[Math.abs(a)] * -1));

        int[] literalsPrimitive = new int[literals.length];

        for (int i = 0 ; i < literals.length; i++) {
            literalsPrimitive[i] = literals[i];
        }

        return Arrays.asList(formula.addDisjunctiveConstraint(literalsPrimitive));

    }

    private int[] getResolutionTermsFromConflictingAMOConstraint(final int[] conflictLiterals, final int conflictLiteral) {
        final int[] termFromAmoLiterals = new int[conflictLiterals.length - 1];
        int counter = 0;
        for (int i = 0; i < conflictLiterals.length; i++) {
            if (conflictLiterals[i] != -conflictLiteral) {
                termFromAmoLiterals[counter] = -conflictLiterals[i];
                counter++;
            }
        }
        return termFromAmoLiterals;
    }

    @Override
    public List<Constraint> resolveConflict(final DNFConstraint conflictConstraint, final IntegerStack trail,
                                            final int[] stateOfResolvedVariables, final Formula formula,
                                            final int[] variablesInvolvedInConflict) {


        int conflictLiteral = formula.getConflictLiteral();
        int[] variableAssignments = formula.getVariables();

        Set<Integer> resolutionLiterals = new HashSet<>();

        conflictConstraint.getConflictResolutionLiterals(conflictLiteral,
                variableAssignments, resolutionLiterals);

        getConflictResolutionLiterals(-conflictLiteral, variableAssignments, resolutionLiterals);

        Integer[] literals = new Integer[resolutionLiterals.size()];

        int pointer = 0;
        for (Integer literal : resolutionLiterals) {
            variablesInvolvedInConflict[Math.abs(literal)] = 1;
            literals[pointer] = literal;
            pointer++;
        }

        Arrays.sort(literals, Comparator.comparingInt(a -> formula.getDecisionLevelOfVariables()[Math.abs(a)] * -1));

        int[] literalsPrimitive = new int[literals.length];

        for (int i = 0 ; i < literals.length; i++) {
            literalsPrimitive[i] = literals[i];
        }

        return Arrays.asList(formula.addDisjunctiveConstraint(literalsPrimitive));

        /*IntegerStack resolvedLiterals = new IntegerStack();
        trail.prepareIterationWithoutPop();
        while (trail.hasNextWithoutPop()) {
            final int stackLiteral = trail.peekNextWithoutPop();
            int currentLiteral = Math.abs(stackLiteral) * variableAssignments[Math.abs(stackLiteral)];
            int currentLiteralAbsoluteValue = Math.abs(currentLiteral);


            if (currentLiteralAbsoluteValue == Math.abs(conflictLiteral)) {
                variablesInvolvedInConflict[currentLiteralAbsoluteValue] = 1;
                continue;
            }

            boolean conflictConstraintContainsLiteral = conflictConstraint.containsLiteral(-currentLiteral);
            boolean reasonConstraintContainsLiteral = containsLiteral(-currentLiteral);

            if (conflictConstraintContainsLiteral || reasonConstraintContainsLiteral) {
                variablesInvolvedInConflict[currentLiteralAbsoluteValue] = 1;
                resolvedLiterals.push(-currentLiteral);
            }

        }

        int[] literals2 = resolvedLiterals.getInternalArray();

        Arrays.sort(literals2);
        Arrays.sort(literals);

        if (!Arrays.equals(literals, literals2)) {
            System.out.println();
        }

        return Arrays.asList(formula.addDisjunctiveConstraint(literals2));*/

        /*int conflictLiteral = formula.getConflictLiteral();
        int[] variableAssignments = formula.getVariables();
        IntegerStack resolvedLiterals = new IntegerStack();
        trail.prepareIterationWithoutPop();
        while (trail.hasNextWithoutPop()) {
            final int stackLiteral = trail.peekNextWithoutPop();
            int currentLiteral = Math.abs(stackLiteral) * variableAssignments[Math.abs(stackLiteral)];
            int currentLiteralAbsoluteValue = Math.abs(currentLiteral);


            if (currentLiteralAbsoluteValue == Math.abs(conflictLiteral)) {
                variablesInvolvedInConflict[currentLiteralAbsoluteValue] = 1;
                continue;
            }

            boolean conflictConstraintContainsLiteral = conflictConstraint.containsLiteral(-currentLiteral);
            boolean reasonConstraintContainsLiteral = containsLiteral(-currentLiteral);

            if (conflictConstraintContainsLiteral || reasonConstraintContainsLiteral) {
                variablesInvolvedInConflict[currentLiteralAbsoluteValue] = 1;
                resolvedLiterals.push(-currentLiteral);
            }

        }

        int[] literals = resolvedLiterals.getInternalArray();

        return Arrays.asList(formula.addDisjunctiveConstraint(literals));*/
        /*final int[][] conflictTerms = conflictConstraint.terms;
        final int[][] reasonTerms = terms;
        final int conflictLiteral = formula.getConflictLiteral();
        final ArrayList<int[]> resolutionTerms = new ArrayList<>();


        this.getResolutionTermsFromConflictingDNFConstraint(conflictTerms, conflictLiteral, resolutionTerms, variablesInvolvedInConflict);

        this.getResolutionTermsFromReasonDNFConstraint(variablesInvolvedInConflict, reasonTerms, conflictLiteral, resolutionTerms);

        return Arrays.asList(formula.addDNFConstraints(resolutionTerms.toArray(int[][]::new)));*/
    }

    private void getResolutionTermsFromConflictingDNFConstraint(final int[][] conflictTerms,
                                                                final int conflictLiteral,
                                                                final ArrayList<int[]> resolutionTerms,
                                                                int[] variablesInvolvedInConflict) {
        for (int i = 0; i < conflictTerms.length; i++) {
            boolean containsConflictLiteral = false;
            for (int j = 0; j < conflictTerms[i].length; j++) {
                if (conflictTerms[i][j] == conflictLiteral) {
                    containsConflictLiteral = true;
                    break;
                }
            }

            if (!containsConflictLiteral) {
                resolutionTerms.add(conflictTerms[i]);
            }
        }
    }

    @Override
    public ConstraintType getConstraintType() {
        return ConstraintType.DNF;
    }

    @Override
    public boolean isUnitConstraint() {
        return this.unitLiteralsPropagatedDuringInitialization.size() > 0;
    }

    @Override
    public int[] getUnitLiterals() {
        if (this.isUnitConstraint()) {
            return this.terms[0];
        } else {
            return new int[0];
        }
    }

    @Override
    public boolean isEmpty() {
        return this.terms.length == 0 || this.terms[0].length == 0;
    }

    public void getConflictResolutionLiterals(int conflictLiteral, int[] variableAssignments,
                                              Set<Integer> resolutionLiterals) {

        for (Integer literal : literalSet) {
            if (variableAssignments[Math.abs(literal)] * literal < 0 && Math.abs(literal) != Math.abs(conflictLiteral)) {
                resolutionLiterals.add(literal);
            }
        }
    }

    @Override
    public int getNeededDecisionLevel(final int[] decisionLevelOfVariables, int[] variables, Formula formula) {

        /*int decisionLevel = Integer.MAX_VALUE;
        int[] term = terms[firstWatchedNonFalseTerm];

        for (int i = 0; i < term.length; i++) {
            decisionLevel = Math.min(decisionLevel, decisionLevelOfVariables[Math.abs(term[i])]);
        }

        term = terms[secondWatchedNonFalseTerm];

        for (int i = 0; i < term.length; i++) {
            decisionLevel = Math.min(decisionLevel, decisionLevelOfVariables[Math.abs(term[i])]);
        }


        return decisionLevel;*/

        if (terms.length == 1) {
            return calculateNeededDecisionLevelForDNFConstraintWithSingleTerm(decisionLevelOfVariables);
        }

        firstWatchedTermSet = null;
        secondWatchedTermSet = null;

        Integer[] literals = literalSet.toArray(new Integer[0]);

        Arrays.sort(literals, Comparator.comparingInt(a -> decisionLevelOfVariables[Math.abs(a)] * -1));

        int[] variablesCopy = Arrays.copyOf(variables, variables.length);

        int[] trueTermCache = new int[terms.length];

        this.falseTermCache = new int[terms.length];
        this.falseCounter = this.terms.length;

        for (int i = 0; i < falseTermCache.length; i++) {
            falseTermCache[i] = -1;
        }

        Set<Integer> unassignedTermsIndeces = new HashSet<>();
        boolean containsFirstWatchedTerm = false;
        boolean containsSecondWatchedTerm = false;
        int smallestTermIndex = Integer.MAX_VALUE;
        int numberOfUnassignedTerms = 0;
        int currentDecisionLevel = formula.getCurrentDecisionLevel();
        boolean foundFirstWatchedTermSet = false;
        boolean foundSecondWatchedTermSet = false;
        int neededDecisionLevel = -1;
        for (int i = 0; i < literals.length; i++) {

            if (decisionLevelOfVariables[Math.abs(literals[i])] == currentDecisionLevel) {
                variablesCopy[Math.abs(literals[i])] = 0;

                List<Integer> termIndexList = positiveLiteralsIntersectionList[literalsMapping.get(literals[i])];
                for (Integer termIndex : termIndexList) {
                    if (trueTermCache[termIndex] != 1 && !checkIfTermIsFalseAfterUnassignment(variablesCopy,
                            terms[termIndex])) {

                        trueTermCache[termIndex] = 1;
                        falseTermCache[termIndex] = 1;
                    }

                }

            } else {
                if (!foundFirstWatchedTermSet) {
                    IntegerStack literalsToPropagate = new IntegerStack();

                    if (firstWatchedTermSet == null) {
                        findWatchedTermSets(variablesCopy, literalsToPropagate);
                    }

                    if (firstWatchedTermSet != null && secondWatchedTermSet != null) {
                        break;
                    }

                    for (int a = 0; a < terms.length; a++) {
                        if (!firstWatchedTermSet.contains(a) && !checkIfTermIsFalse(variablesCopy, a, falseTermCache)) {
                            secondWatchedTermSet = getIntersectionIndeces(a, variables);
                            secondWatchedNonFalseTerm = a;
                            break;
                        }
                    }

                    if (secondWatchedTermSet != null) {
                        break;
                    }

                    propagateSharedLiteralsInTermSet(variablesCopy, literalsToPropagate, firstWatchedTermSet);


                    if (literalsToPropagate.size() == 0) {
                        neededDecisionLevel = currentDecisionLevel;
                        foundFirstWatchedTermSet = true;
                    }

                } else {
                    for (int a = 0; a < terms.length; a++) {
                        if (!firstWatchedTermSet.contains(a) && !checkIfTermIsFalse(variablesCopy, a, falseTermCache)) {
                            secondWatchedTermSet = getIntersectionIndeces(a, variables);
                            secondWatchedNonFalseTerm = a;
                            foundSecondWatchedTermSet = true;
                            break;
                        }
                    }
                }

                if (foundSecondWatchedTermSet) {
                    break;
                }

                variablesCopy[Math.abs(literals[i])] = 0;
                currentDecisionLevel = decisionLevelOfVariables[Math.abs(literals[i])];

                List<Integer> termIndexList = positiveLiteralsIntersectionList[literalsMapping.get(literals[i])];
                for (Integer termIndex : termIndexList) {
                    if (trueTermCache[termIndex] != 1 && !checkIfTermIsFalseAfterUnassignment(variablesCopy,
                            terms[termIndex])) {

                        trueTermCache[termIndex] = 1;
                        falseTermCache[termIndex] = 1;
                    }

                }


            }

        }

        if (secondWatchedTermSet == null) {
            findWatchedTermSets(variablesCopy, new IntegerStack());
        }

        assignWatchedLiterals(formula.getPositivelyWatchedDNFConstraints(), formula.getNegativelyWatchedDNFConstraints());

        return neededDecisionLevel != -1 ? neededDecisionLevel : currentDecisionLevel;


    }

    private int calculateNeededDecisionLevelForDNFConstraintWithSingleTerm(int[] decisionLevelOfVariables) {
        int decisionLevel = Integer.MAX_VALUE;

        for (int i = 0; i < terms[0].length; i++) {
            decisionLevel = Math.min(decisionLevel, decisionLevelOfVariables[Math.abs(terms[0][i])]);
        }

        return decisionLevel;
    }

   private boolean checkIfUnassignedTermsIntersect(int smallestUnassignedTermIndex, int[] smallestUnassignedTerm,
                                                    int[] trueTermCache, int numberOfUnassignedTerms) {

        int trueCounterIntersection = 0;
        int[] intersectionAlreadyCounted = new int[terms.length];
        for (int i = 0; i < smallestUnassignedTerm.length; i++) {
            final int currentLiteral = smallestUnassignedTerm[i];

            if (unitLiteralsPropagatedDuringInitialization.contains(currentLiteral)) {
                continue;
            }

            List<Integer> termIndexList = positiveLiteralsIntersectionList[literalsMapping.get(currentLiteral)];
            for (Integer termIndex : termIndexList) {
                if (intersectionAlreadyCounted[termIndex] != 1 && termIndex != smallestUnassignedTermIndex && trueTermCache[termIndex] == 1) {
                    trueCounterIntersection++;
                    intersectionAlreadyCounted[termIndex] = 1;
                }
            }

        }

        if (trueCounterIntersection == numberOfUnassignedTerms - 1) {
            return false;
        }

        return true;
    }

    private boolean checkIfTermIsFalseAfterUnassignment(int[] variables, int[] term) {
        for (int i = 0; i < term.length; i++) {
            final int literal = term[i];
            if (variables[Math.abs(literal)] * literal < 0) {
                return true;
            }

        }

        return false;
    }

    private boolean checkIfTermIsFalse(int[] variables, int termIndex, int[] falseTermCache) {
        if (falseTermCache[termIndex] == -1) {
            return true;
        }

        if (falseTermCache[termIndex] == 1) {
            return false;
        }

        int[] term = terms[termIndex];

        for (int i = 0; i < term.length; i++) {
            final int literal = term[i];
            if (variables[Math.abs(literal)] * literal < 0) {
                falseTermCache[termIndex] = -1;
                return true;
            }
        }

        falseTermCache[termIndex] = 1;

        return false;
    }

    @Override
    public void addVariableOccurenceCount(final double[] variableOccurences) {
        for (int i = 0; i < this.terms.length; i++) {
            for (int j = 0; j < this.terms[i].length; j++) {
                variableOccurences[Math.abs(this.terms[i][j])] += 1;
            }
        }
    }

    @Override
    public boolean isStillWatched(final int literal, int[] variables) {

        if (watchedLiterals[literalsMapping.get(-literal)] == 1) {
            return true;
        }

        return false;
    }

    public void backtrack(final int variable, final int[] variableAssingments) {

        /*Integer[] falseTerms = positiveLiteralsIntersectionList[literalsMapping.get(-variable)];
        this.visitedLiterals.remove(-variable);

        for (int i = 0; i < falseTerms.length; i++) {
            int termIndex = falseTerms[i];

            if (this.falseTermCache[termIndex] == 0) {
                return;
            }

            if (!this.checkIfTermIsFalseExludingLiteral(this.terms[termIndex], variableAssingments, -variable)) {
                this.falseTermCache[termIndex] = 0;
                falseCounter--;
            }
        }*/
    }

    private boolean checkIfTermIsFalseExludingLiteral(final int[] term, final int[] variables, final int excludedLiteral) {
        for (int i = 0; i < term.length; i++) {
            final int literal = term[i];
            if (literal != excludedLiteral && variables[Math.abs(literal)] * literal < 0) {
                return true;
            }

        }

        return false;
    }

    public String toString() {
        String constraint = "DNF";

        for (int i = 0; i < this.terms.length; i++) {
            constraint += " " + Arrays.toString(this.terms[i]);
        }

        return constraint;
    }

    @Override
    public Set<Integer> getUnitLiteralsNeededBeforePropagation() {
        return this.unitLiteralsPropagatedDuringInitialization;
    }

    private void propagateIfConstraintHasOnlyOneTerm(final int termIndex, final int[] variableAssignments, final int[] unitLiteralState,
                                                     final IntegerArrayQueue unitLiterals,
                                                     final Constraint[] reasonClauses) {

        final int[] currentTerm = this.terms[termIndex];

        for (int i = 0; i < currentTerm.length; i++) {
            final int currentLiteral = currentTerm[i];
            final int currentLiteralAbsoluteValue = Math.abs(currentLiteral);

            if (variableAssignments[currentLiteralAbsoluteValue] * currentLiteral < 0 || unitLiteralState[currentLiteralAbsoluteValue] * currentLiteral < 0) {
                if (reasonClauses[currentLiteralAbsoluteValue] == null) {
                    System.out.println();
                }

                this.hasConflict = true;
                this.conflictLiteral = currentLiteral;
                return;
            } else if (this.isNeededForUnitPropagation(currentLiteralAbsoluteValue, variableAssignments, unitLiteralState)) {
                unitLiterals.offer(currentLiteral);
                unitLiteralState[currentLiteralAbsoluteValue] = currentLiteral < 0 ? -1 : 1;
                if (reasonClauses[currentLiteralAbsoluteValue] == null) {
                    reasonClauses[currentLiteralAbsoluteValue] = this;
                }
            }

        }

    }

    /*private void findLiteralsInIntersectionOfAllNonFalseTerms(final IntegerStack literalsToPropagate,
                                                              final int firstNonFalseTermIndex,
                                                              final int nonFalseCounter,
                                                              final int[] shortestNonFalseTerm, int[] variables) {
        int counterPartTermIndex = firstNonFalseTermIndex == firstWatchedTerm ? secondWatchedTerm : firstWatchedTerm;

        Integer[] intersectionIndeces;
        for (int i = 0; i < shortestNonFalseTerm.length; i++) {
            final int currentLiteral = shortestNonFalseTerm[i];

            intersectionIndeces = positiveLiteralsIntersectionList[literalsMapping.get(currentLiteral)];

            if (intersectionIndeces.length == 1) {
                continue;
            }

            if (!contains(intersectionIndeces, counterPartTermIndex)) {
                continue;
            }

            boolean foundNonFalseNonIntersectingTerm = false;
            for (int a = firstNonFalseTermIndex + 1; a < falseTermCache.length; a++) {
                if (a != counterPartTermIndex && !contains(intersectionIndeces,a) && !checkIfTermIsFalse(variables, a,
                        falseTermCache)) {
                    foundNonFalseNonIntersectingTerm = true;
                    break;
                }
            }

            if (foundNonFalseNonIntersectingTerm) {
                continue;
            }


            literalsToPropagate.push(currentLiteral);
        }
    }*/

    public boolean contains(Integer[] array, int number) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == number){
                return true;
            }
        }

        return false;
    }

    private boolean addUnitLiterals(final int[] variableAssignments, final int[] unitLiteralState, final IntegerArrayQueue unitLiterals, final Constraint[] reasonClauses, final IntegerStack literalsToPropagate) {
        while (literalsToPropagate.hasNext()) {
            final int currentLiteral = literalsToPropagate.pop();
            final int currentLiteralAbsoluteValue = Math.abs(currentLiteral);

            if (variableAssignments[currentLiteralAbsoluteValue] * currentLiteral < 0 || unitLiteralState[currentLiteralAbsoluteValue] * currentLiteral < 0) {
                if (reasonClauses[currentLiteralAbsoluteValue] == null) {
                    System.out.println();
                }

                this.hasConflict = true;
                this.conflictLiteral = currentLiteral;
                return true;
            } else if (this.isNeededForUnitPropagation(currentLiteralAbsoluteValue, variableAssignments, unitLiteralState)) {
                unitLiterals.offer(currentLiteral);
                unitLiteralState[currentLiteralAbsoluteValue] = currentLiteral < 0 ? -1 : 1;
                if (reasonClauses[currentLiteralAbsoluteValue] == null) {
                    reasonClauses[currentLiteralAbsoluteValue] = this;
                }
            }

        }
        return false;
    }

    private boolean isNeededForUnitPropagation(final int literal, final int[] variables, final int[] unitLiteralState) {
        final int literalAbsoluteValue = Math.abs(literal);
        return variables[literalAbsoluteValue] * literal == 0 && unitLiteralState[literalAbsoluteValue] == 0;
    }


    public int[][] getTerms() {
        return terms;
    }

}
