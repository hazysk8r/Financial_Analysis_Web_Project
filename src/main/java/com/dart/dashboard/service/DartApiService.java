package com.dart.dashboard.service;

import com.dart.dashboard.entity.Company;
import com.dart.dashboard.repository.CompanyRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper; 
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class DartApiService {

    private final CompanyRepository companyRepository;
    private final String apiKey;

    public DartApiService(CompanyRepository companyRepository, 
                          @Value("${dart.api-key}") String apiKey) {
        this.companyRepository = companyRepository;
        this.apiKey = apiKey;
    }

    public void fetchAndSaveCompany(String corpCode) {
        String url = "https://opendart.fss.or.kr/api/company.json?crtfc_key=" + apiKey + "&corp_code=" + corpCode;

        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper objectMapper = new ObjectMapper(); // 💡 텍스트를 JSON으로 수동 변환해 줄 도구
        
        try {
            // 1. 에러를 막기 위해 일단 가장 안전한 단순 텍스트(String)로 먼저 받음
            String jsonString = restTemplate.getForObject(url, String.class);

            // 2. 받아온 텍스트를 ObjectMapper를 사용해 JsonNode 객체로 수동 변환
            JsonNode response = objectMapper.readTree(jsonString);

            if (response != null && "000".equals(response.get("status").asText())) {
                Company company = new Company();
                company.setCorpCode(corpCode);
                company.setCorpName(response.get("corp_name").asText());
                
                if (response.has("stock_code") && !response.get("stock_code").isNull()) {
                    company.setStockCode(response.get("stock_code").asText());
                }

                companyRepository.save(company);
                System.out.println("DB 저장 성공: " + company.getCorpName());
            } else {
                System.out.println("DART API 호출 실패 또는 응답 오류");
            }
        } catch (Exception e) {
            System.out.println("에러 발생: " + e.getMessage());
            e.printStackTrace(); // 에러의 상세 원인을 콘솔에 출력
        }
    }
}