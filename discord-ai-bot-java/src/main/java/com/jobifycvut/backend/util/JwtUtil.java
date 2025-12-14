package com.jobifycvut.backend.util;

import com.jobifycvut.backend.model.User;
import com.jobifycvut.backend.model.HrUser; // ✅ Import HR User
import java.util.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class JwtUtil {
    private static final String SECRET_KEY = "super-secret-key-change-this-in-production";
    private static final Long ACCESS_TOKEN_VALIDITY_MINUTES = 15L;
    private static final Long REFRESH_TOKEN_VALIDITY_DAYS = 7L;

    // --- STUDENT Methods (Existing) ---
    public static String generateAccessToken(User user) {
        return generateToken(user.getId(), user.getEmail(), user.getRole().toString(), ACCESS_TOKEN_VALIDITY_MINUTES, ChronoUnit.MINUTES, "access");
    }
    public static String generateRefreshToken(User user) {
        return generateToken(user.getId(), user.getEmail(), user.getRole().toString(), REFRESH_TOKEN_VALIDITY_DAYS, ChronoUnit.DAYS, "refresh");
    }

    // --- HR Methods (NEW) ---
    public static String generateAccessToken(HrUser hrUser) {
        // HR users get the "HR" role
        return generateToken(hrUser.getId(), hrUser.getEmail(), "HR", ACCESS_TOKEN_VALIDITY_MINUTES, ChronoUnit.MINUTES, "access");
    }

    public static String generateRefreshToken(HrUser hrUser) {
        return generateToken(hrUser.getId(), hrUser.getEmail(), "HR", REFRESH_TOKEN_VALIDITY_DAYS, ChronoUnit.DAYS, "refresh");
    }

    // --- SHARED HELPER ---
    private static String generateToken(Long userId, String email, String role, Long validity, ChronoUnit unit, String type) {
        Instant now = Instant.now();
        Instant expires = now.plus(validity, unit);

        String header = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
        String payload = String.format(
                "{\"sub\":\"%s\",\"userId\":%d,\"role\":\"%s\",\"type\":\"%s\",\"iat\":%d,\"exp\":%d}",
                email,
                userId,
                role,
                type,
                now.getEpochSecond(),
                expires.getEpochSecond()
        );

        String encodedHeader = base64UrlEncode(header);
        String encodedPayload = base64UrlEncode(payload);

        String data = encodedHeader + "." + encodedPayload;
        String signature = createSignature(data);

        return encodedHeader + "." + encodedPayload + "." + signature;
    }

    public static Map<String, Object> validateToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) return null;

            String encodedHeader = parts[0];
            String encodedPayload = parts[1];
            String signature = parts[2];

            String data = encodedHeader + "." + encodedPayload;
            String expectedSignature = createSignature(data);

            if (!signature.equals(expectedSignature)) return null;

            String payload = base64UrlDecode(encodedPayload);
            Map<String, Object> claims = parseJsonToMap(payload);

            long exp = ((Number) claims.get("exp")).longValue();
            if (Instant.now().getEpochSecond() > exp) return null;

            return claims;
        } catch (Exception e) {
            return null;
        }
    }

    // --- Helpers (Signature, Encoding) ---
    private static String createSignature(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(SECRET_KEY.getBytes(), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] signature = mac.doFinal(data.getBytes());
            return Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
        } catch (Exception e) {
            throw new RuntimeException("Error creating signature", e);
        }
    }

    private static String base64UrlEncode(String data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data.getBytes());
    }

    private static String base64UrlDecode(String encoded) {
        return new String(Base64.getDecoder().decode(encoded));
    }

    private static Map<String, Object> parseJsonToMap(String json) {
        Map<String, Object> map = new HashMap<>();
        json = json.trim().replace("{", "").replace("}", "");
        if (json.isEmpty()) return map;

        String[] pairs = json.split(",");
        for (String pair : pairs) {
            String[] keyValue = pair.split(":", 2);
            if (keyValue.length != 2) continue;
            String key = keyValue[0].trim().replace("\"", "");
            String value = keyValue[1].trim().replace("\"", "");

            // Try parsing numbers
            try {
                map.put(key, Long.parseLong(value));
            } catch (NumberFormatException e) {
                map.put(key, value);
            }
        }
        return map;
    }
}