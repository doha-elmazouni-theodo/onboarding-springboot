package com.theodo.springblueprint.testhelpers.junitextensions;

import com.theodo.springblueprint.Application;
import com.theodo.springblueprint.common.utils.collections.Immutable;
import com.theodo.springblueprint.testhelpers.utils.BeanBag;
import com.theodo.springblueprint.testhelpers.utils.Instance;
import jakarta.annotation.Nullable;
import org.eclipse.collections.api.set.ImmutableSet;
import org.junit.jupiter.api.extension.*;

public final class InstanceParameterResolver implements ParameterResolver, AfterEachCallback {

    @Nullable private BeanBag currentBeanBag = null;

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return parameterContext.getParameter().getType().getName().startsWith(Application.BASE_PACKAGE_NAME);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return getBeanBag(parameterContext).get(parameterContext.getParameter().getType());
    }

    @Override
    public void afterEach(ExtensionContext context) {
        currentBeanBag = null;
    }

    private BeanBag getBeanBag(ParameterContext parameterContext) {
        if (currentBeanBag == null) {
            ImmutableSet<Class<?>> classes = Immutable.set
                .of(parameterContext.getDeclaringExecutable().getParameterTypes());
            currentBeanBag = Instance.createBeanBag(classes);
        }
        return currentBeanBag;
    }
}
