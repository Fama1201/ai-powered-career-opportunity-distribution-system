package com.jobifycvut.backend.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jobifycvut.backend.dto.ChatResponse;
import com.jobifycvut.backend.dto.CodeReviewRequest;
import com.jobifycvut.backend.dto.CodeReviewResponse;
import com.jobifycvut.backend.dto.AssignmentRequest;
import com.jobifycvut.backend.model.InteractionEntity;
import com.jobifycvut.backend.model.LearningAssignment;
import com.jobifycvut.backend.model.AssignmentSubmission;
import com.jobifycvut.backend.model.StudentEntity;
import com.jobifycvut.backend.repository.AssignmentSubmissionRepository;
import com.jobifycvut.backend.repository.InteractionRepository;
import com.jobifycvut.backend.repository.LearningAssignmentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class AiService {
    private final InteractionRepository interactionRepository;
    private final StudentContextService studentContextService;
    private final JobSearchClient jobSearchClient;
    private final OpenAiClient openAiClient;
    private final LearningAssignmentRepository learningAssignmentRepository;
    private final AssignmentSubmissionRepository assignmentSubmissionRepository;
    private final Gson gson = new Gson();
    private static final List<String> ALLOWED_LANGUAGES = List.of(
            "c", "c++", "c#", "java", "python", "javascript", "typescript",
            "go", "rust", "kotlin", "swift", "php", "ruby", "scala",
            "sql", "bash", "html", "css", "r", "matlab"
    );

    public AiService(InteractionRepository interactionRepository,
                     StudentContextService studentContextService,
                     JobSearchClient jobSearchClient,
                     OpenAiClient openAiClient,
                     LearningAssignmentRepository learningAssignmentRepository,
                     AssignmentSubmissionRepository assignmentSubmissionRepository) {
        this.interactionRepository = interactionRepository;
        this.studentContextService = studentContextService;
        this.jobSearchClient = jobSearchClient;
        this.openAiClient = openAiClient;
        this.learningAssignmentRepository = learningAssignmentRepository;
        this.assignmentSubmissionRepository = assignmentSubmissionRepository;
    }

    public ChatResponse chat(Long userId, String message) {
        StudentEntity student = studentContextService.getOrCreateCurrentStudent();
        String cvText = safeTrim(student.getCvText());
        if (cvText.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "CV is empty. Please upload your CV first."
            );
        }

        String skills = safeTrim(student.getSkills());
        String careerInterest = safeTrim(student.getCareerInterest());
        String extra = safeTrim(message);
        boolean cvReviewOnly = containsCvReviewIntent(extra);
        boolean skillGapsOnly = containsSkillGapIntent(extra);
        boolean assignmentsRequested = containsAssignmentIntent(extra);
        String keywords = String.join(" ",
                List.of(skills, careerInterest, extra).stream().filter(s -> !s.isBlank()).toList());

        List<bot.api.OpportunityClient.Opportunity> jobs;
        if (cvReviewOnly || skillGapsOnly) {
            jobs = List.of();
        } else {
            Set<bot.api.OpportunityClient.Opportunity> rawJobs = jobSearchClient.searchMultipleKeywords(
                    keywords.isBlank() ? "software" : keywords
            );

            if (rawJobs.size() < 10) {
                Set<bot.api.OpportunityClient.Opportunity> fallback = jobSearchClient.searchMultipleKeywords(
                        "software internship backend developer"
                );
                rawJobs.addAll(fallback);
            }

            jobs = rawJobs.stream()
                    .sorted(Comparator.comparing(o -> safeTrim(o.title)))
                    .limit(20)
                    .toList();
        }

        String systemPrompt = """
                You are a CV reviewer and job matcher for student internships.
                Return JSON that matches the provided schema. Do not include any extra keys.
                Score CV from 0-100. Select the top 10 job matches with brief reasons.
                If the user asks for assignments/tasks/homework, include exactly 3 assignments.
                If the user message mentions assignments/tasks/homework in any way, include them.
                Otherwise return an empty assignments array.
                If the user only asks for CV feedback or skill gaps, return an empty topMatches array.
                """;

        String userPrompt = buildUserPrompt(student, trimForAi(cvText, 3000), jobs);

        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "OPENAI_API_KEY is not configured"
            );
        }

        String replyJson;
        try {
            replyJson = openAiClient.chatJson(
                    apiKey,
                    "gpt-4o-mini",
                    List.of(
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user", "content", userPrompt)
                    ),
                    buildSchema()
            );
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "AI provider error: " + e.getMessage()
            );
        }

        ChatResponse response = parseAiResponse(replyJson);
        if (response.getReply() == null || response.getReply().isBlank()) {
            response.setReply(response.getSummary());
        }

        if (assignmentsRequested) {
            ensureAssignments(response, skills, careerInterest);
        }

        if (!jobs.isEmpty()) {
            ensureTopMatches(response, jobs, skills, careerInterest, extra);
        }

        InteractionEntity row = new InteractionEntity();
        row.setStudentId(student.getId());
        row.setOpportunityId(null);
        row.setAction("AI_CHAT");
        row.setPrompt(userPrompt);
        row.setResponse(replyJson);
        interactionRepository.save(row);

        return response;
    }

    public List<InteractionEntity> chatHistory(Long userId) {
        return interactionRepository.findTop50ByStudentIdAndActionOrderByCreatedAtDesc(userId, "AI_CHAT");
    }

    public String cvReview(Long userId, String cvText) {
        String reply = "CV review (stub): Add measurable achievements, fix formatting, and tailor keywords.";
        StudentEntity student = studentContextService.getOrCreateCurrentStudent();
        InteractionEntity row = new InteractionEntity();

        row.setStudentId(student.getId());
        row.setAction("AI_CV_REVIEW");
        row.setPrompt(cvText);
        row.setResponse(reply);
        interactionRepository.save(row);
        return reply;
    }

    public String careerAdvice(Long userId, String prompt) {
        String reply = "Career advice (stub): Pick a target role, build 2 projects, and practice interviews weekly.";
        StudentEntity student = studentContextService.getOrCreateCurrentStudent();
        InteractionEntity row = new InteractionEntity();
        row.setStudentId(student.getId());
        row.setAction("AI_CAR_ADVICE");
        row.setPrompt(prompt);
        row.setResponse(reply);
        interactionRepository.save(row);
        return reply;
    }

    public CodeReviewResponse codeReview(Long userId, CodeReviewRequest req) {
        String language = safeTrim(req.getLanguage());
        String code = safeTrim(req.getCode());

        if (code.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "code is required");
        }
        if (code.length() > 8000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "code is too long (max 8000 chars)");
        }

        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "OPENAI_API_KEY is not configured"
            );
        }

        String systemPrompt = """
                You are a senior software engineer doing a code review for a student.
                Return JSON that matches the provided schema. Do not include extra keys.
                Be constructive and concise. Focus on correctness, security, performance, and readability.
                """;

        LearningAssignment assignment = null;
        if (req.getAssignmentId() != null) {
            assignment = learningAssignmentRepository.findById(req.getAssignmentId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignment not found"));
            language = safeTrim(assignment.getLanguage());
        } else {
            language = normalizeLanguage(language);
            if (!isAllowedLanguage(language)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Unsupported language. Allowed: " + String.join(", ", ALLOWED_LANGUAGES)
                );
            }
        }

        String userPrompt = "Language: " + (language.isBlank() ? "unspecified" : language) + "\n\nCode:\n" + code;
        if (assignment != null) {
            userPrompt = userPrompt + "\n\nAssignment:\n" + safeTrim(assignment.getTitle()) +
                    "\n" + safeTrim(assignment.getDescription());
        }

        String replyJson;
        try {
            replyJson = openAiClient.chatJson(
                    apiKey,
                    "gpt-4o-mini",
                    List.of(
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user", "content", userPrompt)
                    ),
                    buildCodeReviewSchema()
            );
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "AI provider error: " + e.getMessage()
            );
        }

        CodeReviewResponse response = parseCodeReviewResponse(replyJson);

        StudentEntity student = studentContextService.getOrCreateCurrentStudent();
        InteractionEntity row = new InteractionEntity();
        row.setStudentId(student.getId());
        row.setAction("AI_CODE_REVIEW");
        row.setPrompt(userPrompt);
        row.setResponse(replyJson);
        interactionRepository.save(row);

        if (assignment != null) {
            AssignmentSubmission submission = new AssignmentSubmission();
            submission.setAssignmentId(assignment.getId());
            submission.setStudentId(student.getId());
            submission.setCode(code);
            submission.setFeedback(replyJson);
            assignmentSubmissionRepository.save(submission);

            assignment.setStatus("REVIEWED");
            learningAssignmentRepository.save(assignment);
        }

        return response;
    }

    public List<LearningAssignment> getAssignments(Long userId, String category) {
        StudentEntity student = studentContextService.getOrCreateCurrentStudent();
        String cat = safeTrim(category);
        if (cat.isEmpty()) {
            return learningAssignmentRepository.findByStudentIdOrderByCreatedAtDesc(student.getId());
        }
        return learningAssignmentRepository.findByStudentIdAndCategoryOrderByCreatedAtDesc(student.getId(), cat);
    }

    public AssignmentSubmission getSubmission(Long userId, Long assignmentId) {
        StudentEntity student = studentContextService.getOrCreateCurrentStudent();
        List<AssignmentSubmission> submissions = assignmentSubmissionRepository.findByAssignmentIdOrderByCreatedAtDesc(assignmentId);
        if (submissions.isEmpty()) {
            return null;
        }
        // Return the most recent submission for this student
        Long studentId = student.getId();
        return submissions.stream()
                .filter(s -> s.getStudentId() != null && s.getStudentId().equals(studentId))
                .findFirst()
                .orElse(null);
    }

    public List<LearningAssignment> generateAssignments(Long userId, AssignmentRequest req) {
        StudentEntity student = studentContextService.getOrCreateCurrentStudent();

        String language = normalizeLanguage(req.getLanguage());
        String level = safeTrim(req.getLevel()).toLowerCase();
        String category = safeTrim(req.getCategory());
        if (language.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "language is required");
        }
        if (!isAllowedLanguage(language)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unsupported language. Allowed: " + String.join(", ", ALLOWED_LANGUAGES)
            );
        }
        if (!List.of("beginner", "moderate", "advanced", "expert").contains(level)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "level must be beginner, moderate, advanced, or expert");
        }

        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "OPENAI_API_KEY is not configured"
            );
        }

        String systemPrompt = """
                You are a programming instructor. Create exactly 3 assignments.
                Each assignment must be small, practical, and aligned to the student's level.
                If a category is provided, keep all assignments within that category.
                Return JSON that matches the provided schema. Do not include extra keys.
                """;

        String userPrompt = "Language: " + language + "\nLevel: " + level;
        if (!category.isEmpty()) {
            userPrompt = userPrompt + "\nCategory: " + category;
        }

        String replyJson;
        try {
            replyJson = openAiClient.chatJson(
                    apiKey,
                    "gpt-4o-mini",
                    List.of(
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user", "content", userPrompt)
                    ),
                    buildAssignmentSchema()
            );
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "AI provider error: " + e.getMessage()
            );
        }

        List<LearningAssignment> assignments = parseAssignments(replyJson, student.getId(), language, level, category);
        return learningAssignmentRepository.saveAll(assignments);
    }

    private String buildUserPrompt(StudentEntity student, String cvText, List<bot.api.OpportunityClient.Opportunity> jobs) {
        JsonObject root = new JsonObject();
        root.addProperty("studentName", safeTrim(student.getName()));
        root.addProperty("email", safeTrim(student.getEmail()));
        root.addProperty("skills", safeTrim(student.getSkills()));
        root.addProperty("careerInterest", safeTrim(student.getCareerInterest()));
        root.addProperty("cvText", cvText);

        JsonArray jobsArr = new JsonArray();
        for (bot.api.OpportunityClient.Opportunity job : jobs) {
            JsonObject j = new JsonObject();
            j.addProperty("id", safeTrim(job.id));
            j.addProperty("title", safeTrim(job.title));
            j.addProperty("company", safeTrim(job.company));
            j.addProperty("type", safeTrim(job.type));
            j.addProperty("deadline", safeTrim(job.deadline));
            j.addProperty("description", safeTrim(job.description));
            j.addProperty("url", safeTrim(job.url));
            j.addProperty("formalRequirements", safeTrim(job.formReq));
            j.addProperty("technicalRequirements", safeTrim(job.techReq));
            jobsArr.add(j);
        }

        root.add("jobs", jobsArr);
        return root.toString();
    }

    private JsonObject buildSchema() {
        JsonObject schema = new JsonObject();
        schema.addProperty("type", "object");
        schema.addProperty("additionalProperties", false);

        JsonObject properties = new JsonObject();

        JsonObject cvScore = new JsonObject();
        cvScore.addProperty("type", "integer");
        cvScore.addProperty("minimum", 0);
        cvScore.addProperty("maximum", 100);
        properties.add("cvScore", cvScore);

        JsonObject summary = new JsonObject();
        summary.addProperty("type", "string");
        properties.add("summary", summary);

        JsonObject missingSkills = new JsonObject();
        missingSkills.addProperty("type", "array");
        JsonObject missingItem = new JsonObject();
        missingItem.addProperty("type", "string");
        missingSkills.add("items", missingItem);
        properties.add("missingSkills", missingSkills);

        JsonObject recommendations = new JsonObject();
        recommendations.addProperty("type", "array");
        JsonObject recItem = new JsonObject();
        recItem.addProperty("type", "string");
        recommendations.add("items", recItem);
        properties.add("recommendations", recommendations);

        JsonObject assignments = new JsonObject();
        assignments.addProperty("type", "array");
        assignments.addProperty("maxItems", 3);
        JsonObject assignmentItem = new JsonObject();
        assignmentItem.addProperty("type", "object");
        assignmentItem.addProperty("additionalProperties", false);
        JsonObject assignmentProps = new JsonObject();

        JsonObject aTitle = new JsonObject();
        aTitle.addProperty("type", "string");
        assignmentProps.add("title", aTitle);

        JsonObject aDesc = new JsonObject();
        aDesc.addProperty("type", "string");
        assignmentProps.add("description", aDesc);

        JsonObject aTime = new JsonObject();
        aTime.addProperty("type", "string");
        assignmentProps.add("estimatedTime", aTime);

        assignmentItem.add("properties", assignmentProps);
        JsonArray aRequired = new JsonArray();
        aRequired.add("title");
        aRequired.add("description");
        aRequired.add("estimatedTime");
        assignmentItem.add("required", aRequired);

        assignments.add("items", assignmentItem);
        properties.add("assignments", assignments);

        JsonObject topMatches = new JsonObject();
        topMatches.addProperty("type", "array");
        topMatches.addProperty("maxItems", 10);
        JsonObject matchItem = new JsonObject();
        matchItem.addProperty("type", "object");
        matchItem.addProperty("additionalProperties", false);
        JsonObject matchProps = new JsonObject();

        JsonObject id = new JsonObject();
        id.addProperty("type", "string");
        matchProps.add("id", id);

        JsonObject title = new JsonObject();
        title.addProperty("type", "string");
        matchProps.add("title", title);

        JsonObject company = new JsonObject();
        company.addProperty("type", "string");
        matchProps.add("company", company);

        JsonObject url = new JsonObject();
        url.addProperty("type", "string");
        matchProps.add("url", url);

        JsonObject score = new JsonObject();
        score.addProperty("type", "integer");
        score.addProperty("minimum", 0);
        score.addProperty("maximum", 100);
        matchProps.add("score", score);

        JsonObject reason = new JsonObject();
        reason.addProperty("type", "string");
        matchProps.add("matchReason", reason);

        matchItem.add("properties", matchProps);

        JsonArray required = new JsonArray();
        required.add("id");
        required.add("title");
        required.add("company");
        required.add("url");
        required.add("score");
        required.add("matchReason");
        matchItem.add("required", required);

        topMatches.add("items", matchItem);
        properties.add("topMatches", topMatches);

        JsonArray rootRequired = new JsonArray();
        rootRequired.add("cvScore");
        rootRequired.add("summary");
        rootRequired.add("missingSkills");
        rootRequired.add("recommendations");
        rootRequired.add("assignments");
        rootRequired.add("topMatches");

        schema.add("properties", properties);
        schema.add("required", rootRequired);
        return schema;
    }

    private ChatResponse parseAiResponse(String json) {
        JsonObject obj = gson.fromJson(json, JsonObject.class);

        ChatResponse response = new ChatResponse();
        response.setReply(obj.has("summary") ? obj.get("summary").getAsString() : null);
        response.setCvScore(obj.has("cvScore") ? obj.get("cvScore").getAsInt() : null);
        response.setSummary(obj.has("summary") ? obj.get("summary").getAsString() : null);

        if (obj.has("missingSkills") && obj.get("missingSkills").isJsonArray()) {
            var list = new java.util.ArrayList<String>();
            obj.getAsJsonArray("missingSkills").forEach(e -> list.add(e.getAsString()));
            response.setMissingSkills(list);
        }

        if (obj.has("recommendations") && obj.get("recommendations").isJsonArray()) {
            var list = new java.util.ArrayList<String>();
            obj.getAsJsonArray("recommendations").forEach(e -> list.add(e.getAsString()));
            response.setRecommendations(list);
        }

        if (obj.has("topMatches") && obj.get("topMatches").isJsonArray()) {
            var matches = new java.util.ArrayList<ChatResponse.MatchItem>();
            obj.getAsJsonArray("topMatches").forEach(e -> {
                JsonObject m = e.getAsJsonObject();
                matches.add(new ChatResponse.MatchItem(
                        m.has("id") ? m.get("id").getAsString() : null,
                        m.has("title") ? m.get("title").getAsString() : null,
                        m.has("company") ? m.get("company").getAsString() : null,
                        m.has("url") ? m.get("url").getAsString() : null,
                        m.has("score") ? m.get("score").getAsInt() : null,
                        m.has("matchReason") ? m.get("matchReason").getAsString() : null
                ));
            });
            response.setTopMatches(matches);
        }

        if (obj.has("assignments") && obj.get("assignments").isJsonArray()) {
            var list = new java.util.ArrayList<ChatResponse.AssignmentItem>();
            obj.getAsJsonArray("assignments").forEach(e -> {
                JsonObject a = e.getAsJsonObject();
                list.add(new ChatResponse.AssignmentItem(
                        a.has("title") ? a.get("title").getAsString() : null,
                        a.has("description") ? a.get("description").getAsString() : null,
                        a.has("estimatedTime") ? a.get("estimatedTime").getAsString() : null
                ));
            });
            response.setAssignments(list);
        }

        return response;
    }

    private JsonObject buildCodeReviewSchema() {
        JsonObject schema = new JsonObject();
        schema.addProperty("type", "object");
        schema.addProperty("additionalProperties", false);

        JsonObject properties = new JsonObject();

        JsonObject summary = new JsonObject();
        summary.addProperty("type", "string");
        properties.add("summary", summary);

        JsonObject issues = new JsonObject();
        issues.addProperty("type", "array");
        JsonObject issueItem = new JsonObject();
        issueItem.addProperty("type", "object");
        issueItem.addProperty("additionalProperties", false);
        JsonObject issueProps = new JsonObject();

        JsonObject severity = new JsonObject();
        severity.addProperty("type", "string");
        JsonArray sevEnum = new JsonArray();
        sevEnum.add("LOW");
        sevEnum.add("MEDIUM");
        sevEnum.add("HIGH");
        sevEnum.add("CRITICAL");
        severity.add("enum", sevEnum);
        issueProps.add("severity", severity);

        JsonObject title = new JsonObject();
        title.addProperty("type", "string");
        issueProps.add("title", title);

        JsonObject description = new JsonObject();
        description.addProperty("type", "string");
        issueProps.add("description", description);

        issueItem.add("properties", issueProps);
        JsonArray issueRequired = new JsonArray();
        issueRequired.add("severity");
        issueRequired.add("title");
        issueRequired.add("description");
        issueItem.add("required", issueRequired);
        issues.add("items", issueItem);
        properties.add("issues", issues);

        JsonObject suggestions = new JsonObject();
        suggestions.addProperty("type", "array");
        JsonObject sItem = new JsonObject();
        sItem.addProperty("type", "string");
        suggestions.add("items", sItem);
        properties.add("suggestions", suggestions);

        JsonArray required = new JsonArray();
        required.add("summary");
        required.add("issues");
        required.add("suggestions");

        schema.add("properties", properties);
        schema.add("required", required);
        return schema;
    }

    private CodeReviewResponse parseCodeReviewResponse(String json) {
        JsonObject obj = gson.fromJson(json, JsonObject.class);
        CodeReviewResponse response = new CodeReviewResponse();
        response.setSummary(obj.has("summary") ? obj.get("summary").getAsString() : null);

        if (obj.has("issues") && obj.get("issues").isJsonArray()) {
            var list = new java.util.ArrayList<CodeReviewResponse.IssueItem>();
            obj.getAsJsonArray("issues").forEach(e -> {
                JsonObject it = e.getAsJsonObject();
                list.add(new CodeReviewResponse.IssueItem(
                        it.has("severity") ? it.get("severity").getAsString() : null,
                        it.has("title") ? it.get("title").getAsString() : null,
                        it.has("description") ? it.get("description").getAsString() : null
                ));
            });
            response.setIssues(list);
        }

        if (obj.has("suggestions") && obj.get("suggestions").isJsonArray()) {
            var list = new java.util.ArrayList<String>();
            obj.getAsJsonArray("suggestions").forEach(e -> list.add(e.getAsString()));
            response.setSuggestions(list);
        }

        return response;
    }

    private JsonObject buildAssignmentSchema() {
        JsonObject schema = new JsonObject();
        schema.addProperty("type", "object");
        schema.addProperty("additionalProperties", false);

        JsonObject properties = new JsonObject();
        JsonObject assignments = new JsonObject();
        assignments.addProperty("type", "array");
        assignments.addProperty("maxItems", 3);

        JsonObject item = new JsonObject();
        item.addProperty("type", "object");
        item.addProperty("additionalProperties", false);
        JsonObject itemProps = new JsonObject();

        JsonObject title = new JsonObject();
        title.addProperty("type", "string");
        itemProps.add("title", title);

        JsonObject description = new JsonObject();
        description.addProperty("type", "string");
        itemProps.add("description", description);

        JsonObject time = new JsonObject();
        time.addProperty("type", "string");
        itemProps.add("estimatedTime", time);

        item.add("properties", itemProps);
        JsonArray required = new JsonArray();
        required.add("title");
        required.add("description");
        required.add("estimatedTime");
        item.add("required", required);

        assignments.add("items", item);
        properties.add("assignments", assignments);

        JsonArray rootRequired = new JsonArray();
        rootRequired.add("assignments");
        schema.add("properties", properties);
        schema.add("required", rootRequired);
        return schema;
    }

    private List<LearningAssignment> parseAssignments(String json, Long studentId, String language, String level, String category) {
        JsonObject obj = gson.fromJson(json, JsonObject.class);
        if (!obj.has("assignments") || !obj.get("assignments").isJsonArray()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI response missing assignments");
        }

        var list = new java.util.ArrayList<LearningAssignment>();
        obj.getAsJsonArray("assignments").forEach(e -> {
            JsonObject a = e.getAsJsonObject();
            LearningAssignment la = new LearningAssignment();
            la.setStudentId(studentId);
            la.setLanguage(language);
            la.setLevel(level);
            la.setCategory(category.isEmpty() ? null : category);
            la.setTitle(a.has("title") ? a.get("title").getAsString() : "Assignment");
            la.setDescription(a.has("description") ? a.get("description").getAsString() : "");
            la.setEstimatedTime(a.has("estimatedTime") ? a.get("estimatedTime").getAsString() : "");
            la.setStatus("ASSIGNED");
            list.add(la);
        });

        return list;
    }

    private boolean containsAssignmentIntent(String message) {
        String m = safeTrim(message).toLowerCase();
        if (m.isEmpty()) return false;
        return m.contains("assignment") ||
                m.contains("assignments") ||
                m.contains("task") ||
                m.contains("tasks") ||
                m.contains("homework") ||
                m.contains("practice") ||
                m.contains("study plan");
    }

    private boolean containsCvReviewIntent(String message) {
        String m = safeTrim(message).toLowerCase();
        if (m.isEmpty()) return false;
        return m.contains("review my cv") ||
                m.contains("review my resume") ||
                m.contains("cv review") ||
                m.contains("resume review") ||
                (m.contains("review") && m.contains("cv")) ||
                (m.contains("review") && m.contains("resume"));
    }

    private boolean containsSkillGapIntent(String message) {
        String m = safeTrim(message).toLowerCase();
        if (m.isEmpty()) return false;
        return m.contains("missing skills") ||
                m.contains("skill gaps") ||
                m.contains("skills am i missing") ||
                m.contains("what should i learn") ||
                m.contains("improve my job prospects") ||
                m.contains("improve my prospects") ||
                m.contains("skill gap");
    }

    private void ensureAssignments(ChatResponse response, String skills, String careerInterest) {
        if (response.getAssignments() != null && !response.getAssignments().isEmpty()) {
            return;
        }

        List<ChatResponse.AssignmentItem> defaults = List.of(
                new ChatResponse.AssignmentItem(
                        "Build a CRUD API",
                        "Create a Spring Boot REST API with validation, error handling, and tests.",
                        "4–6 hours"
                ),
                new ChatResponse.AssignmentItem(
                        "Database Design Practice",
                        "Design a simple schema for a job board app and implement it with JPA.",
                        "3–4 hours"
                ),
                new ChatResponse.AssignmentItem(
                        "Deployment Basics",
                        "Containerize your API with Docker and write a README for running it.",
                        "2–3 hours"
                )
        );

        response.setAssignments(defaults);
    }

    private void ensureTopMatches(ChatResponse response,
                                  List<bot.api.OpportunityClient.Opportunity> jobs,
                                  String skills,
                                  String careerInterest,
                                  String message) {
        if (response.getTopMatches() != null && response.getTopMatches().size() >= 10) {
            return;
        }

        List<String> tokens = List.of((skills + " " + careerInterest + " " + message)
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s]", " ")
                .split("\\s+"));

        java.util.Set<String> seen = new java.util.HashSet<>();
        if (response.getTopMatches() != null) {
            for (ChatResponse.MatchItem m : response.getTopMatches()) {
                if (m.getId() != null) seen.add(m.getId());
            }
        } else {
            response.setTopMatches(new java.util.ArrayList<>());
        }

        List<bot.api.OpportunityClient.Opportunity> sorted = jobs.stream()
                .sorted((a, b) -> Integer.compare(
                        scoreJob(b, tokens),
                        scoreJob(a, tokens)
                ))
                .toList();

        for (bot.api.OpportunityClient.Opportunity job : sorted) {
            if (response.getTopMatches().size() >= 10) break;
            if (job.id != null && seen.contains(job.id)) continue;

            int score = Math.min(100, Math.max(40, scoreJob(job, tokens) * 10));
            response.getTopMatches().add(new ChatResponse.MatchItem(
                    safeTrim(job.id),
                    safeTrim(job.title),
                    safeTrim(job.company),
                    safeTrim(job.url),
                    score,
                    "Keyword overlap with your skills/interests."
            ));
        }
    }

    private int scoreJob(bot.api.OpportunityClient.Opportunity job, List<String> tokens) {
        String hay = (safeTrim(job.title) + " " + safeTrim(job.description)).toLowerCase();
        int score = 0;
        for (String t : tokens) {
            if (t.length() < 3) continue;
            if (hay.contains(t)) score++;
        }
        return score;
    }

    private String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }

    private String normalizeLanguage(String language) {
        String l = safeTrim(language).toLowerCase();
        if (l.equals("py") || l.equals("python3") || l.equals("python-3")) return "python";
        if (l.equals("js") || l.equals("node")) return "javascript";
        if (l.equals("ts")) return "typescript";
        if (l.equals("csharp") || l.equals("c#")) return "c#";
        if (l.equals("cpp") || l.equals("c++")) return "c++";
        if (l.equals("golang")) return "go";
        return l;
    }

    private boolean isAllowedLanguage(String language) {
        return ALLOWED_LANGUAGES.contains(language);
    }

    private String trimForAi(String s, int maxChars) {
        if (s == null) return "";
        if (s.length() <= maxChars) return s;
        return s.substring(0, maxChars);
    }
}
