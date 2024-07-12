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
        TransitionTable tt = new TransitionTable(auto);

        // initializing visited states list
        ArrayList<State> path = new ArrayList<>();
        Map<State, Boolean> visited = new HashMap<>();
        for (State state : auto.getStates()) {
            visited.put(state, false);
        }

        // finding basis strings
        ArrayList<String> strings = findBasisPath(auto.getInitialState(), path, auto, tt, visited);

        // printing basis string
        System.out.println(strings);
    }

    private static void addPathToList(ArrayList<State> path) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < path.size() - 1; i++) {
            State state = path.get(i);
            int transitionIndex = 0;
            while (!state.getSortedTransitions(false).get(transitionIndex).getDest().equals(path.get(i+1))) {
                transitionIndex++;
            }
            sb.append(state.getSortedTransitions(false).get(transitionIndex).getMin());
        }

        list.add(sb.toString());
    }

    private static ArrayList<State> deepCopy(ArrayList<State> path) {
        return new ArrayList<>(path);
    }

    private static ArrayList<String> findBasisPath(State curr, ArrayList<State> path, Automaton auto, TransitionTable tt, Map<State, Boolean> visited) {

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
            findBasisPath(next, currPath, auto, tt, visited);
        }

        // else:
        else {
            // for each next_state that has a transition from curr_state to next_state:
            List<Transition> transitions = curr.getSortedTransitions(false);
            for (Transition transition : transitions) {
                State dest = transition.getDest();
                findBasisPath(dest, currPath, auto, tt, visited);
            }
        }

        return list;
    }
}
