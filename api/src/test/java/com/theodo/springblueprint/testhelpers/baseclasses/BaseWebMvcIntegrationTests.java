package com.theodo.springblueprint.testhelpers.baseclasses;

import static com.theodo.springblueprint.testhelpers.helpers.Mapper.toUserPrincipal;
import static com.theodo.springblueprint.testhelpers.utils.JsonUtils.jsonIgnoreArrayOrder;
import static com.theodo.springblueprint.testhelpers.utils.StringUtils.urlEncode;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import com.theodo.springblueprint.Application;
import com.theodo.springblueprint.common.api.LoggingEventListener;
import com.theodo.springblueprint.common.api.beans.ObjectMapperConfiguration;
import com.theodo.springblueprint.common.api.security.StandaloneJwtAuthentication;
import com.theodo.springblueprint.common.api.security.WebSecurityConfiguration;
import com.theodo.springblueprint.common.domain.ports.TimeProviderPort;
import com.theodo.springblueprint.common.infra.adapters.SpringEventPublisher;
import com.theodo.springblueprint.common.infra.adapters.fakes.FakeTokenClaimsCodec;
import com.theodo.springblueprint.features.authentication.domain.entities.UserPrincipal;
import com.theodo.springblueprint.features.authentication.domain.properties.TokenProperties;
import com.theodo.springblueprint.features.authentication.domain.usecases.helpers.UserTokensHelpers;
import com.theodo.springblueprint.features.authentication.domain.valueobjects.AccessToken;
import com.theodo.springblueprint.features.users.domain.entities.User;
import com.theodo.springblueprint.testhelpers.baseclasses.BaseWebMvcIntegrationTests.WebMvcIntegrationTestConfiguration;
import com.theodo.springblueprint.testhelpers.configurations.PropertiesTestConfiguration;
import com.theodo.springblueprint.testhelpers.configurations.TimeTestConfiguration;
import com.theodo.springblueprint.testhelpers.utils.BeanUtils;
import com.theodo.springblueprint.testhelpers.utils.ClearableProxiedThreadScope;
import jakarta.servlet.http.Cookie;
import lombok.Getter;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.beans.factory.config.*;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

@Import(
    {
        WebSecurityConfiguration.class,
        StandaloneJwtAuthentication.class,
        FakeTokenClaimsCodec.class,
        TimeTestConfiguration.class,
        PropertiesTestConfiguration.class,
        LoggingEventListener.class,
        WebMvcIntegrationTestConfiguration.class,
        ObjectMapperConfiguration.class,
    }
)
@ExtendWith(BaseWebMvcIntegrationTests.WebMvcIntegrationTestExtension.class)
@Getter
public abstract class BaseWebMvcIntegrationTests implements UserTokensHelpers {

    private static final String SCOPE_NAME = "perCall";
    private static final ClearableProxiedThreadScope SCOPE = new ClearableProxiedThreadScope();

    protected final MockMvc mockMvc;
    private final TimeProviderPort timeProvider;
    private final FakeTokenClaimsCodec tokenClaimsCodec;
    private final TokenProperties tokenProperties;

    protected record BaseWebMvcDependencies(
        MockMvc mockMvc,
        TimeProviderPort timeProvider,
        FakeTokenClaimsCodec tokenClaimsCodec,
        TokenProperties tokenProperties
    ) {
    }

    protected BaseWebMvcIntegrationTests(BaseWebMvcDependencies baseWebMvcDependencies) {
        this.mockMvc = baseWebMvcDependencies.mockMvc();
        this.timeProvider = baseWebMvcDependencies.timeProvider();
        this.tokenClaimsCodec = baseWebMvcDependencies.tokenClaimsCodec();
        this.tokenProperties = baseWebMvcDependencies.tokenProperties();
    }

    protected Cookie getAccessTokenCookie(User user) {
        return getAccessTokenCookie(toUserPrincipal(user));
    }

    protected Cookie getAccessTokenCookie(UserPrincipal userPrincipal) {
        AccessToken accessToken = newAccessToken(userPrincipal);
        return new Cookie("accessToken", urlEncode(accessToken.value()));
    }

    protected ResultMatcher jsonStrictArrayOrder(String jsonContent) {
        return content().json(jsonContent, JsonCompareMode.STRICT);
    }

    protected ResultMatcher jsonIgnoreArrayOrder(String jsonContent) {
        return content().json(jsonContent, jsonIgnoreArrayOrder);
    }

    protected static ResultMatcher noContent() {
        return content().string("");
    }

    static final class WebMvcIntegrationTestExtension implements AfterEachCallback {

        @Override
        public void afterEach(ExtensionContext context) {
            SCOPE.clear();
        }
    }

    @TestConfiguration
    static class WebMvcIntegrationTestConfiguration implements BeanFactoryPostProcessor {

        @Bean
        BaseWebMvcDependencies baseWebMvcDependencies(MockMvc mockMvc, TimeProviderPort timeProviderPort,
            FakeTokenClaimsCodec tokenClaimsCodec,
            TokenProperties tokenProperties) {
            return new BaseWebMvcDependencies(mockMvc, timeProviderPort, tokenClaimsCodec, tokenProperties);
        }

        @Bean
        @Primary
        public SpringEventPublisher springEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
            return new SpringEventPublisher(applicationEventPublisher);
        }

        @Override
        public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
            beanFactory.registerScope(SCOPE_NAME, SCOPE);
            for (String bean : beanFactory.getBeanDefinitionNames()) {
                BeanDefinition beanDefinition = beanFactory.getBeanDefinition(bean);
                String beanClassName = BeanUtils.getBeanClassName(beanDefinition);
                if (beanClassName != null && beanClassName.startsWith(Application.BASE_PACKAGE_NAME)) {
                    beanDefinition.setScope(SCOPE_NAME);
                }
            }
        }
    }
}
