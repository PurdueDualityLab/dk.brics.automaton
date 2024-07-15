package dk.brics.automaton;

import java.io.FileNotFoundException;
import java.util.*;
import java.io.PrintWriter;

public class Egret {

    static int LIMIT = 2000;
    static ArrayList<String> list = new ArrayList<>();

    public static void getRegexStrings(String s) throws FileNotFoundException {

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
        ArrayList<String> strings = findBasisPath(auto.getInitialState(), path, visited, s);

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

    private static ArrayList<String> addCharacters(ArrayList<Transition> transitions, ArrayList<String> pathStrings) {
        ArrayList<String> newPathStrings = new ArrayList<>();
        ArrayList<Character> charsToAppend = new ArrayList<>();

        // making a list of each character option to append
        for (Transition t : transitions) {
            for (char c = t.getMin(); c <= t.getMax(); c++) {
                charsToAppend.add(c);
            }
        }

        // adding characters
        if (!pathStrings.isEmpty()) {
            for (String s : pathStrings) {
                for (Character c : charsToAppend) {
                    newPathStrings.add(s + c);
                }
            }
        } else {
            for (Character c : charsToAppend) {
                newPathStrings.add(c.toString());
            }
        }

        return newPathStrings;
    }

    private static void addPathToList(ArrayList<State> path, String s) throws FileNotFoundException {
        ArrayList<String> pathStrings = new ArrayList<>();
        for (int i = 0; i < path.size() - 1; i++) {
            ArrayList<Transition> transitions = findTransitions(path.get(i), path.get(i + 1));
            pathStrings = addCharacters(transitions, pathStrings);
        }
        while (list.size() < LIMIT && !pathStrings.isEmpty()) {
            list.add(0, pathStrings.remove(0));
        }
        if (list.size() >= LIMIT) {
            PrintWriter log = new PrintWriter("src/test/java/dk/brics/automaton/EgretLog");
            log.println("List size reached the limit (" + LIMIT + "). Cannot add any more strings from RegEx " + s);
            log.close();
        }
    }

    private static ArrayList<State> deepCopy(ArrayList<State> path) {
        return new ArrayList<>(path);
    }

    private static ArrayList<String> findBasisPath(State curr, ArrayList<State> path, Map<State, Boolean> visited, String s) throws FileNotFoundException {

        // add state to path
        ArrayList<State> currPath = deepCopy(path);
        currPath.add(curr);


        // if curr_state is the final state:
        if (curr.isAccept()) {
            // add path to list
            addPathToList(currPath, s);

            // match all states in path as visited
            for (State state : currPath) {
                visited.put(state, true);
            }
        }

        // else if curr_state.visited:
        else if (visited.get(curr)) {
            // next_state = lowest-numbered state that has a transition from curr_state to next_state
            State next = curr.getSortedTransitions(false).get(0).getDest();
            findBasisPath(next, currPath, visited, s);
        }

        // else:
        else {
            // for each next_state that has a transition from curr_state to next_state:
            List<Transition> transitions = curr.getSortedTransitions(false);
            for (Transition transition : transitions) {
                State dest = transition.getDest();
                findBasisPath(dest, currPath, visited, s);
            }
        }

        return list;
    }
}
