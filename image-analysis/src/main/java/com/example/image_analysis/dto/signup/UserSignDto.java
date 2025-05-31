package com.example.image_analysis.dto.signup;


import com.example.image_analysis.entity.Users;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserSignDto {

    @NotBlank(message = "아이디를 입력해주세요.")
    private String loginId;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+=-]).{8,20}$",
            message = "비밀번호는 영문+숫자+특수문자를 포함한 8~15자여야 합니다.")
    private String password;

    private String name;

    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    private String phone;

    private String birthday;

    public Users toEntity(PasswordEncoder passwordEncoder) {
        System.out.println("toEntity - 전달할 email: " + email);
        return new Users(
                loginId,
                passwordEncoder.encode(password),   // 암호화
                name,
                email,
                phone,
                birthday
        );
    }

}
