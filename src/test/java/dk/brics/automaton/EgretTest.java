package dk.brics.automaton;

import static dk.brics.automaton.Egret.getRegexStrings;

public class EgretTest {

    private static void testEgret() {
        String regExp = "[a-zA-Z0-9]";
        getRegexStrings(regExp);
    }

    public static void main(String[] args) {
        testEgret();
    }
}
