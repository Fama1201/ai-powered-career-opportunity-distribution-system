package com.jobifycvut.backend.repository;

import com.jobifycvut.backend.model.User;
import com.jobifycvut.backend.model.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1"
})
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByEmailAndExists() {
        User u = new User();
        u.setEmail("u@example.com");
        u.setPassword("hash");
        u.setFirstName("U");
        u.setLastName("Ser");
        u.setRole(UserRole.STUDENT);
        u.setActive(true);
        u.setCreatedAt(Instant.now());
        userRepository.save(u);

        assertTrue(userRepository.existsByEmail("u@example.com"));
        assertTrue(userRepository.findByEmail("u@example.com").isPresent());
    }

    @Test
    void findByTokens() {
        User u = new User();
        u.setEmail("t@example.com");
        u.setPassword("hash");
        u.setFirstName("T");
        u.setLastName("User");
        u.setRole(UserRole.STUDENT);
        u.setActive(true);
        u.setCreatedAt(Instant.now());
        u.setPasswordResetToken("reset");
        u.setEmailVerificationToken("verify");
        userRepository.save(u);

        assertTrue(userRepository.findByPasswordResetToken("reset").isPresent());
        assertTrue(userRepository.findByEmailVerificationToken("verify").isPresent());
    }
}
