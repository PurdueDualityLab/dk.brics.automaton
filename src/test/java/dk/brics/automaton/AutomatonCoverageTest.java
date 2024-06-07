package dk.brics.automaton;

import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class AutomatonCoverageTest {

    @Test
    void pattern1_coverage_shouldHaveFullCoverage() throws DfaTooLargeException {
        Automaton auto = prepareRegex("(a|b)");
        AutomatonCoverage coverage = new AutomatonCoverage(auto);
        coverage.evaluate("a");
        coverage.evaluate("b");
        coverage.evaluate("c");
        AutomatonCoverage.VisitationInfo info = coverage.getFullMatchVisitationInfo();

        Set<Integer> liveStates = statesToStateNums(auto.getLiveStates());

        assertThat(info.getVisitedNodes()).containsExactlyElementsOf(liveStates);
        assertThat(info.getVisitedEdges().size()).isEqualTo(auto.getNumberOfTransitions());
    }

    @Test
    void pattern2_coverage_shouldHaveFullCoverage() throws DfaTooLargeException {
        Automaton auto = prepareRegex("a(b|c)d");
        System.out.println(auto.toDot());
        AutomatonCoverage coverage = new AutomatonCoverage(auto);

        coverage.evaluate("abd");

        AutomatonCoverage.VisitationInfo info = coverage.getFullMatchVisitationInfo();
        assertThat(info.getVisitedNodes()).containsExactlyInAnyOrderElementsOf(statesToStateNums(auto.getLiveStates()));
        assertThat(info.getVisitedEdges().size()).isEqualTo(auto.getNumberOfTransitions());
    }

    @Test
    void pattern3_coverage_hasIncompleteNodeCoverage() throws DfaTooLargeException {
        Automaton auto = prepareRegex("^http(s)?:\\/\\/$");
        System.out.println(auto.toDot());
        AutomatonCoverage coverage = new AutomatonCoverage(auto);

        coverage.evaluate("http://");

        AutomatonCoverage.VisitationInfo info = coverage.getFullMatchVisitationInfo();

        Set<Integer> states = statesToStateNums(auto.getLiveStates());
        states.remove(4);
        assertThat(info.getVisitedNodes()).containsExactlyInAnyOrderElementsOf(states);
    }

    @Test
    void pattern3_coverage_hasIncompleteEdgeCoverage() throws DfaTooLargeException {
        Automaton auto = prepareRegex("^http(s)?:\\/\\/$");
        System.out.println(auto.toDot());
        AutomatonCoverage coverage = new AutomatonCoverage(auto);

        coverage.evaluate("https://");

        AutomatonCoverage.VisitationInfo info = coverage.getFullMatchVisitationInfo();

        Set<Integer> states = statesToStateNums(auto.getLiveStates());
        assertThat(info.getVisitedNodes()).containsExactlyInAnyOrderElementsOf(states);
        assertThat(info.getVisitedEdges().size()).isEqualTo(auto.getNumberOfTransitions() - 1);
    }

    @Test
    void pattern3_coverage_hasCompleteCoverage() throws DfaTooLargeException {
        Automaton auto = prepareRegex("^http(s)?:\\/\\/$");
        System.out.println(auto.toDot());
        AutomatonCoverage coverage = new AutomatonCoverage(auto);

        coverage.evaluate("https://");
        coverage.evaluate("http://");

        AutomatonCoverage.VisitationInfo info = coverage.getFullMatchVisitationInfo();

        assertThat(info.getVisitedNodes()).containsExactlyInAnyOrderElementsOf(statesToStateNums(auto.getLiveStates()));
        assertThat(info.getVisitedEdges().size()).isEqualTo(auto.getNumberOfTransitions());
    }

    private static Automaton prepareRegex(String pattern) {
        RegExp regex = new RegExp(pattern, RegExp.NONE);
        Automaton auto = regex.toAutomaton();
        auto.determinize();
        auto.minimize();
        return auto;
    }

    private static Set<Integer> statesToStateNums(Collection<State> states) {
        return states.stream().map(state -> state.number).collect(Collectors.toSet());
    }
}