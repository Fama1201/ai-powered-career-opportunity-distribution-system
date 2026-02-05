package com.jobifycvut.backend.service;

import bot.api.OpportunityClient;
import com.jobifycvut.backend.model.Opportunity;
import com.jobifycvut.backend.repository.OpportunityRepository;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class OpportunitySyncService {

    private static final String SOURCE_DISCORD_ID = "EDUMATCH";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final OpportunityRepository opportunityRepository;
    private final JobSearchClient jobSearchClient;

    public OpportunitySyncService(OpportunityRepository opportunityRepository,
                                  JobSearchClient jobSearchClient) {
        this.opportunityRepository = opportunityRepository;
        this.jobSearchClient = jobSearchClient;
    }

    public int syncFromEdumatch() {
        String keywords = String.join(",",
                List.of("intern", "internship", "software", "developer", "engineer",
                        "backend", "frontend", "data", "ml", "devops"));

        Set<OpportunityClient.Opportunity> found = jobSearchClient.searchMultipleKeywords(keywords);

        int upserted = 0;
        for (OpportunityClient.Opportunity o : found) {
            String opportunityId = safeTrim(o.id);
            if (opportunityId.isEmpty()) continue;

            Opportunity entity = findExisting(opportunityId)
                    .orElseGet(Opportunity::new);

            entity.setOpportunityId(opportunityId);
            entity.setDiscordId(SOURCE_DISCORD_ID);
            entity.setTitle(safeTrim(o.title));
            entity.setCompany(safeTrim(o.company));
            entity.setDescription(safeTrim(o.description));
            entity.setJobType(safeTrim(o.type));
            entity.setUrl(safeTrim(o.url));
            entity.setWage(safeTrim(o.wage));
            entity.setHomeOffice(safeTrim(o.homeOffice));
            entity.setBenefits(safeTrim(o.benefits));
            entity.setFormalRequirements(safeTrim(o.formReq));
            entity.setTechnicalRequirements(safeTrim(o.techReq));
            entity.setContactPerson(safeTrim(o.contactPerson));
            entity.setApplicationDeadline(parseDate(o.deadline));

            opportunityRepository.save(entity);
            upserted++;
        }

        return upserted;
    }

    public int archiveExpired(LocalDate today) {
        List<Opportunity> expired = opportunityRepository.findByDiscordIdAndApplicationDeadlineLessThanOrderByIdDesc(
                SOURCE_DISCORD_ID,
                today
        );
        for (Opportunity o : expired) {
            o.setApplicationDeadline(null);
        }
        opportunityRepository.saveAll(expired);
        return expired.size();
    }

    @Scheduled(cron = "0 0 3 * * *")
    public void scheduledSync() {
        int count = syncFromEdumatch();
        int archived = archiveExpired(LocalDate.now());
        System.out.println("✅ Daily EDUMATCH sync completed: " + count + " opportunities");
        System.out.println("🗄️ Archived expired opportunities: " + archived);
    }

    private Optional<Opportunity> findExisting(String opportunityId) {
        return opportunityRepository.findByOpportunityIdAndDiscordId(opportunityId, SOURCE_DISCORD_ID);
    }

    private LocalDate parseDate(String deadline) {
        String d = safeTrim(deadline);
        if (d.isEmpty() || d.equalsIgnoreCase("N/A")) return null;
        try {
            return LocalDate.parse(d, DATE_FMT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }
}
