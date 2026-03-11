package com.theodo.springblueprint.common.api.beans;

import static com.theodo.springblueprint.testhelpers.utils.JsonUtils.assertIsMatchStrictArrayOrder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.core.JsonProcessingException;
import tools.jackson.core.exc.StreamReadException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.exc.InvalidDefinitionException;
import tools.jackson.databind.exc.InvalidFormatException;
import tools.jackson.databind.exc.MismatchedInputException;
import tools.jackson.databind.exc.UnrecognizedPropertyException;
import java.time.*;
import java.util.Objects;
import java.util.stream.Stream;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ImportAutoConfiguration(JacksonAutoConfiguration.class)
@Import(ObjectMapperConfiguration.class)
@RequiredArgsConstructor
class ObjectMapperConfigurationIntegrationTests {

    private final ObjectMapper objectMapper;

    @ParameterizedTest
    @MethodSource("provideValidDataSet")
    void deserializing_valid_json_string_returns_expected_object(String json, Object expectedObject)
        throws JsonProcessingException {

        // Act
        Object result = objectMapper.readValue(json, expectedObject.getClass());

        assertThat(result).isEqualTo(expectedObject);
    }

    @ParameterizedTest
    @MethodSource("provideValidDataSet")
    void serializing_valid_object_returns_expected_string(String expectedJson, Object objectToSerialize)
        throws JsonProcessingException {

        // Act
        String json = objectMapper.writeValueAsString(objectToSerialize);

        assertIsMatchStrictArrayOrder(expectedJson, json);
    }

    @ParameterizedTest
    @MethodSource("provideDeserializationInvalidDataSet")
    void deserializing_invalid_json_string_throws_expected_exception(
        String json,
        Class<?> targetType,
        Class<? extends Throwable> expectedExceptionType,
        String expectedTextInMessage) {

        // Act
        var exceptionAssertion = assertThatThrownBy(() -> objectMapper.readValue(json, targetType));

        exceptionAssertion
            .isExactlyInstanceOf(expectedExceptionType)
            .hasMessageContaining(expectedTextInMessage);
    }

    @Test
    // Test FAIL_ON_READING_DUP_TREE_KEY=true
    void reading_a_tree_from_a_json_string_with_duplicated_properties_throws_MismatchedInputException() {
        String json = """
            {"primitiveInt":10, "primitiveInt":11}""";

        // Act
        var exceptionAssertion = assertThatThrownBy(() -> objectMapper.readTree(json));

        exceptionAssertion
            .isExactlyInstanceOf(MismatchedInputException.class);
    }

    private static Stream<Arguments> provideValidDataSet() {
        return Stream.of(
            Arguments.of(
                """
                {"primitiveInt":10}""",
                new TypeWithPrimitive(10)
            ),
            Arguments.of(
                """
                {"enumField":"VALUE_1"}""",
                new TypeWithEnum(TypeWithEnum.InnerEnum.VALUE_1)
            ),
            // Test json with extra white spaces
            Arguments.of(
                """
                {"primitiveInt":10}

                """,
                new TypeWithPrimitive(10)
            ),
            // Test ADJUST_DATES_TO_CONTEXT_TIME_ZONE=false
            Arguments.of(
                """
                {"instant":         "2025-01-13T20:30:40.123456Z",
                "zonedDateTime":    "2025-01-13T21:30:40.123456+01:00",
                "offsetDateTime":   "2025-01-13T21:30:40.123456+01:00",
                "localDateTime":    "2025-01-13T21:30:40.123456",
                "localDate":        "2025-01-13",
                "localTime":        "21:30:40.123456",
                "duration":         "PT21H30M40.123456S"}""",
                TypeWithDates.getInstance()
            )
        );
    }

    private static Stream<Arguments> provideDeserializationInvalidDataSet() {
        return Stream.of(
            // test FAIL_ON_NULL_FOR_PRIMITIVES=true
            Arguments.of(
                """
                {}""",
                TypeWithPrimitive.class,
                MismatchedInputException.class,
                "Cannot map `null` into type `int`"
            ),
            // test FAIL_ON_NUMBERS_FOR_ENUMS=true
            Arguments.of(
                """
                {"enumField":0}""",
                TypeWithEnum.class,
                InvalidFormatException.class,
                "not allowed to deserialize Enum value out of number"
            ),
            // test FAIL_ON_READING_DUP_TREE_KEY=true
            Arguments.of(
                """
                {"primitiveInt":10, "primitiveInt":11}""",
                TypeWithPrimitive.class,
                InvalidDefinitionException.class,
                "No fallback setter/field defined for creator property 'primitiveInt'"
            ),
            // test ALLOW_COERCION_OF_SCALARS=false
            Arguments.of(
                """
                {"primitiveInt":"10"}""",
                TypeWithPrimitive.class,
                MismatchedInputException.class,
                "Cannot coerce String value (\"10\") to `int` value"
            ),
            // test ACCEPT_FLOAT_AS_INT=false
            Arguments.of(
                """
                {"primitiveInt":10.1}""",
                TypeWithPrimitive.class,
                InvalidFormatException.class,
                "Cannot coerce Floating-point value (10.1) to `int` value"
            ),
            // test FAIL_ON_TRAILING_TOKENS=true
            Arguments.of(
                """
                {"primitiveInt":10} more content after end of json""",
                TypeWithPrimitive.class,
                StreamReadException.class,
                "Unrecognized token 'more'"
            ),
            // test FAIL_ON_UNKNOWN_PROPERTIES=true
            Arguments.of(
                """
                {"primitiveInt":10, "extraProperty":"anything"}""",
                TypeWithPrimitive.class,
                UnrecognizedPropertyException.class,
                "Unrecognized property \"extraProperty\""
            ),
            // test lenient=false
            Arguments.of(
                """
                { "localDateTime": "2025-01-13T20:30:40.123456Z"}""",
                TypeWithLocalDateTime.class,
                InvalidFormatException.class,
                "Should not contain offset when 'strict' mode set for property or type"
            )
        );
    }

    record TypeWithLocalDateTime(LocalDateTime localDateTime) {
    }

    record TypeWithPrimitive(int primitiveInt) {
    }

    record TypeWithEnum(InnerEnum enumField) {
        enum InnerEnum {
            VALUE_1,
        }
    }

    record TypeWithDates(
        Instant instant,
        ZonedDateTime zonedDateTime,
        OffsetDateTime offsetDateTime,
        LocalDateTime localDateTime,
        LocalDate localDate,
        LocalTime localTime,
        Duration duration
    ) {
        static TypeWithDates getInstance() {
            ZoneId zoneId = ZoneId.of("Africa/Casablanca");
            Instant instant = Instant.parse("2025-01-13T20:30:40.123456Z");
            LocalDateTime localDateTime1 = LocalDateTime.ofInstant(instant, zoneId);
            ZonedDateTime zonedDateTime1 = instant.atZone(zoneId);
            Duration duration1 = Duration.ofNanos(localDateTime1.toLocalTime().toNanoOfDay());
            return new TypeWithDates(
                instant,
                zonedDateTime1,
                zonedDateTime1.toOffsetDateTime(),
                localDateTime1,
                localDateTime1.toLocalDate(),
                localDateTime1.toLocalTime(),
                duration1
            );
        }

        @Override
        public boolean equals(@Nullable Object object) {
            if (object == null || getClass() != object.getClass()) {
                return false;
            }
            TypeWithDates that = (TypeWithDates) object;
            return (Objects.equals(instant, that.instant) &&
                Objects.equals(duration, that.duration) &&
                Objects.equals(localDate, that.localDate) &&
                Objects.equals(localTime, that.localTime) &&
                Objects.equals(zonedDateTime.toOffsetDateTime(), that.zonedDateTime.toOffsetDateTime()) &&
                Objects.equals(localDateTime, that.localDateTime) &&
                Objects.equals(offsetDateTime, that.offsetDateTime));
        }

        @Override
        public int hashCode() {
            return Objects.hash(
                instant,
                zonedDateTime.toOffsetDateTime(),
                offsetDateTime,
                localDateTime,
                localDate,
                localTime,
                duration
            );
        }
    }
}
