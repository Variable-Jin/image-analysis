package com.example.image_analysis.chat;


import com.example.image_analysis.dto.analysis.AIAnalysisResult;
import com.example.image_analysis.dto.chat.ChatDto;
import com.example.image_analysis.dto.chat.ChatRequestDto;
import com.example.image_analysis.dto.ocr.OCRResult;
import com.example.image_analysis.entity.Users;
import com.example.image_analysis.ocr.GoogleVisionService;
import com.example.image_analysis.repository.ChatRepository;
import com.example.image_analysis.repository.UserRepository;
import com.example.image_analysis.service.AICodeAnalysisService;
import com.example.image_analysis.service.OpenAiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ChatService {

    @Autowired
    private ChatRepository chatRepository;
    @Autowired
    private AICodeAnalysisService aiCodeAnalysisService;
    @Autowired
    private GoogleVisionService googleVisionService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OpenAiService openAiService;


    /**
     * 💬 텍스트 메시지 처리
     */
    public ChatDto processTextMessage(ChatRequestDto request) {
        log.info("텍스트 메시지 처리 시작: {}", request.getMessage());

        try {
            // 1. 사용자 조회
            Users sender = userRepository.findById(request.getSenderId())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

            // 2. 사용자 메시지 저장
            Chat userChat = Chat.createUserMessage(
                    sender,
                    request.getMessage(),
                    Chat.MessageTypeRole.TEXT
            );
            userChat.setProcessRole(Chat.ProcessRole.COMPLETED);
            Chat savedUserChat = chatRepository.save(userChat);
            log.info("사용자 메시지 저장 완료 - ID: {}", savedUserChat.getId());

            // 3. AI 응답 생성
            String aiResponse = openAiService.generateResponse(request.getMessage());

            // 4. AI 응답 메시지 저장 (createAssistantMessage 메서드가 있다면 사용)
            Chat aiChat = Chat.createAssistantMessage( // 또는 적절한 메서드
                    sender,
                    aiResponse,
                    Chat.MessageTypeRole.TEXT
            );
            // 없다면 이렇게
            /*
            Chat aiChat = Chat.builder()
                    .users(sender)
                    .message(aiResponse)
                    .role(Chat.ChatRole.ASSISTANT)
                    .messageTypeRole(Chat.MessageTypeRole.TEXT)
                    .processRole(Chat.ProcessRole.COMPLETED)
                    .build();
            */

            Chat savedAiChat = chatRepository.save(aiChat);
            log.info("AI 응답 저장 완료 - ID: {}", savedAiChat.getId());

            return ChatDto.fromEntity(savedAiChat);

        } catch (Exception e) {
            log.error("텍스트 메시지 처리 실패: {}", e.getMessage(), e);
            throw new RuntimeException("텍스트 메시지 처리 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 📸 이미지 메시지 처리
     */
    public ChatDto processImageMessage(MultipartFile file, String message, Long senderId) {
        log.info("이미지 메시지 처리 시작 - 파일: {}, 메시지: {}",
                file.getOriginalFilename(), message);

        Chat chat = null;

        try {
            // 1. 사용자 조회
            Users sender = userRepository.findById(senderId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

            // 2. 사용자 이미지 메시지 생성 및 저장
            chat = Chat.createUserMessage(
                    sender,  // Users 객체 사용
                    message != null ? message : "이미지를 분석해주세요.",
                    Chat.MessageTypeRole.IMAGE
            );
            chat.setProcessRole(Chat.ProcessRole.PROCESSING);
            chat = chatRepository.save(chat);
            log.info("이미지 메시지 생성 완료 - ID: {}", chat.getId());

            // 3. Google Vision API로 OCR 처리
            OCRResult ocrResult = googleVisionService.extractTextFromMultipartFile(file);
            ocrResult.toEntity(chat);
            log.info("OCR 처리 완료 - 신뢰도: {}, 언어: {}",
                    ocrResult.getConfidence(), ocrResult.getDetectedLanguage());

            // 4. AI 분석 (필요한 경우)
            if (ocrResult.isHighConfidence() && ocrResult.hasValidText()) {
                AIAnalysisResult aiAnalysis = aiCodeAnalysisService.analyzeCodeWithAI(
                        ocrResult.getExtractedText(),
                        ocrResult.getDetectedLanguage()
                );
                aiAnalysis.toEntity(chat, objectMapper);
                log.info("AI 분석 완료 - 모델: {}", aiAnalysis.getAiModel());
            }

            // 5. 처리 완료
            chat.setProcessRole(Chat.ProcessRole.COMPLETED);
            Chat savedChat = chatRepository.save(chat);

            // 6. AI 응답 생성 및 저장
            Chat aiResponseChat = generateAndSaveSystemResponse(sender, message, ocrResult);

            // AI 응답이 있으면 그걸 반환, 없으면 사용자 메시지 반환
            if (aiResponseChat != null) {
                return ChatDto.fromEntity(aiResponseChat, objectMapper);
            } else {
                return ChatDto.fromEntity(savedChat, objectMapper);
            }

        } catch (Exception e) {
            log.error("이미지 메시지 처리 실패: {}", e.getMessage(), e);

            if (chat != null) {
                chat.setProcessRole(Chat.ProcessRole.FAILED);
                chatRepository.save(chat);
            }

            // 에러 메시지는 시스템 메시지가 아닌 AI 어시스턴트 응답으로
            Users sender = userRepository.findById(senderId).orElse(null);
            Chat errorChat = Chat.createAssistantMessage(
                    sender,
                    "이미지 분석 중 오류가 발생했습니다. 다시 시도해주세요.",
                    Chat.MessageTypeRole.TEXT
            );
            errorChat.setProcessRole(Chat.ProcessRole.FAILED);
            Chat savedErrorChat = chatRepository.save(errorChat);

            return ChatDto.fromEntity(savedErrorChat);
        }
    }

    /**
     * AI 응답 생성 및 저장 + 반환
     */
    private Chat generateAndSaveSystemResponse(Users sender, String userMessage, OCRResult ocrResult) {
        try {
            String aiResponse = openAiService.generateResponse(
                    userMessage,
                    ocrResult.getExtractedText(),
                    ocrResult.getDetectedLanguage(),
                    ocrResult.getConfidence()
            );

            Chat aiChat = Chat.createAssistantMessage(
                    sender,
                    aiResponse,
                    Chat.MessageTypeRole.TEXT
            );
            aiChat.setProcessRole(Chat.ProcessRole.COMPLETED);
            Chat saved = chatRepository.save(aiChat);

            log.info("이미지 분석 AI 응답 저장 완료");
            return saved;  // AI 응답 Chat 반환

        } catch (Exception e) {
            log.error("AI 응답 생성 실패: {}", e.getMessage(), e);
            return null;
        }
    }

    // 나머지 메서드들은 기존과 동일...
    @Transactional(readOnly = true)
    public List<ChatDto> getChatHistory(int limit) {
        log.info("채팅 히스토리 조회 - 제한: {}", limit);
        try {
            Pageable pageable = PageRequest.of(0, limit);
            List<Chat> chats = chatRepository.findAllByOrderByCreatedAtDesc(pageable);
            return chats.stream()
                    .map(chat -> ChatDto.fromEntity(chat, objectMapper))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("채팅 히스토리 조회 실패: {}", e.getMessage(), e);
            throw new RuntimeException("채팅 히스토리 조회 중 오류가 발생했습니다.", e);
        }
    }

    @Transactional(readOnly = true)
    public List<ChatDto> getChatHistory() {
        return getChatHistory(50);
    }

    @Transactional(readOnly = true)
    public ChatDto getChatById(Long chatId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new IllegalArgumentException("채팅을 찾을 수 없습니다: " + chatId));
        return ChatDto.fromEntity(chat, objectMapper);
    }

    public void deleteChat(Long chatId) {
        if (!chatRepository.existsById(chatId)) {
            throw new IllegalArgumentException("채팅을 찾을 수 없습니다: " + chatId);
        }
        chatRepository.deleteById(chatId);
        log.info("채팅 삭제 완료 - ID: {}", chatId);
    }

    public void deleteAllChats() {
        long count = chatRepository.count();
        chatRepository.deleteAll();
        log.info("모든 채팅 삭제 완료 - 삭제된 개수: {}", count);
    }
}