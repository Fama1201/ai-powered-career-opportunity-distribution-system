package com.jobifycvut.backend.repository;

import com.jobifycvut.backend.model.HrToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HrTokenRepository extends JpaRepository<HrToken, Long> {
    Optional<HrToken> findByToken(String token);
    void deleteByHrId(Long hrId);
}

