package storage;

import bot.api.OpportunityClient.Opportunity;
import config.DBConnection;

import java.sql.*;

public class OpportunityDAO {

    public static boolean existsForUser(Opportunity opp, String discordId) throws Exception {
        String sql = "SELECT 1 FROM opportunities WHERE opportunity_id = ? AND discord_id = ? LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, opp.id);
            stmt.setString(2, discordId);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public static void insertForUser(Opportunity opp, String discordId) throws Exception {
        String sql = """
            INSERT INTO opportunities (
                opportunity_id, title, description, job_type, application_deadline,
                discord_id, url,
                wage, home_office, benefits, formal_requirements,
                technical_requirements, contact_person
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

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

            stmt.setString(1, opp.id);
            stmt.setString(2, opp.title);
            stmt.setString(3, opp.description);
            stmt.setString(4, opp.type);

            if (opp.deadline != null && !opp.deadline.isBlank()) {
                stmt.setDate(5, Date.valueOf(opp.deadline));
            } else {
                stmt.setNull(5, Types.DATE);
            }

            stmt.setString(6, discordId);
            stmt.setString(7, emptyToNull(opp.url));
            stmt.setString(8, emptyToNull(opp.wage));
            stmt.setString(9, emptyToNull(opp.homeOffice));
            stmt.setString(10, emptyToNull(opp.benefits));
            stmt.setString(11, emptyToNull(opp.formReq));
            stmt.setString(12, emptyToNull(opp.techReq));
            stmt.setString(13, emptyToNull(opp.contactPerson));

            stmt.executeUpdate();
            System.out.println("✅ Inserted into database.");
        }
    }

    private static String emptyToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
