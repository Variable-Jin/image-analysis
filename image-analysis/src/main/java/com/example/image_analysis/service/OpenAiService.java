package com.example.image_analysis.service;


import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@Slf4j
public class OpenAiService {

    @Value("${openai.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 단순 텍스트 질문용 (오버로딩)
     */
    public String generateResponse(String userMessage) {
        return generateResponse(userMessage, null, null, null);
    }

    /**
     * OCR 결과 포함한 통합 AI 응답 생성 메서드
     */
    public String generateResponse(String userMessage, String extractedText, String language, Double confidence) {
        String prompt = buildPrompt(userMessage, extractedText, language, confidence);
        return callOpenAiApi(prompt);
    }

    /**
     * 프롬프트 생성 로직
     */
    private String buildPrompt(String userMessage, String extractedText, String language, Double confidence) {
        if (extractedText == null || extractedText.trim().isEmpty()) {
            return userMessage; // 단순 텍스트 질문
        }

        // OCR 결과가 있는 경우
        return String.format(
                "사용자 질문: %s\n\n" +
                        "이미지에서 추출된 내용 (언어: %s, 신뢰도: %.0f%%):\n" +
                        "```\n%s\n```\n\n" +
                        "위 내용을 분석하고 사용자의 질문에 답변해주세요.",
                userMessage,
                language != null ? language : "미확인",
                confidence != null ? confidence * 100 : 0,
                extractedText
        );
    }

    /**
     * OpenAI API 호출
     */
    private String callOpenAiApi(String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> requestBody = Map.of(
                    "model", "gpt-3.5-turbo",
                    "messages", List.of(
                            Map.of("role", "user", "content", prompt)
                    ),
                    "max_tokens", 1500,
                    "temperature", 0.7
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    "https://api.openai.com/v1/chat/completions",
                    entity,
                    Map.class
            );

            return extractContent(response.getBody());

        } catch (Exception e) {
            log.error("OpenAI API 호출 실패: {}", e.getMessage());
            return "AI 분석 중 오류가 발생했습니다.";
        }
    }

    /**
     * 응답에서 content 추출
     */
    private String extractContent(Map<String, Object> responseBody) {
        try {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            return (String) message.get("content");
        } catch (Exception e) {
            log.warn("응답 파싱 실패: {}", e.getMessage());
            return "응답을 처리하는 중 오류가 발생했습니다.";
        }
    }
}