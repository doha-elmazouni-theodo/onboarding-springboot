// package com.theodo.springblueprint.testhelpers.utils.sourceparsing;
//
// import static org.assertj.core.api.Assertions.assertThat;
// import static org.assertj.core.api.Assertions.tuple;
//
// import com.theodo.springblueprint.testhelpers.annotations.UnitTest;
// import java.nio.file.Path;
// import java.util.ArrayList;
// import java.util.List;
// import java.util.Map;
// import org.eclipse.collections.api.list.ImmutableList;
// import org.junit.jupiter.api.Nested;
// import org.junit.jupiter.api.Test;
//
// @UnitTest
// class ActPatternCheckerUnitTests {
//
// private static final Path TEST_ROOT = Path.of("src/test/java");
// private static final String EXPECTED_LINE_MARKER = "// ❌ expected line";
//
// @Nested
// class CollectViolationsByType {
//
// @Test
// void missing_act_is_reported_for_test_method() throws Exception {
// Path missingActPath = testPath("com/theodo/sample/MissingActTest.java");
// String source = """
// package com.theodo.sample;
//
// import org.junit.jupiter.api.Test;
//
// class MissingActTest {
// @Test
// void example() { // ❌ expected line
// int value = 1;
// }
// }
// """;
// ActPatternChecker checker = checkerFor(missingActPath, source);
// int expectedLineNumber = expectedLineNumberFromSource(source);
//
// // Act
// var violations = checker.collectViolationsByType(ActPatternChecker.ViolationType.MISSING_ACT);
//
// assertSingleViolation(violations, missingActPath, expectedLineNumber);
// }
//
// @Test
// void missing_act_is_reported_for_multiple_test_methods() throws Exception {
// Path missingActPath = testPath("com/theodo/sample/MultipleMissingActTest.java");
// String source = """
// package com.theodo.sample;
//
// import org.junit.jupiter.api.Test;
//
// class MultipleMissingActTest {
// @Test
// void first() { // ❌ expected line
// int value = 1;
// }
//
// @Test
// void second() { // ❌ expected line
// int result = 2;
// }
// }
// """;
// ActPatternChecker checker = checkerFor(missingActPath, source);
// List<Integer> expectedLineNumbers = expectedLineNumbersFromSource(source);
//
// // Act
// var violations = checker.collectViolationsByType(ActPatternChecker.ViolationType.MISSING_ACT);
//
// assertThat(violations)
// .extracting(ActPatternViolation::path, ActPatternViolation::line)
// .containsExactly(
// tuple(missingActPath, expectedLineNumbers.get(0)),
// tuple(missingActPath, expectedLineNumbers.get(1))
// );
// }
//
// @Test
// void missing_act_is_reported_for_parameterized_test() throws Exception {
// Path missingActPath = testPath("com/theodo/sample/ParameterizedMissingActTest.java");
// String source = """
// package com.theodo.sample;
//
// import org.junit.jupiter.params.ParameterizedTest;
//
// class ParameterizedMissingActTest {
// @ParameterizedTest
// void example(int value) { // ❌ expected line
// int result = value;
// }
// }
// """;
// ActPatternChecker checker = checkerFor(missingActPath, source);
// int expectedLineNumber = expectedLineNumberFromSource(source);
//
// // Act
// var violations = checker.collectViolationsByType(ActPatternChecker.ViolationType.MISSING_ACT);
//
// assertSingleViolation(violations, missingActPath, expectedLineNumber);
// }
//
// @Test
// void missing_act_is_ignored_for_test_factory() throws Exception {
// Path missingActPath = testPath("com/theodo/sample/TestFactoryMissingActTest.java");
// String source = """
// package com.theodo.sample;
//
// import java.util.stream.Stream;
// import org.junit.jupiter.api.DynamicTest;
// import org.junit.jupiter.api.TestFactory;
//
// class TestFactoryMissingActTest {
// @TestFactory
// Stream<DynamicTest> example() {
// return Stream.empty();
// }
// }
// """;
// ActPatternChecker checker = checkerFor(missingActPath, source);
//
// // Act
// var violations = checker.collectViolationsByType(ActPatternChecker.ViolationType.MISSING_ACT);
//
// assertThat(violations).isEmpty();
// }
//
// @Test
// void missing_act_is_ignored_for_suppressed_method() throws Exception {
// Path missingActPath = testPath("com/theodo/sample/SuppressedMethodTest.java");
// String source = """
// package com.theodo.sample;
//
// import org.junit.jupiter.api.Test;
//
// class SuppressedMethodTest {
// @Test
// @SuppressWarnings(value = "Arch.ActPattern")
// void example() {
// int value = 1;
// }
// }
// """;
// ActPatternChecker checker = checkerFor(missingActPath, source);
//
// // Act
// var violations = checker.collectViolationsByType(ActPatternChecker.ViolationType.MISSING_ACT);
//
// assertThat(violations).isEmpty();
// }
//
// @Test
// void missing_act_is_ignored_for_suppressed_class() throws Exception {
// Path missingActPath = testPath("com/theodo/sample/SuppressedClassTest.java");
// String source = """
// package com.theodo.sample;
//
// import org.junit.jupiter.api.Test;
//
// @SuppressWarnings({ "Arch.ActPattern" })
// class SuppressedClassTest {
// @Test
// void example() {
// int value = 1;
// }
// }
// """;
// ActPatternChecker checker = checkerFor(missingActPath, source);
//
// // Act
// var violations = checker.collectViolationsByType(ActPatternChecker.ViolationType.MISSING_ACT);
//
// assertThat(violations).isEmpty();
// }
//
// @Test
// void file_without_test_in_name_is_checked() throws Exception {
// Path helperPath = testPath("com/theodo/sample/Helper.java");
// String source = """
// package com.theodo.sample;
//
// import org.junit.jupiter.api.Test;
//
// class Helper {
// @Test
// void example() { // ❌ expected line
// int value = 1;
// }
// }
// """;
// ActPatternChecker checker = checkerFor(helperPath, source);
// int expectedLineNumber = expectedLineNumberFromSource(source);
//
// // Act
// var violations = checker.collectViolationsByType(ActPatternChecker.ViolationType.MISSING_ACT);
//
// assertSingleViolation(violations, helperPath, expectedLineNumber);
// }
//
// @Test
// void non_test_method_is_ignored_for_banned_comment() throws Exception {
// Path helperPath = testPath("com/theodo/sample/HelperTest.java");
// String source = """
// package com.theodo.sample;
//
// class HelperTest {
// void helper() {
// // Given a value
// doWork();
// }
// }
// """;
// ActPatternChecker checker = checkerFor(helperPath, source);
//
// // Act
// var violations = checker.collectViolationsByType(ActPatternChecker.ViolationType.BANNED_COMMENT);
//
// assertThat(violations).isEmpty();
// }
//
// @Test
// void multiple_act_is_reported() throws Exception {
// Path multipleActPath = testPath("com/theodo/sample/MultipleActTest.java");
// String source = """
// package com.theodo.sample;
//
// import org.junit.jupiter.api.Test;
//
// class MultipleActTest {
// @Test
// void example() {
// // Act
// callFirst();
// // Act // ❌ expected line
// callSecond();
// }
// }
// """;
// ActPatternChecker checker = checkerFor(multipleActPath, source);
// int expectedLineNumber = expectedLineNumberFromSource(source);
//
// // Act
// var violations = checker.collectViolationsByType(ActPatternChecker.ViolationType.MULTIPLE_ACT);
//
// assertSingleViolation(violations, multipleActPath, expectedLineNumber);
// }
//
// @Test
// void missing_act_statement_is_reported() throws Exception {
// Path missingStatementPath = testPath("com/theodo/sample/MissingActStatementTest.java");
// String source = """
// package com.theodo.sample;
//
// import org.junit.jupiter.api.Test;
//
// class MissingActStatementTest {
// @Test
// void example() {
// // Act // ❌ expected line
// }
// }
// """;
// ActPatternChecker checker = checkerFor(missingStatementPath, source);
// int expectedLineNumber = expectedLineNumberFromSource(source);
//
// // Act
// var violations = checker.collectViolationsByType(ActPatternChecker.ViolationType.MISSING_ACT_STATEMENT);
//
// assertSingleViolation(violations, missingStatementPath, expectedLineNumber);
// }
//
// @Test
// void act_statement_not_call_is_reported() throws Exception {
// Path notCallPath = testPath("com/theodo/sample/ActNotCallTest.java");
// String source = """
// package com.theodo.sample;
//
// import static org.assertj.core.api.Assertions.assertThat;
// import org.junit.jupiter.api.Test;
//
// class ActNotCallTest {
// @Test
// void example() {
// // Act
// int value = 1; // ❌ expected line
// assertThat(value).isEqualTo(1);
// }
// }
// """;
// ActPatternChecker checker = checkerFor(notCallPath, source);
// int expectedLineNumber = expectedLineNumberFromSource(source);
//
// // Act
// var violations = checker.collectViolationsByType(ActPatternChecker.ViolationType.ACT_STATEMENT_NOT_CALL);
//
// assertSingleViolation(violations, notCallPath, expectedLineNumber);
// }
//
// @Test
// void constructor_call_is_reported_as_not_call() throws Exception {
// Path constructorCallPath = testPath("com/theodo/sample/ConstructorActTest.java");
// String source = """
// package com.theodo.sample;
//
// import org.junit.jupiter.api.Test;
//
// class ConstructorActTest {
// @Test
// void example() {
// // Act
// new String("value"); // ❌ expected line
// }
// }
// """;
// ActPatternChecker checker = checkerFor(constructorCallPath, source);
// int expectedLineNumber = expectedLineNumberFromSource(source);
//
// // Act
// var violations = checker.collectViolationsByType(ActPatternChecker.ViolationType.ACT_STATEMENT_NOT_CALL);
//
// assertSingleViolation(violations, constructorCallPath, expectedLineNumber);
// }
//
// @Test
// void constructor_assignment_is_allowed_as_act_statement() throws Exception {
// Path constructorCallPath = testPath("com/theodo/sample/ConstructorAssignmentActTest.java");
// String source = """
// package com.theodo.sample;
//
// import org.junit.jupiter.api.Test;
//
// class ConstructorAssignmentActTest {
// @Test
// void example() {
// // Act
// var value = new String("value");
// }
// }
// """;
// ActPatternChecker checker = checkerFor(constructorCallPath, source);
//
// // Act
// var violations = checker.collectViolationsByType(ActPatternChecker.ViolationType.ACT_STATEMENT_NOT_CALL);
//
// assertThat(violations).isEmpty();
// }
//
// @Test
// void act_assertion_is_reported() throws Exception {
// Path assertionPath = testPath("com/theodo/sample/ActAssertionTest.java");
// String source = """
// package com.theodo.sample;
//
// import static org.assertj.core.api.Assertions.assertThat;
// import org.junit.jupiter.api.Test;
//
// class ActAssertionTest {
// @Test
// void example() {
// int value = 1;
// // Act
// assertThat(value); // ❌ expected line
// assertThat(value).isEqualTo(1);
// }
// }
// """;
// ActPatternChecker checker = checkerFor(assertionPath, source);
// int expectedLineNumber = expectedLineNumberFromSource(source);
//
// // Act
// var violations = checker.collectViolationsByType(ActPatternChecker.ViolationType.ACT_ASSERTION);
//
// assertSingleViolation(violations, assertionPath, expectedLineNumber);
// }
//
// @Test
// void assert_wrappers_are_allowed_as_act_statement() throws Exception {
// Path wrapperPath = testPath("com/theodo/sample/AssertWrapperActTest.java");
// String source = """
// package com.theodo.sample;
//
// import static org.assertj.core.api.Assertions.assertThatThrownBy;
// import org.junit.jupiter.api.Test;
//
// class AssertWrapperActTest {
// @Test
// void example() {
// // Act
// assertThatThrownBy(() -> doWork());
// }
// }
// """;
// ActPatternChecker checker = checkerFor(wrapperPath, source);
//
// // Act
// var violations = checker.collectViolationsByType(ActPatternChecker.ViolationType.ACT_ASSERTION);
//
// assertThat(violations).isEmpty();
// }
//
// @Test
// void act_comment_inside_try_with_resources_does_not_report_assertion() throws Exception {
// Path nestedActPath = testPath("com/theodo/sample/NestedActCommentTest.java");
// String source = """
// package com.theodo.sample;
//
// import static org.assertj.core.api.Assertions.assertThat;
// import java.io.ByteArrayInputStream;
// import org.junit.jupiter.api.Test;
//
// class NestedActCommentTest {
// @Test
// void example() {
// try (ByteArrayInputStream stream = new ByteArrayInputStream(new byte[] { 1 })) {
// // Act
// int value = read(stream);
// }
//
// assertThat(true).isTrue();
// }
//
// private int read(ByteArrayInputStream stream) {
// return stream.read();
// }
// }
// """;
// ActPatternChecker checker = checkerFor(nestedActPath, source);
//
// // Act
// var violations = checker.collectViolationsByType(ActPatternChecker.ViolationType.ACT_ASSERTION);
//
// assertThat(violations).isEmpty();
// }
//
// @Test
// void banned_comment_is_reported_for_line_comment() throws Exception {
// Path bannedCommentPath = testPath("com/theodo/sample/BannedLineCommentTest.java");
// String source = """
// package com.theodo.sample;
//
// import org.junit.jupiter.api.Test;
//
// class BannedLineCommentTest {
// @Test
// void example() {
// // Given // ❌ expected line
// // Act
// doWork();
// }
// }
// """;
// ActPatternChecker checker = checkerFor(bannedCommentPath, source);
// int expectedLineNumber = expectedLineNumberFromSource(source);
//
// // Act
// var violations = checker.collectViolationsByType(ActPatternChecker.ViolationType.BANNED_COMMENT);
//
// assertSingleViolation(violations, bannedCommentPath, expectedLineNumber);
// }
//
// @Test
// void banned_comment_is_ignored_for_phrase_with_then() throws Exception {
// Path bannedCommentPath = testPath("com/theodo/sample/BannedThenPhraseCommentTest.java");
// String source = """
// package com.theodo.sample;
//
// import org.junit.jupiter.api.Test;
//
// class BannedThenPhraseCommentTest {
// @Test
// void example() {
// // Create then delete
// // Act
// doWork();
// }
// }
// """;
// ActPatternChecker checker = checkerFor(bannedCommentPath, source);
//
// // Act
// var violations = checker.collectViolationsByType(ActPatternChecker.ViolationType.BANNED_COMMENT);
//
// assertThat(violations).isEmpty();
// }
//
// @Test
// void banned_comment_is_ignored_for_block_comment() throws Exception {
// Path bannedCommentPath = testPath("com/theodo/sample/BannedBlockCommentTest.java");
// String source = """
// package com.theodo.sample;
//
// import org.junit.jupiter.api.Test;
//
// class BannedBlockCommentTest {
// @Test
// void example() {
// /*
// * When doing work
// */
// // Act
// doWork();
// }
// }
// """;
// ActPatternChecker checker = checkerFor(bannedCommentPath, source);
//
// // Act
// var violations = checker.collectViolationsByType(ActPatternChecker.ViolationType.BANNED_COMMENT);
//
// assertThat(violations).isEmpty();
// }
//
// @Test
// void spacing_before_act_is_reported() throws Exception {
// Path spacingPath = testPath("com/theodo/sample/SpacingBeforeActTest.java");
// String source = """
// package com.theodo.sample;
//
// import static org.assertj.core.api.Assertions.assertThat;
// import org.junit.jupiter.api.Test;
//
// class SpacingBeforeActTest {
// @Test
// void example() {
// int value = 1;
// // Act // ❌ expected line
// doWork();
// assertThat(value).isEqualTo(1);
// }
// }
// """;
// ActPatternChecker checker = checkerFor(spacingPath, source);
// int expectedLineNumber = expectedLineNumberFromSource(source);
//
// // Act
// var violations = checker.collectViolationsByType(ActPatternChecker.ViolationType.SPACING_BEFORE_ACT);
//
// assertSingleViolation(violations, spacingPath, expectedLineNumber);
// }
//
// @Test
// void spacing_after_act_is_reported() throws Exception {
// Path spacingPath = testPath("com/theodo/sample/SpacingAfterActTest.java");
// String source = """
// package com.theodo.sample;
//
// import static org.assertj.core.api.Assertions.assertThat;
// import org.junit.jupiter.api.Test;
//
// class SpacingAfterActTest {
// @Test
// void example() {
// // Act
// doWork(); // ❌ expected line
// assertThat(true).isTrue();
// }
// }
// """;
// ActPatternChecker checker = checkerFor(spacingPath, source);
// int expectedLineNumber = expectedLineNumberFromSource(source);
//
// // Act
// var violations = checker.collectViolationsByType(ActPatternChecker.ViolationType.SPACING_AFTER_ACT);
//
// assertSingleViolation(violations, spacingPath, expectedLineNumber);
// }
//
// @Test
// void parse_error_is_reported() throws Exception {
// Path brokenPath = testPath("com/theodo/sample/BrokenTest.java");
// String source = "# // ❌ expected line";
// ActPatternChecker checker = checkerFor(brokenPath, source);
// int expectedLineNumber = expectedLineNumberFromSource(source);
//
// // Act
// var violations = checker.collectViolationsByType(ActPatternChecker.ViolationType.PARSE_ERROR);
//
// assertSingleViolation(violations, brokenPath, expectedLineNumber);
// }
//
// @Test
// void record_syntax_is_not_reported_as_parse_error() throws Exception {
// Path recordPath = testPath("com/theodo/sample/RecordSyntaxTest.java");
// String source = """
// package com.theodo.sample;
//
// import org.junit.jupiter.api.Test;
//
// class RecordSyntaxTest {
// @Test
// void example() {
// // Act
// use(new Example("value"));
// }
//
// private void use(Example example) {
// }
//
// private record Example(String value) {
// }
// }
// """;
// ActPatternChecker checker = checkerFor(recordPath, source);
//
// // Act
// var violations = checker.collectViolationsByType(ActPatternChecker.ViolationType.PARSE_ERROR);
//
// assertThat(violations).isEmpty();
// }
//
// private ActPatternChecker checkerFor(Path path, String source) {
// return new ActPatternChecker(ioWithFile(path, source));
// }
//
// private void assertSingleViolation(
// ImmutableList<ActPatternViolation> violations,
// Path expectedPath,
// int expectedLineNumber) {
// assertThat(violations).singleElement()
// .returns(expectedPath, ActPatternViolation::path)
// .returns(expectedLineNumber, ActPatternViolation::line);
// }
//
// private int expectedLineNumberFromSource(String source) {
// List<Integer> expectedLineNumbers = expectedLineNumbersFromSource(source);
// return assertThat(expectedLineNumbers)
// .as("Expected exactly one expected line marker: %s", EXPECTED_LINE_MARKER)
// .singleElement()
// .actual();
// }
//
// private List<Integer> expectedLineNumbersFromSource(String source) {
// String[] lines = source.split("\\R", -1);
// List<Integer> expectedLineNumbers = new ArrayList<>();
// for (int index = 0; index < lines.length; index++) {
// if (lines[index].contains(EXPECTED_LINE_MARKER)) {
// expectedLineNumbers.add(index + 1);
// }
// }
// if (!expectedLineNumbers.isEmpty()) {
// return expectedLineNumbers;
// }
// throw new IllegalArgumentException("Missing expected line marker: " + EXPECTED_LINE_MARKER);
// }
// }
//
// private static InMemoryFileSystemAccess ioWithFile(Path path, String content) {
// return new InMemoryFileSystemAccess(Map.of(path, stripExpectedLineMarkers(content)));
// }
//
// private static String stripExpectedLineMarkers(String content) {
// return content.replaceAll("(?m)\\s*// ❌ expected line\\s*$", "");
// }
//
// private static Path testPath(String relativePath) {
// return TEST_ROOT.resolve(relativePath);
// }
// }
