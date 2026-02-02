package com.jobifycvut.backend.controller;

import com.jobifycvut.backend.dto.ChatRequest;
import com.jobifycvut.backend.dto.ChatResponse;
import com.jobifycvut.backend.dto.HistoryItemResponse;
import com.jobifycvut.backend.security.SecurityUtil;
import com.jobifycvut.backend.service.AiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest req) {
        Long userId = SecurityUtil.requireUserId();
        String reply = aiService.chat(userId, req.getMessage());
        return ResponseEntity.ok(new ChatResponse(reply));
    }

    @GetMapping("/chat/history")
    public ResponseEntity<List<HistoryItemResponse>> history() {
        Long userId = SecurityUtil.requireUserId();
        var rows = aiService.chatHistory(userId);

        var out = rows.stream()
                .map(r -> new HistoryItemResponse(r.getId(), r.getAction(), r.getPrompt(), r.getResponse(), r.getCreatedAt()))
                .toList();

        return ResponseEntity.ok(out);
    }

    @PostMapping("/cv-review")
    public ResponseEntity<ChatResponse> cvReview(@RequestBody ChatRequest req) {
        Long userId = SecurityUtil.requireUserId();
        String reply = aiService.cvReview(userId, req.getMessage());
        return ResponseEntity.ok(new ChatResponse(reply));
    }

    @PostMapping("/career-advice")
    public ResponseEntity<ChatResponse> careerAdvice(@RequestBody ChatRequest req) {
        Long userId = SecurityUtil.requireUserId();
        String reply = aiService.careerAdvice(userId, req.getMessage());
        return ResponseEntity.ok(new ChatResponse(reply));
    }
}
