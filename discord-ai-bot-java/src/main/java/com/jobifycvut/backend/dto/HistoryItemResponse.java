package com.jobifycvut.backend.dto;

import java.time.OffsetDateTime;

public class HistoryItemResponse {
    private Long id;
    private String action;
    private String prompt;
    private String response;
    private OffsetDateTime createdAt;

    public HistoryItemResponse(Long id, String action, String prompt, String response,  OffsetDateTime createdAt) {
        this.id = id;
        this.action = action;
        this.prompt = prompt;
        this.response = response;
        this.createdAt = OffsetDateTime.now();
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getAction() {
        return action;
    }
    public void setAction(String action) {
        this.action = action;
    }
    public String getPrompt() {
        return prompt;
    }
    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }
    public String getResponse() {
        return response;
    }
    public void setResponse(String response) {
        this.response = response;
    }
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
