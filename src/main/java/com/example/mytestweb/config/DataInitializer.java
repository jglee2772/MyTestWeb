package com.example.mytestweb.config;

import com.example.mytestweb.entity.User;
import com.example.mytestweb.entity.UserStatus;
import com.example.mytestweb.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== 데이터 초기화 시작 ===");
        
        // 관리자 계정 확인 및 생성/업데이트
        Optional<User> adminOpt = userRepository.findByUsername("admin");
        if (adminOpt.isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@mytestweb.com");
            admin.setName("관리자");
            admin.setPhone("010-1234-5678");
            admin.setPassword(passwordEncoder.encode("password")); // PasswordEncoder로 인코딩
            admin.setStatus(UserStatus.APPROVED);
            admin.setAdmin(true);
            admin.setCreatedAt(java.time.LocalDateTime.now());
            userRepository.save(admin);
            System.out.println("✓ 관리자 계정 생성 완료: admin / password");
        } else {
            User admin = adminOpt.get();
            // 관리자 계정이 있지만 상태가 APPROVED가 아니거나 권한이 없으면 업데이트
            if (admin.getStatus() != UserStatus.APPROVED || !admin.isAdmin()) {
                admin.setStatus(UserStatus.APPROVED);
                admin.setAdmin(true);
                // 비밀번호가 올바른 형식이 아니면 재설정
                if (!admin.getPassword().startsWith("$2a$")) {
                    admin.setPassword(passwordEncoder.encode("password"));
                }
                userRepository.save(admin);
                System.out.println("✓ 관리자 계정 업데이트 완료: admin / password");
            } else {
                System.out.println("✓ 관리자 계정 이미 존재: admin");
            }
        }

        // 데모 사용자 계정 확인 및 생성/업데이트
        Optional<User> demoOpt = userRepository.findByUsername("demo_user");
        if (demoOpt.isEmpty()) {
            User demoUser = new User();
            demoUser.setUsername("demo_user");
            demoUser.setEmail("demo@mytestweb.com");
            demoUser.setName("데모사용자");
            demoUser.setPhone("010-9876-5432");
            demoUser.setPassword(passwordEncoder.encode("password")); // PasswordEncoder로 인코딩
            demoUser.setStatus(UserStatus.APPROVED);
            demoUser.setAdmin(false);
            demoUser.setCreatedAt(java.time.LocalDateTime.now());
            userRepository.save(demoUser);
            System.out.println("✓ 데모 사용자 계정 생성 완료: demo_user / password");
        } else {
            User demoUser = demoOpt.get();
            // 데모 사용자 계정이 있지만 상태가 APPROVED가 아니면 업데이트
            if (demoUser.getStatus() != UserStatus.APPROVED) {
                demoUser.setStatus(UserStatus.APPROVED);
                // 비밀번호가 올바른 형식이 아니면 재설정
                if (!demoUser.getPassword().startsWith("$2a$")) {
                    demoUser.setPassword(passwordEncoder.encode("password"));
                }
                userRepository.save(demoUser);
                System.out.println("✓ 데모 사용자 계정 업데이트 완료: demo_user / password");
            } else {
                System.out.println("✓ 데모 사용자 계정 이미 존재: demo_user");
            }
        }
        
        System.out.println("=== 데이터 초기화 완료 ===");
    }
}

