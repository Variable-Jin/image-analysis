package com.example.image_analysis;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${springboot.jwt.secret}")
    private String secretKey;

    // 1시간
    private static final long TOKEN_VALID_MILLISECOND = 1000L * 60 * 60;

    private SecretKey secretKeySpec;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @PostConstruct
    protected void init() {
        log.info("[init] JwtTokenProvider secretKey 초기화 시작");
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        secretKeySpec = Keys.hmacShaKeyFor(keyBytes); // Spring Boot 3.x 기준 권장 방식
        log.info("[init] JwtTokenProvider secretKey 초기화 완료");
    }

    public String createToken(String loginId, List<String> roles) {
        log.info("[createToken] 토큰 생성 시작");
        Date now = new Date();

        String token = Jwts.builder()
                .subject(loginId) // setSubject
                .claim("roles", roles) // custom claim 추가
                .issuedAt(now) // setIssuedAt
                .expiration(new Date(now.getTime() + TOKEN_VALID_MILLISECOND)) // setExpiration
                .signWith(secretKeySpec, Jwts.SIG.HS256)
                .compact();

        log.info("[createToken] 토큰 생성 완료");
        return token;
    }

    public Authentication getAuthentication(String token) {
        log.info("[getAuthentication] 토큰 인증 정보 조회 시작");

        String username = getUsername(token); // token → username 파싱
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

        log.info("[getAuthentication] 인증 완료, 사용자 ID: {}", userDetails.getUsername());
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public String getUsername(String token) {
        log.info("[getUsername] 토큰에서 사용자 이름 추출");

        Claims claims = Jwts.parser()
                .verifyWith(secretKeySpec)  // or secretKeySpec
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }

    public String resolveToken(HttpServletRequest request) {
        log.info("[resolveToken] HTTP Header에서 토큰 추출");
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer " 이후 실제 토큰 반환
        }
        return null;
    }

    public boolean validateToken(String token) {
        log.info("[validateToken] 토큰 유효성 검사 시작");
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKeySpec) // 또는 SecretKey 객체
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return !claims.getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("[validateToken] 토큰 유효성 검사 실패: {}", e.getMessage());
            return false;
        }
    }
}
