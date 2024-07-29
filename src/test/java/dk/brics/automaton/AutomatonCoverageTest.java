package dk.brics.automaton;

import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;
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
        coverage.evaluate("aa");
        assertFullMatchCoverage(
                coverage,
                info -> {
                    assertThat(info.getVisitedNodes()).containsExactlyInAnyOrder(-1, 0, 1);
                },
                summary -> {
                    assertThat(summary.getNodeCoverage()).isEqualTo(1.0);
                    assertThat(summary.getEdgeCoverage()).isEqualTo(1.0);
                }
        );
    }

    @Test
    void pattern2_coverage_shouldHaveFullCoverage() throws DfaTooLargeException {
        Automaton auto = prepareRegex("a(b|c)d");
        System.out.println(auto.toDot());
        AutomatonCoverage coverage = new AutomatonCoverage(auto);

        coverage.evaluate("abd");
        assertFullMatchCoverage(
                coverage,
                info -> {
                    assertThat(info.getVisitedNodes()).containsExactlyInAnyOrderElementsOf(statesToStateNums(auto.getLiveStates()));
                    assertThat(info.getVisitedEdges().size()).isEqualTo(auto.getNumberOfTransitions());
                },
                summary -> {
                    assertThat(summary.getNodeCoverage()).isLessThan(1.0);
                    assertThat(summary.getEdgeCoverage()).isLessThan(1.0);
                }
        );

        coverage.evaluate("b");
        coverage.evaluate("ae");
        coverage.evaluate("abe");
        coverage.evaluate("abde");
        assertFullMatchCoverage(
                coverage,
                info -> {},
                summary -> {
                    assertThat(summary.getNodeCoverage()).isEqualTo(1.0);
                    assertThat(summary.getEdgeCoverage()).isEqualTo(1.0);
                }
        );
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

    @Test
    void pattern4_coverage_hasCompleteCoverage() throws DfaTooLargeException {
        Automaton auto = prepareRegex("[a-z0-9]*A");
        System.out.println(auto.toDot());
        AutomatonCoverage coverage = new AutomatonCoverage(auto);

        coverage.evaluate("A");
        assertFullMatchCoverage(
                coverage,
                info -> {
                    assertThat(info.getVisitedNodes()).containsExactly(0, 1);
                },
                summary -> {
                    assertThat(summary.getNodeCoverage()).isLessThan(1.0);
                    assertThat(summary.getEdgeCoverage()).isEqualTo(1.0 / 5.0);
                }
        );

        coverage.evaluate("*");
        assertFullMatchCoverage(
                coverage,
                info -> {
                    assertThat(info.getVisitedNodes()).containsExactlyInAnyOrder(0, 1, -1);
                },
                summary -> {
                    assertThat(summary.getNodeCoverage()).isEqualTo(1.0);
                    assertThat(summary.getEdgeCoverage()).isEqualTo(2.0 / 5.0);
                }
        );

        coverage.evaluate("Ab");
        assertFullMatchCoverage(
                coverage,
                info -> {
                    assertThat(info.getVisitedNodes()).containsExactlyInAnyOrder(0, 1, -1);
                },
                summary -> {
                    assertThat(summary.getNodeCoverage()).isEqualTo(1.0);
                    assertThat(summary.getEdgeCoverage()).isEqualTo(3.0 / 5.0);
                }
        );

        coverage.evaluate("aA");
        assertFullMatchCoverage(
                coverage,
                info -> {
                    assertThat(info.getVisitedNodes()).containsExactlyInAnyOrder(0, 1, -1);
                },
                summary -> {
                    assertThat(summary.getNodeCoverage()).isEqualTo(1.0);
                    assertThat(summary.getEdgeCoverage()).isEqualTo(4.0 / 5.0);
                }
        );

        coverage.evaluate("0A");
        assertFullMatchCoverage(
                coverage,
                info -> {
                    assertThat(info.getVisitedNodes()).containsExactlyInAnyOrder(0, 1, -1);
                },
                summary -> {
                    assertThat(summary.getNodeCoverage()).isEqualTo(1.0);
                    assertThat(summary.getEdgeCoverage()).isEqualTo(1.0);
                }
        );
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

    private static void assertFullMatchCoverage(AutomatonCoverage coverage,
                                       Consumer<AutomatonCoverage.VisitationInfo> onVisitationInfo,
                                       Consumer<AutomatonCoverage.VisitationInfoSummary> onVisitationInfoSummary) {

        onVisitationInfo.accept(coverage.getFullMatchVisitationInfo());
        onVisitationInfoSummary.accept(coverage.getFullMatchVisitationInfoSummary());
    }

    private static void assertPartialMatchCoverage(AutomatonCoverage coverage,
                                                Consumer<AutomatonCoverage.VisitationInfo> onVisitationInfo,
                                                Consumer<AutomatonCoverage.VisitationInfoSummary> onVisitationInfoSummary) {

        onVisitationInfo.accept(coverage.getPartialMatchVisitationInfo());
        onVisitationInfoSummary.accept(coverage.getPartialMatchVisitationInfoSummary());
    }
}