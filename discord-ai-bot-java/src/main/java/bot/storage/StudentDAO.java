package bot.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface StudentDAO extends JpaRepository<Student, Integer> {

    // Finds a student by their Discord ID
    Optional<Student> findByDiscordId(String discordId);

    // Deletes a student by their Discord ID
    @Transactional
    @Modifying
    void deleteByDiscordId(String discordId);

    // Updates the CV text for a specific Discord ID
    @Transactional
    @Modifying
    @Query("UPDATE Student s SET s.cvText = :cvText WHERE s.discordId = :discordId")
    void upsertCvText(@Param("discordId") String discordId, @Param("cvText") String cvText);

    // (Optional) If you need the custom UPSERT logic for the profile,
    // usually .save() is enough in Spring Data, but here is the SQL equivalent if needed:
    /*
    @Transactional
    @Modifying
    @Query(value = "INSERT INTO student (name, email, skills, career_interest, discord_id) ...", nativeQuery = true)
    void upsertStudent(...);
    */
    // For this test, the standard .save() method (inherited from JpaRepository) is sufficient.
}