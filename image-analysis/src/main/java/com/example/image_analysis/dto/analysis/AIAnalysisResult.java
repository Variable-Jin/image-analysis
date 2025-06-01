package com.example.image_analysis.dto.analysis;


import com.example.image_analysis.chat.Chat;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AIAnalysisResult {

    private String originalCode;
    private String detectedLanguage;
    private String codeExplanation;
    private List<String> issues;
    private String improvedCode;
    private List<String> learningTips;
    private String fullAnalysis;
    private LocalDateTime analyzedAt;
    private String aiModel;
    private Integer tokenUsed;

    public static AIAnalysisResult createError(String originalCode, String detectedLanguage, String errorMessage) {
        return new AIAnalysisResult(
                originalCode,
                detectedLanguage,
                "AI 분석 중 오류가 발생했습니다: " + errorMessage,
                Arrays.asList("AI 분석을 다시 시도해 주세요."),
                originalCode,
                Arrays.asList("나중에 다시 분석을 요청해 보세요."),
                "AI 분석 오류: " + errorMessage,
                LocalDateTime.now(),
                "Error",
                0
        );
    }

    public static AIAnalysisResult createUnavailable(String originalCode, String detectedLanguage) {
        return new AIAnalysisResult(
                originalCode,
                detectedLanguage,
                "AI 분석 서비스를 사용할 수 없습니다.",
                Arrays.asList("AI 서비스 연결을 확인해주세요."),
                originalCode,
                Arrays.asList("인터넷 연결을 확인하고 다시 시도해보세요."),
                "AI 분석 서비스 사용 불가",
                LocalDateTime.now(),
                "Unavailable",
                0
        );
    }

    /**
     * DTO → Entity 변환
     */
    public void toEntity(Chat chat, ObjectMapper objectMapper) {
        if (chat == null) return;

        try {
            String json = objectMapper.writeValueAsString(this);
            chat.setAiAnalysisResult(json);  // ✅ 이제 이 메서드가 있음
            chat.setAiModel(this.aiModel);
            chat.setAnalyzedAt(this.analyzedAt);
        } catch (Exception e) {
            System.err.println("JSON 변환 실패: " + e.getMessage());
            chat.setAiAnalysisResult("AI 분석 결과 저장 실패: " + e.getMessage());
            chat.setAiModel("Error");
            chat.setAnalyzedAt(LocalDateTime.now());
        }
    }

    /**
     * Entity → DTO 변환
     */
    public static AIAnalysisResult fromEntity(Chat chat, ObjectMapper objectMapper) {
        if (chat == null) {
            return createUnavailable("", "");
        }

        try {
            String json = chat.getAiAnalysisResult();  // ✅ 이제 이 메서드가 있음
            if (json != null && !json.trim().isEmpty()) {
                return objectMapper.readValue(json, AIAnalysisResult.class);
            }
        } catch (Exception e) {
            System.err.println("JSON 파싱 실패: " + e.getMessage());
        }

        // 실패시 기본값
        String originalCode = chat.getOcrExtractedText() != null ? chat.getOcrExtractedText() : "";
        String detectedLanguage = chat.getDetectedLanguage() != null ? chat.getDetectedLanguage() : "";

        return createUnavailable(originalCode, detectedLanguage);
    }

    @Override
    public String toString() {
        return String.format("AIAnalysisResult{model='%s', issues=%d, tips=%d, analyzed=%s}",
                aiModel,
                issues != null ? issues.size() : 0,
                learningTips != null ? learningTips.size() : 0,
                analyzedAt);
    }
}
