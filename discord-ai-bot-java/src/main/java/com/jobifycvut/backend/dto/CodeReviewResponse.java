package com.jobifycvut.backend.dto;

import java.util.List;

public class CodeReviewResponse {
    private String summary;
    private List<IssueItem> issues;
    private List<String> suggestions;

    public CodeReviewResponse() {
    }

    public CodeReviewResponse(String summary, List<IssueItem> issues, List<String> suggestions) {
        this.summary = summary;
        this.issues = issues;
        this.suggestions = suggestions;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<IssueItem> getIssues() {
        return issues;
    }

    public void setIssues(List<IssueItem> issues) {
        this.issues = issues;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions;
    }

    public static class IssueItem {
        private String severity;
        private String title;
        private String description;

        public IssueItem() {
        }

        public IssueItem(String severity, String title, String description) {
            this.severity = severity;
            this.title = title;
            this.description = description;
        }

        public String getSeverity() {
            return severity;
        }

        public void setSeverity(String severity) {
            this.severity = severity;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}
