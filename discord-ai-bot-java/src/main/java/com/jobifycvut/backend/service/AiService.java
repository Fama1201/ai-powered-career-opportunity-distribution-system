package com.jobifycvut.backend.service;

import com.jobifycvut.backend.model.InteractionEntity;
import com.jobifycvut.backend.repository.InteractionRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AiService {
    private InteractionRepository interactionRepository;
    public AiService(InteractionRepository interactionRepository) {
        this.interactionRepository = interactionRepository;
    }

    public String chat(Long userId, String message) {
        String reply="AI reply (stub):" + message;

        InteractionEntity row = new InteractionEntity();
        row.setStudentId(userId);
        row.setOpportunityId(null);
        row.setAction("AI_CHAT");
        row.setPrompt(message);
        row.setResponse(reply);

        interactionRepository.save(row);
        return reply;
    }

    public List<InteractionEntity> chatHistory(Long userId) {
        return interactionRepository.findTop50ByStudentIdAndActionOrderByCreatedAtDesc(userId, "AI_CHAT");
    }

    public String cvReview(Long userId, String cvText){
        String reply="CV review (stub): Add measurable achievements, fix formatting, and tailor keywords.";
        InteractionEntity row = new InteractionEntity();

        row.setStudentId(userId);
        row.setAction("AI_CV_REVIEW");
        row.setPrompt(cvText);
        row.setResponse(reply);
        interactionRepository.save(row);
        return reply;
    }

    public String careerAdvice(Long userId, String prompt){
        String reply="Career advice (stub): Pick a target role, build 2 projects, and practice interviews weekly." ;
        InteractionEntity row = new InteractionEntity();
        row.setStudentId(userId);
        row.setAction("AI_CAR_ADVICE");
        row.setPrompt(prompt);
        row.setResponse(reply);
        interactionRepository.save(row);
        return reply;
    }
}
