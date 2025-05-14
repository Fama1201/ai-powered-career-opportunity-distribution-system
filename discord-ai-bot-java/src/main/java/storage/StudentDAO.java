package storage;

import config.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;
import java.sql.SQLException;

/**
 * Data Access Object for the 'student' table.
 * Provides methods to insert/update (upsert) a student,
 * retrieve profile data by Discord ID, delete a profile,
 * and update the student's resume (CV) text.
 */
public class StudentDAO {

    /**
     * Inserts a new student record or updates an existing one based on the Discord ID.
     * Only non-null fields in the upsert call will be updated; others are preserved.
     *
     * @param fullName        student's full name
     * @param email           student's email
     * @param skills          list of skills as a string
     * @param careerInterest  preferred career field
     * @param discordId       unique Discord user ID (primary key)
     * @param jobType         job type preference (e.g., "Full Stack")
     * @throws Exception if the database operation fails
     */
    public static void upsertStudent(
            String fullName,
            String email,
            String skills,
            String careerInterest,
            String discordId,
            String jobType
    ) throws Exception {
        String sql = """
            INSERT INTO student
                (name, email, skills, career_interest, discord_id, job_type)
            VALUES (?, ?, ?, ?, ?, ?)
            ON CONFLICT (discord_id) DO UPDATE
              SET name            = COALESCE(EXCLUDED.name,            student.name),
                  email           = COALESCE(EXCLUDED.email,           student.email),
                  skills          = COALESCE(EXCLUDED.skills,          student.skills),
                  career_interest = COALESCE(EXCLUDED.career_interest, student.career_interest),
                  job_type        = COALESCE(EXCLUDED.job_type,        student.job_type)
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Set parameters for insert/update; nulls are handled with setObject
            stmt.setObject(1, fullName, Types.VARCHAR);
            stmt.setObject(2, email, Types.VARCHAR);
            stmt.setObject(3, skills, Types.VARCHAR);
            stmt.setObject(4, careerInterest, Types.VARCHAR);
            stmt.setString(5, discordId);
            stmt.setObject(6, jobType, Types.VARCHAR);

            stmt.executeUpdate(); // Execute the SQL statement
        }
    }

    /**
     * Retrieves a student's profile information from the database using their Discord ID.
     *
     * @param discordId the unique Discord user ID
     * @return a map with keys like "Name", "Email", "Skills", etc., or null if not found
     * @throws Exception if the database query fails
     */
    public static Map<String, String> getStudentProfile(String discordId) throws Exception {
        String sql = "SELECT name, email, skills, career_interest, job_type FROM student WHERE discord_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, discordId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Map column values to a string-based profile
                    Map<String, String> profile = new HashMap<>();
                    profile.put("Name", rs.getString("name"));
                    profile.put("Email", rs.getString("email"));
                    profile.put("Skills", rs.getString("skills"));
                    profile.put("Career Interest", rs.getString("career_interest"));
                    profile.put("Job Type", rs.getString("job_type"));
                    return profile;
                } else {
                    return null; // No result found
                }
            }
        }
    }

    /**
     * Deletes a student's profile from the database using their Discord ID.
     *
     * @param discordId the user's Discord ID
     * @return true if a row was deleted, false if no match was found
     */
    public static boolean deleteProfileByDiscordId(String discordId) {
        String sql = "DELETE FROM student WHERE discord_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, discordId);
            int rows = stmt.executeUpdate();
            return rows > 0; // Return true if at least one row was affected
        } catch (Exception e) {
            e.printStackTrace();
            return false; // Return false if an error occurred
        }
    }

    /**
     * Updates the `cv_text` column for a student given their Discord ID.
     * This stores the extracted content from a PDF resume.
     *
     * @param discordId the user's Discord ID
     * @param cvText    the extracted plain text content of the resume
     * @throws Exception if the update fails
     */
    public static void updateCvTextByDiscordId(String discordId, String cvText) throws Exception {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "UPDATE student SET cv_text = ? WHERE discord_id = ?")) {

            pstmt.setString(1, cvText);
            pstmt.setString(2, discordId);
            pstmt.executeUpdate(); // Execute update on the record
        }
    }
}
