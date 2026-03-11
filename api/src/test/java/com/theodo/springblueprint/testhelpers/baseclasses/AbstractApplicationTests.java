package com.theodo.springblueprint.testhelpers.baseclasses;

import com.theodo.springblueprint.testhelpers.utils.CookieStore;
import jakarta.annotation.PostConstruct;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.UseMainMethod;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.web.servlet.client.RestTestClient;

import static org.checkerframework.checker.nullness.util.NullnessUtil.castNonNull;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, useMainMethod = UseMainMethod.ALWAYS)
public abstract class AbstractApplicationTests {

    @LocalServerPort
    private int port;

    @Value("${server.servlet.context-path}")
    @Nullable private String contextPath;

    @Nullable private String baseUrl;

    protected AbstractApplicationTests() {
    }

    public RestTestClient buildSessionRestTestClient() {
        return RestTestClient.bindToServer()
            .baseUrl(baseUrl())
            .requestInterceptor(new CookieStore().requestInterceptor())
            .build();
    }

    protected String baseUrl() {
        return castNonNull(baseUrl);
    }

    protected String contextPath() {
        return castNonNull(contextPath);
    }

    @PostConstruct
    private void postConstruct() {
        this.baseUrl = "http://localhost:%d%s".formatted(port, contextPath());
    }
}
