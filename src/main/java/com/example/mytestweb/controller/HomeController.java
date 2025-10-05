package com.example.mytestweb.controller;

import com.example.mytestweb.entity.User;
import com.example.mytestweb.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("message", "안녕하세요! MyTestWeb에 오신 것을 환영합니다!");
        addUserToModel(model);
        return "index";
    }

    // /hello 경로는 ChatController로 이동
    // @GetMapping("/hello")
    // public String hello(@RequestParam(name="name", required=false, defaultValue="World") String name, Model model) {
    //     model.addAttribute("name", name);
    //     addUserToModel(model);
    //     return "hello";
    // }

    @GetMapping("/about")
    public String about(Model model) {
        addUserToModel(model);
        return "about";
    }

    @GetMapping("/locked-lock")
    public String lockedLock(Model model) {
        addUserToModel(model);
        return "locked-lock";
    }

    private void addUserToModel(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            User user = userRepository.findByUsername(auth.getName()).orElse(null);
            model.addAttribute("user", user);
        }
    }
}
