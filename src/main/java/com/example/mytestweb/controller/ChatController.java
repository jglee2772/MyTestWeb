package com.example.mytestweb.controller;

import com.example.mytestweb.entity.ChatMessage;
import com.example.mytestweb.entity.User;
import com.example.mytestweb.repository.ChatMessageRepository;
import com.example.mytestweb.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class ChatController {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/hello")
    public String chatPage(Model model) {
        // 현재 로그인한 사용자 정보 가져오기
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("=== 채팅 페이지 접근 ===");
        System.out.println("Authentication: " + auth);
        System.out.println("인증 상태: " + (auth != null && auth.isAuthenticated()));
        System.out.println("사용자명: " + (auth != null ? auth.getName() : "null"));
        
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            User user = userRepository.findByUsername(auth.getName()).orElse(null);
            if (user != null) {
                model.addAttribute("user", user);
                model.addAttribute("currentUsername", user.getUsername());
                System.out.println("✅ 로그인 사용자 정보 설정: " + user.getUsername());
            } else {
                System.out.println("❌ 사용자 정보를 찾을 수 없음");
            }
        } else {
            System.out.println("❌ 비로그인 사용자 또는 익명 사용자");
        }
        
        // 최근 50개 메시지 가져오기 (오래된 것부터 최신 순으로)
        List<ChatMessage> recentMessages = chatMessageRepository.findTop50ByOrderByCreatedAtAsc();
        model.addAttribute("recentMessages", recentMessages);
        return "hello";
    }

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage) {
        System.out.println("=== 채팅 메시지 수신 ===");
        System.out.println("사용자명: " + chatMessage.getUsername());
        System.out.println("메시지 내용: " + chatMessage.getMessage());
        
        // 메시지를 데이터베이스에 저장
        chatMessage.setCreatedAt(LocalDateTime.now());
        chatMessage.setMessageType(ChatMessage.MessageType.CHAT);
        chatMessageRepository.save(chatMessage);
        
        System.out.println("✅ 새 메시지 저장 및 전송: " + chatMessage.getUsername() + " - " + chatMessage.getMessage());
        System.out.println("=== 채팅 메시지 처리 완료 ===");
        
        return chatMessage;
    }

    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessage addUser(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        System.out.println("=== WebSocket 메시지 수신 ===");
        System.out.println("사용자명: " + chatMessage.getUsername());
        System.out.println("메시지 타입: " + chatMessage.getMessageType());
        
        ChatMessage responseMessage = new ChatMessage();
        responseMessage.setUsername(chatMessage.getUsername());
        responseMessage.setCreatedAt(LocalDateTime.now());
        
        if (chatMessage.getMessageType() == ChatMessage.MessageType.JOIN) {
            // WebSocket 세션에 사용자명 추가 (입장 시에만)
            headerAccessor.getSessionAttributes().put("username", chatMessage.getUsername());
            
            // 입장 메시지 생성
            responseMessage.setMessage(chatMessage.getUsername() + "님이 채팅방에 입장했습니다.");
            responseMessage.setMessageType(ChatMessage.MessageType.JOIN);
            System.out.println("✅ 사용자 입장: " + chatMessage.getUsername());
        } else if (chatMessage.getMessageType() == ChatMessage.MessageType.LEAVE) {
            // 퇴장 메시지 생성
            responseMessage.setMessage(chatMessage.getUsername() + "님이 채팅방을 떠났습니다.");
            responseMessage.setMessageType(ChatMessage.MessageType.LEAVE);
            System.out.println("❌ 사용자 퇴장: " + chatMessage.getUsername());
        }
        
        // 메시지를 데이터베이스에 저장
        chatMessageRepository.save(responseMessage);
        
        System.out.println("응답 메시지 전송: " + responseMessage.getMessage());
        System.out.println("=== WebSocket 메시지 처리 완료 ===");
        
        return responseMessage;
    }
}
