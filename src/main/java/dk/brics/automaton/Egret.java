package dk.brics.automaton;

import java.sql.Array;
import java.util.*;

public class Egret {

    static ArrayList<String> list = new ArrayList<>();
    static Map<Integer, ArrayList<Integer>> nextStates = new HashMap<>();

    public static void getRegexStrings(String s) {

        // setting up the automaton
        RegExp regExp = new RegExp(s);
        Automaton auto = regExp.toAutomaton();

        // initializing visited states list
        ArrayList<State> path = new ArrayList<>();
        Map<State, Boolean> visited = new HashMap<>();
        for (State state : auto.getStates()) {
            visited.put(state, false);
        }

        // finding basis strings
        ArrayList<String> strings = findBasisPath(auto.getInitialState(), path, visited);

        // printing basis string
        System.out.println(strings);
    }

    private static ArrayList<Transition> findTransitions(State state1, State state2) {
        ArrayList<Transition> transitions = new ArrayList<>();
        for (Transition t : state1.getTransitions()) {
            if (t.getDest().equals(state2)) {
                transitions.add(t);
            }
        }
        return transitions;
    }

    private static ArrayList<String> addCharacters(Transition transition, ArrayList<String> pathStrings) {
        ArrayList<String> newPathStrings = new ArrayList<>();

        // if the transition is a range
        if (transition.getMin() != transition.getMax()) {
            if (!pathStrings.isEmpty()) {
                for (String s : pathStrings) {
                    // replace each string every combination of itself and the range characters
                    for (char c = transition.getMin(); c <= transition.getMax(); c++) {
                        newPathStrings.add(s + String.valueOf(c));
                    }
                }
            }
            else {
                // add all range characters to the list
                for (char c = transition.getMin(); c <= transition.getMax(); c++) {
                    newPathStrings.add(c + "");
                }
            }
        }
        // if the transition is not a range
        else {
            if (!pathStrings.isEmpty()) {
                pathStrings.replaceAll(s -> s + transition.getMin());
                newPathStrings.addAll(pathStrings);
            }
            else {
                newPathStrings.add(String.valueOf(transition.getMin()));
            }
        }

        return newPathStrings;
    }

    private static void addPathToList(ArrayList<State> path) {
        ArrayList<String> pathStrings = new ArrayList<>();
        for (int i = 0; i < path.size() - 1; i++) {
            ArrayList<Transition> transitions = findTransitions(path.get(i), path.get(i + 1));
            for (Transition t : transitions) {
                pathStrings = addCharacters(t, pathStrings);
            }
        }

        list.addAll(pathStrings);
    }

    private static ArrayList<State> deepCopy(ArrayList<State> path) {
        return new ArrayList<>(path);
    }

    private static ArrayList<String> findBasisPath(State curr, ArrayList<State> path, Map<State, Boolean> visited) {

        // add state to path
        ArrayList<State> currPath = deepCopy(path);
        currPath.add(curr);


        // if curr_state is the final state:
        if (curr.isAccept()) {
            // add path to list
            addPathToList(currPath);
            // match all states in path as visited
            for (State state : currPath) {
                visited.put(state, true);
            }
        }

        // else if curr_state.visited:
        else if (visited.get(curr)) {
            // next_state = lowest-numbered state that has a transition from curr_state to next_state
            State next = curr.getSortedTransitions(false).get(0).getDest();
            findBasisPath(next, currPath, visited);
        }

        // else:
        else {
            // for each next_state that has a transition from curr_state to next_state:
            List<Transition> transitions = curr.getSortedTransitions(false);
            for (Transition transition : transitions) {
                State dest = transition.getDest();
                findBasisPath(dest, currPath, visited);
            }
        }

        return list;
    }
}
