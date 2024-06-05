package dk.brics.automaton;

import java.util.*;

public class AutomatonCoverage {

    public static final class EdgePair {
        private final int left;
        private final int middle;
        private final int right;

        public EdgePair(int left, int middle, int right) {
            this.left = left;
            this.middle = middle;
            this.right = right;
        }

        public int getLeft() {
            return left;
        }

        public int getMiddle() {
            return middle;
        }

        public int getRight() {
            return right;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            EdgePair edgePair = (EdgePair) o;
            return left == edgePair.left && middle == edgePair.middle && right == edgePair.right;
        }

        @Override
        public int hashCode() {
            return Objects.hash(left, middle, right);
        }
    }

    public static final class VisitationInfo {
        private final Set<Integer> visitedNodes;
        private final Set<Integer> visitedEdges;
        private final Set<EdgePair> visitedEdgePairs;

        public VisitationInfo() {
            this(new HashSet<>(), new HashSet<>(), new HashSet<>());
        }

        public VisitationInfo(Set<Integer> visitedNodes, Set<Integer> visitedEdges, Set<EdgePair> visitedEdgePairs) {
            this.visitedNodes = visitedNodes;
            this.visitedEdges = visitedEdges;
            this.visitedEdgePairs = visitedEdgePairs;
        }

        public Set<Integer> getVisitedNodes() {
            return visitedNodes;
        }

        public Set<Integer> getVisitedEdges() {
            return visitedEdges;
        }

        public Set<EdgePair> getVisitedEdgePairs() {
            return visitedEdgePairs;
        }

        public void foldIn(VisitationInfo other) {
            this.visitedNodes.addAll(other.getVisitedNodes());
            this.visitedEdges.addAll(other.getVisitedEdges());
            this.visitedEdgePairs.addAll(other.getVisitedEdgePairs());
        }

        public VisitationInfo combineWith(VisitationInfo other) {
            Set<Integer> combinedVisitedNodes = new HashSet<>(this.visitedNodes);
            combinedVisitedNodes.addAll(other.visitedNodes);
            Set<Integer> combinedVisitedEdges = new HashSet<>(this.visitedEdges);
            combinedVisitedEdges.addAll(other.visitedEdges);
            Set<EdgePair> combinedVisitedEdgePairs = new HashSet<>(this.visitedEdgePairs);
            combinedVisitedEdgePairs.addAll(other.visitedEdgePairs);

            return new VisitationInfo(
                    combinedVisitedNodes,
                    combinedVisitedEdges,
                    combinedVisitedEdgePairs
            );
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            VisitationInfo that = (VisitationInfo) o;
            return Objects.equals(visitedNodes, that.visitedNodes) && Objects.equals(visitedEdges, that.visitedEdges) && Objects.equals(visitedEdgePairs, that.visitedEdgePairs);
        }

        @Override
        public int hashCode() {
            return Objects.hash(visitedNodes, visitedEdges, visitedEdgePairs);
        }
    }

    public static final class VisitationInfoSummary {
        private final double nodeCoverage;
        private final double edgeCoverage;
        private final double edgePairCoverage;

        public VisitationInfoSummary(double nodeCoverage, double edgeCoverage, double edgePairCoverage) {
            this.nodeCoverage = nodeCoverage;
            this.edgeCoverage = edgeCoverage;
            this.edgePairCoverage = edgePairCoverage;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            VisitationInfoSummary that = (VisitationInfoSummary) o;
            return Double.compare(nodeCoverage, that.nodeCoverage) == 0 && Double.compare(edgeCoverage, that.edgeCoverage) == 0 && Double.compare(edgePairCoverage, that.edgePairCoverage) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(nodeCoverage, edgeCoverage, edgePairCoverage);
        }

        public double getNodeCoverage() {
            return nodeCoverage;
        }

        public double getEdgeCoverage() {
            return edgeCoverage;
        }

        public double getEdgePairCoverage() {
            return edgePairCoverage;
        }
    }

    private final Automaton originalAutomaton;
    private final RunAutomaton runAutomaton;
    private final TransitionTable transitionTable;

    private final VisitationInfo positiveVisitationInfo;
    private final VisitationInfo negativeVisitationInfo;

    public AutomatonCoverage(Automaton automaton) {
        this.originalAutomaton = automaton;
        this.runAutomaton = new RunAutomaton(automaton);

        this.transitionTable = new TransitionTable(automaton);

        positiveVisitationInfo = new VisitationInfo();
        negativeVisitationInfo = new VisitationInfo();
    }

    public void evaluate(Collection<? extends String> strings) {
        strings.forEach(this::evaluate);
    }

    public void evaluate(String subject, boolean matches) {
        VisitationInfo visited = this.evaluateString(subject);
        if (matches) {
            positiveVisitationInfo.foldIn(visited);
        } else {
            negativeVisitationInfo.foldIn(visited);
        }
    }

    public void evaluate(String subject) {
        boolean matches = runAutomaton.run(subject);
        evaluate(subject, matches);
    }

    public VisitationInfoSummary getVisitationInfoSummary() {
        VisitationInfo info = getVisitationInfo();
        double nodeCoverage = (double) info.getVisitedNodes().size() / originalAutomaton.getLiveStates().size();
        double edgeCoverage = (double) info.getVisitedEdges().size() / originalAutomaton.getNumberOfTransitions();
        double edgePairCoverage = (double) info.getVisitedEdgePairs().size() / transitionTable.countPossibleEdgePairs();

        return new VisitationInfoSummary(nodeCoverage, edgeCoverage, edgePairCoverage);
    }

    public VisitationInfo getVisitationInfo() {
        return positiveVisitationInfo.combineWith(negativeVisitationInfo);
    }

    public VisitationInfo getPositiveVisitationInfo() {
        return positiveVisitationInfo;
    }

    public VisitationInfo getNegativeVisitationInfo() {
        return negativeVisitationInfo;
    }

    private VisitationInfo evaluateString(String input) {
        Set<Integer> visited = new HashSet<>();
        Set<Integer> visitedEdges = new HashSet<>();
        Set<EdgePair> visitedEdgePairs = new HashSet<>();

        OptionalInt previousState = OptionalInt.empty(); // used for edge pair
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

                // record edge pair if possible
                int finalStateCursor = stateCursor;
                previousState.ifPresent(prevState -> visitedEdgePairs.add(new EdgePair(prevState, finalStateCursor, nextState)));

                // move states along
                previousState = OptionalInt.of(stateCursor);
                stateCursor = nextState;
            }
            // Update the cursor
            currentPos++;
        }

        return new VisitationInfo(visited, visitedEdges, visitedEdgePairs);
    }
}
