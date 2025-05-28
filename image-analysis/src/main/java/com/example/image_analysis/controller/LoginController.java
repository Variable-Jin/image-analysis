package com.example.image_analysis.controller;


import com.example.image_analysis.dto.login.LoginRequestDto;
import com.example.image_analysis.dto.login.LoginResponseDto;
import com.example.image_analysis.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class LoginController {

    @Autowired
    private LoginService loginService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto requestDto) {
        LoginResponseDto response = loginService.login(requestDto.getLoginId(), requestDto.getPassword());
        return ResponseEntity.ok(response);
    }


}
