package com.jobifycvut.backend.security;

import com.jobifycvut.backend.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * Skip filtering for endpoints that must always be public.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        // Always allow CORS preflight without JWT
        if (HttpMethod.OPTIONS.matches(request.getMethod())) return true;

        // Public endpoints
        if (path.startsWith("/api/auth/")) return true;
        if (path.startsWith("/api/hr/auth/")) return true;
        if (path.equals("/error")) return true;

        return false;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // If already authenticated, continue
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");

        // DEBUG (remove later if you want)
        // System.out.println("REQ " + request.getMethod() + " " + request.getRequestURI() + " AUTH=" + header);

        // No bearer token => continue (will become 401 later if endpoint requires auth)
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring("Bearer ".length()).trim();
        if (token.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Map<String, Object> claims = jwtUtil.validateToken(token);

            // Invalid token / expired / bad signature
            if (claims == null) {
                // System.out.println("JWT INVALID: validateToken returned null");
                filterChain.doFilter(request, response);
                return;
            }

            // Reject refresh tokens for normal API access
            Object type = claims.get("type");
            if (type != null && !"access".equalsIgnoreCase(String.valueOf(type))) {
                // System.out.println("JWT REJECTED: token type=" + type);
                filterChain.doFilter(request, response);
                return;
            }

            // JwtUtil generates: {"sub":"email","userId":123,"role":"STUDENT","type":"access",...}
            Long userId = toLong(claims.get("userId"));
            String email = toStringSafe(claims.get("sub")); // subject
            String roleRaw = toStringSafe(claims.get("role"));

            if (userId == null || roleRaw == null || roleRaw.isBlank()) {
                // System.out.println("JWT INVALID CLAIMS: userId=" + userId + " role=" + roleRaw);
                filterChain.doFilter(request, response);
                return;
            }

            // Spring Security "hasRole('STUDENT')" expects authority "ROLE_STUDENT"
            String authority = roleRaw.startsWith("ROLE_") ? roleRaw : "ROLE_" + roleRaw;

            AuthPrincipal principal = new AuthPrincipal(userId, email, roleRaw);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            principal,
                            null,
                            List.of(new SimpleGrantedAuthority(authority))
                    );

            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception e) {
            // Any parsing error -> don't authenticate
            System.out.println("JWT FAILED (" + request.getRequestURI() + "): " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private Long toLong(Object v) {
        if (v == null) return null;
        if (v instanceof Number n) return n.longValue();
        try {
            return Long.parseLong(String.valueOf(v));
        } catch (Exception e) {
            return null;
        }
    }

    private String toStringSafe(Object v) {
        return v == null ? null : String.valueOf(v);
    }
}
