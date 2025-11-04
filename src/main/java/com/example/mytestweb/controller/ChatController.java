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
        
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            User user = userRepository.findByUsername(auth.getName()).orElse(null);
            if (user != null) {
                model.addAttribute("user", user);
                model.addAttribute("currentUsername", user.getUsername());
            }
        }
        
        // 최근 50개 메시지 가져오기 (오래된 것부터 최신 순으로)
        List<ChatMessage> recentMessages = chatMessageRepository.findTop50ByOrderByCreatedAtAsc();
        model.addAttribute("recentMessages", recentMessages);
        return "hello";
    }

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage) {
        // 메시지를 데이터베이스에 저장
        chatMessage.setCreatedAt(LocalDateTime.now());
        chatMessage.setMessageType(ChatMessage.MessageType.CHAT);
        chatMessageRepository.save(chatMessage);
        
        return chatMessage;
    }

    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessage addUser(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        ChatMessage responseMessage = new ChatMessage();
        responseMessage.setUsername(chatMessage.getUsername());
        responseMessage.setCreatedAt(LocalDateTime.now());
        
        if (chatMessage.getMessageType() == ChatMessage.MessageType.JOIN) {
            // WebSocket 세션에 사용자명 추가 (입장 시에만)
            headerAccessor.getSessionAttributes().put("username", chatMessage.getUsername());
            
            // 입장 메시지 생성
            responseMessage.setMessage(chatMessage.getUsername() + "님이 채팅방에 입장했습니다.");
            responseMessage.setMessageType(ChatMessage.MessageType.JOIN);
        } else if (chatMessage.getMessageType() == ChatMessage.MessageType.LEAVE) {
            // 퇴장 메시지 생성
            responseMessage.setMessage(chatMessage.getUsername() + "님이 채팅방을 떠났습니다.");
            responseMessage.setMessageType(ChatMessage.MessageType.LEAVE);
        }
        
        // 메시지를 데이터베이스에 저장
        chatMessageRepository.save(responseMessage);
        
        return responseMessage;
    }
}
