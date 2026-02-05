package com.jobifycvut.backend.service;

import com.jobifycvut.backend.dto.OpportunityListResponse;
import com.jobifycvut.backend.model.Opportunity;
import com.jobifycvut.backend.model.StudentEntity;
import com.jobifycvut.backend.repository.OpportunityRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for matching students with job opportunities based on:
 * - Skills overlap
 * - Career interest alignment
 * - Technical requirements match
 * - Location preferences
 */
@Service
public class MatchService {

    private final OpportunityRepository opportunityRepository;

    public MatchService(OpportunityRepository opportunityRepository) {
        this.opportunityRepository = opportunityRepository;
    }

    /**
     * Find matching opportunities for a student
     * Returns opportunities sorted by match score (highest first)
     */
    public List<OpportunityListResponse> findMatches(StudentEntity student, int limit) {
        if (student == null) {
            return Collections.emptyList();
        }

        String skills = safeTrim(student.getSkills());
        String careerInterest = safeTrim(student.getCareerInterest());
        String cvText = safeTrim(student.getCvText());

        // Build search keywords from student profile
        Set<String> keywords = extractKeywords(skills, careerInterest, cvText);

        if (keywords.isEmpty()) {
            // Fallback: return recent opportunities
            return opportunityRepository.findAll(PageRequest.of(0, limit))
                    .stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        }

        // Pull a reasonable batch and score locally.
        // This avoids "full string contains" issues when keywords are many.
        Pageable pageable = PageRequest.of(0, 200);
        List<Opportunity> candidates = opportunityRepository.findAll(pageable).getContent();

        // Score and sort by match quality
        List<ScoredOpportunity> scored = candidates.stream()
                .map(opp -> new ScoredOpportunity(
                        opp,
                        calculateMatchScore(opp, student, keywords)
                ))
                .sorted((a, b) -> Integer.compare(b.score, a.score))
                .limit(limit)
                .collect(Collectors.toList());

        return scored.stream()
                .map(so -> {
                    OpportunityListResponse response = mapToResponse(so.opportunity);
                    response.setMatchScore(so.score);
                    return response;
                })
                .collect(Collectors.toList());
    }

    /**
     * Calculate match score (0-100) for an opportunity
     */
    private int calculateMatchScore(Opportunity opp, StudentEntity student, Set<String> studentKeywords) {
        String oppRequirements = safeTrim(opp.getTechnicalRequirements()) + " " + safeTrim(opp.getFormalRequirements());
        Set<String> oppKeywords = extractKeywords(oppRequirements, opp.getTitle(), opp.getDescription());

        int skillsMatchPct = calculateOverlap(studentKeywords, oppKeywords); // 0-100

        String interest = safeTrim(student.getCareerInterest());
        int interestMatchPct = 0;
        if (!interest.isEmpty()) {
            String oppText = (safeTrim(opp.getTitle()) + " " + safeTrim(opp.getDescription())).toLowerCase();
            if (oppText.contains(interest.toLowerCase())) {
                interestMatchPct = 100;
            } else {
                String[] interestWords = interest.toLowerCase().split("\\s+");
                int matches = 0;
                for (String word : interestWords) {
                    if (oppText.contains(word)) matches++;
                }
                interestMatchPct = (int) ((matches / (double) interestWords.length) * 100);
            }
        }

        String titleDesc = (safeTrim(opp.getTitle()) + " " + safeTrim(opp.getDescription())).toLowerCase();
        int keywordMatches = 0;
        for (String keyword : studentKeywords) {
            if (titleDesc.contains(keyword.toLowerCase())) {
                keywordMatches++;
            }
        }
        int keywordMatchPct = studentKeywords.isEmpty()
                ? 0
                : (int) ((keywordMatches / (double) studentKeywords.size()) * 100);

        // Weighted score: skills 50%, interest 30%, keywords 20%
        double weighted = (skillsMatchPct * 0.5) + (interestMatchPct * 0.3) + (keywordMatchPct * 0.2);

        // Small bump if job type is present (signal of higher quality)
        if (opp.getJobType() != null && !opp.getJobType().isEmpty()) {
            weighted += 5;
        }

        int score = (int) Math.round(weighted);
        if (score > 0 && score < 30) score = 30; // avoid tiny-looking matches
        return Math.min(100, score);
    }

    /**
     * Extract keywords from text (simple tokenization)
     */
    private Set<String> extractKeywords(String... texts) {
        Set<String> keywords = new HashSet<>();
        for (String text : texts) {
            if (text == null || text.isEmpty()) continue;
            
            // Tokenize: split by spaces, remove special chars, lowercase
            String[] tokens = text.toLowerCase()
                    .replaceAll("[^a-z0-9\\s]", " ")
                    .split("\\s+");
            
            // Filter: keep only meaningful words (length > 2)
            for (String token : tokens) {
                if (token.length() > 2) {
                    keywords.add(token);
                }
            }
        }
        return keywords;
    }

    /**
     * Calculate overlap percentage between two keyword sets
     */
    private int calculateOverlap(Set<String> set1, Set<String> set2) {
        if (set1.isEmpty() || set2.isEmpty()) return 0;
        
        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);
        
        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);
        
        if (union.isEmpty()) return 0;
        
        // Jaccard similarity * 100
        return (int) ((intersection.size() / (double) union.size()) * 100);
    }

    private String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }

    private OpportunityListResponse mapToResponse(Opportunity opp) {
        String companyName = "Unknown";
        if (opp.getCompany() != null && !opp.getCompany().isEmpty()) {
            companyName = opp.getCompany();
        }
        
        return new OpportunityListResponse(
                opp.getId(),
                opp.getTitle(),
                companyName,
                opp.getJobType(),
                opp.getHomeOffice(),
                opp.getWage(),
                opp.getApplicationDeadline()
        );
    }

    /**
     * Helper class for scoring opportunities
     */
    private static class ScoredOpportunity {
        final Opportunity opportunity;
        final int score;

        ScoredOpportunity(Opportunity opportunity, int score) {
            this.opportunity = opportunity;
            this.score = score;
        }
    }
}
