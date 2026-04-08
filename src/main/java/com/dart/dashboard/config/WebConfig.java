package com.dart.dashboard.config; 

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration // 스프링이 켜질 때 이 설정 파일을 제일 먼저 읽음
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginCheckInterceptor())
                .addPathPatterns("/**") // 1. 웹사이트의 "모든 주소(/**)"에 interceptor를 둠
                .excludePathPatterns("/login", "/logout", "/error", "/css/**", "/js/**"); 
                // 2. 단, 로그인 화면이나 에러 화면 등은 interceptor 없이도 들어갈 수 있게 예외로 놔둠
    }
}