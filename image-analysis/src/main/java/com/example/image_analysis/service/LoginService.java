package com.example.image_analysis.service;

import com.example.image_analysis.JwtTokenProvider;
import com.example.image_analysis.dto.login.LoginRequestDto;
import com.example.image_analysis.dto.login.LoginResponseDto;
import com.example.image_analysis.entity.Users;
import com.example.image_analysis.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class LoginService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    public LoginResponseDto login(LoginRequestDto requestDto) {
        Users user = userRepository.findByLoginId(requestDto.getLoginId())
                .orElseThrow(() -> new UsernameNotFoundException("회원이 존재하지 않습니다."));

        String token = jwtTokenProvider.createToken(requestDto.getLoginId(), List.of("ROLE_USER"));
        return LoginResponseDto.from(user, token);
    }

}
