package com.example.mytestweb.controller;

import com.example.mytestweb.entity.User;
import com.example.mytestweb.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PasswordResetController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/password-reset")
    public String passwordResetPage() {
        return "password-reset";
    }

    @PostMapping("/password-reset")
    public String resetPassword(@RequestParam String username, 
                               @RequestParam String newPassword, 
                               Model model) {
        
        User user = userRepository.findByUsername(username).orElse(null);
        
        if (user == null) {
            model.addAttribute("error", "존재하지 않는 사용자입니다.");
            return "password-reset";
        }
        
        if (user.getStatus() != com.example.mytestweb.entity.UserStatus.APPROVED) {
            model.addAttribute("error", "승인되지 않은 사용자입니다.");
            return "password-reset";
        }
        
        // 비밀번호 재설정
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        model.addAttribute("success", "비밀번호가 성공적으로 재설정되었습니다.");
        return "password-reset";
    }
}
