package com.jobifycvut.backend.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@Repository
public class HrUserRepository {

    @Autowired
    private DataSource dataSource;

    /**
     * Checks if the email exists and the password matches.
     * @param email The email provided by the user.
     * @param password The password provided by the user.
     * @return true if credentials are correct, false otherwise.
     */
    public boolean validateCredentials(String email, String password) {
        // Query to get the password_hash for the given email
        String sql = "SELECT password_hash FROM hr WHERE email = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");

                    // ⚠️ SECURITY NOTE:
                    // In a real production app, we would use BCrypt here.
                    // Since you manually inserted plain text 'hash123', we compare plain text.
                    return storedHash.equals(password);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false; // User not found or error
    }
}