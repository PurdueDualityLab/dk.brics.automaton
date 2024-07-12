package dk.brics.automaton;

import static dk.brics.automaton.Egret.getRegexStrings;

public class EgretTest {

    private static void testEgret() {
        String regExp = "a[b|c+]";
        getRegexStrings(regExp);
    }

    public static void main(String[] args) {
        testEgret();
    }
}
