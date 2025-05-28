package com.example.image_analysis.controller;

import com.example.image_analysis.dto.signup.UserSignDto;
import com.example.image_analysis.entity.Users;
import com.example.image_analysis.repository.UserRepository;
import com.example.image_analysis.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private UserRepository userRepository;

    /**
     * 최종 이메일 중복 체크
     * 비밀번호 유효성 체크
     * 비밀번호 암호화
     * DB 저장
     * success / error response
     */

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody UserSignDto signDto) {
        if (userRepository.existsByLoginId(signDto.getLoginId())) {
            return ResponseEntity.status(
                    HttpStatus.CONFLICT)
                    .body("이미 존재하는 아이디입니다.");
        }

        accountService.signup(signDto);
        return ResponseEntity.status(HttpStatus.CREATED).body("회원가입이 완료되었습니다.");
    }

    @GetMapping("/{loginId}/exists")
    public ResponseEntity<Boolean> checkLoginId(@PathVariable String loginId) {
        return ResponseEntity.ok(accountService.checkLoginIdDuplication(loginId));
    }


}
