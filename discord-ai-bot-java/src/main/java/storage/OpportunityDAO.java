package storage;

import bot.api.OpportunityClient.Opportunity;
import config.DBConnection;

import java.sql.*;

/**
 * Data Access Object (DAO) for interacting with the 'opportunities' table.
 * Handles logic for checking if an opportunity already exists for a user,
 * and inserting new opportunities into the database.
 */
public class OpportunityDAO {

    /**
     * Checks if a given opportunity already exists in the database for a specific Discord user.
     *
     * @param opp       the opportunity object (contains ID, title, etc.)
     * @param discordId the Discord user ID
     * @return true if the opportunity already exists for the user, false otherwise
     * @throws Exception if a database error occurs
     */
    public static boolean existsForUser(Opportunity opp, String discordId) throws Exception {
        String sql = "SELECT 1 FROM opportunities WHERE opportunity_id = ? AND discord_id = ? LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, opp.id);          // Set the opportunity ID
            stmt.setString(2, discordId);       // Set the user ID

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();              // Return true if a matching row exists
            }
        }
    }

    /**
     * Inserts a new opportunity into the database for a specific user.
     *
     * @param opp       the opportunity object containing all data fields
     * @param discordId the Discord user ID to associate with the opportunity
     * @throws Exception if insertion fails
     */
    public static void insertForUser(Opportunity opp, String discordId) throws Exception {
        String sql = """
            INSERT INTO opportunities (
                opportunity_id, title, description, job_type, application_deadline,
                discord_id, url,
                wage, home_office, benefits, formal_requirements,
                technical_requirements, contact_person
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        // Debug log showing opportunity info before inserting
        System.out.println("\n📥 Preparing to insert opportunity:");
        System.out.println("→ ID: " + opp.id);
        System.out.println("→ Title: " + opp.title);
        System.out.println("→ Wage: " + opp.wage);
        System.out.println("→ Home Office: " + opp.homeOffice);
        System.out.println("→ Benefits: " + opp.benefits);
        System.out.println("→ Form Req: " + opp.formReq);
        System.out.println("→ Tech Req: " + opp.techReq);
        System.out.println("→ Contact: " + opp.contactPerson);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Basic info
            stmt.setString(1, opp.id);
            stmt.setString(2, opp.title);
            stmt.setString(3, opp.description);
            stmt.setString(4, opp.type);

            // Convert deadline to SQL date or set null
            if (opp.deadline != null && !opp.deadline.isBlank()) {
                stmt.setDate(5, Date.valueOf(opp.deadline));
            } else {
                stmt.setNull(5, Types.DATE);
            }

            // Associate with user and optional fields
            stmt.setString(6, discordId);
            stmt.setString(7, emptyToNull(opp.url));
            stmt.setString(8, emptyToNull(opp.wage));
            stmt.setString(9, emptyToNull(opp.homeOffice));
            stmt.setString(10, emptyToNull(opp.benefits));
            stmt.setString(11, emptyToNull(opp.formReq));
            stmt.setString(12, emptyToNull(opp.techReq));
            stmt.setString(13, emptyToNull(opp.contactPerson));

            // Insert into database
            stmt.executeUpdate();
            System.out.println("✅ Inserted into database.");
        }
    }

    /**
     * Utility method that returns null if the string is blank, otherwise returns the original string.
     * Used to sanitize values before insertion to avoid empty strings in DB.
     *
     * @param value the string to evaluate
     * @return null if blank or null, else the original string
     */
    private static String emptyToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
