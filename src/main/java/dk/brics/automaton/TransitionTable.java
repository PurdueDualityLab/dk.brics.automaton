package dk.brics.automaton;

import java.util.*;
import java.util.stream.Collectors;

public class TransitionTable {

    private static Map<Integer, Set<Transition>> createDestTableFromState(State state) {
        Map<Integer, Set<Transition>> destinationMap = new HashMap<>();
        for (Transition trans : state.transitions) {
            int destNumber = trans.to.number;
            // Upsert the transition
            if (destinationMap.containsKey(destNumber)) {
                destinationMap.get(destNumber).add(trans);
            } else {
                Set<Transition> transitionSet = new HashSet<>();
                transitionSet.add(trans);
                destinationMap.put(destNumber, transitionSet);
            }
        }

        return destinationMap;
    }

    private static Set<State> getAdjacentStates(State state) {
        Set<State> neighbors = new HashSet<>();
        for (Transition dest : state.transitions) {
            neighbors.add(dest.to);
        }

        return neighbors;
    }

    // Sparse matrix transition table. For each map, the key is the destination state, and the value is a set of
    // transitions that can be used to transition between the two
    private final Map<Integer, Map<Integer, Set<Transition>>> table;

    public TransitionTable(Automaton auto) {

        // Initialize table
        table = new HashMap<>();

        // Populate table
        Set<Integer> visitedStates = new HashSet<>();
        Queue<State> traversalQueue = new ArrayDeque<>();
        traversalQueue.add(auto.getInitialState());
        while (!traversalQueue.isEmpty()) {
            State state = traversalQueue.remove();
            visitedStates.add(state.id);
            // Create the entry for this state
            Map<Integer, Set<Transition>> destinationMap = createDestTableFromState(state);
            table.put(state.number, destinationMap);
            // Get the next states to check. Only enqueue states that we haven't visited yet
            getAdjacentStates(state).stream()
                    .filter(neighbor -> !visitedStates.contains(neighbor.id))
                    .forEach(item -> {
                        if (!traversalQueue.contains(item)) {
                            traversalQueue.add(item);
                        }
                    });
        }

        // Give everything an ID
        for (Map.Entry<Integer, Map<Integer, Set<Transition>>> originEntry : table.entrySet()) {
            for (Map.Entry<Integer, Set<Transition>> destEntry : originEntry.getValue().entrySet()) {
                for (Transition transition : destEntry.getValue()) {
                    int id = Objects.hash(originEntry.getKey(), destEntry.getKey(), transition.min, transition.max);
                    transition.setId(id);
                }
            }
        }
    }

    /**
     * Count how many total transitions there are in this transition table
     * @return Transition count
     */
    public long countTotalTransitions() {
        return table.values().stream()
                .flatMap(destMap -> destMap.values().stream())
                .mapToInt(Set::size)
                .sum();
    }

    /**
     * Get the set of transitions between two states. Directed edges going from left->right
     * @param leftStateNumber Left state number (e.g. state.number)
     * @param rightStateNumber Right state number
     * @return Set of edges going from left to right
     * @throws NoSuchElementException If there are no edges between these two states
     */
    public Set<Transition> getTransitionsBetweenStates(int leftStateNumber, int rightStateNumber) {
        // Bounds checking
        if (!this.table.containsKey(leftStateNumber) || !this.table.get(leftStateNumber).containsKey(rightStateNumber)) {
            throw new NoSuchElementException("There are no edges between these two states");
        }

        return this.table.get(leftStateNumber).get(rightStateNumber);
    }

    /**
     * Finds any edge between states left and right that accept the given character
     * @param leftStateNumber Left state id
     * @param rightStateNumber Right state id
     * @param testChar The character to try moving across
     * @return An edge that accepts the two, otherwise empty
     * @throws NoSuchElementException if there are no edges between the two states
     */
    public Optional<Transition> findEdgeBetweenStates(int leftStateNumber, int rightStateNumber, char testChar) {
        return getTransitionsBetweenStates(leftStateNumber, rightStateNumber).stream()
                .filter(transition -> transition.accepts(testChar))
                .findAny();
    }

    public int countPossibleEdgePairs() {
        Set<AutomatonCoverage.EdgePair> edgePairs = new HashSet<>();
        for (int leftState : table.keySet()) {
            Set<AutomatonCoverage.Edge> leftEdges = getSuccessors(leftState).stream()
                    .flatMap(leftSuccessor -> {
                        return getTransitionsBetweenStates(leftState, leftSuccessor).stream()
                                .map(transition -> new AutomatonCoverage.Edge(leftState, leftSuccessor, transition));
                    })
                    .collect(Collectors.toSet());

            for (AutomatonCoverage.Edge leftEdge : leftEdges) {
                getSuccessors(leftEdge.getRightStateId()).stream()
                        .flatMap(middleSuccessor -> {
                            return getTransitionsBetweenStates(leftEdge.getRightStateId(), middleSuccessor).stream()
                                    .map(transition -> new AutomatonCoverage.Edge(leftEdge.getRightStateId(), middleSuccessor, transition));
                        })
                        .forEach(destEdge -> edgePairs.add(new AutomatonCoverage.EdgePair(leftEdge, destEdge)));
            }
        }

        return edgePairs.size();
    }

    private Set<Integer> getSuccessors(int state) {
        Map<Integer, Set<Transition>> outgoingTransitions = this.table.get(state);
        if (outgoingTransitions == null) {
            return Collections.emptySet();
        }

        return outgoingTransitions.keySet();
    }
}
