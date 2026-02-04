package com.jobifycvut.backend.repository;

import com.jobifycvut.backend.model.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findByDiscordId(String discordId);
    List<Feedback> findByStars(Integer stars);
}

