package com.example.image_analysis.entity;

import com.example.image_analysis.dto.user.UsersDto;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;


@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Users implements UserDetails {

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
    private Role role = Role.USER;  // 기본값

    private Integer planType;
    public static final int FREE = 0;
    public static final int PRICING = 1;

    private int dailyUploadCount;   // 일일 업로드 수

    private int loginFailCount;     // 로그인 실패 횟수

    private boolean isLocked;       // 계정 잠금 여부

    private boolean isDeleted;      // 계정 탈퇴 여부

    private boolean isEmailVerified;    // 이메일 인증 여부

    public Users(@NotBlank(message = "아이디를 입력해주세요.") String loginId, String encode, String name, @NotBlank(message = "이메일을 입력해주세요.") @Email(message = "이메일 형식이 올바르지 않습니다.")
        String email, String phone, String birthday) {
        this.loginId = loginId;
        this.password = encode;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.birthday = birthday;
    }

    public enum Role {
        ADMIN,
        USER
    }

    public void patch(UsersDto usersDto) {
        if (usersDto.getEmail() != null) {
            this.email = usersDto.getEmail();
        }
        if (usersDto.getPhone() != null) {
            this.phone = usersDto.getPhone();
        }
        if (usersDto.getBirthday() != null) {
            this.birthday = usersDto.getBirthday();
        }
        if (usersDto.getPlanType() != null) {
            this.planType = usersDto.getPlanType();
        }
    }

    // 인증된 사용자의 권한 정보 반환
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(role.name()));
    }

    // 인증된 사용자의 비밀번호 반환
    @Override
    public String getPassword() {
        return password;
    }

    // 인증된 사용자의 아이디 반환
    @Override
    public String getUsername() {
        return loginId; // loginId를 username으로 사용
    }

    // 인증된 사용자의 계정 유효기간 정보 반환
    // false : 기간만료
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // 인증된 사용자의 계정 잠금 상태 반환
    // false : 잠금상태
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    // 인증된 사용자의 비밀번호 유효기간 상태를 반환
    // false : 기간만료
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // 인증된 사용자의 활성화 상태를 반환
    // 항상 활성화 상태
    @Override
    public boolean isEnabled() {
        return true;
    }
}
