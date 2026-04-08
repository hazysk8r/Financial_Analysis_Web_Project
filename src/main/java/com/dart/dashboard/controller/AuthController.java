package com.dart.dashboard.controller; 

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    // 1. 로그인 화면 보여주기
    @GetMapping("/login")
    public String loginForm() {
        return "login"; 
    }

    // 2. 로그인 버튼 눌렀을 때 아이디/비번 검사
    @PostMapping("/login")
    public String login(@RequestParam("id") String id, 
                        @RequestParam("password") String password, 
                        HttpSession session) {
        
        // 실제로는 DB에 있는 관리자 테이블과 비교
        if ("admin".equals(id) && "1234".equals(password)) {
            // 성공하면 'loginUser'라는 이름으로 방문증(세션)을 발급
            session.setAttribute("loginUser", true);
            return "redirect:/"; // 대시보드 화면으로 이동!
        } else {
            // 실패하면 다시 로그인 화면으로 쫓아내고 에러 메시지 띄움
            return "redirect:/login?error";
        }
    }

    // 3. 로그아웃 기능 
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // 발급했던 세션을 없앰
        return "redirect:/login"; // 다시 로그인 화면으로 이동
    }
}