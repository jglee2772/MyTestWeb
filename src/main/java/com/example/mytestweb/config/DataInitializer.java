package com.example.mytestweb.config;

import com.example.mytestweb.entity.User;
import com.example.mytestweb.entity.UserStatus;
import com.example.mytestweb.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        // 관리자 계정이 없으면 생성
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@mytestweb.com");
            admin.setName("관리자");
            admin.setPhone("010-1234-5678");
            admin.setPassword("$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi"); // password
            admin.setStatus(UserStatus.APPROVED);
            admin.setAdmin(true);
            admin.setCreatedAt(java.time.LocalDateTime.now());
            userRepository.save(admin);
            System.out.println("관리자 계정이 생성되었습니다: admin / password");
        }

        // 데모 사용자 계정이 없으면 생성
        if (!userRepository.existsByUsername("demo_user")) {
            User demoUser = new User();
            demoUser.setUsername("demo_user");
            demoUser.setEmail("demo@mytestweb.com");
            demoUser.setName("데모사용자");
            demoUser.setPhone("010-9876-5432");
            demoUser.setPassword("$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi"); // password
            demoUser.setStatus(UserStatus.APPROVED);
            demoUser.setAdmin(false);
            demoUser.setCreatedAt(java.time.LocalDateTime.now());
            userRepository.save(demoUser);
            System.out.println("데모 사용자 계정이 생성되었습니다: demo_user / password");
        }
    }
}

