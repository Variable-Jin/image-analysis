package com.example.image_analysis.service;

import com.example.image_analysis.dto.signup.UserSignDto;
import com.example.image_analysis.entity.Users;
import com.example.image_analysis.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AccountService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public Users signup(UserSignDto signDto) {

        Users user = signDto.toEntity(passwordEncoder);
        return userRepository.save(user);
    }

    public boolean checkLoginIdDuplication(String loginId) {
        return userRepository.existsByLoginId(loginId);
    }
}
