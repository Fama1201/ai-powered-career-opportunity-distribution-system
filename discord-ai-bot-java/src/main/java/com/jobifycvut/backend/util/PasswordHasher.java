package com.jobifycvut.backend.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;


//* Password hashing using PBKDF2-HMAC-SHA256

public class PasswordHasher {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int SALT_LENGTH = 16;
    private static final int HASH_LENGTH = 32;
    private static final int ITERATIONS = 310000; // OWASP recommendation

    public static String hashPassword(String password) {
        try {
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);

            byte[] hash = pbkdf2(password.toCharArray(), salt, ITERATIONS, HASH_LENGTH);

            String saltBase64 = Base64.getEncoder().encodeToString(salt);
            String hashBase64 = Base64.getEncoder().encodeToString(hash);

            return ITERATIONS + ":" + saltBase64 + ":" + hashBase64;

        } catch (Exception e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    public static boolean verifyPassword(String password, String storedHash) {
        try {
            String[] parts = storedHash.split(":");
            if (parts.length != 3) {
                return false;
            }

            int iterations = Integer.parseInt(parts[0]);
            byte[] salt = Base64.getDecoder().decode(parts[1]);
            byte[] storedHashBytes = Base64.getDecoder().decode(parts[2]);

            byte[] testHash = pbkdf2(password.toCharArray(), salt, iterations, HASH_LENGTH);

            return slowEquals(storedHashBytes, testHash);

        } catch (Exception e) {
            return false;
        }
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyLength)
            throws NoSuchAlgorithmException, InvalidKeySpecException {

        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength * 8);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
        return factory.generateSecret(spec).getEncoded();
    }

    private static boolean slowEquals(byte[] a, byte[] b) {
        int diff = a.length ^ b.length;
        for (int i = 0; i < a.length && i < b.length; i++) {
            diff |= a[i] ^ b[i];
        }
        return diff == 0;
    }
}