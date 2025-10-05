package com.example.mytestweb.repository;

import com.example.mytestweb.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findTop50ByOrderByCreatedAtDesc();
    List<ChatMessage> findTop50ByOrderByCreatedAtAsc();
    
    // 7일 이전 메시지 삭제
    @Modifying
    @Query("DELETE FROM ChatMessage c WHERE c.createdAt < :cutoffDate")
    int deleteByCreatedAtBefore(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    // 특정 날짜 이전 메시지 개수 조회
    @Query("SELECT COUNT(c) FROM ChatMessage c WHERE c.createdAt < :cutoffDate")
    long countByCreatedAtBefore(@Param("cutoffDate") LocalDateTime cutoffDate);
}
