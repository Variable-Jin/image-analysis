package com.example.image_analysis.dto.login;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponseDto {

    private String loginId;
    private String name;
    private Integer planType;
    private boolean emailVerified;

//    public LoginResponseDto(String loginId, String name, String nickname, Integer planType, boolean emailVerified) {
//    }
    // private String token;
}

