package dk.brics.automaton;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GenerateStrings {

    /**
     * Finds an estimation of all the strings a regular expression can match with
     * @param regExpStr String representation of a regex
     * @param maxNumVisits Max number of visits per state in regex automaton
     * @param genPositiveStrings Flag for generating either positive strings (true) or negative (false) strings
     * @return ArrayList containing positive strings for the regex
     */
    public static Set<String> generateStrings(String regExpStr, int maxNumVisits, boolean genPositiveStrings) throws IllegalArgumentException {
        RegExp regExp = new RegExp(regExpStr);
        Automaton automaton = regExp.toAutomaton();
        ArrayList<State> path = new ArrayList<>();
        Set<String> strings = new HashSet<>();

        if (genPositiveStrings) {
            traverse(automaton.getInitialState(), maxNumVisits, path, strings, true);
        }
        else {
            Automaton autoCompliment = automaton.complement();
            traverse(autoCompliment.getInitialState(), maxNumVisits, path, strings, false);
        }
        return strings;
    }


    private static void traverse(State curr, int maxNumVisits, ArrayList<State> path, Set<String> strings, boolean sign) throws IllegalArgumentException {

        ArrayList<State> currPath = shallowCopy(path);
        currPath.add(curr);
        curr.numVisits++;

        try {
            if (curr.isAccept()) {
                addPathToList(currPath, strings, sign);
            }
            for (Transition t : curr.getTransitions()) {
                if (t.getDest().numVisits < maxNumVisits) {
                    if (curr.equals(t.getDest())) {
                        traverse(t.getDest(), maxNumVisits / 2, currPath, strings, sign);
                    }
                    else {
                        traverse(t.getDest(), maxNumVisits, currPath, strings, sign);
                    }
                }
            }
        }
        catch (OutOfMemoryError | IllegalArgumentException e) {
            throw new IllegalArgumentException("Cannot approximate language of regex", e);
        }
        catch (Exception e) {
            throw new RuntimeException("Cannot approximate language of regex, but for a reason i can't think of", e);
        }
    }

    /**
     * Finds the e-similarity score between two regular expressions
     *
     * @param truthRegexStr     String representation of the truth regex
     * @param reuseCandidateStr String representation of the reuse candidate regex
     * @return e-similarity score as a float (between 0 and 1)
     */
    public static double eSimilarity(String truthRegexStr, String reuseCandidateStr, int maxNumVisits) {

        Set<String> truthPositiveStr = generateStrings(truthRegexStr, maxNumVisits, true);
        Set<String> truthNegativeStr = generateStrings(truthRegexStr, maxNumVisits, false);
        Pattern reuseCandidateRegex = Pattern.compile(reuseCandidateStr);

        int numPositiveStr = truthPositiveStr.size();
        int numNegativeStr = truthNegativeStr.size();
        int numMatches = 0;
        int numRejects = 0;
        for (String s : truthPositiveStr) {
            Matcher match = reuseCandidateRegex.matcher(s);
            if (match.matches()) {
                numMatches++;
            }
        }
        for (String s : truthNegativeStr) {
            Matcher match = reuseCandidateRegex.matcher(s);
            if (!match.matches()) {
                numRejects++;
            }
        }
        float e = 1 - ((float) (numMatches + numRejects) / (numPositiveStr + numNegativeStr));
        return 1 - e;
    }

    private static ArrayList<State> shallowCopy(ArrayList<State> path) {
        return new ArrayList<>(path);
    }

    private static void addPathToList(ArrayList<State> path, Set<String> strings, boolean sign) {
        ArrayList<String> pathStrings = new ArrayList<>();
        for (int i = 0; i < path.size() - 1; i++) {
            ArrayList<Transition> transitions = findTransitions(path.get(i), path.get(i + 1));
            pathStrings = addCharacters(transitions, pathStrings, sign);
        }
        strings.addAll(pathStrings);
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


    private static ArrayList<String> addCharacters(ArrayList<Transition> transitions, ArrayList<String> pathStrings, boolean sign) {
        ArrayList<String> newPathStrings = new ArrayList<>();
        ArrayList<Character> charsToAppend = getCharsToAppend(transitions, sign);

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

    private static ArrayList<Character> getCharsToAppend(ArrayList<Transition> transitions, boolean sign) {
        ArrayList<Character> charsToAppend = new ArrayList<>();

        if (sign) {
            for (Transition t : transitions) {
                for (char c = t.getMin(); c <= t.getMax(); c++) {
                    charsToAppend.add(c);
                }
            }
        }
        else {
            for (Transition t : transitions) {
                charsToAppend.add(genCharacters(t));
            }
        }
        return charsToAppend;
    }

    private static char genCharacters(Transition transition) {

        if (transition.getMin() == transition.getMax()) {
            return transition.getMin();
        }

        Random random = new Random();
        int minRangeValue;
        int maxRangeValue;
        if (transition.getMin() > 0x007e || transition.getMax() < 0x0020) {
            minRangeValue = transition.getMin();
            maxRangeValue = transition.getMax();
        } else {
            minRangeValue = Math.max(transition.getMin(), 0x0020);
            maxRangeValue = Math.min(transition.getMax(), 0x007e);
        }
        int randomCharacter = random.nextInt(maxRangeValue - minRangeValue) + minRangeValue;
        return (char) randomCharacter;
    }
}
