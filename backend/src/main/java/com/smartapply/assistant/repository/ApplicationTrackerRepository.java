package com.smartapply.assistant.repository;

import com.smartapply.assistant.entity.ApplicationTracker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationTrackerRepository extends JpaRepository<ApplicationTracker, Long> {
    List<ApplicationTracker> findByUserIdOrderByAppliedAtDesc(Long userId);
    boolean existsByUserIdAndApplicationUrl(Long userId, String applicationUrl);
}
