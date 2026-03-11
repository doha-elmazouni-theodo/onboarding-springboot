package com.theodo.springblueprint.testhelpers.baseclasses;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import jakarta.annotation.Nullable;
import lombok.SneakyThrows;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.support.NoOpTaskScheduler;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@Import(BaseScheduledTaskIntegrationTests.ImmediateOneTimeTaskScheduler.class)
@ContextConfiguration(initializers = BaseScheduledTaskIntegrationTests.CustomPropertySourceInitializer.class)
@EnableScheduling
public abstract class BaseScheduledTaskIntegrationTests {

    // this scheduler will execute once any scheduled method immediately after discovery
    static class ImmediateOneTimeTaskScheduler extends NoOpTaskScheduler {

        @Override
        @Nullable public ScheduledFuture<?> schedule(Runnable task, @NotNull Trigger trigger) {
            task.run();
            return null;
        }
    }

    // read application.yml in case the schedule trigger is defined with a property
    static class CustomPropertySourceInitializer
        implements
            ApplicationContextInitializer<ConfigurableApplicationContext> {

        @SneakyThrows
        @Override
        public void initialize(@NotNull ConfigurableApplicationContext context) {
            final YamlPropertySourceLoader loader = new YamlPropertySourceLoader();
            addDefaultPropertySource(loader, context);
        }

        private static void addDefaultPropertySource(
            YamlPropertySourceLoader loader,
            ConfigurableApplicationContext context) throws IOException {
            final Resource resource = new ClassPathResource("application.yml");
            final List<PropertySource<?>> propertySources = loader.load("application.yml", resource);
            context.getEnvironment().getPropertySources().addFirst(propertySources.getFirst());
        }
    }
}
