package com.example.image_analysis.service;

import com.example.image_analysis.dto.login.LoginResponseDto;
import com.example.image_analysis.entity.Users;
import com.example.image_analysis.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LoginService {

    @Autowired
    private UserRepository userRepository;

    public LoginResponseDto login(String loginId, String password) {
        Users user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));
        return new LoginResponseDto(user.getLoginId(), user.getName(), user.getPlanType(), user.isEmailVerified());
    }

}
