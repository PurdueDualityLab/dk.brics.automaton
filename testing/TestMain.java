package testing;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.AutomatonCoverage;
import dk.brics.automaton.BasicOperations;
import dk.brics.automaton.RegExp;

public class TestMain {
    public static void main(String[] args) {
        BasicOperations.setDefaultDfaBudget(100);

        RegExp regex = new RegExp("[a-z]+123[A-Z]+");

        Automaton auto = regex.toAutomaton();
        auto.determinize();
        System.out.println(auto.toDot());
        int stateCount = auto.getStates().size();
        System.out.printf("Built automaton with %d states", stateCount);

        AutomatonCoverage coverage = new AutomatonCoverage(auto);
        coverage.evaluateNegative("");

        double score = coverage.getCoverageScore();
        System.out.printf("Coverage score: %.4f%n", score);

        coverage.evaluateNegative("a");
        score = coverage.getCoverageScore();
        System.out.printf("Coverage score: %.4f%n", score);

        coverage.evaluateNegative("aaaaa");
        score = coverage.getCoverageScore();
        System.out.printf("Coverage score: %.4f%n", score);

        coverage.evaluatePositive("b123Z");
        score = coverage.getCoverageScore();
        System.out.printf("Coverage score: %.4f%n", score);

        coverage.evaluatePositive("aaaaa123ZZZZZZZZZZ");
        score = coverage.getCoverageScore();
        System.out.printf("Coverage score: %.4f%n", score);

        System.out.printf("All scores: combined=%.4f, positive=%.4f, negative=%.4f%n", score, coverage.getPositiveCoverageScore(), coverage.getNegativeCoverageScore());
    }
}
