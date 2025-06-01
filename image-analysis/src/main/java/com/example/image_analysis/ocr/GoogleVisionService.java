package com.example.image_analysis.ocr;

import com.example.image_analysis.dto.ocr.OCRResult;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
@Slf4j
public class GoogleVisionService {

    @Value("${google.cloud.vision.credentials-path}")
    private String credentialsPath;

    private ImageAnnotatorClient visionClient;  // ✅ 이제 변수 선언됨

    /**
     * 서비스 초기화 시 Vision Client 생성
     */
    @PostConstruct
    public void initializeVisionClient() {
        try {
            log.info("Google Vision API 클라이언트 초기화 시작");

            // credentialsPath 변수를 실제로 사용
            String cleanPath = credentialsPath.replace("classpath:", "");
            InputStream credentialsStream = getClass().getClassLoader()
                    .getResourceAsStream(cleanPath);

            if (credentialsStream == null) {
                throw new RuntimeException("인증 파일을 찾을 수 없습니다: " + cleanPath);
            }

            GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream);

            ImageAnnotatorSettings settings = ImageAnnotatorSettings.newBuilder()
                    .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                    .build();

            this.visionClient = ImageAnnotatorClient.create(settings);
            log.info("Google Vision API 클라이언트 초기화 완료");

        } catch (Exception e) {
            log.error("Google Vision API 초기화 실패: {}", e.getMessage(), e);
            throw new RuntimeException("Vision API 초기화 실패", e);
        }
    }

    /**
     * 서비스 종료 시 리소스 정리
     */
    @PreDestroy
    public void cleanup() {
        if (visionClient != null) {
            try {
                visionClient.close();
                log.info("Google Vision API 클라이언트 정리 완료");
            } catch (Exception e) {
                log.warn("Vision 클라이언트 정리 실패: {}", e.getMessage());
            }
        }
    }

    /**
     * MultipartFile에서 텍스트 추출
     */
    public OCRResult extractTextFromMultipartFile(MultipartFile file) {
        long startTime = System.currentTimeMillis();

        try {
            log.info("OCR 처리 시작 - 파일: {}, 크기: {}KB",
                    file.getOriginalFilename(), file.getSize() / 1024);

            // 1. MultipartFile을 ByteString으로 변환
            ByteString imgBytes = ByteString.copyFrom(file.getBytes());

            // 2. Vision API Image 객체 생성
            Image img = Image.newBuilder().setContent(imgBytes).build();

            // 3. TEXT_DETECTION 요청 생성
            Feature feature = Feature.newBuilder()
                    .setType(Feature.Type.TEXT_DETECTION)
                    .build();

            AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                    .addFeatures(feature)
                    .setImage(img)
                    .build();

            // 4. Vision API 호출
            BatchAnnotateImagesResponse response = visionClient.batchAnnotateImages(
                    List.of(request));

            List<AnnotateImageResponse> responses = response.getResponsesList();

            // 5. 응답 처리
            if (responses.isEmpty()) {
                log.warn("Vision API 응답이 비어있음");
                return createEmptyResult(file.getOriginalFilename(), startTime);
            }

            AnnotateImageResponse imageResponse = responses.get(0);

            // 6. 에러 체크
            if (imageResponse.hasError()) {
                String errorMessage = imageResponse.getError().getMessage();
                log.error("Vision API 에러: {}", errorMessage);
                return createErrorResult(errorMessage, file.getOriginalFilename(), startTime);
            }

            // 7. 텍스트 추출
            List<EntityAnnotation> annotations = imageResponse.getTextAnnotationsList();

            if (annotations.isEmpty()) {
                log.info("이미지에서 텍스트를 찾을 수 없음");
                return createEmptyResult(file.getOriginalFilename(), startTime);
            }

            // 첫 번째 annotation이 전체 텍스트
            String extractedText = annotations.get(0).getDescription();
            float confidence = annotations.get(0).getConfidence();

            // 8. 언어 감지 (간단한 휴리스틱)
            String detectedLanguage = detectLanguage(extractedText);

            long processingTime = System.currentTimeMillis() - startTime;

            log.info("OCR 처리 완료 - 신뢰도: {:.2f}, 언어: {}, 처리시간: {}ms",
                    confidence, detectedLanguage, processingTime);

            return OCRResult.fromGoogleVision(
                    extractedText,
                    confidence,
                    detectedLanguage,
                    processingTime,
                    file.getOriginalFilename()
            );

        } catch (IOException e) {
            log.error("파일 읽기 실패: {}", e.getMessage(), e);
            return createErrorResult("파일 읽기 실패: " + e.getMessage(),
                    file.getOriginalFilename(), startTime);

        } catch (Exception e) {
            log.error("OCR 처리 실패: {}", e.getMessage(), e);
            return createErrorResult("OCR 처리 실패: " + e.getMessage(),
                    file.getOriginalFilename(), startTime);
        }
    }

    /**
     * 언어 감지 (간단한 휴리스틱)
     */
    private String detectLanguage(String text) {
        try {
            if (text == null || text.trim().isEmpty()) {
                return "unknown";
            }

            // 한글 문자 비율 체크
            long koreanChars = text.chars()
                    .filter(ch -> ch >= 0xAC00 && ch <= 0xD7AF)
                    .count();

            // 영문 문자 비율 체크
            long englishChars = text.chars()
                    .filter(ch -> (ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z'))
                    .count();

            double koreanRatio = (double) koreanChars / text.length();
            double englishRatio = (double) englishChars / text.length();

            if (koreanRatio > 0.1) {
                return englishRatio > 0.3 ? "mixed" : "korean";
            } else if (englishRatio > 0.5) {
                return "english";
            } else {
                return "unknown";
            }

        } catch (Exception e) {
            log.warn("언어 감지 실패: {}", e.getMessage());
            return "unknown";
        }
    }

    /**
     * 빈 결과 생성
     */
    private OCRResult createEmptyResult(String fileName, long startTime) {
        long processingTime = System.currentTimeMillis() - startTime;
        return OCRResult.createEmpty(fileName);
    }

    /**
     * 에러 결과 생성
     */
    private OCRResult createErrorResult(String errorMessage, String fileName, long startTime) {
        long processingTime = System.currentTimeMillis() - startTime;
        return OCRResult.createError(errorMessage, fileName, processingTime);
    }
}