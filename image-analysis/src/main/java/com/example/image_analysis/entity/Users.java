package com.example.image_analysis.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "login_id", unique = true, nullable = false, length = 15)
    private String loginId;     // login 시 사용 id

    @Column(nullable = false)
    private String password;

    @Column(name = "user_name", nullable = false)
    private String name;        // 사용자명

    @Column(unique = true, nullable = false)
    private String email;

    private String phone;

    private String birthday;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private Integer planType;
    public static final int FREE = 0;
    public static final int PRICING = 1;

    private int dailyUploadCount;   // 일일 업로드 수

    private int loginFailCount;     // 로그인 실패 횟수

    private boolean isLocked;       // 계정 잠금 여부

    private boolean isDeleted;      // 계정 탈퇴 여부

    private boolean isEmailVerified;    // 이메일 인증 여부

    public Users(@NotBlank(message = "아이디를 입력해주세요.") String loginId, @NotBlank(message = "비밀번호를 입력해주세요.") @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+=-]).{8,20}$",
            message = "비밀번호는 영문+숫자+특수문자를 포함한 8~20자여야 합니다.") String password, String name, @NotBlank(message = "이메일을 입력해주세요.") @Email(message = "이메일 형식이 올바르지 않습니다.") String email, String phone, String birthday) {
    }

    public enum Role {
        ADMIN,
        USER
    }
}
