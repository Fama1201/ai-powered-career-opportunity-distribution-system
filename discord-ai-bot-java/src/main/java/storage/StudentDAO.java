package storage;

import config.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Types;

/**
 * Data Access Object for the 'student' table.
 * Provides an upsert method based on the unique discord_id key.
 */
public class StudentDAO {

    /**
     * Inserts a new student record or updates an existing one by discord_id.
     *
     * @param fullName       the student's full name, or null to leave unchanged
     * @param email          the student's email address
     * @param skills         comma-separated skills list, or null to leave unchanged
     * @param careerInterest the desired career position, or null to leave unchanged
     * @param discordId      the unique Discord identifier for the student (primary key)
     * @param cvUrl          URL or path to the student's resume, or null to leave unchanged
     * @param jobType        type of job (e.g., full-time, part-time), or null to leave unchanged
     * @throws Exception if a database access error occurs
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

            // 1) Set name or NULL
            if (fullName != null) {
                stmt.setString(1, fullName);
            } else {
                stmt.setNull(1, Types.VARCHAR);
            }

            // 2) Set email (required)
            stmt.setString(2, email);

            // 3) Set skills or NULL
            if (skills != null) {
                stmt.setString(3, skills);
            } else {
                stmt.setNull(3, Types.VARCHAR);
            }

            // 4) Set career interest or NULL
            if (careerInterest != null) {
                stmt.setString(4, careerInterest);
            } else {
                stmt.setNull(4, Types.VARCHAR);
            }

            // 5) Set discord_id (required)
            stmt.setString(5, discordId);

            // 6) Set CV URL or NULL
            if (cvUrl != null) {
                stmt.setString(6, cvUrl);
            } else {
                stmt.setNull(6, Types.VARCHAR);
            }

            // 7) Set job type or NULL
            if (jobType != null) {
                stmt.setString(7, jobType);
            } else {
                stmt.setNull(7, Types.VARCHAR);
            }

            // Execute insert or update
            stmt.executeUpdate();
        }
    }
}