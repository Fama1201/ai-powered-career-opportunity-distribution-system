package bot.storage;

import bot.BotMain;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = BotMain.class) // Loads your Spring context
@Testcontainers // Tells JUnit to look for Docker containers
class StudentDAOTest {

    // 1. Define the Container
    // This tells Docker to download and start PostgreSQL 16
    @Container
    @ServiceConnection // Automatically configures spring.datasource.url, user, password!
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private StudentDAO studentDAO;

    @Test
    void testSaveAndFindStudent() {
        // 2. Create a student object
        Student student = new Student();
        student.setName("Test User");
        student.setEmail("test@example.com");
        student.setDiscordId("123456789");
        student.setSkills("Java, Docker");

        // 3. Save it to the DOCKER database (not your local one!)
        studentDAO.save(student);

        // 4. Retrieve it
        Student retrieved = studentDAO.findByDiscordId("123456789").orElse(null);

        // 5. Verify it worked
        assertNotNull(retrieved);
        assertEquals("Test User", retrieved.getName());
        System.out.println("✅ Successfully saved and retrieved student from Docker database!");
    }
}