package dk.brics.automaton;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Egret {

    static int LANG_LIMIT = 2000;


    /**
     * Generates a representative set of strings for a given RegExp
     * @param regExpStr String representation of RegExp
     * @return Set of two sets: set one containing positive strings; set two containing negative strings
     * @throws OutOfMemoryError if the set of strings is too large to process
     * @throws IllegalArgumentException if there is an illegal character used
     */
    public static Set<String> findBasisPaths(String regExpStr) {

        regExpStr = refactorTokens(regExpStr);
        RegExp regExp = new RegExp(regExpStr);
        Automaton auto = regExp.toAutomaton();

        ArrayList<String> positiveStr = new ArrayList<>();
        ArrayList<State> path = new ArrayList<>();
        Map<State, Boolean> visited = new HashMap<>();
        for (State state : auto.getStates()) {
            visited.put(state, false);
        }

        traverse(auto.getInitialState(), path, visited, regExpStr, positiveStr);
        return new HashSet<>(positiveStr);
    }


    /**
     * Finds the e-similarity score between two RegExps
     * @param truthRegexStr String representation of the truth RegExp
     * @param reuseCandidateStr String representation of the reuse candidate RegExp
     * @return e-similarity score as a float (between 0 and 1)
     */
    public static float eSimilarity(String truthRegexStr, String reuseCandidateStr) {

        Set<String> truthPositiveStr = findBasisPaths(truthRegexStr);
        System.out.println(truthPositiveStr);
        Pattern reuseCandidateRegex = Pattern.compile(reuseCandidateStr);

        int numPositiveStr = truthPositiveStr.size();
        int numMatches = 0;
        for (String s : truthPositiveStr) {
            Matcher match = reuseCandidateRegex.matcher(s);
            if (match.matches()) {
                numMatches++;
            }
        }
        int e = 1 - (numMatches / numPositiveStr);
        return 1 - e;
    }


    private static void traverse(State curr, ArrayList<State> path, Map<State, Boolean> visited, String s, ArrayList<String> language) {

        ArrayList<State> currPath = deepCopy(path);
        currPath.add(curr);
        visited.put(curr, true);

        try {
            // RegExp matches with empty string
            if (curr.isAccept() && currPath.size() == 1) {
                language.add("<empty>");
                visited.put(curr, true);
            }

            if (curr.isAccept() && currPath.size() != 1) {
                addPathToList(currPath, s, language);
            } else {
                List<Transition> transitions = curr.getSortedTransitions(false);
                for (Transition t : transitions) {
                    // if (t.getDest().equals(curr)) continue;
                    if (!visited.get(t.getDest())) {
                        traverse(t.getDest(), currPath, visited, s, language);
                    }
                }
            }
        }
        catch (OutOfMemoryError e) {
            System.out.println("OutOfMemoryError" + e + " for RegExp " + s);
        }
        catch (IllegalArgumentException e) {
            System.out.println("IllegalArgumentException " + e + " for RegExp " + s);
        }
        catch (Exception e) {
            System.out.println("Exception " + e + " for RegExp " + s);
        }
    }

    private static String refactorTokens(String regExpStr) {
        regExpStr = regExpStr.replaceAll("\\\\d", "[0-9]");
        regExpStr = regExpStr.replaceAll("\\\\w", "[a-zA-Z0-9_]");
        return regExpStr;
    }

    private static ArrayList<State> deepCopy(ArrayList<State> path) {
        return new ArrayList<>(path);
    }


    private static void addPathToList(ArrayList<State> path, String s, ArrayList<String> language) {

        ArrayList<String> pathStrings = new ArrayList<>();
        for (int i = 0; i < path.size() - 1; i++) {
            ArrayList<Transition> transitions = findTransitions(path.get(i), path.get(i + 1));
            pathStrings = addCharacters(transitions, pathStrings);
        }
        while (language.size() < LANG_LIMIT && !pathStrings.isEmpty()) {
            language.add(pathStrings.remove(0));
        }
        if (language.size() >= LANG_LIMIT) {
            System.out.println("List size reached the limit (" + LANG_LIMIT + "). Cannot add any more strings from RegEx " + s);
        }
    }


    private static ArrayList<Transition> findTransitions(State currState, State destState) {
        ArrayList<Transition> transitions = new ArrayList<>();
        for (Transition t : currState.getTransitions()) {
            if (t.getDest().equals(destState)) {
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
}
