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
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.debug("로그인 시도: {}", username);
        
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
    }
}
