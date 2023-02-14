package dk.brics.automaton;

import java.util.HashSet;
import java.util.Set;

public class AutomatonCoverage {

    private final Automaton automaton;
    private final RunAutomaton runAutomaton;

    private final Set<Integer> positiveVisitedStates;
    private final Set<Integer> negativeVisitedStates;

    public AutomatonCoverage(Automaton automaton) {
        this.automaton = automaton;
        this.runAutomaton = new RunAutomaton(this.automaton);

        this.positiveVisitedStates = new HashSet<>();
        this.negativeVisitedStates = new HashSet<>();
    }

    public double getCoverageScore() {
        // for now, let's do something naive. Just combine
        Set<Integer> combinedVisited = new HashSet<>(this.positiveVisitedStates);
        combinedVisited.addAll(negativeVisitedStates);

        // How many of the states get visited
        return combinedVisited.size() / ((double) this.automaton.getNumberOfStates());
    }

    public double getPositiveCoverageScore() {
        return positiveVisitedStates.size() / ((double) this.automaton.getNumberOfStates());
    }

    public double getNegativeCoverageScore() {
        return negativeVisitedStates.size() / ((double) this.automaton.getNumberOfStates());
    }

    public void evaluatePositive(String positive) {
        Set<Integer> visited = this.evaluateString(positive);
        this.positiveVisitedStates.addAll(visited);
    }

    public void evaluateNegative(String negative) {
        Set<Integer> visited = this.evaluateString(negative);
        this.negativeVisitedStates.addAll(visited);
    }

    private Set<Integer> evaluateString(String input) {
        Set<Integer> visited = new HashSet<>();
        int stateCursor = this.runAutomaton.getInitialState();
        visited.add(stateCursor); // first state is always visited
        int currentPos = 0;
        while (currentPos < input.length()) {
            int nextState = this.runAutomaton.step(stateCursor, input.charAt(currentPos));
            if (nextState == -1) {
                // there's no outgoing state
                break;
            } else {
                // We moved to another state, so that state should be marked as visited
                visited.add(nextState);
                stateCursor = nextState;
            }
            // Update the cursor
            currentPos++;
        }

        return visited;
    }
}
