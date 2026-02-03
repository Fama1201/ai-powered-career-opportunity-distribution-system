package com.jobifycvut.backend.dto;

public class ChatResponse {
    private String reply;
    private Integer cvScore;
    private String summary;
    private java.util.List<String> missingSkills;
    private java.util.List<String> recommendations;
    private java.util.List<MatchItem> topMatches;
    private java.util.List<AssignmentItem> assignments;

    public ChatResponse() {
    }

    public ChatResponse(String reply) {
        this.reply = reply;
    }

    public ChatResponse(String reply,
                        Integer cvScore,
                        String summary,
                        java.util.List<String> missingSkills,
                        java.util.List<String> recommendations,
                        java.util.List<MatchItem> topMatches,
                        java.util.List<AssignmentItem> assignments) {
        this.reply = reply;
        this.cvScore = cvScore;
        this.summary = summary;
        this.missingSkills = missingSkills;
        this.recommendations = recommendations;
        this.topMatches = topMatches;
        this.assignments = assignments;
    }

    public String getReply() {
        return reply;
    }
    public void setReply(String reply) {
        this.reply = reply;
    }
    public Integer getCvScore() {
        return cvScore;
    }
    public void setCvScore(Integer cvScore) {
        this.cvScore = cvScore;
    }
    public String getSummary() {
        return summary;
    }
    public void setSummary(String summary) {
        this.summary = summary;
    }
    public java.util.List<String> getMissingSkills() {
        return missingSkills;
    }
    public void setMissingSkills(java.util.List<String> missingSkills) {
        this.missingSkills = missingSkills;
    }
    public java.util.List<String> getRecommendations() {
        return recommendations;
    }
    public void setRecommendations(java.util.List<String> recommendations) {
        this.recommendations = recommendations;
    }
    public java.util.List<MatchItem> getTopMatches() {
        return topMatches;
    }
    public void setTopMatches(java.util.List<MatchItem> topMatches) {
        this.topMatches = topMatches;
    }
    public java.util.List<AssignmentItem> getAssignments() {
        return assignments;
    }
    public void setAssignments(java.util.List<AssignmentItem> assignments) {
        this.assignments = assignments;
    }

    public static class MatchItem {
        private String id;
        private String title;
        private String company;
        private String url;
        private Integer score;
        private String matchReason;

        public MatchItem() {
        }

        public MatchItem(String id, String title, String company, String url, Integer score, String matchReason) {
            this.id = id;
            this.title = title;
            this.company = company;
            this.url = url;
            this.score = score;
            this.matchReason = matchReason;
        }

        public String getId() {
            return id;
        }
        public void setId(String id) {
            this.id = id;
        }
        public String getTitle() {
            return title;
        }
        public void setTitle(String title) {
            this.title = title;
        }
        public String getCompany() {
            return company;
        }
        public void setCompany(String company) {
            this.company = company;
        }
        public String getUrl() {
            return url;
        }
        public void setUrl(String url) {
            this.url = url;
        }
        public Integer getScore() {
            return score;
        }
        public void setScore(Integer score) {
            this.score = score;
        }
        public String getMatchReason() {
            return matchReason;
        }
        public void setMatchReason(String matchReason) {
            this.matchReason = matchReason;
        }
    }

    public static class AssignmentItem {
        private String title;
        private String description;
        private String estimatedTime;

        public AssignmentItem() {
        }

        public AssignmentItem(String title, String description, String estimatedTime) {
            this.title = title;
            this.description = description;
            this.estimatedTime = estimatedTime;
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
        public String getEstimatedTime() {
            return estimatedTime;
        }
        public void setEstimatedTime(String estimatedTime) {
            this.estimatedTime = estimatedTime;
        }
    }
}
