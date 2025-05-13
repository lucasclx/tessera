package com.backend.tessera.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StrongPasswordValidatorTest {

    private Validator validator;

    // Classe de teste para o validador
    private static class TestClass {
        @StrongPassword
        private String password;

        public TestClass(String password) {
            this.password = password;
        }
    }

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidPassword() {
        TestClass test = new TestClass("Test1234!");
        Set<ConstraintViolation<TestClass>> violations = validator.validate(test);
        assertTrue(violations.isEmpty(), "A senha válida não deveria gerar violações");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "abcdef", // Muito curta
            "12345678", // Apenas números
            "ABCDEFGH", // Apenas maiúsculas
            "abcdefgh", // Apenas minúsculas
            "Abcde123", // Sem caractere especial
            "Abcde!@#", // Sem número
            "123!@#$%", // Sem letra
            "abcd!@#$"  // Sem letra maiúscula
    })
    void testInvalidPasswords(String password) {
        TestClass test = new TestClass(password);
        Set<ConstraintViolation<TestClass>> violations = validator.validate(test);
        assertFalse(violations.isEmpty(), "A senha inválida '" + password + "' deveria gerar violações");
        assertEquals(1, violations.size(), "Deveria haver exatamente uma violação");
    }

    @Test
    void testNullPassword() {
        TestClass test = new TestClass(null);
        Set<ConstraintViolation<TestClass>> violations = validator.validate(test);
        assertFalse(violations.isEmpty(), "Uma senha nula deveria gerar violações");
    }

    @Test
    void testEmptyPassword() {
        TestClass test = new TestClass("");
        Set<ConstraintViolation<TestClass>> violations = validator.validate(test);
        assertFalse(violations.isEmpty(), "Uma senha vazia deveria gerar violações");
    }
}