package com.jobifycvut.backend.repository;

import com.jobifycvut.backend.model.HrUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1"
})
class HrUserRepositoryTest {

    @Autowired
    private HrUserRepository hrUserRepository;

    @Test
    void findByEmailAndExists() {
        HrUser hr = new HrUser();
        hr.setEmail("hr@example.com");
        hr.setPasswordHash("hash");
        hr.setFullName("HR User");
        hr.setCompanyName("Company");
        hrUserRepository.save(hr);

        assertTrue(hrUserRepository.existsByEmail("hr@example.com"));
        assertTrue(hrUserRepository.findByEmail("hr@example.com").isPresent());
    }
}
