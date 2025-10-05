package com.example.mytestweb.service;

import com.example.mytestweb.repository.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ChatCleanupService {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    /**
     * 매일 새벽 2시에 7일 이전 채팅 메시지를 삭제
     * cron = "0 0 2 * * ?" = 매일 새벽 2시
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupOldMessages() {
        try {
            // 7일 전 날짜 계산
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(7);
            
            // 삭제될 메시지 개수 확인
            long messageCount = chatMessageRepository.countByCreatedAtBefore(cutoffDate);
            
            if (messageCount > 0) {
                // 7일 이전 메시지 삭제
                int deletedCount = chatMessageRepository.deleteByCreatedAtBefore(cutoffDate);
                
                System.out.println("=== 채팅 메시지 정리 완료 ===");
                System.out.println("삭제된 메시지 수: " + deletedCount + "개");
                System.out.println("삭제 기준 날짜: " + cutoffDate);
                System.out.println("정리 시간: " + LocalDateTime.now());
            } else {
                System.out.println("삭제할 오래된 채팅 메시지가 없습니다.");
            }
            
        } catch (Exception e) {
            System.err.println("채팅 메시지 정리 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 수동으로 오래된 메시지 삭제 (관리자용)
     */
    @Transactional
    public int manualCleanup(int days) {
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
            long messageCount = chatMessageRepository.countByCreatedAtBefore(cutoffDate);
            
            if (messageCount > 0) {
                int deletedCount = chatMessageRepository.deleteByCreatedAtBefore(cutoffDate);
                System.out.println("수동 정리 완료: " + deletedCount + "개 메시지 삭제 (" + days + "일 이전)");
                return deletedCount;
            } else {
                System.out.println("삭제할 메시지가 없습니다.");
                return 0;
            }
        } catch (Exception e) {
            System.err.println("수동 정리 중 오류 발생: " + e.getMessage());
            return -1;
        }
    }

    /**
     * 현재 채팅 메시지 통계 조회
     */
    public void getChatStatistics() {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime oneDayAgo = now.minusDays(1);
            LocalDateTime sevenDaysAgo = now.minusDays(7);
            LocalDateTime thirtyDaysAgo = now.minusDays(30);
            
            long totalMessages = chatMessageRepository.count();
            long messagesLast24Hours = chatMessageRepository.countByCreatedAtBefore(oneDayAgo);
            long messagesLast7Days = chatMessageRepository.countByCreatedAtBefore(sevenDaysAgo);
            long messagesLast30Days = chatMessageRepository.countByCreatedAtBefore(thirtyDaysAgo);
            
            System.out.println("=== 채팅 메시지 통계 ===");
            System.out.println("전체 메시지 수: " + totalMessages + "개");
            System.out.println("최근 24시간: " + (totalMessages - messagesLast24Hours) + "개");
            System.out.println("최근 7일: " + (totalMessages - messagesLast7Days) + "개");
            System.out.println("최근 30일: " + (totalMessages - messagesLast30Days) + "개");
            System.out.println("30일 이전: " + messagesLast30Days + "개");
            
        } catch (Exception e) {
            System.err.println("통계 조회 중 오류 발생: " + e.getMessage());
        }
    }
}
