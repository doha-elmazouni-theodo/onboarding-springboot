package com.theodo.springblueprint.features.authentication.api.schedules.cleanupsessions;

import com.theodo.springblueprint.features.authentication.domain.usecases.purgerefreshtokens.PurgeRefreshTokensUseCase;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

// If there are many Scheduled tasks, it may be cleaner to move them to a new top level package
// beside api.
@Component
public class CleanUpUserSessionsScheduledTask {

    private final PurgeRefreshTokensUseCase purgeRefreshTokensUseCase;

    public CleanUpUserSessionsScheduledTask(final PurgeRefreshTokensUseCase purgeRefreshTokensUseCase) {
        this.purgeRefreshTokensUseCase = purgeRefreshTokensUseCase;
    }

    @Scheduled(cron = "${security.removeExpiredRefreshTokensCron}")
    public void removeExpiredRefreshToken() {
        purgeRefreshTokensUseCase.handle();
    }
}
