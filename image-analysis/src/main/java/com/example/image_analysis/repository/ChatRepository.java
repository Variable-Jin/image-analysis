package com.example.image_analysis.repository;

import com.example.image_analysis.chat.Chat;
import com.example.image_analysis.entity.Users;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatRepository extends JpaRepository<Chat, Long> {

    List<Chat> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<Chat> findBySenderOrderByCreatedAtDesc(Users sender);

    Long countByMessageType(Chat.MessageTypeRole messageType);

    Long countByProcessRole(Chat.ProcessRole processRole);

    @Query("SELECT AVG(c.ocrConfidence) FROM Chat c WHERE c.ocrConfidence IS NOT NULL")
    Double getAverageOcrConfidence();

    // ✅ JPQL 방식 수정 (LIMIT 제거 → Pageable 사용)
    @Query("SELECT c.detectedLanguage FROM Chat c " +
            "WHERE c.detectedLanguage IS NOT NULL " +
            "GROUP BY c.detectedLanguage " +
            "ORDER BY COUNT(c.detectedLanguage) DESC")
    List<String> findTopDetectedLanguages(Pageable pageable);

    @Query("SELECT MIN(c.createdAt) FROM Chat c")
    LocalDateTime findFirstChatDate();

    @Query("SELECT MAX(c.createdAt) FROM Chat c")
    LocalDateTime findLastChatDate();

    List<Chat> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime start, LocalDateTime end);

    @Query("SELECT c FROM Chat c WHERE c.aiAnalysisResult IS NOT NULL")
    List<Chat> findChatsWithAiAnalysis();

    List<Chat> findByDetectedLanguageOrderByCreatedAtDesc(String language);
}
