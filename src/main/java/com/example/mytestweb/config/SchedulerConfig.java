package com.example.mytestweb.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class SchedulerConfig {
    // Spring Boot 스케줄러 활성화
    // @Scheduled 어노테이션을 사용할 수 있도록 설정
}
