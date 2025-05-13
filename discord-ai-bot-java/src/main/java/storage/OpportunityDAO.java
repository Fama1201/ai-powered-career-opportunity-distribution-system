package storage;

import bot.api.OpportunityClient.Opportunity;
import config.DBConnection;

import java.sql.*;

public class OpportunityDAO {

    public static boolean existsForUser(Opportunity opp, String discordId) throws Exception {
        String sql = "SELECT 1 FROM opportunities WHERE title = ? AND description = ? AND discord_id = ? LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, opp.title);
            stmt.setString(2, opp.description);
            stmt.setString(3, discordId);

            ResultSet rs = stmt.executeQuery();
            return rs.next();  // Returns true if it already exists
        }
    }

    public static void insertForUser(Opportunity opp, String discordId) throws Exception {
        String sql = """
            INSERT INTO opportunities (title, duration, tech_stack, description, job_type, application_deadline, discord_id)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, opp.title);
            stmt.setString(2, "N/A"); // duration (not provided by API)
            stmt.setString(3, "");    // tech_stack (can be improved later)
            stmt.setString(4, opp.description);
            stmt.setString(5, opp.type);
            stmt.setDate(6, Date.valueOf(opp.deadline));
            stmt.setString(7, discordId);

            stmt.executeUpdate();
        }
    }
}
