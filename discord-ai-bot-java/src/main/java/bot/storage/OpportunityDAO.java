package bot.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OpportunityDAO extends JpaRepository<Opportunity, Integer>, JpaSpecificationExecutor<Opportunity> {

    // --- Methods for JobController (/api/jobs/filters) ---
    @Query("SELECT DISTINCT o.jobType FROM Opportunity o WHERE o.jobType IS NOT NULL AND o.jobType != ''")
    List<String> findDistinctJobTypes();

    @Query("SELECT DISTINCT o.homeOffice FROM Opportunity o WHERE o.homeOffice IS NOT NULL AND o.homeOffice != ''")
    List<String> findDistinctHomeOffice();

    // --- Method for JobController (/api/jobs/search) ---
    List<Opportunity> findByTitleContainingIgnoreCase(String keyword);

    // --- Methods for the Bot ---
    List<Opportunity> findByDiscordId(String discordId);

    boolean existsByOpportunityIdAndDiscordId(String opportunityId, String discordId);
}