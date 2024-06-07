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

        private void addVisitedNode(int node) {
            visitedNodes.add(node);
        }

        public void addVisitedEdge(int edge) {
            visitedEdges.add(edge);
        }

        public void addVisitedEdgePair(EdgePair edgePair) {
            visitedEdgePairs.add(edgePair);
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

    private final VisitationInfo fullMatchVisitationInfo;
    private final VisitationInfo partialMatchVisitationInfo;

    public AutomatonCoverage(Automaton automaton) {
        this.originalAutomaton = automaton;
        this.runAutomaton = new RunAutomaton(automaton);

        this.transitionTable = new TransitionTable(automaton);

        fullMatchVisitationInfo = new VisitationInfo();
        partialMatchVisitationInfo = new VisitationInfo();
    }

    public void evaluate(String subject) {
        fullMatchVisitationInfo.foldIn(evaluateString(subject, true));
        partialMatchVisitationInfo.foldIn(evaluateString(subject, false));
    }

    public VisitationInfoSummary getFullMatchVisitationInfoSummary() {
        return summarizeVisitationInfo(getFullMatchVisitationInfo());
    }

    public VisitationInfoSummary getPartialMatchVisitationInfoSummary() {
        return summarizeVisitationInfo(getPartialMatchVisitationInfo());
    }

    private VisitationInfoSummary summarizeVisitationInfo(VisitationInfo info) {
        double nodeCoverage = (double) info.getVisitedNodes().size() / originalAutomaton.getLiveStates().size();
        double edgeCoverage = (double) info.getVisitedEdges().size() / originalAutomaton.getNumberOfTransitions();
        double edgePairCoverage = (double) info.getVisitedEdgePairs().size() / transitionTable.countPossibleEdgePairs();

        return new VisitationInfoSummary(nodeCoverage, edgeCoverage, edgePairCoverage);
    }

    public VisitationInfo getFullMatchVisitationInfo() {
        return fullMatchVisitationInfo;
    }

    public VisitationInfo getPartialMatchVisitationInfo() {
        return partialMatchVisitationInfo;
    }

    private VisitationInfo evaluateString(String input, boolean fullMatch) {
        VisitationInfo visitationInfo = new VisitationInfo();

        OptionalInt previousState = OptionalInt.empty(); // used for edge pair
        int stateCursor = this.runAutomaton.getInitialState();
        visitationInfo.addVisitedNode(stateCursor); // first state is always visited

        int currentPos = 0;
        while (currentPos < input.length()) {
            char transitionCharacter = input.charAt(currentPos);
            int nextState = this.runAutomaton.step(stateCursor, transitionCharacter);
            if (nextState == -1) {
                // there's no outgoing state, then there are two things we can try:
                if (fullMatch) {
                    // if we're in full match mode, then we're done
                    break;
                } else {
                    // otherwise, we should restart the automaton
                    stateCursor = runAutomaton.getInitialState();
                    previousState = OptionalInt.empty();
                }
            } else {
                // We moved to another state, so that state should be marked as visited
                visitationInfo.addVisitedNode(nextState);

                // Find an edge between the two states and mark it as visited
                Optional<Transition> takenEdge = transitionTable.findEdgeBetweenStates(stateCursor, nextState, transitionCharacter);
                takenEdge.ifPresent(transition -> visitationInfo.addVisitedEdge(transition.getId()));

                // record edge pair if possible
                int finalStateCursor = stateCursor;
                previousState.ifPresent(prevState -> visitationInfo.addVisitedEdgePair(new EdgePair(prevState, finalStateCursor, nextState)));

                // move states along
                previousState = OptionalInt.of(stateCursor);
                stateCursor = nextState;
            }
            // Update the cursor
            currentPos++;
        }

        return visitationInfo;
    }
}
