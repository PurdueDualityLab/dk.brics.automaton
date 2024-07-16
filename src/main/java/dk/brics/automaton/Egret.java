package dk.brics.automaton;

import java.io.FileNotFoundException;
import java.util.*;
import java.io.PrintWriter;

public class Egret {

    static int LANG_LIMIT = 2000;
    static int MEM_LIMIT = 1000;

    public static Set<String> getRegexStrings(String s) throws FileNotFoundException {

        // setting up the automaton
        RegExp regExp = new RegExp(s);
        Automaton auto = regExp.toAutomaton();
        auto.reduce();

        // initializing lists
        ArrayList<String> language = new ArrayList<>();
        ArrayList<State> path = new ArrayList<>();
        Map<State, Boolean> visited = new HashMap<>();
        for (State state : auto.getStates()) {
            visited.put(state, false);
        }

        // finding basis strings
        findBasisPath(auto.getInitialState(), path, visited, s, language);

        // returning language
        return new HashSet<>(language);
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

    private static void addPathToList(ArrayList<State> path, String s, ArrayList<String> language) throws FileNotFoundException {
        ArrayList<String> pathStrings = new ArrayList<>();
        for (int i = 0; i < path.size() - 1; i++) {
            ArrayList<Transition> transitions = findTransitions(path.get(i), path.get(i + 1));
            pathStrings = addCharacters(transitions, pathStrings);
        }
        while (language.size() < LANG_LIMIT && !pathStrings.isEmpty()) {
            language.add(0, pathStrings.remove(0));
        }
        if (language.size() >= LANG_LIMIT) {
            PrintWriter log = new PrintWriter("src/test/java/dk/brics/automaton/EgretLog");
            log.println("List size reached the limit (" + LANG_LIMIT + "). Cannot add any more strings from RegEx " + s);
            log.close();
        }
    }

    private static ArrayList<State> deepCopy(ArrayList<State> path) {
        return new ArrayList<>(path);
    }

    private static void findBasisPath(State curr, ArrayList<State> path, Map<State, Boolean> visited, String s, ArrayList<String> language) throws FileNotFoundException {

        ArrayList<State> currPath = deepCopy(path);
        currPath.add(curr);
        boolean beenHere = visited.get(curr);

        if (curr.isAccept()) {
            addPathToList(currPath, s, language);
            for (State state : currPath) {
                visited.put(state, true);
            }
        }
        else {
            List<Transition> transitions = curr.getSortedTransitions(false);
            for (Transition t : transitions) {
                if (t.getDest().equals(curr)) continue;
                findBasisPath(t.getDest(), currPath, visited, s, language);
                if (beenHere) break;
            }
        }
    }
}
