package com.jobifycvut.backend.service;

import bot.api.OpportunityClient;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jobifycvut.backend.dto.ChatResponse;
import com.jobifycvut.backend.model.InteractionEntity;
import com.jobifycvut.backend.model.StudentEntity;
import com.jobifycvut.backend.repository.InteractionRepository;
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
    private final Gson gson = new Gson();

    public AiService(InteractionRepository interactionRepository,
                     StudentContextService studentContextService) {
        this.interactionRepository = interactionRepository;
        this.studentContextService = studentContextService;
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
        String keywords = String.join(" ",
                List.of(skills, careerInterest, extra).stream().filter(s -> !s.isBlank()).toList());

        Set<OpportunityClient.Opportunity> rawJobs = OpportunityClient.searchMultipleKeywords(
                keywords.isBlank() ? "software" : keywords
        );

        List<OpportunityClient.Opportunity> jobs = rawJobs.stream()
                .sorted(Comparator.comparing(o -> safeTrim(o.title)))
                .limit(30)
                .toList();

        String systemPrompt = """
                You are a CV reviewer and job matcher for student internships.
                Return JSON that matches the provided schema. Do not include any extra keys.
                Score CV from 0-100. Select the top 10 job matches with brief reasons.
                """;

        String userPrompt = buildUserPrompt(student, cvText, jobs);

        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "OPENAI_API_KEY is not configured"
            );
        }

        OpenAiChatClient client = new OpenAiChatClient(apiKey, "gpt-4o-mini");
        String replyJson;
        try {
            replyJson = client.chatJson(
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

    private String buildUserPrompt(StudentEntity student, String cvText, List<OpportunityClient.Opportunity> jobs) {
        JsonObject root = new JsonObject();
        root.addProperty("studentName", safeTrim(student.getName()));
        root.addProperty("email", safeTrim(student.getEmail()));
        root.addProperty("skills", safeTrim(student.getSkills()));
        root.addProperty("careerInterest", safeTrim(student.getCareerInterest()));
        root.addProperty("cvText", cvText);

        JsonArray jobsArr = new JsonArray();
        for (OpportunityClient.Opportunity job : jobs) {
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

        return response;
    }

    private String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }
}
