package com.theodo.springblueprint.testhelpers.helpers;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.theodo.springblueprint.common.utils.collections.Immutable;
import org.eclipse.collections.api.list.ImmutableList;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentLinkedQueue;

public class InMemoryLogs {
    private final InMemoryLogbackAppender inMemoryLogbackAppender;

    public InMemoryLogs() {
        inMemoryLogbackAppender = new InMemoryLogbackAppender();
    }

    public void start() {
        inMemoryLogbackAppender.clear();
        inMemoryLogbackAppender.attach();
    }

    public void stop() {
        inMemoryLogbackAppender.detach();
    }

    public ImmutableList<ILoggingEvent> getCurrentThreadLogs() {
        return inMemoryLogbackAppender.getLogs(Thread.currentThread().getName());
    }

    private static class InMemoryLogbackAppender extends AppenderBase<ILoggingEvent> {
        private final ConcurrentLinkedQueue<ILoggingEvent> eventQueue;
        private final LoggerContext context;
        private final Logger rootLogger;

        public InMemoryLogbackAppender() {
            super();
            this.eventQueue = new ConcurrentLinkedQueue<>();
            this.context = (LoggerContext) LoggerFactory.getILoggerFactory();
            this.rootLogger = context.getLogger(Logger.ROOT_LOGGER_NAME);
        }

        public void attach() {
            this.setContext(this.context);
            this.rootLogger.addAppender(this);
            this.start();
        }

        public void detach() {
            this.stop();
            rootLogger.detachAppender(this);
        }

        public void clear() {
            eventQueue.clear();
        }

        public ImmutableList<ILoggingEvent> getLogs(String threadName) {
            return Immutable.list.fromStream(eventQueue.stream().filter(e -> e.getThreadName().equals(threadName)));
        }

        @Override
        protected void append(ILoggingEvent eventObject) {
            eventQueue.add(eventObject);
        }
    }
}
