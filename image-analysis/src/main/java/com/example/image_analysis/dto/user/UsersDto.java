package com.example.image_analysis.dto.user;


import com.example.image_analysis.entity.Users;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UsersDto {

    private Long id;
    private String loginId;
    private String password;
    private String name;
    private String email;
    private String phone;
    private String birthday;
    private Integer planType;
    private boolean isEmailVerified;


    public static UsersDto fromEntity(Users users) {
        return new UsersDto(
                users.getId(),
                users.getLoginId(),
                users.getPassword(),
                users.getName(),
                users.getEmail(),
                users.getPhone(),
                users.getBirthday(),
                users.getPlanType(),
                users.isEmailVerified()
        );
    }
}
