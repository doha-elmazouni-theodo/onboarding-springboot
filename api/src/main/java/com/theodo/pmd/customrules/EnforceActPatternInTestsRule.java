package com.theodo.pmd.customrules;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import net.sourceforge.pmd.lang.Language;
import net.sourceforge.pmd.lang.LanguageRegistry;
import net.sourceforge.pmd.lang.LanguageVersion;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.ast.NodeStream;
import net.sourceforge.pmd.lang.document.Chars;
import net.sourceforge.pmd.lang.document.FileLocation;
import net.sourceforge.pmd.lang.java.ast.ASTAnnotation;
import net.sourceforge.pmd.lang.java.ast.ASTAssignmentExpression;
import net.sourceforge.pmd.lang.java.ast.ASTBlock;
import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit;
import net.sourceforge.pmd.lang.java.ast.ASTConstructorCall;
import net.sourceforge.pmd.lang.java.ast.ASTExpression;
import net.sourceforge.pmd.lang.java.ast.ASTExpressionStatement;
import net.sourceforge.pmd.lang.java.ast.ASTLocalVariableDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTMethodCall;
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTStatement;
import net.sourceforge.pmd.lang.java.ast.ASTStringLiteral;
import net.sourceforge.pmd.lang.java.ast.ASTTypeDeclaration;
import net.sourceforge.pmd.lang.java.ast.JavaComment;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import net.sourceforge.pmd.lang.rule.RulePriority;
import net.sourceforge.pmd.reporting.RuleContext;
import org.jspecify.annotations.Nullable;

import static org.checkerframework.checker.nullness.util.NullnessUtil.castNonNull;

@SuppressWarnings({ "PMD.CouplingBetweenObjects", "PMD.GodClass", "PMD.TooManyMethods" })
public final class EnforceActPatternInTestsRule extends AbstractJavaRule {

    private static final String RULE_NAME = "EnforceActPatternInTests";

    private static final String ACT_COMMENT = "Act";
    private static final String FLOW_APPLICATION_TESTS_SUFFIX = "FlowApplicationTests";
    private static final String SCHEDULED_TASK_INTEGRATION_TESTS_SUFFIX = "ScheduledTaskIntegrationTests";
    private static final Pattern BANNED_COMMENT_PATTERN = Pattern.compile("\\b(Given|When|Then|Arrange|Assert)\\b");

    public EnforceActPatternInTestsRule() {
        super();
        Language javaLanguage = castNonNull(LanguageRegistry.PMD.getLanguageById("java"));
        LanguageVersion javaVersion = javaLanguage.getLatestVersion();
        setLanguage(javaLanguage);
        setMinimumLanguageVersion(javaVersion);
        setName(RULE_NAME);
        setMessage("Enforce Act pattern in tests.");
        setPriority(RulePriority.HIGH);
    }

    @Override
    public Object visit(ASTCompilationUnit node, @Nullable Object data) {
        RuleContext context = asCtx(castNonNull(data));
        checkCompilationUnit(node, context);
        return data;
    }

    private void checkCompilationUnit(ASTCompilationUnit compilationUnit, RuleContext context) {
        List<String> lines = collectLines(compilationUnit);
        List<LineComment> lineComments = collectLineComments(compilationUnit);
        NodeStream<ASTMethodDeclaration> methods = compilationUnit.descendants(ASTTypeDeclaration.class)
            .flatMap(type -> type.getDeclarations(ASTMethodDeclaration.class));
        methods.forEach(method -> checkMethod(method, compilationUnit, context, lines, lineComments));
    }

    private void checkMethod(
        ASTMethodDeclaration method,
        ASTCompilationUnit compilationUnit,
        RuleContext context,
        List<String> lines,
        List<LineComment> lineComments) {
        if (!isTestMethod(method)) {
            return;
        }
        ASTBlock body = method.getBody();
        if (body == null) {
            return;
        }
        ASTTypeDeclaration reportType = findTopLevelType(method);
        if (isIgnoredTestClass(reportType)) {
            return;
        }
        Node reportNode = isSuppressed(method) ? method : reportType;
        List<LineComment> methodComments = filterCommentsInMethodBody(lineComments, body);
        MethodContext methodContext = new MethodContext(compilationUnit, context, reportNode, body);
        reportBannedComments(methodComments, methodContext);

        List<LineComment> actComments = findActComments(methodComments);
        if (actComments.isEmpty()) {
            reportViolation(methodContext, method.getReportLocation(), ViolationType.MISSING_ACT);
            return;
        }
        checkActComments(methodContext, lines, actComments);
    }

    private void checkActComments(
        MethodContext methodContext,
        List<String> lines,
        List<LineComment> actComments) {
        List<ASTStatement> methodStatements = methodContext.body().descendants(ASTStatement.class).toList();
        for (int i = 0; i < actComments.size(); i++) {
            LineComment actComment = actComments.get(i);
            if (i > 0) {
                reportCommentViolation(methodContext, actComment, ViolationType.MULTIPLE_ACT);
            }
            checkActComment(methodContext, lines, actComment, methodStatements);
        }
    }

    private void checkActComment(
        MethodContext methodContext,
        List<String> lines,
        LineComment actComment,
        List<ASTStatement> methodStatements) {
        int actLine = actComment.line();
        ASTBlock actBlock = findInnermostBlock(methodContext.body(), actLine);
        List<ASTStatement> blockStatements = actBlock.descendants(ASTStatement.class).toList();
        if (hasStatementBefore(blockStatements, actLine) && !isBlankLine(lines, actLine - 1)) {
            reportCommentViolation(methodContext, actComment, ViolationType.SPACING_BEFORE_ACT);
        }
        ASTStatement actStatement = nextStatementAfter(actBlock, actLine);
        if (actStatement == null) {
            reportCommentViolation(methodContext, actComment, ViolationType.MISSING_ACT_STATEMENT);
            return;
        }
        FileLocation actLocation = actStatement.getReportLocation();
        if (isAssertionStatement(actStatement)) {
            reportViolation(methodContext, actLocation, ViolationType.ACT_ASSERTION);
        } else if (!isAllowedActStatement(actStatement)) {
            reportViolation(methodContext, actLocation, ViolationType.ACT_STATEMENT_NOT_CALL);
        }
        int actEndLine = actStatement.getLastToken().getReportLocation().getEndLine();
        boolean hasStatementAfterAct = hasStatementAfter(methodStatements, actEndLine);
        if (!hasStatementAfterAct) {
            reportViolation(methodContext, actLocation, ViolationType.MISSING_POST_ACT_STATEMENT);
            return;
        }
        boolean hasStatementAfterInBlock = hasStatementAfter(blockStatements, actEndLine);
        if (hasStatementAfterInBlock && !isBlankLine(lines, actEndLine + 1)) {
            reportViolation(methodContext, actLocation, ViolationType.SPACING_AFTER_ACT);
        }
    }

    private boolean isTestMethod(ASTMethodDeclaration method) {
        List<String> annotationNames = method.getDeclaredAnnotations()
            .toList(ASTAnnotation::getSimpleName);
        return !annotationNames.contains("TestFactory")
            && annotationNames.stream().anyMatch(this::isTestAnnotationName);
    }

    private boolean isTestAnnotationName(String annotationName) {
        return switch (annotationName) {
            case "ParameterizedTest", "RepeatedTest", "Test", "TestTemplate" -> true;
            default -> false;
        };
    }

    private boolean isIgnoredTestClass(ASTTypeDeclaration type) {
        String typeName = type.getSimpleName();
        return typeName.endsWith(FLOW_APPLICATION_TESTS_SUFFIX)
            || typeName.endsWith(SCHEDULED_TASK_INTEGRATION_TESTS_SUFFIX);
    }

    private ASTTypeDeclaration findTopLevelType(ASTMethodDeclaration method) {
        return method.ancestors(ASTTypeDeclaration.class)
            .filter(ASTTypeDeclaration::isTopLevel)
            .firstOpt()
            .orElseThrow();
    }

    private boolean isSuppressed(ASTMethodDeclaration method) {
        return hasSuppression(method.getDeclaredAnnotations())
            || method.ancestors(ASTTypeDeclaration.class)
                .any(type -> hasSuppression(type.getDeclaredAnnotations()));
    }

    private boolean hasSuppression(NodeStream<ASTAnnotation> annotations) {
        return annotations.any(this::isSuppressWarningsForRule);
    }

    private boolean isSuppressWarningsForRule(ASTAnnotation annotation) {
        return "SuppressWarnings".equals(annotation.getSimpleName())
            && annotation.getFlatValues()
                .filterIs(ASTStringLiteral.class)
                .any(value -> "PMD.EnforceActPatternInTests".equals(value.getConstValue()));
    }

    private void reportBannedComments(List<LineComment> methodComments, MethodContext methodContext) {
        for (LineComment comment : methodComments) {
            if (ACT_COMMENT.equals(comment.text())) {
                continue;
            }
            if (BANNED_COMMENT_PATTERN.matcher(comment.text()).find()) {
                reportCommentViolation(methodContext, comment, ViolationType.BANNED_COMMENT);
            }
        }
    }

    private List<LineComment> findActComments(List<LineComment> methodComments) {
        return methodComments.stream()
            .filter(comment -> ACT_COMMENT.equals(comment.text()))
            .sorted(Comparator.comparingInt(LineComment::line))
            .toList();
    }

    private boolean isAllowedActStatement(ASTStatement statement) {
        return (statement instanceof ASTExpressionStatement expressionStatement
            && isAllowedExpressionStatement(expressionStatement.getExpr()))
            || (statement instanceof ASTLocalVariableDeclaration declaration
                && isAllowedVariableDeclaration(declaration));
    }

    private boolean isAllowedExpressionStatement(ASTExpression expression) {
        if (expression instanceof ASTAssignmentExpression assignmentExpression) {
            return isCallExpression(assignmentExpression.getRightOperand());
        }
        return !expression.descendants(ASTAssignmentExpression.class).nonEmpty()
            && expression.descendantsOrSelf()
                .filterIs(ASTMethodCall.class)
                .nonEmpty();
    }

    private boolean isAllowedVariableDeclaration(ASTLocalVariableDeclaration declaration) {
        return declaration.descendants(ASTMethodCall.class).nonEmpty()
            || declaration.descendants(ASTConstructorCall.class).nonEmpty();
    }

    private boolean isCallExpression(ASTExpression expression) {
        return expression instanceof ASTMethodCall
            || expression instanceof ASTConstructorCall
            || expression.descendants(ASTMethodCall.class).nonEmpty()
            || expression.descendants(ASTConstructorCall.class).nonEmpty();
    }

    private boolean isAssertionStatement(ASTStatement statement) {
        return statement instanceof ASTExpressionStatement expressionStatement
            && hasDisallowedAssertionCall(expressionStatement.getExpr());
    }

    private boolean hasDisallowedAssertionCall(ASTExpression expression) {
        return expression.descendantsOrSelf()
            .filterIs(ASTMethodCall.class)
            .any(call -> isDisallowedAssertionName(call.getMethodName()));
    }

    private boolean isDisallowedAssertionName(String methodName) {
        return "assertThat".equals(methodName)
            || (!methodName.startsWith("assertThat") && methodName.startsWith("assert"));
    }

    private boolean hasStatementBefore(List<ASTStatement> statements, int line) {
        return statements.stream().anyMatch(statement -> statement.getBeginLine() < line);
    }

    private boolean hasStatementAfter(List<ASTStatement> statements, int line) {
        return statements.stream().anyMatch(statement -> statement.getBeginLine() > line);
    }

    private ASTBlock findInnermostBlock(ASTBlock methodBody, int line) {
        return methodBody.descendantsOrSelf()
            .filterIs(ASTBlock.class)
            .toStream()
            .filter(block -> line >= block.getBeginLine() && line <= block.getEndLine())
            .min(Comparator.comparingInt(block -> block.getEndLine() - block.getBeginLine()))
            .orElse(methodBody);
    }

    private @Nullable ASTStatement nextStatementAfter(ASTBlock block, int line) {
        return block.descendants(ASTStatement.class)
            .toStream()
            .filter(statement -> statement.getBeginLine() > line)
            .min(Comparator.comparingInt(ASTStatement::getBeginLine))
            .orElse(null);
    }

    private boolean isBlankLine(List<String> lines, int line) {
        return lines.get(line - 1).trim().isEmpty();
    }

    private List<String> collectLines(ASTCompilationUnit compilationUnit) {
        List<String> lines = new ArrayList<>();
        for (Chars line : compilationUnit.getTextDocument().getText().lines()) {
            lines.add(line.toString());
        }
        return lines;
    }

    private List<LineComment> collectLineComments(ASTCompilationUnit compilationUnit) {
        List<LineComment> lineComments = new ArrayList<>();
        for (JavaComment comment : compilationUnit.getComments()) {
            LineComment lineComment = toLineComment(comment);
            if (lineComment != null) {
                lineComments.add(lineComment);
            }
        }
        return lineComments;
    }

    private @Nullable LineComment toLineComment(JavaComment comment) {
        String rawText = comment.getText().toString().trim();
        if (!rawText.startsWith("//")) {
            return null;
        }
        return new LineComment(comment.getReportLocation().getStartLine(), rawText.substring(2).trim());
    }

    private List<LineComment> filterCommentsInMethodBody(List<LineComment> comments, ASTBlock body) {
        int startLine = body.getBeginLine();
        int endLine = body.getEndLine();
        return comments.stream()
            .filter(comment -> comment.line() >= startLine && comment.line() <= endLine)
            .toList();
    }

    private void reportCommentViolation(
        ASTCompilationUnit compilationUnit,
        RuleContext context,
        Node reportNode,
        LineComment comment,
        ViolationType violationType) {
        FileLocation location = FileLocation.caret(
            compilationUnit.getTextDocument().getFileId(),
            comment.line(),
            1
        );
        reportViolation(context, reportNode, location, violationType);
    }

    private void reportCommentViolation(
        MethodContext methodContext,
        LineComment comment,
        ViolationType violationType) {
        reportCommentViolation(
            methodContext.compilationUnit(),
            methodContext.context(),
            methodContext.reportNode(),
            comment,
            violationType
        );
    }

    private void reportViolation(
        RuleContext context,
        Node reportNode,
        FileLocation location,
        ViolationType violationType) {
        context.addViolationWithPosition(
            reportNode,
            reportNode.getAstInfo(),
            location,
            violationType.message
        );
    }

    private void reportViolation(MethodContext methodContext, FileLocation location, ViolationType violationType) {
        reportViolation(methodContext.context(), methodContext.reportNode(), location, violationType);
    }

    private record MethodContext(
        ASTCompilationUnit compilationUnit,
        RuleContext context,
        Node reportNode,
        ASTBlock body
    ) {
    }

    private record LineComment(int line, String text) {
    }

    public enum ViolationType {
        ACT_ASSERTION("Act statement cannot be an assertion."),
        ACT_STATEMENT_NOT_CALL("Act statement must be a call or assignment."),
        BANNED_COMMENT("Do not use Given/When/Then/Arrange/Assert comments; only Act is allowed."),
        MISSING_ACT("Missing // Act comment."),
        MISSING_ACT_STATEMENT("Missing act statement after // Act."),
        MISSING_POST_ACT_STATEMENT("Missing statement after act statement."),
        MULTIPLE_ACT("Only one // Act comment is allowed."),
        PARSE_ERROR("Parse error."),
        SPACING_AFTER_ACT("Blank line required after act statement."),
        SPACING_BEFORE_ACT("Blank line required before // Act.");

        private final String message;

        ViolationType(String message) {
            this.message = message;
        }

        public String message() {
            return message;
        }
    }
}
