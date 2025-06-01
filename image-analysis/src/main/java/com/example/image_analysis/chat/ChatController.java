package com.example.image_analysis.chat;

import com.example.image_analysis.dto.chat.ChatDto;
import com.example.image_analysis.dto.chat.ChatRequestDto;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chat")
@Slf4j
public class ChatController {

    @Autowired
    private ChatService chatService;

    /**
     * 💬 텍스트 채팅 메시지 전송
     */
    @PostMapping("/message")
    public ResponseEntity<ChatDto> sendTextMessage(@RequestBody ChatRequestDto request) {
        log.info("텍스트 메시지 수신: {}", request.getMessage());

        try {
            ChatDto response = chatService.processTextMessage(request);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("텍스트 메시지 처리 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 📸 이미지 분석 (파일 업로드 + OCR + AI 분석)
     */
    @PostMapping("/analyze-image")
    public ResponseEntity<ChatDto> analyzeImage(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "message", required = false) String message,
            @RequestParam("senderId") Long senderId) {  // ✅ senderId 추가!

        log.info("이미지 분석 요청 - 파일: {}, 메시지: {}, 사용자: {}",
                file.getOriginalFilename(), message, senderId);

        try {
            // 파일 유효성 검사
            if (file.isEmpty()) {
                log.warn("빈 파일이 업로드됨");
                return ResponseEntity.badRequest().build();
            }

            // 파일 타입 검사 (이미지만 허용)
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                log.warn("이미지가 아닌 파일 업로드 시도: {}", contentType);
                return ResponseEntity.badRequest().build();
            }

            // 파일 크기 검사 (10MB 제한)
            if (file.getSize() > 10 * 1024 * 1024) {
                log.warn("파일 크기 초과: {}MB", file.getSize() / 1024 / 1024);
                return ResponseEntity.badRequest().build();
            }

            // senderId 유효성 검사 추가
            if (senderId == null) {
                log.warn("senderId가 필요합니다");
                return ResponseEntity.badRequest().build();
            }

            // ✅ 3개 파라미터 모두 전달
            ChatDto response = chatService.processImageMessage(file, message, senderId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("이미지 분석 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 📋 채팅 히스토리 조회
     */
    @GetMapping("/history")
    public ResponseEntity<List<ChatDto>> getChatHistory(
            @RequestParam(value = "limit", defaultValue = "50") int limit) {

        log.info("채팅 히스토리 조회 요청 - 제한: {}", limit);

        try {
            List<ChatDto> history = chatService.getChatHistory(limit);
            return ResponseEntity.ok(history);

        } catch (Exception e) {
            log.error("채팅 히스토리 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 🔍 특정 채팅 조회
     */
    @GetMapping("/{chatId}")
    public ResponseEntity<ChatDto> getChat(@PathVariable Long chatId) {
        log.info("채팅 조회 요청 - ID: {}", chatId);

        try {
            ChatDto chat = chatService.getChatById(chatId);
            return ResponseEntity.ok(chat);

        } catch (IllegalArgumentException e) {
            log.warn("존재하지 않는 채팅 ID: {}", chatId);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("채팅 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 🗑️ 채팅 삭제
     */
    @DeleteMapping("/{chatId}")
    public ResponseEntity<Void> deleteChat(@PathVariable Long chatId) {
        log.info("채팅 삭제 요청 - ID: {}", chatId);

        try {
            chatService.deleteChat(chatId);
            return ResponseEntity.ok().build();

        } catch (IllegalArgumentException e) {
            log.warn("존재하지 않는 채팅 삭제 시도 - ID: {}", chatId);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("채팅 삭제 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 🧹 모든 채팅 삭제
     */
    @DeleteMapping("/all")
    public ResponseEntity<Void> deleteAllChats() {
        log.info("모든 채팅 삭제 요청");

        try {
            chatService.deleteAllChats();
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            log.error("모든 채팅 삭제 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 📊 채팅 통계 조회
     */
//    @GetMapping("/stats")
//    public ResponseEntity<ChatStatsDto> getChatStats() {
//        log.info("채팅 통계 조회 요청");
//
//        try {
//            ChatStatsDto stats = chatService.getChatStats();
//            return ResponseEntity.ok(stats);
//
//        } catch (Exception e) {
//            log.error("채팅 통계 조회 실패: {}", e.getMessage(), e);
//            return ResponseEntity.internalServerError().build();
//        }
//    }
}


