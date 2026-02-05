package com.jobifycvut.backend.service;

import com.jobifycvut.backend.dto.OpportunityDetailResponse;
import com.jobifycvut.backend.exception.ResourceNotFoundException;
import com.jobifycvut.backend.model.JobApplicationEntity;
import com.jobifycvut.backend.model.Opportunity;
import com.jobifycvut.backend.model.SavedJob;
import com.jobifycvut.backend.repository.JobApplicationRepository;
import com.jobifycvut.backend.repository.OpportunityRepository;
import com.jobifycvut.backend.repository.SavedJobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OpportunityServiceTest {

    @Mock
    private OpportunityRepository opportunityRepository;
    @Mock
    private JobApplicationRepository jobApplicationRepository;
    @Mock
    private SavedJobRepository savedJobRepository;

    @InjectMocks
    private OpportunityService service;

    private Opportunity opportunity;

    @BeforeEach
    void setUp() {
        opportunity = new Opportunity();
        opportunity.setId(10L);
        opportunity.setTitle("Java Intern");
        opportunity.setCompany("TestCo");
    }

    @Test
    void getOpportunityById_withFlags() {
        when(opportunityRepository.findById(10L)).thenReturn(Optional.of(opportunity));
        when(jobApplicationRepository.existsByUserIdAndOpportunityId(5L, 10L)).thenReturn(true);
        when(savedJobRepository.existsByUserIdAndOpportunityId(5L, 10L)).thenReturn(false);

        OpportunityDetailResponse response = service.getOpportunityById(10L, 5L);

        assertEquals(10L, response.getId());
        assertTrue(response.isApplied());
        assertFalse(response.isSaved());
    }

    @Test
    void getOpportunityById_missing_throws() {
        when(opportunityRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.getOpportunityById(99L));
    }

    @Test
    void searchOpportunities_mapsResults() {
        // Corrección: Usamos searchWithFilters que es lo que llama el servicio real
        when(opportunityRepository.searchWithFilters(
                eq("java"), 
                any(), 
                any(), 
                any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of(opportunity)));

        var page = service.searchOpportunities("java", PageRequest.of(0, 10), null, null);
        assertEquals(1, page.getTotalElements());
        assertEquals("Java Intern", page.getContent().get(0).getTitle());
    }

    @Test
    void saveOpportunity_savesWhenNotAlreadySaved() {
        when(opportunityRepository.findById(10L)).thenReturn(Optional.of(opportunity));
        when(savedJobRepository.existsByUserIdAndOpportunityId(5L, 10L)).thenReturn(false);

        service.saveOpportunity(10L, 5L);

        ArgumentCaptor<SavedJob> captor = ArgumentCaptor.forClass(SavedJob.class);
        verify(savedJobRepository).save(captor.capture());
        assertEquals(5L, captor.getValue().getUserId());
        assertEquals(10L, captor.getValue().getOpportunity().getId());
    }

    @Test
    void saveOpportunity_whenAlreadySaved_throws() {
        when(opportunityRepository.findById(10L)).thenReturn(Optional.of(opportunity));
        when(savedJobRepository.existsByUserIdAndOpportunityId(5L, 10L)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> service.saveOpportunity(10L, 5L));
    }

    @Test
    void applyToOpportunity_savesWhenNotAlreadyApplied() {
        when(opportunityRepository.findById(10L)).thenReturn(Optional.of(opportunity));
        when(jobApplicationRepository.existsByUserIdAndOpportunityId(5L, 10L)).thenReturn(false);

        service.applyToOpportunity(10L, 5L);

        ArgumentCaptor<JobApplicationEntity> captor = ArgumentCaptor.forClass(JobApplicationEntity.class);
        verify(jobApplicationRepository).save(captor.capture());
        assertEquals(5L, captor.getValue().getUserId());
        assertEquals(10L, captor.getValue().getOpportunityId());
        assertEquals("APPLIED", captor.getValue().getStatus());
    }

    @Test
    void applyToOpportunity_whenAlreadyApplied_throws() {
        when(opportunityRepository.findById(10L)).thenReturn(Optional.of(opportunity));
        when(jobApplicationRepository.existsByUserIdAndOpportunityId(5L, 10L)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> service.applyToOpportunity(10L, 5L));
    }
}
