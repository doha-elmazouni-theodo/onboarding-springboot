package com.theodo.springblueprint.testhelpers.junitextensions.querycount;

import static org.checkerframework.checker.nullness.util.NullnessUtil.castNonNull;
import static org.junit.jupiter.api.Assertions.*;

import com.theodo.springblueprint.common.utils.collections.Immutable;
import com.theodo.springblueprint.common.utils.collections.Mutable;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TransactionRequiredException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.impl.collector.Collectors2;
import org.hibernate.Session;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.stat.spi.StatisticsImplementor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.extension.*;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.opentest4j.AssertionFailedError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;

@SuppressWarnings("PMD.DisallowJUnitAssertions")
public class QueryCountingExtension
    implements
        BeforeAllCallback,
        AfterAllCallback,
        BeforeEachCallback,
        AfterEachCallback {

    // These ThreadLocal variables are used to communicate with SutMethodWrapBeanPostProcessor
    @SuppressWarnings("nullness")
    private static final ThreadLocal<ImmutableList<SutMethodQueryCount>> currentExpectedQueryCounts = new ThreadLocal<>();
    @SuppressWarnings("nullness")
    private static final ThreadLocal<String> currentTestMethod = new ThreadLocal<>();
    @SuppressWarnings("nullness")
    private static final ThreadLocal<MutableList<SutMethodQueryCount>> currentCounts = new ThreadLocal<>();

    @Nullable private ImmutableMap<String, ImmutableList<SutMethodQueryCount>> expectedQueryCountsByTestMethod;

    @Override
    public void beforeAll(ExtensionContext context) {
        ImmutableList<SutMethodQueryCount> data = getSutMethodQueryCounts(context);
        this.expectedQueryCountsByTestMethod = groupByTestMethod(data);
        currentExpectedQueryCounts.set(data);
    }

    @Override
    public void afterAll(ExtensionContext context) {
        currentExpectedQueryCounts.remove();
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        currentCounts.set(Mutable.list.of());
        currentTestMethod.set(getTestMethodName(context));
    }

    @Override
    public void afterEach(ExtensionContext context) {
        MutableList<SutMethodQueryCount> counts = currentCounts.get();
        currentCounts.remove();
        currentTestMethod.remove();
        if (context.getExecutionException().isPresent()) {
            return;
        }

        String testMethodName = getTestMethodName(context);
        ImmutableList<SutMethodQueryCount> expectedQueryCounts = castNonNull(this.expectedQueryCountsByTestMethod)
            .get(testMethodName);

        assertNotNull(
            expectedQueryCounts,
            """
            [%s] Test method '%s' has no expected query count defined.
            Add this line to the existing @%s annotation on your test class (adjust values):

            @%s(count = <changeme>, sutActMethod = "<changeme>", testMethod = "%s"),

            """.formatted(
                AssertQueryCount.class.getSimpleName(),
                testMethodName,
                AssertQueryCount.class.getSimpleName(),
                Expected.class.getSimpleName(),
                testMethodName
            )
        );

        for (SutMethodQueryCount expected : expectedQueryCounts) {
            SutMethodQueryCount sutMethodQueryCount = counts
                .asReversed() // to use the last execution count
                .detectOptional(c -> c.sutMethod().equals(expected.sutMethod()))
                .orElseThrow(
                    () -> new AssertionFailedError(
                        "[%s] Method '%s.%s' was expected to be called in test '%s' but it wasn't.".formatted(
                            AssertQueryCount.class.getSimpleName(),
                            expected.sutClass().getSimpleName(),
                            expected.sutMethod(),
                            expected.testMethod()
                        )
                    )
                );
            assertEquals(
                expected.queryCount(),
                sutMethodQueryCount.queryCount(),
                "Call '%s.%s' in test method '%s' should have done exactly %d queries, but did %d instead".formatted(
                    expected.sutClass().getSimpleName(),
                    expected.sutMethod(),
                    testMethodName,
                    expected.queryCount(),
                    sutMethodQueryCount.queryCount()
                )
            );
        }
    }

    private static ImmutableMap<String, ImmutableList<SutMethodQueryCount>> groupByTestMethod(
        ImmutableList<SutMethodQueryCount> data) {
        Map<String, ImmutableList<SutMethodQueryCount>> collect = data
            .stream()
            .collect(Collectors.groupingBy(d -> d.testMethod, Collectors2.toImmutableList()));
        return Immutable.map.ofMap(collect);
    }

    private static String getTestMethodName(ExtensionContext context) {
        String methodName = context.getRequiredTestMethod().getName();
        if (Objects.equals(context.getDisplayName(), methodName + "()")) {
            return methodName;
        }
        return methodName + " | " + context.getDisplayName();
    }

    private static ImmutableList<SutMethodQueryCount> getSutMethodQueryCounts(ExtensionContext context) {
        AssertQueryCount annotation = castNonNull(findAssertQueryCountAnnotation(context));
        ImmutableList<SutMethodQueryCount> data = Immutable.collectList(
            annotation.value(),
            expected -> getSutMethodQueryCount(expected, annotation)
        );
        checkForDuplications(data);
        return data;
    }

    @Nullable private static AssertQueryCount findAssertQueryCountAnnotation(ExtensionContext context) {
        ExtensionContext current = context;
        while (true) {
            Class<?> testClass = current.getTestClass().orElse(null);
            if (testClass != null) {
                AssertQueryCount annotation = testClass.getAnnotation(AssertQueryCount.class);
                if (annotation != null) {
                    return annotation;
                }
            }
            ExtensionContext parent = current.getParent().orElse(null);
            if (parent == null) {
                return null;
            }
            current = parent;
        }
    }

    private static void checkForDuplications(ImmutableList<SutMethodQueryCount> data) {
        Map<List<String>, List<SutMethodQueryCount>> collect = data
            .stream()
            .collect(Collectors.groupingBy(d -> List.of(d.sutMethod(), d.testMethod())));
        List<List<String>> duplicates = collect.keySet().stream().filter(k -> collect.get(k).size() > 1).toList();
        if (duplicates.isEmpty()) {
            return;
        }

        fail(
            "[%s] Duplicated values found. Key is [sutActMethod, testMethod]: \n  - %s".formatted(
                AssertQueryCount.class.getSimpleName(),
                duplicates.stream().map(d -> String.join(", ", d)).collect(Collectors.joining("\n  - "))
            )
        );
    }

    private static SutMethodQueryCount getSutMethodQueryCount(Expected expected, AssertQueryCount annotation) {
        return new SutMethodQueryCount(
            annotation.sutClass(),
            expected.sutActMethod(),
            expected.testMethod(),
            expected.count()
        );
    }

    private record SutMethodQueryCount(Class<?> sutClass, String sutMethod, String testMethod, int queryCount) {
    }

    @TestConfiguration
    static class SutMethodWrapBeanPostProcessor implements BeanPostProcessor {

        private static final Logger log = LoggerFactory.getLogger("QueryCounter");

        private final ApplicationContext context;
        private final ImmutableMap<String, String> methodsToSpyOn;
        private final Class<?> sutClass;

        public SutMethodWrapBeanPostProcessor(ApplicationContext context) {
            ImmutableList<SutMethodQueryCount> sutMethodQueryCounts = currentExpectedQueryCounts.get();
            sutClass = sutMethodQueryCounts.getAny().sutClass();
            methodsToSpyOn = sutMethodQueryCounts.toImmutableMap(
                SutMethodQueryCount::testMethod,
                SutMethodQueryCount::sutMethod
            );
            this.context = context;
        }

        @Override
        public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName) {
            if (!shouldSpy(bean)) {
                return bean;
            }
            return Mockito.mock(
                bean.getClass(),
                Mockito.withSettings()
                    .spiedInstance(bean)
                    .defaultAnswer(invocation -> {
                        String testMethod = currentTestMethod.get();
                        Method method = invocation.getMethod();
                        String sutMethod = method.getName();
                        if (!shouldSpy(testMethod, sutMethod)) {
                            return invocation.callRealMethod();
                        }
                        EntityManager entityManager = context.getBean(EntityManager.class);
                        flushAndClearCache(entityManager);
                        StatisticsImplementor statisticsImplementor = resetCounters(entityManager);
                        log.debug("\n\n\n=============== Start of: {}.{}", sutClass.getSimpleName(), sutMethod);
                        Throwable thrownException = null;
                        try {
                            return invocation.callRealMethod();
                        } catch (Throwable ex) {
                            thrownException = ex;
                            throw ex;
                        } finally {
                            if (thrownException == null || !isRelatedToHibernate(thrownException)) {
                                safeFlush(entityManager);
                            }
                            log.debug("\n=============== End of: {}.{}\n\n", sutClass.getSimpleName(), sutMethod);
                            addExecutionData(invocation, testMethod, statisticsImplementor);
                        }
                    })
            );
        }

        private static void safeFlush(EntityManager entityManager) {
            try {
                entityManager.flush();
            } catch (TransactionRequiredException e) {
                log.warn("Non-blocking error while flushing:", e);
            }
        }

        private static boolean isRelatedToHibernate(Throwable ex) {
            Throwable current = ex;
            while (true) {
                if (current.getClass().getName().contains(".hibernate.")) {
                    return true;
                }
                if (current.getCause() == null || current.getCause() == current) {
                    break;
                }
                current = current.getCause();
            }
            return false;
        }

        private static void flushAndClearCache(EntityManager entityManager) {
            safeFlush(entityManager);
            entityManager.clear();
            entityManager.getEntityManagerFactory().getCache().evictAll();
        }

        private void addExecutionData(
            InvocationOnMock invocation,
            String testMethod,
            StatisticsImplementor statisticsImplementor) {
            currentCounts
                .get()
                .add(
                    new SutMethodQueryCount(
                        invocation.getMethod().getDeclaringClass(),
                        invocation.getMethod().getName(),
                        testMethod,
                        (int) statisticsImplementor.getPrepareStatementCount()
                    )
                );
        }

        private boolean shouldSpy(Object bean) {
            if (sutClass.isInterface()) {
                return sutClass.isAssignableFrom(bean.getClass());
            }
            return (sutClass.equals(bean.getClass()) || sutClass.equals(AopUtils.getTargetClass(bean)));
        }

        private boolean shouldSpy(String testMethod, String sutMethod) {
            String matchingSutMethod = methodsToSpyOn.get(testMethod);
            return matchingSutMethod != null && matchingSutMethod.contains(sutMethod);
        }

        private static StatisticsImplementor resetCounters(EntityManager entityManager) {
            Session session = entityManager.unwrap(Session.class);
            SessionFactoryImplementor sessionFactory = session
                .getSessionFactory()
                .unwrap(SessionFactoryImplementor.class);
            StatisticsImplementor statistics = sessionFactory.getStatistics();
            statistics.setStatisticsEnabled(true);
            statistics.clear();
            return statistics;
        }
    }
}
