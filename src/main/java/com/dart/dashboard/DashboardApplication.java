package com.dart.dashboard; 

import com.dart.dashboard.service.DartApiService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DashboardApplication {

    public static void main(String[] args) {
        SpringApplication.run(DashboardApplication.class, args);
    }

    // 서버가 켜질 때 자동으로 한 번 실행되는 테스트 코드
    @Bean
    public CommandLineRunner testRunner(DartApiService dartApiService) {
        return args -> {
            System.out.println("🚀 [TEST] DART API 데이터 수집을 시작합니다...");
            
            // 삼성전자(00126380) 고유번호를 넣고 수집 명령
            dartApiService.fetchAndSaveCompany("00126380");
        };
    }
}