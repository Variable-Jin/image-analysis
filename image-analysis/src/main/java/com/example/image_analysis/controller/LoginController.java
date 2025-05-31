package com.example.image_analysis.controller;


import com.example.image_analysis.JwtTokenProvider;
import com.example.image_analysis.dto.login.LoginRequestDto;
import com.example.image_analysis.dto.login.LoginResponseDto;
import com.example.image_analysis.entity.Users;
import com.example.image_analysis.repository.UserRepository;
import com.example.image_analysis.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class LoginController {

    @Autowired
    private LoginService loginService;
    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto requestDto) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        requestDto.getLoginId(),
                        requestDto.getPassword()
                )
        );
        LoginResponseDto response = loginService.login(requestDto);
        return ResponseEntity.ok(response);
    }
}
