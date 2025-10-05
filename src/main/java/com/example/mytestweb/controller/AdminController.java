package com.example.mytestweb.controller;

import com.example.mytestweb.entity.User;
import com.example.mytestweb.entity.UserStatus;
import com.example.mytestweb.repository.UserRepository;
import com.example.mytestweb.service.ChatCleanupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ChatCleanupService chatCleanupService;

    @GetMapping("/admin")
    public String adminPage(Model model) {
        // 관리자 권한 체크
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }
        
        String username = auth.getName();
        User currentUser = userRepository.findByUsername(username).orElse(null);
        
        if (currentUser == null || !currentUser.isAdmin()) {
            return "redirect:/";
        }
        
        // 승인 대기 중인 사용자 목록
        List<User> pendingUsers = userRepository.findByStatus(UserStatus.PENDING);
        System.out.println("승인 대기 사용자 수: " + pendingUsers.size());
        for (User user : pendingUsers) {
            System.out.println("대기 중인 사용자: " + user.getUsername() + " - " + user.getStatus());
        }
        
        // 모든 사용자 상태 확인
        List<User> allUsers = userRepository.findAll();
        System.out.println("=== 모든 사용자 상태 ===");
        for (User user : allUsers) {
            System.out.println("사용자: " + user.getUsername() + " - 상태: " + user.getStatus() + " - 관리자: " + user.isAdmin());
        }
        
        model.addAttribute("pendingUsers", pendingUsers);
        model.addAttribute("allUsers", allUsers);
        
        return "admin";
    }

    @PostMapping("/admin/approve")
    public String approveUser(@RequestParam Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            user.setStatus(UserStatus.APPROVED);
            userRepository.save(user);
        }
        return "redirect:/admin";
    }

    @PostMapping("/admin/reject")
    public String rejectUser(@RequestParam Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            user.setStatus(UserStatus.REJECTED);
            userRepository.save(user);
        }
        return "redirect:/admin";
    }

    @PostMapping("/admin/reset-password")
    public String resetUserPassword(@RequestParam Long userId,
                                    @RequestParam String newPassword) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
        }
        return "redirect:/admin";
    }

    @PostMapping("/admin/delete-user")
    public String deleteUser(@RequestParam Long userId, Model model) {
        // 현재 로그인한 관리자 정보 가져오기
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }
        
        String currentUsername = auth.getName();
        User currentUser = userRepository.findByUsername(currentUsername).orElse(null);
        
        if (currentUser == null || !currentUser.isAdmin()) {
            return "redirect:/";
        }
        
        // 삭제할 사용자 정보 가져오기
        User userToDelete = userRepository.findById(userId).orElse(null);
        if (userToDelete == null) {
            return "redirect:/admin";
        }
        
        // 관리자 본인은 삭제할 수 없도록 방지
        if (userToDelete.getId().equals(currentUser.getId())) {
            System.out.println("관리자 본인은 삭제할 수 없습니다: " + currentUsername);
            return "redirect:/admin";
        }
        
        // 사용자 삭제
        userRepository.delete(userToDelete);
        System.out.println("사용자 삭제됨: " + userToDelete.getUsername());
        
        return "redirect:/admin";
    }

    @PostMapping("/admin/cleanup-chat")
    public String cleanupChat(@RequestParam(defaultValue = "7") int days, Model model) {
        // 관리자 권한 체크
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }
        
        String username = auth.getName();
        User currentUser = userRepository.findByUsername(username).orElse(null);
        
        if (currentUser == null || !currentUser.isAdmin()) {
            return "redirect:/";
        }
        
        // 채팅 메시지 수동 정리
        int deletedCount = chatCleanupService.manualCleanup(days);
        
        if (deletedCount >= 0) {
            System.out.println("관리자가 채팅 메시지를 정리했습니다: " + deletedCount + "개 삭제 (" + days + "일 이전)");
        }
        
        return "redirect:/admin";
    }

    @PostMapping("/admin/chat-statistics")
    public String getChatStatistics(Model model) {
        // 관리자 권한 체크
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }
        
        String username = auth.getName();
        User currentUser = userRepository.findByUsername(username).orElse(null);
        
        if (currentUser == null || !currentUser.isAdmin()) {
            return "redirect:/";
        }
        
        // 채팅 통계 조회
        chatCleanupService.getChatStatistics();
        
        return "redirect:/admin";
    }
}
