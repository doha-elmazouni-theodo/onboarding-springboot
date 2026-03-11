package com.theodo.springblueprint.common.infra.mappers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.theodo.springblueprint.testhelpers.annotations.UnitTest;
import org.junit.jupiter.api.Test;

@UnitTest
class AbstractDefaultEnumMapperUnitTests {

    @Test
    void cannot_parse_non_string_objects() {
        TestEnumMapper testEnumMapper = new TestEnumMapper();

        // Act
        boolean canParse = testEnumMapper.canParse(43);

        assertThat(canParse).isFalse();
    }

    @Test
    void can_parse_string_with_valid_value() {
        TestEnumMapper testEnumMapper = new TestEnumMapper();

        // Act
        boolean canParse = testEnumMapper.canParse("VALID_VALUE");

        assertThat(canParse).isTrue();
    }

    @Test
    void can_parse_string_with_invalid_value() {
        TestEnumMapper testEnumMapper = new TestEnumMapper();

        // Act
        boolean canParse = testEnumMapper.canParse("INVALID_VALUE");

        assertThat(canParse).isFalse();
    }

    @Test
    void toValueObject_with_valid_value_returns_enum_object() {
        TestEnumMapper testEnumMapper = new TestEnumMapper();

        // Act
        TestEnum value = testEnumMapper.toValueObject("VALID_VALUE");

        assertThat(value).isEqualTo(TestEnum.VALID_VALUE);
    }

    @Test
    void toValueObject_with_invalid_value_throws_IllegalArgumentException() {
        TestEnumMapper testEnumMapper = new TestEnumMapper();

        // Act
        var exceptionAssertion = assertThatThrownBy(() -> testEnumMapper.toValueObject("INVALID_VALUE"));

        exceptionAssertion
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void fromValueObject_returns_enum_name() {
        TestEnumMapper testEnumMapper = new TestEnumMapper();

        // Act
        String stringValue = testEnumMapper.fromValueObject(TestEnum.VALID_VALUE);

        assertThat(stringValue).isEqualTo("VALID_VALUE");
    }

    private enum TestEnum {
        VALID_VALUE,
    }

    private static class TestEnumMapper extends AbstractDefaultEnumMapper<TestEnum> {

        protected TestEnumMapper() {
            super(TestEnum.class);
        }
    }
}
