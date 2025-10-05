package com.example.mytestweb.controller;

import com.example.mytestweb.entity.User;
import com.example.mytestweb.entity.UserStatus;
import com.example.mytestweb.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username, 
                          @RequestParam String email, 
                          @RequestParam String name,
                          @RequestParam String phone,
                          @RequestParam String password, 
                          @RequestParam String confirmPassword,
                          Model model) {
        
        // 비밀번호 확인
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "비밀번호가 일치하지 않습니다.");
            return "register";
        }
        
        // 중복 체크
        if (userRepository.existsByUsername(username)) {
            model.addAttribute("error", "이미 존재하는 사용자명입니다.");
            return "register";
        }
        
        if (userRepository.existsByEmail(email)) {
            model.addAttribute("error", "이미 존재하는 이메일입니다.");
            return "register";
        }
        
        // 새 사용자 생성 (승인 대기 상태)
        User user = new User(username, email, name, phone, passwordEncoder.encode(password));
        user.setStatus(UserStatus.PENDING);
        userRepository.save(user);
        
        System.out.println("새 사용자 등록됨: " + username + " - 상태: " + user.getStatus());
        
        model.addAttribute("success", "회원가입이 완료되었습니다. 관리자 승인 후 로그인 가능합니다.");
        return "login";
    }
}
