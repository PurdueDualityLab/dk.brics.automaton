package dk.brics.automaton;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class AutomatonCoverage {

    private static class VisitationInfo {
        private final Set<Integer> visitedNodes;
        private final Set<Integer> visitedEdges;

        public VisitationInfo(Set<Integer> visitedNodes, Set<Integer> visitedEdges) {
            this.visitedNodes = visitedNodes;
            this.visitedEdges = visitedEdges;
        }

        public Set<Integer> getVisitedNodes() {
            return visitedNodes;
        }

        public Set<Integer> getVisitedEdges() {
            return visitedEdges;
        }
    }

    private final Automaton automaton;
    private final RunAutomaton runAutomaton;
    private final TransitionTable transitionTable;

    private final Set<Integer> positiveVisitedStates;
    private final Set<Integer> negativeVisitedStates;

    private final Set<Integer> positiveVisitedEdges;
    private final Set<Integer> negativeVisitedEdges;

    public AutomatonCoverage(Automaton automaton) {
        this.automaton = automaton;
        this.runAutomaton = new RunAutomaton(this.automaton);
        this.transitionTable = new TransitionTable(automaton);

        this.positiveVisitedStates = new HashSet<>();
        this.negativeVisitedStates = new HashSet<>();
        this.positiveVisitedEdges = new HashSet<>();
        this.negativeVisitedEdges = new HashSet<>();
    }

    public double getCoverageScore() {
        // for now, let's do something naive. Just combine
        Set<Integer> combinedVisited = new HashSet<>(this.positiveVisitedStates);
        combinedVisited.addAll(negativeVisitedStates);

        Set<Integer> combinedVisitedEdges = new HashSet<>(this.positiveVisitedEdges);
        combinedVisitedEdges.addAll(negativeVisitedEdges);

        // How many of the states get visited
        double nodeCoverageRatio = combinedVisited.size() / ((double) this.automaton.getNumberOfStates());
        double edgeCoverageRatio = combinedVisitedEdges.size() / ((double) this.transitionTable.countTotalTransitions());
        return nodeCoverageRatio * edgeCoverageRatio;
    }

    public double getPositiveCoverageScore() {
        double nodeCoverageRatio = positiveVisitedStates.size() / ((double) this.automaton.getNumberOfStates());
        double edgeCoverageRatio = positiveVisitedEdges.size() / ((double) this.transitionTable.countTotalTransitions());
        return nodeCoverageRatio * edgeCoverageRatio;
    }

    public double getNegativeCoverageScore() {
        double nodeCoverageRatio = negativeVisitedStates.size() / ((double) this.automaton.getNumberOfStates());
        double edgeCoverageRatio = negativeVisitedEdges.size() / ((double) this.transitionTable.countTotalTransitions());
        return nodeCoverageRatio * edgeCoverageRatio;
    }

    public void evaluatePositive(String positive) {
        VisitationInfo visited = this.evaluateString(positive);
        this.positiveVisitedStates.addAll(visited.getVisitedNodes());
        this.positiveVisitedEdges.addAll(visited.getVisitedEdges());
    }

    public void evaluateNegative(String negative) {
        VisitationInfo visited = this.evaluateString(negative);
        this.negativeVisitedStates.addAll(visited.getVisitedNodes());
        this.negativeVisitedEdges.addAll(visited.getVisitedEdges());
    }

    public Set<Integer> getPositiveVisitedStates() {
        return positiveVisitedStates;
    }

    public Set<Integer> getNegativeVisitedStates() {
        return negativeVisitedStates;
    }

    public Set<Integer> getPositiveVisitedEdges() {
        return positiveVisitedEdges;
    }

    public Set<Integer> getNegativeVisitedEdges() {
        return negativeVisitedEdges;
    }

    public Set<Integer> getVisitedStates() {
        Set<Integer> combinedVisited = new HashSet<>(this.positiveVisitedStates);
        combinedVisited.addAll(negativeVisitedStates);
        return combinedVisited;
    }

    public Set<Integer> getVisitedEdges() {
        Set<Integer> combinedVisitedEdges = new HashSet<>(this.positiveVisitedEdges);
        combinedVisitedEdges.addAll(negativeVisitedEdges);
        return combinedVisitedEdges;
    }

    private VisitationInfo evaluateString(String input) {
        Set<Integer> visited = new HashSet<>();
        Set<Integer> visitedEdges = new HashSet<>();
        int stateCursor = this.runAutomaton.getInitialState();
        visited.add(stateCursor); // first state is always visited
        int currentPos = 0;
        while (currentPos < input.length()) {
            char transitionCharacter = input.charAt(currentPos);
            int nextState = this.runAutomaton.step(stateCursor, transitionCharacter);
            if (nextState == -1) {
                // there's no outgoing state
                break;
            } else {
                // We moved to another state, so that state should be marked as visited
                visited.add(nextState);

                // Find an edge between the two states and mark it as visited
                Optional<Transition> takenEdge = transitionTable.findEdgeBetweenStates(stateCursor, nextState, transitionCharacter);
                takenEdge.ifPresent(transition -> visitedEdges.add(transition.getId()));

                stateCursor = nextState;
            }
            // Update the cursor
            currentPos++;
        }

        return new VisitationInfo(visited, visitedEdges);
    }
}
