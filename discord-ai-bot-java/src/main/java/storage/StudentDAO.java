package storage;

import config.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

/**
 * Data Access Object for the 'student' table.
 * Provides an upsert method and profile retrieval by Discord ID.
 */
public class StudentDAO {

    /**
     * Inserts a new student record or updates an existing one by discord_id.
     */
    public static void upsertStudent(
            String fullName,
            String email,
            String skills,
            String careerInterest,
            String discordId,
            String cvUrl,
            String jobType
    ) throws Exception {
        String sql = """
            INSERT INTO student
                (name, email, skills, career_interest, discord_id, cv_url, job_type)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (discord_id) DO UPDATE
              SET name            = COALESCE(EXCLUDED.name,            student.name),
                  email           = COALESCE(EXCLUDED.email,           student.email),
                  skills          = COALESCE(EXCLUDED.skills,          student.skills),
                  career_interest = COALESCE(EXCLUDED.career_interest, student.career_interest),
                  cv_url          = COALESCE(EXCLUDED.cv_url,          student.cv_url),
                  job_type        = COALESCE(EXCLUDED.job_type,        student.job_type)
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, fullName, Types.VARCHAR);
            stmt.setObject(2, email, Types.VARCHAR);
            stmt.setObject(3, skills, Types.VARCHAR);
            stmt.setObject(4, careerInterest, Types.VARCHAR);
            stmt.setString(5, discordId);
            stmt.setObject(6, cvUrl, Types.VARCHAR);
            stmt.setObject(7, jobType, Types.VARCHAR);

            stmt.executeUpdate();
        }
    }

    /**
     * Retrieves the profile of a student by their Discord ID.
     *
     * @param discordId the unique Discord user ID
     * @return a map of field names to values, or null if not found
     */
    public static Map<String, String> getStudentProfile(String discordId) throws Exception {
        String sql = "SELECT name, email, skills, career_interest, cv_url, job_type FROM student WHERE discord_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, discordId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Map<String, String> profile = new HashMap<>();
                    profile.put("Name", rs.getString("name"));
                    profile.put("Email", rs.getString("email"));
                    profile.put("Skills", rs.getString("skills"));
                    profile.put("Career Interest", rs.getString("career_interest"));
                    profile.put("CV", rs.getString("cv_url"));
                    profile.put("Job Type", rs.getString("job_type"));
                    return profile;
                } else {
                    return null;
                }
            }
        }
    }

    public static void updateCvTextByDiscordId(String discordId, String cvText) throws Exception {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "UPDATE student SET cv_text = ? WHERE discord_id = ?")) {
            pstmt.setString(1, cvText);
            pstmt.setString(2, discordId);
            pstmt.executeUpdate();
        }
    }

}
