package com.dart.dashboard.scheduler; 

import com.dart.dashboard.entity.Company;
import com.dart.dashboard.repository.CompanyRepository;
import com.dart.dashboard.service.DartApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component // Spring이 알아서 관리
public class DataUpdateScheduler {

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private DartApiService dartApiService;

    // 매일 밤 12시 정각(00:00:00)에 아래 코드가 자동으로 실행됨
    // cron = "초 분 시 일 월 요일" 순서
    @Scheduled(cron = "0 0 0 * * *")
    public void autoUpdateCompaniesDaily() {
        System.out.println("[스케줄러 작동] 전체 기업 데이터 자동 업데이트를 시작합니다.");

        List<Company> companies = companyRepository.findAll();
        for (Company company : companies) {
            dartApiService.fetchAndSaveCompany(company.getCorpCode());
        }

        System.out.println("[스케줄러 완료] 모든 기업 데이터 최신화가 완료되었습니다.");
    }
}