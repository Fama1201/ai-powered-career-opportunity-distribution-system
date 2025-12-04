package com.jobifycvut.backend.util;


import com.jobifycvut.backend.model.User;
import java.util.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;


//Jwt token generator for login into the system
public class JwtUtil {
    private static final String SECRET_KEY = "super-secret-key";
    private static final Long ACCESS_TOKEN_VALIDITY_MINUTES = 15L;
    private static final Long REFRESH_TOKEN_VALIDITY_DAYS=7L;

    public static String generateAccessToken(User user) {
        return generateToken(user, ACCESS_TOKEN_VALIDITY_MINUTES, ChronoUnit.MINUTES, "access");
    }
    public static String generateRefreshToken(User user) {
        return generateToken(user, REFRESH_TOKEN_VALIDITY_DAYS, ChronoUnit.DAYS, "refresh");
    }

    private static String generateToken(User user, Long validity, ChronoUnit unit, String type) {
        Instant now = Instant.now();
        Instant expires = now.plus(validity, unit);

        String header="{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
        String payload = String.format(
                "{\"sub\":\"%s\",\"userId\":%d,\"role\":\"%s\",\"type\":\"%s\",\"iat\":%d,\"exp\":%d}",
                user.getEmail(),
                user.getId(),
                user.getRole().toString(),
                type,
                now.getEpochSecond(),
                expires.getEpochSecond()
        );

        String encodedHeader= base64UrlEncode(header);
        String encodedPayload= base64UrlEncode(payload);

        String data=encodedHeader + ":" + encodedPayload;
        String signature= createSignature(data);

        return signature + "." + data;
    }

    public static Map<String, Object>  validateToken(String token) {
        try{
            String[] parts=token.split("\\.");
            if(parts.length!=3){
                return null;
            }
            String encodedHeader=parts[0];
            String encodedPayload=parts[1];
            String signature=parts[2];

            String data=encodedHeader + "." + encodedPayload;
            String expectedSignature=createSignature(data);

            if(!signature.equals(expectedSignature)){
                return null;
            }
            String payload=base64UrlDecode(encodedPayload);
            Map<String,Object> claims=parseJsonToMap(payload);

            long exp=((Number)claims.get("exp")).longValue();
            if(Instant.now().getEpochSecond()>exp){
                return null;
            }
            return claims;
        } catch (Exception e) {
            return null;
        }
    }
    public static Long getUserIdFromToken(String token) {
        Map<String, Object>claims=validateToken(token);
        if(claims==null){
            return null;
        }
        return ((Number) claims.get("userId")).longValue();
    }

    public static String getEmailFromToken(String token) {
        Map<String, Object>claims=validateToken(token);
        if(claims==null){
            return null;
        }
        return (String) claims.get("sub");
    }

    public static boolean isTokenExpired(String token) {
        return validateToken(token)==null;
    }
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
        return Base64UrlEncode(data.getBytes());
    }
    private static String Base64UrlEncode(byte[] data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }
    private static String base64UrlDecode(String encoded) {
        byte[] decoded=Base64.getDecoder().decode(encoded);
        return new String(decoded);
    }
    private static Map<String, Object> parseJsonToMap(String json) {
        Map<String,Object> map=new HashMap<>();

        json=json.trim();
        if(json.startsWith("{")){
            json=json.substring(1);
        }
        if(json.endsWith("}")){
            json=json.substring(0,json.length()-1);
        }

        String[] pairs=json.split(",");
        for(String pair:pairs){
            String[] keyValue=pair.split(":", 2);
            String key=keyValue[0].trim().replace("\"", "");
            String value=keyValue[1].trim();

            Object parsedValue;
            if(value.startsWith("{")){
                parsedValue=value.replace("\"", "");
            }
            else{
                try{
                    parsedValue=Long.parseLong(value);
                }catch (NumberFormatException e){
                    parsedValue=value;
                }

            }
            map.put(key,parsedValue);
        }
        return map;
    }


}

