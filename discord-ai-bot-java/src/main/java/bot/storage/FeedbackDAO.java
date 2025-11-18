package bot.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository; // <-- IMPORTANT IMPORT
import org.springframework.transaction.annotation.Transactional;

@Repository // <-- THIS ANNOTATION IS REQUIRED
public interface FeedbackDAO extends JpaRepository<Feedback, Integer> {

    @Transactional
    @Modifying
    @Query(value = """
        UPDATE feedback
        SET stars = :stars
        WHERE id = (
            SELECT id FROM feedback
            WHERE discord_id = :discordId AND stars IS NULL
            ORDER BY id DESC
            LIMIT 1
        )
        """, nativeQuery = true)
    void updateLatestStars(@Param("discordId") String discordId, @Param("stars") int stars);
}