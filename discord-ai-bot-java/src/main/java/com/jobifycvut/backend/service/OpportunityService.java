package com.jobifycvut.backend.service;

import com.jobifycvut.backend.dto.OpportunityDetailResponse;
import com.jobifycvut.backend.dto.OpportunityListResponse;
import com.jobifycvut.backend.exception.ResourceNotFoundException;
import com.jobifycvut.backend.model.JobApplication;
import com.jobifycvut.backend.model.Opportunity;
import com.jobifycvut.backend.model.SavedJob;
import com.jobifycvut.backend.repository.JobApplicationRepository;
import com.jobifycvut.backend.repository.OpportunityRepository;
import com.jobifycvut.backend.repository.SavedJobRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class OpportunityService {
    private final OpportunityRepository opportunityRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final SavedJobRepository savedJobRepository;

    public OpportunityService(OpportunityRepository opportunityRepository,
                              JobApplicationRepository jobApplicationRepository,
                              SavedJobRepository savedJobRepository) {
        this.opportunityRepository = opportunityRepository;
        this.jobApplicationRepository = jobApplicationRepository;
        this.savedJobRepository = savedJobRepository;
    }



    //get /api/jobs
    public Page<OpportunityListResponse> getAllOpportunities(Pageable pageable) {
        return opportunityRepository.findAll(pageable).map(this::mapToListResponse);
    }
    // GET /api/jobs/ {jobId} details
    public OpportunityDetailResponse getOpportunityById(Long id) {
        Opportunity opportunity = opportunityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Opportunity not found with id: " + id));

        return mapToDetailResponse(opportunity);
    }

    // GET /api/jobs/search?keyword= means shows every job opportunity avaiable
    public Page<OpportunityListResponse> searchOpportunities(String keyword, Pageable pageable) {
        return opportunityRepository
                .findByTitleContainingIgnoreCaseOrCompanyContainingIgnoreCase(keyword, keyword, pageable)
                .map(this::mapToListResponse);
    }

    // Book or save the job opportunity.
    public void saveOpportunity(Long jobId, Long userId) {

        Opportunity opportunity = opportunityRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException(("Opportunity not found with id: " + jobId)));

        boolean alreadySaved=savedJobRepository.existsByUserIdAndOpportunityId(userId, opportunity.getId());

        if(alreadySaved){
            throw new IllegalStateException(("Opportunity already saved."));
        }

        SavedJob savedJob = new SavedJob();
        savedJob.setUserId(userId);
        savedJob.setOpportunity(opportunity);

        savedJobRepository.save(savedJob);
    }

    //Apply for a job opportunity
    public void applyToOpportunity(Long jobId, Long userId) {

        Opportunity opportunity= opportunityRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Opportunity not found with id: " + jobId));

        boolean alreadyApplied=jobApplicationRepository.existsByUserIdAndOpportunityId(userId, opportunity.getId());

        if(alreadyApplied){
            throw new IllegalStateException(("Opportunity already applied."));
        }
        JobApplication jobApplication = new JobApplication();

        jobApplication.setUserId(userId);
        jobApplication.setOpportunity(opportunity);

        jobApplicationRepository.save(jobApplication);

    }





    private OpportunityListResponse mapToListResponse(Opportunity o){
        OpportunityListResponse response=new OpportunityListResponse();
        response.setId(o.getId());
        response.setTitle(o.getTitle());
        response.setCompany(o.getCompany());
        response.setJobType(o.getJobType());
        response.setHomeOffice(o.getHomeOffice());
        response.setWage(o.getWage());
        response.setApplicationDeadline(o.getApplicationDeadline());

        return response;
    }

    private OpportunityDetailResponse mapToDetailResponse(Opportunity o){
        OpportunityDetailResponse response=new OpportunityDetailResponse();
        response.setId(o.getId());
        response.setTitle(o.getTitle());
        response.setCompany(o.getCompany());
        response.setJobType(o.getJobType());
        response.setHomeOffice(o.getHomeOffice());
        response.setWage(o.getWage());
        response.setApplicationDeadline(o.getApplicationDeadline());
        response.setBenefits(o.getBenefits());
        response.setFormalRequirements(o.getFormalRequirements());
        response.setTechnicalRequirements(o.getTechnicalRequirements());
        response.setContactPerson(o.getContactPerson());
        response.setUrl(o.getUrl());
        return response;
    }

    private OpportunityDetailResponse getOpportunityById(Long id, Long userId) {
        Opportunity opportunity = opportunityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Opportunity not found with id: " + id));

        OpportunityDetailResponse response = mapToDetailResponse(opportunity);

        if (userId != null) {
            boolean applied = jobApplicationRepository
                    .existsByUserIdAndOpportunityId(userId, id);
            boolean saved = savedJobRepository
                    .existsByUserIdAndOpportunityId(userId, id);

            response.setApplied(applied);
            response.setSaved(saved);
        }

        return response;
    }


}