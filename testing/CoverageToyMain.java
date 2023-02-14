package testing;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.AutomatonCoverage;
import dk.brics.automaton.RegExp;

import java.util.Scanner;

public class CoverageToyMain {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("pattern: ");
        String pattern = scanner.nextLine();

        Automaton auto = new RegExp(pattern).toAutomaton();
        auto.determinize();
        System.out.println(auto.toDot());

        AutomatonCoverage coverage = new AutomatonCoverage(auto);

        do {
            System.out.print("[p]ositive, [n]egative, or [q]uit: ");
            String mode = scanner.nextLine().toLowerCase();
            if (mode.contains("q")) {
                break;
            }

            boolean isPositive = mode.contains("p");

            System.out.print("> ");
            String testString = scanner.nextLine();
            if (isPositive) {
                coverage.evaluatePositive(testString);
            } else {
                coverage.evaluateNegative(testString);
            }

            System.out.printf("Score: combined=%.4f, positive=%.4f, negative=%.4f%n", coverage.getCoverageScore(), coverage.getPositiveCoverageScore(), coverage.getNegativeCoverageScore());
        } while (true);
        System.out.println("done");
    }
}
