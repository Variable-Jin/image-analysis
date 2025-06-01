package com.example.image_analysis.chat;


import com.example.image_analysis.base.BaseEntity;
import com.example.image_analysis.dto.analysis.AIAnalysisResult;
import com.example.image_analysis.entity.Users;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Chat extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // user 발송한 메세지 -> FK
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id")
    private Users sender;

    // 메세지 발신자 구분
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChatRole role;

    @Column(columnDefinition = "TEXT")
    private String content;

    private boolean hasAttachment;  // 첨부파일 여부

    @Enumerated
    @Column(nullable = false)
    private MessageTypeRole messageType;

    @Enumerated(EnumType.STRING)
    @Column(name = "processing_status")
    private ProcessRole processRole;

    // OCR 추출 원본 텍스트 -> (SYSTEM 메세지 사용)
    @Column(name = "ocr_extracted_text", columnDefinition = "TEXT")
    private String ocrExtractedText;

    // OCR 신뢰도 점수 (0.0 ~ 1.0)
    @Column(name = "ocr_confidence")
    private Double ocrConfidence;

    private String detectedLanguage;      // 언어 감지 결과
    private Long ocrProcessingTime;       // 처리 시간
    private String ocrProvider;           // 서비스 제공자
    private String originalFileName;      // 원본 파일명
    private String imageFormat;           // 이미지 포맷
    private Integer imageWidth;           // 이미지 너비
    private Integer imageHeight;          // 이미지 높이

    // ✅ AI 분석 결과 필드들 추가 (이게 없어서 오류!)
    @Column(name = "ai_analysis_result", columnDefinition = "TEXT")
    private String aiAnalysisResult;  // JSON 문자열로 저장

    @Column(name = "ai_model")
    private String aiModel;  // 사용된 AI 모델명

    @Column(name = "analyzed_at")
    private LocalDateTime analyzedAt;  // 분석 완료 시간


    /**
     * 사용자 메시지 생성
     */
    public static Chat createUserMessage(Users sender, String content, MessageTypeRole messageType) {
        Chat chat = new Chat();
        chat.setSender(sender);
        chat.setRole(ChatRole.USER);
        chat.setContent(content);
        chat.setMessageType(messageType);
        chat.setProcessRole(ProcessRole.PENDING);
        chat.setHasAttachment(MessageTypeRole.IMAGE.equals(messageType));
      //  chat.setCreatedAt(LocalDateTime.now());
        return chat;
    }

    /**
     * AI 어시스턴트 메시지 생성
     */
    public static Chat createAssistantMessage(Users sender, String aiResponse, MessageTypeRole messageTypeRole) {
        return Chat.builder()
                .sender(sender)
                .content(aiResponse)
                .role(ChatRole.ASSISTANT)  // 핵심 차이점!
                .messageType(messageTypeRole)
                .processRole(ProcessRole.COMPLETED)  // AI 응답은 바로 완료
                .build();
    }


    public enum ChatRole {
        USER,
        ASSISTANT,
        SYSTEM
    }

    /*
        필드별 사용 목적 분리
        - text only
        - image only
        - text + image
     */

    public enum MessageTypeRole {
        IMAGE,
        TEXT,
        MIXED
    }

    /*
        처리 상태
        - 업로드 완료 -> OCR 처리 대기
        - FastAPI -> 처리 중
        - 처리 완료
        - 처리 실패 ( 재시도 필요 )
     */

    public enum ProcessRole {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED
    }


}

