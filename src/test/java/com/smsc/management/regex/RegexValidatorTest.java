package com.smsc.management.regex;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class RegexValidatorTest {
    RegexValidator validator;
    ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        validator = new RegexValidator();
        context = mock(ConstraintValidatorContext.class);
    }

    @Test
    void whenValueIsNull_thenIsValidReturnsTrue() {
        assertTrue(validator.isValid(null, context));
    }

    @Test
    void whenValueIsEmpty_thenIsValidReturnsTrue() {
        assertTrue(validator.isValid("", context));
    }

    @Test
    void whenValueIsBlank_thenIsValidReturnsTrue() {
        assertTrue(validator.isValid(" ", context));
    }

    @ParameterizedTest
    @ValueSource(strings = {"[a-z", "*invalid_regex", "(unmatched_parentheses"})
    void whenValueIsInvalidRegex_thenIsValidReturnsFalse(String regex) {
        assertFalse(validator.isValid(regex, context));
    }

    @ParameterizedTest
    @ValueSource(strings = {"a-z]"})
    void whenValueIsAmbiguousButValidRegex_thenIsValidReturnsTrue(String regex) {
        assertTrue(validator.isValid(regex, context));
    }

    @Test
    void whenValueIsValidRegex_thenIsValidReturnsTrue() {
        assertTrue(validator.isValid("[a-z]+", context));
    }

    @Test
    void whenValueIsInvalidRegex_thenIsValidReturnsFalse() {
        assertFalse(validator.isValid("[a-z", context));
    }

    @Test
    void initialize() {
        assertDoesNotThrow(() -> validator.initialize(null));
    }


    @Test
    void whenValueIsNotEmptyOrBlank_thenIsValidReturnsTrue() {
        assertTrue(validator.isValid("[a-z]+", context));
    }
}