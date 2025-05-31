package com.example.image_analysis.service;

import com.example.image_analysis.dto.user.UsersDto;
import com.example.image_analysis.entity.Users;
import com.example.image_analysis.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;


    public UsersDto show(String loginId) {
        Users users = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원정보를 찾을 수 없습니다."));

        return UsersDto.fromEntity(users);
    }


    public UsersDto update(String loginId, UsersDto usersDto) {
        Users users = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("해당 id의 회원정보를 찾을 수 없습니다."));
        users.patch(usersDto);
        Users updatedUser = userRepository.save(users);
        return UsersDto.fromEntity(updatedUser);
    }

    public UsersDto delete(String loginId) {
        Users users = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("해당 id의 회원정보를 삭제할 수 없습니다."));
        UsersDto usersDto = UsersDto.fromEntity(users);
        userRepository.delete(users);
        return usersDto;
    }
}
