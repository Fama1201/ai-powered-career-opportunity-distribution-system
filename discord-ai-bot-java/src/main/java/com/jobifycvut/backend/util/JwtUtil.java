package com.jobifycvut.backend.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobifycvut.backend.model.HrUser;
import com.jobifycvut.backend.model.User;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Map;

@Component
public class JwtUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String SECRET_KEY = "super-secret-key-change-this-in-production";
    private static final long ACCESS_TOKEN_VALIDITY_MINUTES = 15L;
    private static final long REFRESH_TOKEN_VALIDITY_DAYS = 7L;

    // --- STUDENT ---
    public String generateAccessToken(User user) {
        return generateToken(user.getId(), user.getEmail(), user.getRole().toString(),
                ACCESS_TOKEN_VALIDITY_MINUTES, ChronoUnit.MINUTES, "access");
    }

    public String generateRefreshToken(User user) {
        return generateToken(user.getId(), user.getEmail(), user.getRole().toString(),
                REFRESH_TOKEN_VALIDITY_DAYS, ChronoUnit.DAYS, "refresh");
    }

    // --- HR ---
    public String generateAccessToken(HrUser hrUser) {
        return generateToken(hrUser.getId(), hrUser.getEmail(), "HR",
                ACCESS_TOKEN_VALIDITY_MINUTES, ChronoUnit.MINUTES, "access");
    }

    public String generateRefreshToken(HrUser hrUser) {
        return generateToken(hrUser.getId(), hrUser.getEmail(), "HR",
                REFRESH_TOKEN_VALIDITY_DAYS, ChronoUnit.DAYS, "refresh");
    }

    // --- VALIDATION ---
    public Map<String, Object> validateToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) return null;

            String encodedHeader = parts[0];
            String encodedPayload = parts[1];
            String signature = parts[2];

            String data = encodedHeader + "." + encodedPayload;
            String expectedSignature = createSignature(data);

            if (!constantTimeEquals(signature, expectedSignature)) return null;

            String payloadJson = base64UrlDecode(encodedPayload);
            Map<String, Object> claims = MAPPER.readValue(payloadJson, new TypeReference<>() {});

            Object expObj = claims.get("exp");
            if (!(expObj instanceof Number)) return null;

            long exp = ((Number) expObj).longValue();
            if (Instant.now().getEpochSecond() > exp) return null;

            return claims;
        } catch (Exception e) {
            return null;
        }
    }

    // --- INTERNALS ---
    private String generateToken(Long userId, String email, String role, long validity, ChronoUnit unit, String type) {
        Instant now = Instant.now();
        Instant expires = now.plus(validity, unit);

        String header = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
        String payload = String.format(
                "{\"sub\":\"%s\",\"userId\":%d,\"role\":\"%s\",\"type\":\"%s\",\"iat\":%d,\"exp\":%d}",
                email, userId, role, type, now.getEpochSecond(), expires.getEpochSecond()
        );

        String encodedHeader = base64UrlEncode(header);
        String encodedPayload = base64UrlEncode(payload);

        String data = encodedHeader + "." + encodedPayload;
        String signature = createSignature(data);

        return data + "." + signature;
    }

    private String createSignature(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] signature = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
        } catch (Exception e) {
            throw new RuntimeException("Error creating signature", e);
        }
    }

    private String base64UrlEncode(String data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data.getBytes(StandardCharsets.UTF_8));
    }

    private String base64UrlDecode(String encoded) {
        // URL decoder + handle missing padding safely
        int pad = (4 - (encoded.length() % 4)) % 4;
        String padded = encoded + "=".repeat(pad);
        return new String(Base64.getUrlDecoder().decode(padded), StandardCharsets.UTF_8);
    }

    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) return false;
        if (a.length() != b.length()) return false;
        int result = 0;
        for (int i = 0; i < a.length(); i++) result |= a.charAt(i) ^ b.charAt(i);
        return result == 0;
    }
}
