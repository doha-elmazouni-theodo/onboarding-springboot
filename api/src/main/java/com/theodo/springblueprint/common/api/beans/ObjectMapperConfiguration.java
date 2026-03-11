package com.theodo.springblueprint.common.api.beans;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MapperFeature;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.cfg.EnumFeature;
import tools.jackson.datatype.eclipsecollections.EclipseCollectionsModule;

@Configuration
public class ObjectMapperConfiguration {

    @Bean
    public JsonMapperBuilderCustomizer jacksonCustomizer() {
        return jacksonObjectMapperBuilder -> jacksonObjectMapperBuilder
            .addModule(new EclipseCollectionsModule())
            .defaultLeniency(false)
            .enable(
                DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES,
                DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY,
                DeserializationFeature.FAIL_ON_TRAILING_TOKENS,
                DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
            )
            .enable(
                EnumFeature.FAIL_ON_NUMBERS_FOR_ENUMS
            )
            .disable(
                DateTimeFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE
            )
            .disable(
                MapperFeature.ALLOW_COERCION_OF_SCALARS
            )
            .disable(
                DeserializationFeature.ACCEPT_FLOAT_AS_INT
            );
    }
}
