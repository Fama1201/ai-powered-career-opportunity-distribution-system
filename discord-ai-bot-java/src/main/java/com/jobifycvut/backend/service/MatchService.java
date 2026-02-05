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

        Set<String> interestKeywords = extractKeywords(careerInterest);
        Set<String> skillKeywords = extractKeywords(skills, cvText);

        // Pull a reasonable batch and score locally.
        // This avoids "full string contains" issues when keywords are many.
        // Pull multiple pages so we have enough candidates to score.
        List<Opportunity> candidates = new java.util.ArrayList<>();
        int page = 0;
        int pageSize = 200;
        while (candidates.size() < 800) {
            Pageable pageable = PageRequest.of(page, pageSize);
            var result = opportunityRepository.findAll(pageable);
            if (result.isEmpty()) break;
            candidates.addAll(
                    result.getContent().stream()
                            .filter(o -> o.getApplicationDeadline() == null || !o.getApplicationDeadline().isBefore(java.time.LocalDate.now()))
                            .toList()
            );
            if (!result.hasNext()) break;
            page++;
        }

        List<Opportunity> filtered = candidates;
        if (!interestKeywords.isEmpty()) {
            filtered = candidates.stream()
                    .filter(o -> containsAnyKeyword(o, interestKeywords))
                    .toList();
        }
        if (filtered.isEmpty()) {
            filtered = candidates;
        }

        // Score and sort by match quality (skills only)
        List<ScoredOpportunity> scored = filtered.stream()
                .map(opp -> new ScoredOpportunity(
                        opp,
                        calculateMatchScore(opp, skillKeywords)
                ))
                .sorted((a, b) -> Integer.compare(b.score, a.score))
                .collect(Collectors.toList());

        if (limit > 0 && scored.size() > limit) {
            scored = scored.subList(0, limit);
        }

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
    private int calculateMatchScore(Opportunity opp, Set<String> skillKeywords) {
        String oppRequirements = safeTrim(opp.getTechnicalRequirements()) + " " + safeTrim(opp.getFormalRequirements());
        Set<String> oppKeywords = extractKeywords(oppRequirements, opp.getTitle(), opp.getDescription());

        // Skill match based on explicit skill tokens
        Set<String> normalizedSkills = normalizeSkills(skillKeywords);
        Set<String> normalizedOpp = normalizeSkills(oppKeywords);

        int skillsMatchPct = calculateOverlap(normalizedSkills, normalizedOpp); // 0-100

        // Boost if key skills appear in technical requirements/title
        int boost = 0;
        for (String sk : normalizedSkills) {
            if (normalizedOpp.contains(sk)) boost += 5;
        }

        int score = Math.min(100, skillsMatchPct + boost);
        if (score > 0 && score < 40) score = 40; // ensure visible minimum for matching roles
        return score;
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

    private boolean containsAnyKeyword(Opportunity opp, Set<String> interestKeywords) {
        String text = (safeTrim(opp.getTitle()) + " " +
                safeTrim(opp.getDescription()) + " " +
                safeTrim(opp.getTechnicalRequirements()) + " " +
                safeTrim(opp.getFormalRequirements())).toLowerCase();
        for (String kw : interestKeywords) {
            if (text.contains(kw.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private Set<String> normalizeSkills(Set<String> raw) {
        Set<String> out = new HashSet<>();
        for (String t : raw) {
            String s = t.toLowerCase();
            if (s.equals("javascript") || s.equals("js")) out.add("javascript");
            else if (s.equals("typescript") || s.equals("ts")) out.add("typescript");
            else if (s.equals("c++") || s.equals("cpp")) out.add("c++");
            else if (s.equals("c#") || s.equals("csharp")) out.add("c#");
            else if (s.equals("springboot") || s.equals("spring")) out.add("spring");
            else if (s.equals("nodejs") || s.equals("node")) out.add("node");
            else out.add(s);
        }
        return out;
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
