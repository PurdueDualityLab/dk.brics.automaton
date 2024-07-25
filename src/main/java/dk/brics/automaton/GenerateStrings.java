package dk.brics.automaton;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GenerateStrings {

    /**
     * Finds an estimation of all the strings a regular expression can match with
     * @param s String representation of a regex
     * @param maxNumVisits Max number of visits per state in regex automaton
     * @return ArrayList containing positive strings for the regex
     */
    public static ArrayList<String> generateStrings(String s, int maxNumVisits) {
        RegExp re = new RegExp(s);
        Automaton auto = re.toAutomaton();

        ArrayList<State> path = new ArrayList<>();
        ArrayList<String> positiveStr = new ArrayList<>();
        traverse(auto.getInitialState(), maxNumVisits, path, positiveStr);

        return positiveStr;
    }

    private static void traverse(State curr, int maxNumVisits, ArrayList<State> path, ArrayList<String> positiveStr) {

        ArrayList<State> currPath = shallowCopy(path);
        currPath.add(curr);
        curr.numVisits++;

        try {
            if (curr.isAccept()) {
                addPathToList(currPath, positiveStr);
            }
            for (Transition t : curr.getTransitions()) {
                if (t.getDest().numVisits < maxNumVisits) {
                    traverse(t.getDest(), maxNumVisits, currPath, positiveStr);
                }
            }
        }
        catch (OutOfMemoryError e) {
            System.out.println("OutOfMemoryError" + e);
        }
        catch (IllegalArgumentException e) {
            System.out.println("IllegalArgumentException " + e);
        }
        catch (Exception e) {
            System.out.println("Exception " + e);
        }
    }

    /**
     * Finds the e-similarity score between two regular expressions
     * @param truthRegexStr String representation of the truth regex
     * @param reuseCandidateStr String representation of the reuse candidate regex
     * @return e-similarity score as a float (between 0 and 1)
     */
    public static float eSimilarity(String truthRegexStr, String reuseCandidateStr) {

        ArrayList<String> truthPositiveStr = generateStrings(truthRegexStr, 1);
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

    private static ArrayList<State> shallowCopy(ArrayList<State> path) {
        return new ArrayList<>(path);
    }

    public static void addPathToList(ArrayList<State> path, ArrayList<String> language) {
        ArrayList<String> pathStrings = new ArrayList<>();
        for (int i = 0; i < path.size() - 1; i++) {
            ArrayList<Transition> transitions = findTransitions(path.get(i), path.get(i + 1));
            pathStrings = addCharacters(transitions, pathStrings);
        }
        language.add(pathStrings.remove(0));
    }


    public static ArrayList<Transition> findTransitions(State currState, State destState) {
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
