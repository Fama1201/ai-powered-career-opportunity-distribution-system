package com.jobifycvut.backend.service;

import com.jobifycvut.backend.dto.CvResponse;
import com.jobifycvut.backend.dto.StudentProfileResponse;
import com.jobifycvut.backend.model.StudentEntity;
import com.jobifycvut.backend.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HrStudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private HrStudentService service;

    @Captor
    private ArgumentCaptor<String> stringCaptor;

    private StudentEntity student;

    @BeforeEach
    void setUp() {
        student = new StudentEntity();
        student.setId(1L);
        student.setName("Jane Doe");
        student.setEmail("jane@example.com");
        student.setSkills("Java, Spring");
        student.setCareerInterest("Backend");
        student.setCvText("CV text");
    }

    @Test
    void getProfile_returnsProfile() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

        StudentProfileResponse response = service.getProfile(1L);

        assertEquals(1L, response.getId());
        assertEquals("Jane Doe", response.getName());
        assertEquals("jane@example.com", response.getEmail());
        assertEquals("Java, Spring", response.getSkills());
        assertEquals("Backend", response.getCareerInterest());
    }

    @Test
    void getProfile_nullId_throwsBadRequest() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.getProfile(null));
        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void getProfile_missingStudent_throwsNotFound() {
        when(studentRepository.findById(2L)).thenReturn(Optional.empty());
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.getProfile(2L));
        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void getCv_withCv_returnsHasCvTrue() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

        CvResponse response = service.getCv(1L);

        assertTrue(response.isHasCv());
        assertEquals("CV text", response.getCvText());
    }

    @Test
    void getCv_withoutCv_returnsHasCvFalse() {
        student.setCvText("  ");
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

        CvResponse response = service.getCv(1L);

        assertFalse(response.isHasCv());
        assertNull(response.getCvText());
    }

    @Test
    void search_trimsAndPassesNulls() {
        when(studentRepository.search(any(), any(), any())).thenReturn(List.of(student));

        List<StudentProfileResponse> result = service.search("  ", "Backend", "  java  ");

        assertEquals(1, result.size());
        verify(studentRepository).search(
                isNull(),
                eq("Backend"),
                eq("java")
        );
    }
}
