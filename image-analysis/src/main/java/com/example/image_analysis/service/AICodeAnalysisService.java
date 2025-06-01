package com.example.image_analysis.service;

import com.example.image_analysis.dto.analysis.AIAnalysisResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
@RequiredArgsConstructor
public class AICodeAnalysisService {

    @Autowired
    private OpenAiService openAiService;

    /**
     * AI를 활용한 코드 분석
     */
    public AIAnalysisResult analyzeCodeWithAI(String extractedCode, String detectedLanguage) {
        try {
            log.info("AI 코드 분석 시작 - 언어: {}, 코드 길이: {}", detectedLanguage, extractedCode.length());

            // AI에게 코드 분석 요청
            String userMessage = "다음 코드를 분석해주세요";
            String aiResponse = openAiService.generateResponse(userMessage, extractedCode, detectedLanguage, 1.0);

            log.info("AI 분석 완료 - 응답 길이: {}", aiResponse.length());

            // AI 응답 파싱해서 구조화된 결과 생성
            return parseAIResponse(aiResponse, extractedCode, detectedLanguage);

        } catch (Exception e) {
            log.error("AI 분석 실패: {}", e.getMessage(), e);
            return createErrorResponse(extractedCode, detectedLanguage, e.getMessage());
        }
    }

    /**
     * AI 응답을 파싱해서 구조화된 결과로 변환
     */
    private AIAnalysisResult parseAIResponse(String aiResponse, String originalCode, String detectedLanguage) {
        AIAnalysisResult result = new AIAnalysisResult(
                originalCode,
                detectedLanguage,
                extractCodeExplanation(aiResponse),
                extractIssues(aiResponse),
                extractImprovedCode(aiResponse, originalCode),
                extractLearningTips(aiResponse),
                aiResponse,
                LocalDateTime.now(),
                "gpt-3.5-turbo",
                0
        );

        return result;
    }

    /**
     * 코드 설명 추출
     */
    private String extractCodeExplanation(String response) {
        try {
            // "분석" 또는 "설명" 관련 첫 번째 문단 추출
            String[] lines = response.split("\n");
            StringBuilder explanation = new StringBuilder();

            boolean foundStart = false;
            for (String line : lines) {
                line = line.trim();
                if (!foundStart && (line.contains("분석") || line.contains("설명") || line.contains("기능"))) {
                    foundStart = true;
                }
                if (foundStart && !line.isEmpty()) {
                    explanation.append(line).append(" ");
                    if (line.endsWith(".") || line.endsWith("다.") || line.endsWith("습니다.")) {
                        break;
                    }
                }
            }

            return explanation.length() > 0 ? explanation.toString().trim() : "코드 기능을 분석했습니다.";
        } catch (Exception e) {
            log.warn("코드 설명 추출 실패: {}", e.getMessage());
            return "코드 설명을 추출할 수 없습니다.";
        }
    }

    /**
     * 문제점 추출
     */
    private List<String> extractIssues(String response) {
        List<String> issues = new ArrayList<>();
        try {
            String[] lines = response.split("\n");

            for (String line : lines) {
                line = line.trim();
                if (line.contains("문제") || line.contains("오류") || line.contains("개선") ||
                        line.matches("^[\\d\\-\\*•◦]+.*") && line.length() > 10) {

                    String cleanIssue = line.replaceFirst("^[\\d\\-\\*•◦\\.\\s]+", "").trim();
                    if (!cleanIssue.isEmpty() && cleanIssue.length() > 5) {
                        issues.add(cleanIssue);
                    }
                }
            }

            if (issues.isEmpty()) {
                issues.add("특별한 문제점이 발견되지 않았습니다.");
            }

        } catch (Exception e) {
            log.warn("문제점 추출 실패: {}", e.getMessage());
            issues.add("문제점 분석 중 오류가 발생했습니다.");
        }

        return issues;
    }

    /**
     * 개선된 코드 추출
     */
    private String extractImprovedCode(String response, String originalCode) {
        try {
            // 코드 블록 찾기 (```로 감싸진 부분)
            Pattern codePattern = Pattern.compile("```(?:\\w+)?\\s*([\\s\\S]*?)```");
            Matcher matcher = codePattern.matcher(response);

            String lastCodeBlock = null;
            while (matcher.find()) {
                lastCodeBlock = matcher.group(1).trim();
            }

            // 개선된 코드가 있고 원본과 다르면 반환
            if (lastCodeBlock != null && !lastCodeBlock.equals(originalCode.trim())) {
                return lastCodeBlock;
            }

            return originalCode;

        } catch (Exception e) {
            log.warn("개선된 코드 추출 실패: {}", e.getMessage());
            return originalCode;
        }
    }

    /**
     * 학습 팁 추출
     */
    private List<String> extractLearningTips(String response) {
        List<String> tips = new ArrayList<>();
        try {
            String[] lines = response.split("\n");

            for (String line : lines) {
                line = line.trim();
                if (line.contains("학습") || line.contains("공부") || line.contains("추천") ||
                        line.contains("참고") || line.contains("권장")) {

                    String cleanTip = line.replaceFirst("^[\\d\\-\\*•◦\\.\\s]+", "").trim();
                    if (!cleanTip.isEmpty() && cleanTip.length() > 10) {
                        tips.add(cleanTip);
                    }
                }
            }

            if (tips.isEmpty()) {
                tips.addAll(getDefaultLearningTips());
            }

        } catch (Exception e) {
            log.warn("학습 팁 추출 실패: {}", e.getMessage());
            tips.add("학습 자료를 찾아보시기 바랍니다.");
        }

        return tips;
    }

    /**
     * 기본 학습 팁
     */
    private List<String> getDefaultLearningTips() {
        return Arrays.asList(
                "코드의 가독성을 높이기 위해 의미있는 변수명을 사용하세요",
                "함수는 하나의 기능만 담당하도록 작성하세요",
                "주석을 활용해 복잡한 로직을 설명하세요",
                "에러 처리를 통해 안정성을 높이세요"
        );
    }

    /**
     * 에러 발생 시 기본 응답 생성
     */
    private AIAnalysisResult createErrorResponse(String code, String language, String errorMessage) {
        return new AIAnalysisResult(
                code,
                language,
                "AI 분석 서비스를 일시적으로 사용할 수 없습니다.",
                Arrays.asList("AI 분석을 다시 시도해 주세요: " + errorMessage),
                code,
                Arrays.asList("나중에 다시 분석을 요청해 보세요."),
                "AI 분석 서비스 오류",
                LocalDateTime.now(),
                "error",
                0
        );
    }
}