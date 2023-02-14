package testing;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.AutomatonCoverage;
import dk.brics.automaton.BasicOperations;
import dk.brics.automaton.RegExp;

public class TestMain {

    private static void displayCoverage(AutomatonCoverage coverage) {
        System.out.printf("All scores: combined=%.4f, positive=%.4f, negative=%.4f%n", coverage.getCoverageScore(), coverage.getPositiveCoverageScore(), coverage.getNegativeCoverageScore());
        System.out.printf("Details: covered nodes=%d, covered edges=%d%n", coverage.getVisitedStates().size(), coverage.getVisitedEdges().size());
    }

    public static void main(String[] args) {
        BasicOperations.setDefaultDfaBudget(100);

        RegExp regex = new RegExp("(a+|b+)(c+|d+)");

        Automaton auto = regex.toAutomaton();
        auto.determinize();
        System.out.println(auto.toDot());
        int stateCount = auto.getStates().size();
        System.out.printf("Built automaton with %d states", stateCount);

        AutomatonCoverage coverage = new AutomatonCoverage(auto);
        coverage.evaluatePositive("ac");
        coverage.evaluatePositive("aac");
        coverage.evaluatePositive("acc");
        coverage.evaluatePositive("ad");
        coverage.evaluatePositive("aad");
        coverage.evaluatePositive("add");
        coverage.evaluatePositive("bd");
        coverage.evaluatePositive("bbd");
        coverage.evaluatePositive("bdd");
        coverage.evaluatePositive("bc");
        coverage.evaluatePositive("bbc");
        coverage.evaluatePositive("bcc");

        displayCoverage(coverage);
        System.out.println(coverage.getVisitedEdges());
        System.out.println(coverage.getVisitedStates());
    }
}
