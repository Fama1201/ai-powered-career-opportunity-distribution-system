package com.jobifycvut.backend.repository;

import com.jobifycvut.backend.model.Opportunity;
import com.jobifycvut.backend.model.SavedJob;
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
class SavedJobRepositoryTest {

    @Autowired
    private SavedJobRepository savedJobRepository;

    @Autowired
    private OpportunityRepository opportunityRepository;

    @Test
    void existsAndCountWork() {
        Opportunity opp = new Opportunity();
        opp.setTitle("Intern");
        opp.setCompany("ACME");
        opp = opportunityRepository.save(opp);

        SavedJob saved = new SavedJob();
        saved.setUserId(5L);
        saved.setOpportunity(opp);
        savedJobRepository.save(saved);

        assertTrue(savedJobRepository.existsByUserIdAndOpportunityId(5L, opp.getId()));
        assertEquals(1, savedJobRepository.countByUserId(5L));
    }
}
