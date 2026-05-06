package com.UI.cab302_project.util;

import com.example.syntaxio.ui.util.PasswordHasher;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordHasherTest {

    @Test
    void hashPasswordShouldNotReturnPlainPassword() {
        String password = "password123";

        String hash = PasswordHasher.hashPassword(password);

        assertNotEquals(password, hash);
    }

    @Test
    void samePasswordShouldProduceSameHash() {
        String password = "password123";

        assertEquals(
                PasswordHasher.hashPassword(password),
                PasswordHasher.hashPassword(password)
        );
    }

    @Test
    void verifyPasswordShouldReturnTrueForCorrectPassword() {
        String hash = PasswordHasher.hashPassword("password123");

        assertTrue(PasswordHasher.verifyPassword("password123", hash));
    }

    @Test
    void verifyPasswordShouldReturnFalseForWrongPassword() {
        String hash = PasswordHasher.hashPassword("password123");

        assertFalse(PasswordHasher.verifyPassword("wrongpassword", hash));
    }
}