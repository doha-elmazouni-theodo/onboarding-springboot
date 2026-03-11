package com.theodo.springblueprint.common.infra.configurations;

import com.theodo.springblueprint.common.infra.database.logging.CommentingQueryTransformer;
import com.theodo.springblueprint.common.infra.database.logging.DatabaseQueryLogger;
import javax.sql.DataSource;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSourceProxyConfig {

    @Bean
    public CommentingQueryTransformer commentingQueryTransformer() {
        return new CommentingQueryTransformer();
    }

    @Bean
    public DatabaseQueryLogger databaseQueryLogger() {
        return new DatabaseQueryLogger();
    }

    @Bean
    public BeanPostProcessor dataSourceProxyBeanPostProcessor(
        CommentingQueryTransformer queryTransformer,
        DatabaseQueryLogger queryLogger) {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) {
                if (!(bean instanceof DataSource dataSource)) {
                    return bean;
                }

                return ProxyDataSourceBuilder.create(dataSource)
                    .name(beanName)
                    .queryTransformer(queryTransformer)
                    .listener(queryLogger)
                    .build();
            }
        };
    }
}
