package com.jobifycvut.backend.repository;

import com.jobifycvut.backend.model.StudentEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1"
})
class StudentRepositoryTest {

    @Autowired
    private StudentRepository studentRepository;

    @Test
    void search_filtersBySkillsMajorKeywords() {
        StudentEntity s1 = new StudentEntity();
        s1.setEmail("a@example.com");
        s1.setName("Alice");
        s1.setSkills("Java, Spring");
        s1.setCareerInterest("Backend");
        s1.setCvText("Experienced in REST APIs");

        StudentEntity s2 = new StudentEntity();
        s2.setEmail("b@example.com");
        s2.setName("Bob");
        s2.setSkills("Python, ML");
        s2.setCareerInterest("Data");
        s2.setCvText("Machine learning projects");

        studentRepository.saveAll(List.of(s1, s2));

        List<StudentEntity> bySkills = studentRepository.search("Java", null, null);
        assertEquals(1, bySkills.size());
        assertEquals("Alice", bySkills.get(0).getName());

        List<StudentEntity> byMajor = studentRepository.search(null, "Data", null);
        assertEquals(1, byMajor.size());
        assertEquals("Bob", byMajor.get(0).getName());

        List<StudentEntity> byKeyword = studentRepository.search(null, null, "Alice");
        assertEquals(1, byKeyword.size());
        assertEquals("Alice", byKeyword.get(0).getName());
    }
}
