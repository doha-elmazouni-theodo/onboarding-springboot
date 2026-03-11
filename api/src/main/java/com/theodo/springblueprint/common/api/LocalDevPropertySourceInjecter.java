package com.theodo.springblueprint.common.api;

import java.util.List;
import java.util.Map;
import lombok.Generated;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.ClassUtils;

@Slf4j
@Generated // "The ROI to cover this class is very low. It's only used for local development."
public class LocalDevPropertySourceInjecter implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    private static final String DEVTOOLS_CLASS_PATH = "org.springframework.boot.devtools.restart.Restarter";

    @Override
    public void onApplicationEvent(@NotNull final ApplicationEnvironmentPreparedEvent event) {
        if (!isDevToolsInClassPath()) {
            return;
        }
        event.getEnvironment().getPropertySources().addFirst(getLocalDevPropertySource());
        log.info("Local development PropertySource successfully injected");
    }

    private static boolean isDevToolsInClassPath() {
        return ClassUtils.isPresent(DEVTOOLS_CLASS_PATH, null);
    }

    @SneakyThrows
    private static PropertySource<?> getLocalDevPropertySource() {
        final YamlPropertySourceLoader loader = new YamlPropertySourceLoader();
        final Resource path = new ClassPathResource("application-localdev.yml");
        final String propertySourceName = "Local development config resource";
        final List<PropertySource<?>> propertySources = loader.load(propertySourceName, path);
        return propertySources.isEmpty()
            ? new MapPropertySource(propertySourceName, Map.of())
            : propertySources.getFirst();
    }
}
