package com.theodo.springblueprint.features.authentication.api.schedules.cleanupsessions;

import static org.mockito.Mockito.*;

import com.theodo.springblueprint.features.authentication.domain.usecases.purgerefreshtokens.PurgeRefreshTokensUseCase;
import com.theodo.springblueprint.testhelpers.baseclasses.BaseScheduledTaskIntegrationTests;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@Import(CleanUpUserSessionsScheduledTask.class)
class CleanUpUserSessionsScheduledTaskIntegrationTests extends BaseScheduledTaskIntegrationTests {

    @MockitoBean
    private final PurgeRefreshTokensUseCase useCase;

    CleanUpUserSessionsScheduledTaskIntegrationTests(PurgeRefreshTokensUseCase useCase) {
        super();
        this.useCase = useCase;
    }

    // for this use case, we only need to make sure
    // that the handle method is called.
    @Test
    void useCase_is_called() {
        verify(useCase, times(1)).handle();
    }
}
