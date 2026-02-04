package com.jobifycvut.backend.repository;

import com.jobifycvut.backend.model.InteractionEntity;
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
class InteractionRepositoryTest {

    @Autowired
    private InteractionRepository repository;

    @Test
    void findsByStudentAndActionOrdered() {
        InteractionEntity older = new InteractionEntity();
        older.setStudentId(1L);
        older.setAction("AI_CHAT");
        older.setPrompt("p1");
        older.setResponse("r1");
        older.setCreatedAt(OffsetDateTime.now().minusHours(2));

        InteractionEntity newer = new InteractionEntity();
        newer.setStudentId(1L);
        newer.setAction("AI_CHAT");
        newer.setPrompt("p2");
        newer.setResponse("r2");
        newer.setCreatedAt(OffsetDateTime.now().minusHours(1));

        repository.saveAll(List.of(older, newer));

        List<InteractionEntity> out = repository.findTop50ByStudentIdAndActionOrderByCreatedAtDesc(1L, "AI_CHAT");
        assertEquals(2, out.size());
        assertEquals("p2", out.get(0).getPrompt());
    }
}
