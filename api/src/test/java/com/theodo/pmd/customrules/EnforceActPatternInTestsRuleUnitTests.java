package com.theodo.pmd.customrules;

import static org.assertj.core.api.Assertions.assertThat;
import static org.checkerframework.checker.nullness.util.NullnessUtil.castNonNull;

import com.theodo.pmd.customrules.EnforceActPatternInTestsRule.ViolationType;
import com.theodo.springblueprint.testhelpers.annotations.UnitTest;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sourceforge.pmd.PMDConfiguration;
import net.sourceforge.pmd.PmdAnalysis;
import net.sourceforge.pmd.lang.Language;
import net.sourceforge.pmd.lang.LanguageRegistry;
import net.sourceforge.pmd.lang.LanguageVersion;
import net.sourceforge.pmd.lang.document.FileId;
import net.sourceforge.pmd.lang.document.TextFile;
import net.sourceforge.pmd.lang.rule.RuleSet;
import net.sourceforge.pmd.reporting.Report;
import net.sourceforge.pmd.reporting.RuleViolation;
import org.assertj.core.api.AbstractObjectAssert;
import org.assertj.core.api.AbstractListAssert;
import org.assertj.core.api.ObjectAssert;
import org.junit.jupiter.api.Test;

@SuppressWarnings("PMD.GodClass")
@UnitTest
class EnforceActPatternInTestsRuleUnitTests {

    private static final Path TEST_ROOT = Path.of("src/test/java");
    private static final String EXPECTED_LINE_MARKER = "// ❌ EXPECTED_LINE";
    private static final Pattern PARSE_ERROR_LINE_PATTERN = Pattern.compile("line\\s+(\\d+)");
    private static final Language JAVA_LANGUAGE = getJavaLanguage();
    private static final LanguageVersion JAVA_VERSION = JAVA_LANGUAGE.getLatestVersion();

    @Test
    void missing_act_is_reported_for_test_method() {
        Path missingActPath = testPath("com/theodo/sample/MissingActTest.java");
        String source = """
            package com.theodo.sample;

            import org.junit.jupiter.api.Test;

            class MissingActTest {
                @Test
                void example() { // ❌ EXPECTED_LINE
                }
            }
            """;
        int expectedLineNumber = expectedLineNumberFromSource(source);

        // Act
        Report report = analyze(missingActPath, source);

        assertSingleViolationOfType(report, ViolationType.MISSING_ACT)
            .isEqualTo(new Violation(missingActPath, expectedLineNumber));
    }

    @Test
    void missing_act_is_ignored_when_non_test_annotation_present() {
        Path missingActPath = testPath("com/theodo/sample/DisabledMissingActTest.java");
        String source = """
            package com.theodo.sample;

            class DisabledMissingActTest {
                @Deprecated
                void example() {
                }
            }
            """;

        // Act
        Report report = analyze(missingActPath, source);

        assertViolationsOfType(report, ViolationType.MISSING_ACT).isEmpty();
    }

    @Test
    void missing_act_is_reported_for_multiple_test_methods() {
        Path missingActPath = testPath("com/theodo/sample/MultipleMissingActTest.java");
        String source = """
            package com.theodo.sample;

            import org.junit.jupiter.api.Test;

            class MultipleMissingActTest {
                @Test
                void first() { // ❌ EXPECTED_LINE
                }

                @Test
                void second() { // ❌ EXPECTED_LINE
                }
            }
            """;
        List<Integer> expectedLineNumbers = expectedLineNumbersFromSource(source);

        // Act
        Report report = analyze(missingActPath, source);

        assertViolationsOfType(report, ViolationType.MISSING_ACT)
            .containsExactly(
                new Violation(missingActPath, expectedLineNumbers.get(0)),
                new Violation(missingActPath, expectedLineNumbers.get(1))
            );
    }

    @Test
    void missing_act_is_reported_for_nested_test_class() {
        Path missingActPath = testPath("com/theodo/sample/NestedContractTest.java");
        String source = """
            package com.theodo.sample;

            import org.junit.jupiter.api.Nested;
            import org.junit.jupiter.api.Test;

            class NestedContractTest {
                @Nested
                class Create {
                    @Test
                    void creating_returns_value() { // ❌ EXPECTED_LINE
                    }
                }
            }
            """;
        int expectedLineNumber = expectedLineNumberFromSource(source);

        // Act
        Report report = analyze(missingActPath, source);

        assertSingleViolationOfType(report, ViolationType.MISSING_ACT)
            .isEqualTo(new Violation(missingActPath, expectedLineNumber));
    }

    @Test
    void missing_act_uses_top_level_class_name_for_nested_types() {
        Path violationPath = testPath("com/theodo/sample/NestedReportingTest.java");
        String source = """
            package com.theodo.sample;

            import org.junit.jupiter.api.Nested;
            import org.junit.jupiter.api.Test;

            class NestedReportingTest {
                @Nested
                class Create {
                    @Test
                    void creating_returns_value() {
                    }
                }
            }
            """;

        // Act
        Report report = analyze(violationPath, source);

        assertThat(report.getViolations())
            .singleElement()
            .extracting(RuleViolation::getAdditionalInfo)
            .satisfies(
                info -> assertThat(info)
                    .containsEntry(RuleViolation.CLASS_NAME, "NestedReportingTest")
            );
    }

    @Test
    void missing_act_is_reported_for_parameterized_test() {
        Path missingActPath = testPath("com/theodo/sample/ParameterizedMissingActTest.java");
        String source = """
            package com.theodo.sample;

            import org.junit.jupiter.params.ParameterizedTest;

            class ParameterizedMissingActTest {
                @ParameterizedTest
                void example(int value) { // ❌ EXPECTED_LINE
                }
            }
            """;
        int expectedLineNumber = expectedLineNumberFromSource(source);

        // Act
        Report report = analyze(missingActPath, source);

        assertSingleViolationOfType(report, ViolationType.MISSING_ACT)
            .isEqualTo(new Violation(missingActPath, expectedLineNumber));
    }

    @Test
    void missing_act_is_ignored_for_test_factory() {
        Path missingActPath = testPath("com/theodo/sample/TestFactoryMissingActTest.java");
        String source = """
            package com.theodo.sample;

            import org.junit.jupiter.api.TestFactory;

            class TestFactoryMissingActTest {
                @TestFactory
                Object example() {
                    return null;
                }
            }
            """;

        // Act
        Report report = analyze(missingActPath, source);

        assertViolationsOfType(report, ViolationType.MISSING_ACT).isEmpty();
    }

    @Test
    void missing_act_is_reported_without_unit_test_annotation() {
        Path missingActPath = testPath("com/theodo/sample/NonUnitTestTest.java");
        String source = """
            package com.theodo.sample;

            import org.junit.jupiter.api.Test;

            class NonUnitTestTest {
                @Test
                void example() { // ❌ EXPECTED_LINE
                }
            }
            """;
        int expectedLineNumber = expectedLineNumberFromSource(source);

        // Act
        Report report = analyze(missingActPath, source);

        assertSingleViolationOfType(report, ViolationType.MISSING_ACT)
            .isEqualTo(new Violation(missingActPath, expectedLineNumber));
    }

    @Test
    void missing_act_is_ignored_for_suppressed_method() {
        Path missingActPath = testPath("com/theodo/sample/SuppressedMethodTest.java");
        String source = """
            package com.theodo.sample;

            import org.junit.jupiter.api.Test;

            class SuppressedMethodTest {
                @Test
                @SuppressWarnings("PMD.EnforceActPatternInTests")
                void example() {
                }
            }
            """;

        // Act
        Report report = analyze(missingActPath, source);

        assertViolationsOfType(report, ViolationType.MISSING_ACT).isEmpty();
    }

    @Test
    void missing_act_is_ignored_for_abstract_test_method() {
        Path missingActPath = testPath("com/theodo/sample/AbstractTest.java");
        String source = """
            package com.theodo.sample;

            import org.junit.jupiter.api.Test;

            abstract class AbstractTest {
                @Test
                abstract void example();
            }
            """;

        // Act
        Report report = analyze(missingActPath, source);

        assertViolationsOfType(report, ViolationType.MISSING_ACT).isEmpty();
    }

    @Test
    void missing_act_is_reported_for_non_abstract_test_method_in_abstract_class() {
        Path missingActPath = testPath("com/theodo/sample/AbstractConcreteTest.java");
        String source = """
            package com.theodo.sample;

            import org.junit.jupiter.api.Test;

            abstract class AbstractConcreteTest {
                @Test
                void example() { // ❌ EXPECTED_LINE
                }
            }
            """;
        int expectedLineNumber = expectedLineNumberFromSource(source);

        // Act
        Report report = analyze(missingActPath, source);

        assertSingleViolationOfType(report, ViolationType.MISSING_ACT)
            .isEqualTo(new Violation(missingActPath, expectedLineNumber));
    }

    @Test
    void missing_act_is_ignored_for_suppressed_class() {
        Path missingActPath = testPath("com/theodo/sample/SuppressedClassTest.java");
        String source = """
            package com.theodo.sample;

            import org.junit.jupiter.api.Test;

            @SuppressWarnings("PMD.EnforceActPatternInTests")
            class SuppressedClassTest {
                @Test
                void example() {
                }
            }
            """;

        // Act
        Report report = analyze(missingActPath, source);

        assertViolationsOfType(report, ViolationType.MISSING_ACT).isEmpty();
    }

    @Test
    void missing_act_is_ignored_for_flow_application_tests_class() {
        Path missingActPath = testPath("com/theodo/sample/AuthenticationFlowApplicationTests.java");
        String source = """
            package com.theodo.sample;

            import org.junit.jupiter.api.Test;

            class AuthenticationFlowApplicationTests {
                @Test
                void example() {
                }
            }
            """;

        // Act
        Report report = analyze(missingActPath, source);

        assertViolationsOfType(report, ViolationType.MISSING_ACT).isEmpty();
    }

    @Test
    void missing_act_is_ignored_for_scheduled_task_integration_tests_class() {
        Path missingActPath = testPath("com/theodo/sample/CleanUpUserSessionsScheduledTaskIntegrationTests.java");
        String source = """
            package com.theodo.sample;

            import org.junit.jupiter.api.Test;

            class CleanUpUserSessionsScheduledTaskIntegrationTests {
                @Test
                void example() {
                }
            }
            """;

        // Act
        Report report = analyze(missingActPath, source);

        assertViolationsOfType(report, ViolationType.MISSING_ACT).isEmpty();
    }

    @Test
    void missing_act_is_reported_for_unmatched_suppression() {
        Path missingActPath = testPath("com/theodo/sample/UnmatchedSuppressionTest.java");
        String source = """
            package com.theodo.sample;

            import org.junit.jupiter.api.Test;

            class UnmatchedSuppressionTest {
                @Test
                @SuppressWarnings("PMD.OtherRule")
                void example() { // ❌ EXPECTED_LINE
                }
            }
            """;
        int expectedLineNumber = expectedLineNumberFromSource(source);

        // Act
        Report report = analyze(missingActPath, source);

        assertSingleViolationOfType(report, ViolationType.MISSING_ACT)
            .isEqualTo(new Violation(missingActPath, expectedLineNumber));
    }

    @Test
    void file_without_test_in_name_is_checked() {
        Path helperPath = testPath("com/theodo/sample/Helper.java");
        String source = """
            package com.theodo.sample;

            import org.junit.jupiter.api.Test;

            class Helper {
                @Test
                void example() { // ❌ EXPECTED_LINE
                }
            }
            """;
        int expectedLineNumber = expectedLineNumberFromSource(source);

        // Act
        Report report = analyze(helperPath, source);

        assertSingleViolationOfType(report, ViolationType.MISSING_ACT)
            .isEqualTo(new Violation(helperPath, expectedLineNumber));
    }

    @Test
    void non_test_method_is_ignored_for_banned_comment() {
        Path helperPath = testPath("com/theodo/sample/HelperTest.java");
        String source = """
            package com.theodo.sample;

            class HelperTest {
                void helper() {
                    // Given a value
                }
            }
            """;

        // Act
        Report report = analyze(helperPath, source);

        assertViolationsOfType(report, ViolationType.BANNED_COMMENT).isEmpty();
    }

    @Test
    void multiple_act_is_reported() {
        Path multipleActPath = testPath("com/theodo/sample/MultipleActTest.java");
        String source = """
            package com.theodo.sample;

            import org.junit.jupiter.api.Test;

            class MultipleActTest {
                @Test
                void example() {
                    // Act
                    callFirst();
                    // Act // ❌ EXPECTED_LINE
                    callSecond();

                    verify();
                }
            }
            """;
        int expectedLineNumber = expectedLineNumberFromSource(source);

        // Act
        Report report = analyze(multipleActPath, source);

        assertSingleViolationOfType(report, ViolationType.MULTIPLE_ACT)
            .isEqualTo(new Violation(multipleActPath, expectedLineNumber));
    }

    @Test
    void missing_act_statement_is_reported() {
        Path missingStatementPath = testPath("com/theodo/sample/MissingActStatementTest.java");
        String source = """
            package com.theodo.sample;

            import org.junit.jupiter.api.Test;

            class MissingActStatementTest {
                @Test
                void example() {
                    // Act // ❌ EXPECTED_LINE
                }
            }
            """;
        int expectedLineNumber = expectedLineNumberFromSource(source);

        // Act
        Report report = analyze(missingStatementPath, source);

        assertSingleViolationOfType(report, ViolationType.MISSING_ACT_STATEMENT)
            .isEqualTo(new Violation(missingStatementPath, expectedLineNumber));
    }

    @Test
    void missing_post_act_statement_is_reported() {
        Path missingStatementPath = testPath("com/theodo/sample/MissingPostActStatementTest.java");
        String source = """
            package com.theodo.sample;

            import org.junit.jupiter.api.Test;

            class MissingPostActStatementTest {
                @Test
                void example() {
                    // Act
                    doWork(); // ❌ EXPECTED_LINE
                }
            }
            """;
        int expectedLineNumber = expectedLineNumberFromSource(source);

        // Act
        Report report = analyze(missingStatementPath, source);

        assertSingleViolationOfType(report, ViolationType.MISSING_POST_ACT_STATEMENT)
            .isEqualTo(new Violation(missingStatementPath, expectedLineNumber));
    }

    @Test
    void act_statement_not_call_is_reported() {
        Path notCallPath = testPath("com/theodo/sample/ActNotCallTest.java");
        String source = """
            package com.theodo.sample;

            import org.junit.jupiter.api.Test;

            class ActNotCallTest {
                @Test
                void example() {
                    // Act
                    int value = 1; // ❌ EXPECTED_LINE

                    verify();
                }
            }
            """;
        int expectedLineNumber = expectedLineNumberFromSource(source);

        // Act
        Report report = analyze(notCallPath, source);

        assertSingleViolationOfType(report, ViolationType.ACT_STATEMENT_NOT_CALL)
            .isEqualTo(new Violation(notCallPath, expectedLineNumber));
    }

    @Test
    void assignment_without_call_is_reported() {
        Path notCallPath = testPath("com/theodo/sample/AssignmentNotCallTest.java");
        String source = """
            package com.theodo.sample;

            import org.junit.jupiter.api.Test;

            class AssignmentNotCallTest {
                private int value;

                @Test
                void example() {
                    // Act
                    value = 1; // ❌ EXPECTED_LINE

                    verify();
                }
            }
            """;
        int expectedLineNumber = expectedLineNumberFromSource(source);

        // Act
        Report report = analyze(notCallPath, source);

        assertSingleViolationOfType(report, ViolationType.ACT_STATEMENT_NOT_CALL)
            .isEqualTo(new Violation(notCallPath, expectedLineNumber));
    }

    @Test
    void constructor_call_is_reported_as_not_call() {
        Path constructorCallPath = testPath("com/theodo/sample/ConstructorActTest.java");
        String source = """
            package com.theodo.sample;

            import org.junit.jupiter.api.Test;

            class ConstructorActTest {
                @Test
                void example() {
                    // Act
                    new String("value"); // ❌ EXPECTED_LINE

                    verify();
                }
            }
            """;
        int expectedLineNumber = expectedLineNumberFromSource(source);

        // Act
        Report report = analyze(constructorCallPath, source);

        assertSingleViolationOfType(report, ViolationType.ACT_STATEMENT_NOT_CALL)
            .isEqualTo(new Violation(constructorCallPath, expectedLineNumber));
    }

    @Test
    void constructor_assignment_is_allowed_as_act_statement() {
        Path constructorCallPath = testPath("com/theodo/sample/ConstructorAssignmentActTest.java");
        String source = """
            package com.theodo.sample;

            import org.junit.jupiter.api.Test;

            class ConstructorAssignmentActTest {
                @Test
                void example() {
                    // Act
                    var value = new String("value");

                    verify();
                }
            }
            """;

        // Act
        Report report = analyze(constructorCallPath, source);

        assertViolationsOfType(report, ViolationType.ACT_STATEMENT_NOT_CALL).isEmpty();
    }

    @Test
    void assignment_calls_are_allowed_as_act_statement() {
        Path assignmentPath = testPath("com/theodo/sample/AssignmentCallActTest.java");
        String source = """
            package com.theodo.sample;

            import org.junit.jupiter.api.Test;

            class AssignmentCallActTest {
                private String value;

                @Test
                void assigns_method_call() {
                    // Act
                    value = read();

                    verify();
                }

                @Test
                void assigns_constructor_call() {
                    // Act
                    value = new String("value");

                    verify();
                }

                @Test
                void assigns_casted_method_call() {
                    // Act
                    value = (String) read();

                    verify();
                }

                @Test
                void assigns_casted_constructor_call() {
                    // Act
                    value = (String) new String("value");

                    verify();
                }

                @Test
                void assigns_local_variable_call() {
                    // Act
                    String localValue = String.valueOf(1);

                    verify();
                }
            }
            """;

        // Act
        Report report = analyze(assignmentPath, source);

        assertViolationsOfType(report, ViolationType.ACT_STATEMENT_NOT_CALL).isEmpty();
    }

    @Test
    void assignment_in_argument_is_reported_as_not_call() {
        Path assignmentPath = testPath("com/theodo/sample/AssignmentInArgumentActTest.java");
        String source = """
            package com.theodo.sample;

            import org.junit.jupiter.api.Test;

            class AssignmentInArgumentActTest {
                private String value;

                @Test
                void example() {
                    // Act
                    use(value = read()); // ❌ EXPECTED_LINE

                    verify();
                }
            }
            """;
        int expectedLineNumber = expectedLineNumberFromSource(source);

        // Act
        Report report = analyze(assignmentPath, source);

        assertSingleViolationOfType(report, ViolationType.ACT_STATEMENT_NOT_CALL)
            .isEqualTo(new Violation(assignmentPath, expectedLineNumber));
    }

    @Test
    void act_assertion_is_reported() {
        Path assertionPath = testPath("com/theodo/sample/ActAssertionTest.java");
        String source = """
            package com.theodo.sample;

            import org.junit.jupiter.api.Test;

            class ActAssertionTest {
                @Test
                void example() {
                    // Act
                    assertThat(true); // ❌ EXPECTED_LINE

                    verify();
                }
            }
            """;
        int expectedLineNumber = expectedLineNumberFromSource(source);

        // Act
        Report report = analyze(assertionPath, source);

        assertSingleViolationOfType(report, ViolationType.ACT_ASSERTION)
            .isEqualTo(new Violation(assertionPath, expectedLineNumber));
    }

    @Test
    void act_assertion_reports_class_details() {
        Path assertionPath = testPath("com/theodo/sample/ActAssertionDetailsTest.java");
        String source = """
            package com.theodo.sample;

            import org.junit.jupiter.api.Test;

            class ActAssertionDetailsTest {
                @Test
                void example() {
                    // Act
                    assertThat(true); // ❌ EXPECTED_LINE

                    verify();
                }
            }
            """;

        // Act
        Report report = analyze(assertionPath, source);

        assertThat(report.getViolations())
            .filteredOn(violation -> ViolationType.ACT_ASSERTION.message().equals(violation.getDescription()))
            .singleElement()
            .extracting(RuleViolation::getAdditionalInfo)
            .satisfies(
                info -> assertThat(info)
                    .containsEntry(RuleViolation.CLASS_NAME, "ActAssertionDetailsTest")
                    .containsEntry(RuleViolation.PACKAGE_NAME, "com.theodo.sample")
            );
    }

    @Test
    void act_junit_assertion_is_reported() {
        Path assertionPath = testPath("com/theodo/sample/ActJunitAssertionTest.java");
        String source = """
            package com.theodo.sample;

            import org.junit.jupiter.api.Test;

            class ActJunitAssertionTest {
                @Test
                void example() {
                    // Act
                    assertTrue(true); // ❌ EXPECTED_LINE

                    verify();
                }
            }
            """;
        int expectedLineNumber = expectedLineNumberFromSource(source);

        // Act
        Report report = analyze(assertionPath, source);

        assertSingleViolationOfType(report, ViolationType.ACT_ASSERTION)
            .isEqualTo(new Violation(assertionPath, expectedLineNumber));
    }

    @Test
    void act_comment_ignores_unrelated_block() {
        Path blockPath = testPath("com/theodo/sample/ActUnrelatedBlockTest.java");
        String source = """
            package com.theodo.sample;

            import org.junit.jupiter.api.Test;

            class ActUnrelatedBlockTest {
                @Test
                void example() {
                    if (true) {
                        doBeforeWork();
                    }

                    // Act
                    doWork();

                    if (true) {
                        doOtherWork();
                    }
                }
            }
            """;

        // Act
        Report report = analyze(blockPath, source);

        assertViolationsOfType(report, ViolationType.ACT_STATEMENT_NOT_CALL).isEmpty();
    }

    @Test
    void act_assertion_assignment_is_reported() {
        Path assertionPath = testPath("com/theodo/sample/ActAssertionAssignmentTest.java");
        String source = """
            package com.theodo.sample;

            import org.junit.jupiter.api.Test;

            class ActAssertionAssignmentTest {
                private Object assertion;

                @Test
                void example() {
                    // Act
                    assertion = assertThat(true); // ❌ EXPECTED_LINE

                    verify();
                }
            }
            """;
        int expectedLineNumber = expectedLineNumberFromSource(source);

        // Act
        Report report = analyze(assertionPath, source);

        assertSingleViolationOfType(report, ViolationType.ACT_ASSERTION)
            .isEqualTo(new Violation(assertionPath, expectedLineNumber));
    }

    @Test
    void assert_wrappers_are_allowed_as_act_statement() {
        Path wrapperPath = testPath("com/theodo/sample/AssertWrapperActTest.java");
        String source = """
            package com.theodo.sample;

            import org.junit.jupiter.api.Test;

            class AssertWrapperActTest {
                @Test
                void example() {
                    // Act
                    assertThatThrownBy(() -> doWork());

                    verify();
                }
            }
            """;

        // Act
        Report report = analyze(wrapperPath, source);

        assertViolationsOfType(report, ViolationType.ACT_ASSERTION).isEmpty();
    }

    @Test
    void act_comment_inside_try_with_resources_does_not_report_assertion() {
        Path nestedActPath = testPath("com/theodo/sample/NestedActCommentTest.java");
        String source = """
            package com.theodo.sample;

            import org.junit.jupiter.api.Test;

            class NestedActCommentTest {
                @Test
                void example() {
                    try (AutoCloseable resource = () -> { }) {
                        // Act
                        doWork();

                        verify();
                    }
                }
            }
            """;

        // Act
        Report report = analyze(nestedActPath, source);

        assertViolationsOfType(report, ViolationType.ACT_ASSERTION).isEmpty();
    }

    @Test
    void banned_comment_is_reported_for_line_comment() {
        Path bannedCommentPath = testPath("com/theodo/sample/BannedLineCommentTest.java");
        String source = """
            package com.theodo.sample;

            import org.junit.jupiter.api.Test;

            class BannedLineCommentTest {
                @Test
                void example() {
                    // Given // ❌ EXPECTED_LINE
                    // Act
                    doWork();

                    verify();
                }
            }
            """;
        int expectedLineNumber = expectedLineNumberFromSource(source);

        // Act
        Report report = analyze(bannedCommentPath, source);

        assertSingleViolationOfType(report, ViolationType.BANNED_COMMENT)
            .isEqualTo(new Violation(bannedCommentPath, expectedLineNumber));
    }

    @Test
    void banned_comment_is_reported_for_arrange_comment() {
        Path bannedCommentPath = testPath("com/theodo/sample/BannedArrangeCommentTest.java");
        String source = """
            package com.theodo.sample;

            import org.junit.jupiter.api.Test;

            class BannedArrangeCommentTest {
                @Test
                void example() {
                    // Arrange // ❌ EXPECTED_LINE
                    // Act
                    doWork();

                    verify();
                }
            }
            """;
        int expectedLineNumber = expectedLineNumberFromSource(source);

        // Act
        Report report = analyze(bannedCommentPath, source);

        assertSingleViolationOfType(report, ViolationType.BANNED_COMMENT)
            .isEqualTo(new Violation(bannedCommentPath, expectedLineNumber));
    }

    @Test
    void banned_comment_is_reported_for_assert_comment() {
        Path bannedCommentPath = testPath("com/theodo/sample/BannedAssertCommentTest.java");
        String source = """
            package com.theodo.sample;

            import org.junit.jupiter.api.Test;

            class BannedAssertCommentTest {
                @Test
                void example() {
                    // Assert // ❌ EXPECTED_LINE
                    // Act
                    doWork();

                    verify();
                }
            }
            """;
        int expectedLineNumber = expectedLineNumberFromSource(source);

        // Act
        Report report = analyze(bannedCommentPath, source);

        assertSingleViolationOfType(report, ViolationType.BANNED_COMMENT)
            .isEqualTo(new Violation(bannedCommentPath, expectedLineNumber));
    }

    @Test
    void banned_comment_is_ignored_for_phrase_with_then() {
        Path bannedCommentPath = testPath("com/theodo/sample/BannedThenPhraseCommentTest.java");
        String source = """
            package com.theodo.sample;

            import org.junit.jupiter.api.Test;

            class BannedThenPhraseCommentTest {
                @Test
                void example() {
                    // Create then delete
                    // Act
                    doWork();

                    verify();
                }
            }
            """;

        // Act
        Report report = analyze(bannedCommentPath, source);

        assertViolationsOfType(report, ViolationType.BANNED_COMMENT).isEmpty();
    }

    @Test
    void banned_comment_is_ignored_for_block_comment() {
        Path bannedCommentPath = testPath("com/theodo/sample/BannedBlockCommentTest.java");
        String source = """
            package com.theodo.sample;

            import org.junit.jupiter.api.Test;

            class BannedBlockCommentTest {
                @Test
                void example() {
                    /*
                     * When doing work
                     */
                    // Act
                    doWork();

                    verify();
                }
            }
            """;

        // Act
        Report report = analyze(bannedCommentPath, source);

        assertViolationsOfType(report, ViolationType.BANNED_COMMENT).isEmpty();
    }

    @Test
    void spacing_before_act_is_reported() {
        Path spacingPath = testPath("com/theodo/sample/SpacingBeforeActTest.java");
        String source = """
            package com.theodo.sample;

            import org.junit.jupiter.api.Test;

            class SpacingBeforeActTest {
                @Test
                void example() {
                    int value = 1;
                    // Act // ❌ EXPECTED_LINE
                    doWork();

                    verify();
                }
            }
            """;
        int expectedLineNumber = expectedLineNumberFromSource(source);

        // Act
        Report report = analyze(spacingPath, source);

        assertSingleViolationOfType(report, ViolationType.SPACING_BEFORE_ACT)
            .isEqualTo(new Violation(spacingPath, expectedLineNumber));
    }

    @Test
    void spacing_after_act_is_reported() {
        Path spacingPath = testPath("com/theodo/sample/SpacingAfterActTest.java");
        String source = """
            package com.theodo.sample;

            import org.junit.jupiter.api.Test;

            class SpacingAfterActTest {
                @Test
                void example() {
                    // Act
                    doWork(); // ❌ EXPECTED_LINE
                    verify();
                }
            }
            """;
        int expectedLineNumber = expectedLineNumberFromSource(source);

        // Act
        Report report = analyze(spacingPath, source);

        assertSingleViolationOfType(report, ViolationType.SPACING_AFTER_ACT)
            .isEqualTo(new Violation(spacingPath, expectedLineNumber));
    }

    @Test
    void spacing_after_act_is_ignored_when_next_statement_is_outside_block() {
        Path spacingPath = testPath("com/theodo/sample/SpacingAfterActOutsideBlockTest.java");
        String source = """
            package com.theodo.sample;

            import org.junit.jupiter.api.Test;

            class SpacingAfterActOutsideBlockTest {
                @Test
                void example() {
                    if (true) {
                        // Act
                        doWork();
                    }
                    verify();
                }
            }
            """;

        // Act
        Report report = analyze(spacingPath, source);

        assertViolationsOfType(report, ViolationType.SPACING_AFTER_ACT).isEmpty();
    }

    @Test
    void parse_error_is_reported() {
        Path brokenPath = testPath("com/theodo/sample/BrokenTest.java");
        String source = "# // ❌ EXPECTED_LINE";
        int expectedLineNumber = expectedLineNumberFromSource(source);

        // Act
        Report report = analyze(brokenPath, source);

        assertSingleViolation(parseProcessingErrors(report), brokenPath, expectedLineNumber);
    }

    @Test
    void record_syntax_is_not_reported_as_parse_error() {
        Path recordPath = testPath("com/theodo/sample/RecordSyntaxTest.java");
        String source = """
            package com.theodo.sample;

            class RecordSyntaxTest {
                private record Example(String value) {
                }
            }
            """;

        // Act
        Report report = analyze(recordPath, source);

        assertThat(parseProcessingErrors(report)).isEmpty();
    }

    private static Report analyze(Path path, String source) {
        PMDConfiguration configuration = new PMDConfiguration(LanguageRegistry.PMD);
        configuration.setDefaultLanguageVersion(JAVA_VERSION);
        configuration.setOnlyRecognizeLanguage(JAVA_LANGUAGE);
        String sanitizedSource = source.replaceAll("(?m)\\s*// ❌ EXPECTED_LINE\\s*$", "");
        try (PmdAnalysis analysis = PmdAnalysis.create(configuration)) {
            analysis.addRuleSet(RuleSet.forSingleRule(new EnforceActPatternInTestsRule()));
            FileId fileId = FileId.fromPathLikeString(path.toString());
            TextFile textFile = TextFile.forCharSeq(sanitizedSource, fileId, JAVA_VERSION);
            analysis.files().addFile(textFile);
            return analysis.performAnalysisAndCollectReport();
        }
    }

    private static AbstractObjectAssert<?, Violation> assertSingleViolationOfType(Report report,
        ViolationType violationType) {
        return assertThat(report.getViolations())
            .filteredOn(violation -> violationType.message().equals(violation.getDescription()))
            .singleElement()
            .extracting(EnforceActPatternInTestsRuleUnitTests::toViolation);
    }

    private static AbstractListAssert<?, ?, Violation, ObjectAssert<Violation>> assertViolationsOfType(
        Report report,
        ViolationType type) {
        return assertThat(report.getViolations())
            .filteredOn(violation -> type.message().equals(violation.getDescription()))
            .<Violation>extracting(EnforceActPatternInTestsRuleUnitTests::toViolation);
    }

    private static List<Violation> parseProcessingErrors(Report report) {
        List<Violation> violations = new ArrayList<>();
        for (Report.ProcessingError error : report.getProcessingErrors()) {
            Path path = Path.of(error.getFileId().getOriginalPath());
            int line = parseLineNumber(error);
            violations.add(new Violation(path, line));
        }
        return violations;
    }

    private static int parseLineNumber(Report.ProcessingError error) {
        String detail = error.getDetail();
        String message = error.getMsg() + " " + detail;
        Matcher matcher = PARSE_ERROR_LINE_PATTERN.matcher(message);
        if (matcher.find()) {
            String lineValue = matcher.group(1);
            if (lineValue == null) {
                return 1;
            }
            return Integer.parseInt(lineValue);
        }
        return 1;
    }

    private static Violation toViolation(RuleViolation violation) {
        Path path = Path.of(violation.getFileId().getOriginalPath());
        return new Violation(path, violation.getBeginLine());
    }

    private static void assertSingleViolation(
        List<Violation> violations,
        Path expectedPath,
        int expectedLineNumber) {
        assertThat(violations)
            .singleElement()
            .isEqualTo(new Violation(expectedPath, expectedLineNumber));
    }

    private static int expectedLineNumberFromSource(String source) {
        List<Integer> expectedLineNumbers = expectedLineNumbersFromSource(source);
        return assertThat(expectedLineNumbers)
            .as("Expected exactly one expected line marker: %s", EXPECTED_LINE_MARKER)
            .singleElement()
            .actual();
    }

    private static List<Integer> expectedLineNumbersFromSource(String source) {
        String[] lines = source.split("\\R", -1);
        List<Integer> expectedLineNumbers = new ArrayList<>();
        for (int index = 0; index < lines.length; index++) {
            if (lines[index].contains(EXPECTED_LINE_MARKER)) {
                expectedLineNumbers.add(index + 1);
            }
        }
        if (!expectedLineNumbers.isEmpty()) {
            return expectedLineNumbers;
        }
        throw new IllegalArgumentException("Missing expected line marker: " + EXPECTED_LINE_MARKER);
    }

    private static Path testPath(String relativePath) {
        return TEST_ROOT.resolve(relativePath);
    }

    private record Violation(Path path, int line) {
    }

    private static Language getJavaLanguage() {
        return castNonNull(LanguageRegistry.PMD.getLanguageById("java"));
    }
}
