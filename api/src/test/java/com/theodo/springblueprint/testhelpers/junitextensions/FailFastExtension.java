package com.theodo.springblueprint.testhelpers.junitextensions;

import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.opentest4j.TestAbortedException;

public class FailFastExtension implements TestExecutionExceptionHandler, BeforeTestExecutionCallback {

    private boolean failed = false;

    @Override
    public void beforeTestExecution(ExtensionContext context) {
        if (failed) {
            throw new TestAbortedException("Skipping test due to previous failure");
        }
    }

    @Override
    public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
        failed = true;
        throw throwable;
    }
}
