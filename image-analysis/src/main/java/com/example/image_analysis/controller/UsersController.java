package com.example.image_analysis.controller;


import com.example.image_analysis.JwtTokenProvider;
import com.example.image_analysis.dto.signup.UserSignDto;
import com.example.image_analysis.dto.user.UsersDto;
import com.example.image_analysis.entity.Users;
import com.example.image_analysis.service.UserService;
import com.nimbusds.oauth2.sdk.util.JWTClaimsSetUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class UsersController {

    @Autowired
    private UserService userService;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @GetMapping("/users/me")
    public ResponseEntity<UsersDto> show(@RequestHeader("Authorization") String authHeader) {
        String jwtToken = authHeader.replace("Bearer ", "");
        String loginId = jwtTokenProvider.getUsername(jwtToken);
        UsersDto usersDto = userService.show(loginId);
        return ResponseEntity.ok(usersDto);
    }

    @PatchMapping("/users/update")
    public ResponseEntity<UsersDto> update(@RequestHeader("Authorization") String authHeader,
                                           @RequestBody UsersDto usersDto) {
        String jwtToken = authHeader.replace("Bearer ", "");
        String loginId = jwtTokenProvider.getUsername(jwtToken);

        UsersDto updated = userService.update(loginId, usersDto);
        return (updated != null) ?
                ResponseEntity.status(HttpStatus.OK).body(updated) :
                ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @DeleteMapping("/users/delete")
    public ResponseEntity<UsersDto> delete(@RequestHeader("Authorization") String authHeader) {
        String jwtToken = authHeader.replace("Bearer ", "");
        String loginId = jwtTokenProvider.getUsername(jwtToken);
        UsersDto deleted = userService.delete(loginId);
        return (deleted != null) ?
                ResponseEntity.status(HttpStatus.NO_CONTENT).build() :
                ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    // authHeader에서 token 추출 및 loginId 반환 로직은 JwtUtil 클래스로 분리 예정

}
