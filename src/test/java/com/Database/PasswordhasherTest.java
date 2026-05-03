package com.Database;

import com.UI.cab302_project.util.Passwordhasher;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordhasherTest {

    @Test
    void samePasswordProducesSameHash() {
        String password = "test123";

        String hash1 = Passwordhasher.hashPassword(password);
        String hash2 = Passwordhasher.hashPassword(password);

        assertEquals(hash1, hash2);
    }

    @Test
    void differentPasswordsProduceDifferentHashes() {
        String hash1 = Passwordhasher.hashPassword("password1");
        String hash2 = Passwordhasher.hashPassword("password2");

        assertNotEquals(hash1, hash2);
    }
}