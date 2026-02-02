package com.jobifycvut.backend.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {

    public static AuthPrincipal getPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof AuthPrincipal)) return null;
        return (AuthPrincipal) auth.getPrincipal();
    }

    public static Long requireUserId() {
        AuthPrincipal p = getPrincipal();
        if (p == null) throw new RuntimeException("Unauthorized");
        return p.getUserId();
    }

    public static String requireRole() {
        AuthPrincipal p = getPrincipal();
        if (p == null) throw new RuntimeException("Unauthorized");
        return p.getRole();
    }
}
