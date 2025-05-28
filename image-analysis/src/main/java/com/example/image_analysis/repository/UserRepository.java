package com.example.image_analysis.repository;

import com.example.image_analysis.entity.Users;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<Users, Long> {


    Optional<Users> findByLoginId(String loginId);
    boolean existsByLoginId(String loginId);
}
