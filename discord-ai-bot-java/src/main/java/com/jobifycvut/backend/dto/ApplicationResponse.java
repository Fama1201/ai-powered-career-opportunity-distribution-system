package com.jobifycvut.backend.dto;

import net.dv8tion.jda.api.entities.MessageActivity;

import java.time.OffsetDateTime;

public class ApplicationResponse {
    public Long id;
    public Long userId;
    public Long opportunityId;
    public OffsetDateTime appliedAt;
    public String status;
    public String notes;

    public static ApplicationResponse from(
            Long id,
            Long userId,
            Long opportunityId,
            OffsetDateTime appliedAt,
            String status,
            String notes
    ) {
        ApplicationResponse r= new ApplicationResponse();
        r.id = id;
        r.userId = userId;
        r.opportunityId = opportunityId;
        r.appliedAt = appliedAt;
        r.status = status;
        r.notes = notes;
        return r;

    }
}
