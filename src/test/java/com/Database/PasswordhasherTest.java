package com.UI.cab302_project.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordhasherTest {

    @Test
    void hashPasswordShouldNotReturnPlainPassword() {
        String password = "password123";

        String hash = Passwordhasher.hashPassword(password);

        assertNotEquals(password, hash);
    }

    @Test
    void samePasswordShouldProduceSameHash() {
        String password = "password123";

        assertEquals(
                Passwordhasher.hashPassword(password),
                Passwordhasher.hashPassword(password)
        );
    }

    @Test
    void verifyPasswordShouldReturnTrueForCorrectPassword() {
        String hash = Passwordhasher.hashPassword("password123");

        assertTrue(Passwordhasher.verifyPassword("password123", hash));
    }

    @Test
    void verifyPasswordShouldReturnFalseForWrongPassword() {
        String hash = Passwordhasher.hashPassword("password123");

        assertFalse(Passwordhasher.verifyPassword("wrongpassword", hash));
    }
}