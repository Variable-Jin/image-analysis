package com.example.image_analysis.dto.login;

import com.example.image_analysis.entity.Users;
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
    private String accessToken;

    public static LoginResponseDto from(Users users, String accessToken) {
        return new LoginResponseDto(
                users.getLoginId(),
                users.getName(),
                users.getPlanType(),
                users.isEmailVerified(),
                accessToken
        );
    }
}

