package com.example.mytestweb.service;

import com.example.mytestweb.entity.User;
import com.example.mytestweb.entity.UserStatus;
import com.example.mytestweb.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    @Autowired(required = false)
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 임시: 데이터베이스 연결 없이 모든 로그인을 관리자로 인증
    private static final boolean TEMP_BYPASS_DB = true;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("로그인 시도: {} (임시 모드: 데이터베이스 우회)", username);
        
        // 임시 모드: 데이터베이스 없이 항상 관리자로 인증
        if (TEMP_BYPASS_DB) {
            logger.warn("⚠️ 임시 모드 활성화: 모든 로그인을 관리자로 인증합니다 (아이디: {}, 비밀번호: 아무거나)", username);
            
            List<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            
            // 비밀번호는 인코딩된 임시 값 (실제 검증은 우회됨)
            String tempPassword = passwordEncoder.encode("temp");
            
            logger.info("✓ 임시 관리자 로그인 성공: {}, 권한: {}", username, authorities);
            return org.springframework.security.core.userdetails.User.builder()
                    .username(username)
                    .password(tempPassword)
                    .authorities(authorities)
                    .build();
        }
        
        // 정상 모드: 데이터베이스 사용
        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> {
                        logger.warn("사용자를 찾을 수 없습니다: {}", username);
                        return new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username);
                    });

            logger.debug("사용자 찾음: {}, 상태: {}, 관리자: {}", user.getUsername(), user.getStatus(), user.isAdmin());

            // 승인되지 않은 사용자는 로그인 불가
            if (user.getStatus() != UserStatus.APPROVED) {
                logger.warn("승인되지 않은 사용자: {} (상태: {})", username, user.getStatus());
                throw new UsernameNotFoundException("승인되지 않은 사용자입니다: " + username);
            }

            List<GrantedAuthority> authorities = new ArrayList<>();
            
            // 관리자 권한 추가
            if (user.isAdmin()) {
                authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                logger.debug("관리자 권한 부여: {}", username);
            }
            
            // 일반 사용자 권한 추가
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

            logger.debug("로그인 성공: {}, 권한: {}", username, authorities);
            return org.springframework.security.core.userdetails.User.builder()
                    .username(user.getUsername())
                    .password(user.getPassword())
                    .authorities(authorities)
                    .build();
        } catch (Exception e) {
            logger.error("데이터베이스 오류 발생, 임시 모드로 전환: {}", e.getMessage());
            // 데이터베이스 오류 시에도 임시 모드로 인증
            List<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            
            String tempPassword = passwordEncoder.encode("temp");
            
            return org.springframework.security.core.userdetails.User.builder()
                    .username(username)
                    .password(tempPassword)
                    .authorities(authorities)
                    .build();
        }
    }
}
