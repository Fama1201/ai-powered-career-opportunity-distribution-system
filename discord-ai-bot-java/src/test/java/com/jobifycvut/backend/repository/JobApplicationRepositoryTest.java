package com.jobifycvut.backend.repository;

import com.jobifycvut.backend.model.JobApplicationEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1"
})
class JobApplicationRepositoryTest {

    @Autowired
    private JobApplicationRepository repository;

    @Test
    void existsAndFindersWork() {
        JobApplicationEntity a1 = new JobApplicationEntity();
        a1.setUserId(1L);
        a1.setOpportunityId(10L);
        a1.setAppliedAt(OffsetDateTime.now());
        a1.setStatus("APPLIED");

        JobApplicationEntity a2 = new JobApplicationEntity();
        a2.setUserId(2L);
        a2.setOpportunityId(10L);
        a2.setAppliedAt(OffsetDateTime.now());
        a2.setStatus("HIRED");

        repository.saveAll(List.of(a1, a2));

        assertTrue(repository.existsByUserIdAndOpportunityId(1L, 10L));
        assertEquals(1, repository.findByUserId(1L).size());
        assertEquals(1, repository.findByStatusIgnoreCase("hired").size());
        assertEquals(2, repository.findByOpportunityId(10L).size());
        assertEquals(1, repository.findByUserIdAndOpportunityId(2L, 10L).size());
        assertEquals(1, repository.findByUserIdAndStatusIgnoreCase(2L, "HIRED").size());
        assertEquals(2, repository.countByUserId(1L) + repository.countByUserId(2L));
    }
}
