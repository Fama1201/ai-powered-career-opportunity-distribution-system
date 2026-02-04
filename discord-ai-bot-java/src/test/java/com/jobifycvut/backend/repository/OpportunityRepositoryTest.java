package com.jobifycvut.backend.repository;

import com.jobifycvut.backend.model.Opportunity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1"
})
class OpportunityRepositoryTest {

    @Autowired
    private OpportunityRepository opportunityRepository;

    @Test
    void findByTitleOrCompanyContaining() {
        Opportunity o1 = new Opportunity();
        o1.setTitle("Java Intern");
        o1.setCompany("ACME");

        Opportunity o2 = new Opportunity();
        o2.setTitle("Frontend Intern");
        o2.setCompany("JavaSoft");

        opportunityRepository.saveAll(List.of(o1, o2));

        var page = opportunityRepository.findByTitleContainingIgnoreCaseOrCompanyContainingIgnoreCase(
                "java", "java", PageRequest.of(0, 10)
        );
        assertEquals(2, page.getTotalElements());
    }

    @Test
    void findByDiscordIdAndStatusFilters() {
        Opportunity open = new Opportunity();
        open.setDiscordId("7");
        open.setApplicationDeadline(LocalDate.now().plusDays(5));

        Opportunity closed = new Opportunity();
        closed.setDiscordId("7");
        closed.setApplicationDeadline(LocalDate.now().minusDays(1));

        Opportunity archived = new Opportunity();
        archived.setDiscordId("7");
        archived.setApplicationDeadline(null);

        opportunityRepository.saveAll(List.of(open, closed, archived));

        assertEquals(1, opportunityRepository
                .findByDiscordIdAndApplicationDeadlineGreaterThanEqualOrderByIdDesc("7", LocalDate.now())
                .size());
        assertEquals(1, opportunityRepository
                .findByDiscordIdAndApplicationDeadlineLessThanOrderByIdDesc("7", LocalDate.now())
                .size());
        assertEquals(1, opportunityRepository
                .findByDiscordIdAndApplicationDeadlineIsNullOrderByIdDesc("7")
                .size());
    }
}
