package com.UI.cab302_project.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class Passwordhasher {
    
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        String hashedInput = hashPassword(plainPassword);
        return hashedInput.equals(hashedPassword);
    }

}
