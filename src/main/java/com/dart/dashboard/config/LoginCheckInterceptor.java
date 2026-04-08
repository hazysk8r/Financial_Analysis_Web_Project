package com.dart.dashboard.config; 

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

// 모든 요청을 가로채서 세션을 검사
public class LoginCheckInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        
        // 1. 현재 접속한 사람의 세션을 확인
        HttpSession session = request.getSession();
        
        // 2. 세션에 'loginUser'가 없다면?
        if (session.getAttribute("loginUser") == null) {
            System.out.println("미인증 사용자 접근 시도 차단!");
            // 로그인 화면으로 강제로 내보냄
            response.sendRedirect("/login");
            return false; // 컨트롤러(대시보드)로 넘어가지 못하게 차단
        }
        
        // 3. 'loginUser'가 있으면 통과
        return true; 
    }
}