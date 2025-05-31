package com.example.image_analysis.controller;

import com.example.image_analysis.JwtTokenProvider;
import com.example.image_analysis.dto.login.LoginResponseDto;
import com.example.image_analysis.dto.signup.SignUpResponseDto;
import com.example.image_analysis.dto.signup.UserSignDto;
import com.example.image_analysis.entity.Users;
import com.example.image_analysis.repository.UserRepository;
import com.example.image_analysis.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;


    /**
     * 최종 이메일 중복 체크
     * 비밀번호 유효성 체크
     * 비밀번호 암호화
     * DB 저장
     * success / error response
     */

    @PostMapping("/signup")
    public ResponseEntity<SignUpResponseDto> signup(@RequestBody UserSignDto signDto) {
        if (userRepository.existsByLoginId(signDto.getLoginId())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        Users users = accountService.signup(signDto);
        String token = jwtTokenProvider.createToken(users.getLoginId(), List.of("ROLE_USER"));

        SignUpResponseDto response = new SignUpResponseDto(users.getLoginId(), users.getName(), token);
        return ResponseEntity.status(HttpStatus.CREATED).body(response); // ✅ 타입 일치
    }

    @GetMapping("/{loginId}/exists")
    public ResponseEntity<Boolean> checkLoginId(@PathVariable String loginId) {
        return ResponseEntity.ok(accountService.checkLoginIdDuplication(loginId));
    }


}
