package storage;

import bot.api.OpportunityClient.Opportunity;
import config.DBConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class OpportunityDAO {

    @Autowired
    private DataSource dataSource;

    // ==========================================
    //  WEB API METHODS (Dynamic & Paginated)
    // ==========================================

    /**
     * Get all jobs with pagination.
     * @param page The page number (1-based index)
     * @param size The number of items per page
     */
    public List<Opportunity> findAllPaginated(int page, int size) {
        List<Opportunity> list = new ArrayList<>();
        // Calculate OFFSET: (Page 1 -> Offset 0), (Page 2 -> Offset 10), etc.
        int offset = (page - 1) * size;

        String sql = """
            SELECT opportunity_id, title, description, job_type, application_deadline,
                   url, wage, home_office, benefits, formal_requirements,
                   technical_requirements, contact_person, company
            FROM opportunities
            ORDER BY application_deadline DESC NULLS LAST
            LIMIT ? OFFSET ?
        """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, size);
            stmt.setInt(2, offset);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToOpportunity(rs));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Search for jobs by keyword in Title or Description.
     */
    public List<Opportunity> searchByKeyword(String keyword) {
        List<Opportunity> list = new ArrayList<>();
        String sql = """
            SELECT opportunity_id, title, description, job_type, application_deadline,
                   url, wage, home_office, benefits, formal_requirements,
                   technical_requirements, contact_person, company
            FROM opportunities
            WHERE LOWER(title) LIKE LOWER(?) 
               OR LOWER(description) LIKE LOWER(?)
            ORDER BY application_deadline DESC NULLS LAST
        """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + keyword + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToOpportunity(rs));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Find a specific job by its ID.
     */
    public Opportunity findById(String id) {
        String sql = """
            SELECT opportunity_id, title, description, job_type, application_deadline,
                   url, wage, home_office, benefits, formal_requirements,
                   technical_requirements, contact_person, company
            FROM opportunities
            WHERE opportunity_id = ?
        """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToOpportunity(rs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // Return null if not found
    }

    // ==========================================
    //  STATIC METHODS (For Discord Bot - Legacy)
    // ==========================================

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
                discord_id, url, wage, home_office, benefits, formal_requirements,
                technical_requirements, contact_person, company
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

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
            stmt.setString(14, emptyToNull(opp.company));
            stmt.executeUpdate();
        }
    }

    public static void deleteAllForUser(String discordId) throws Exception {
        String sql = "DELETE FROM opportunities WHERE discord_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, discordId);
            stmt.executeUpdate();
        }
    }

    public static List<Opportunity> getAllForUser(String discordId) throws Exception {
        List<Opportunity> list = new ArrayList<>();
        String sql = """
            SELECT opportunity_id, title, description, job_type, application_deadline,
                   url, wage, home_office, benefits, formal_requirements,
                   technical_requirements, contact_person, company
            FROM opportunities
            WHERE discord_id = ?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, discordId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapResultSetToOpportunity(rs));
            }
        }
        return list;
    }

    // Helper mapping method
    private static Opportunity mapResultSetToOpportunity(ResultSet rs) throws SQLException {
        Opportunity opp = new Opportunity();
        opp.id = rs.getString("opportunity_id");
        opp.title = rs.getString("title");
        opp.description = rs.getString("description");
        opp.type = rs.getString("job_type");
        Date deadline = rs.getDate("application_deadline");
        opp.deadline = (deadline != null) ? deadline.toString() : null;
        opp.url = rs.getString("url");
        opp.wage = rs.getString("wage");
        opp.homeOffice = rs.getString("home_office");
        opp.benefits = rs.getString("benefits");
        opp.formReq = rs.getString("formal_requirements");
        opp.techReq = rs.getString("technical_requirements");
        opp.contactPerson = rs.getString("contact_person");
        opp.company = rs.getString("company");
        return opp;
    }

    private static String emptyToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}