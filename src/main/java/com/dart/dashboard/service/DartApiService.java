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
        String financeUrl = "https://opendart.fss.or.kr/api/fnlttSinglAcnt.json?crtfc_key=" + apiKey + "&corp_code=" + corpCode + "&bsns_year=2023&reprt_code=11011";

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

                company.setRevenue("데이터 없음");
                company.setOperatingProfit("데이터 없음");

                try {
                    String finJsonString = restTemplate.getForObject(financeUrl, String.class);
                    JsonNode finResponse = objectMapper.readTree(finJsonString);

                    if (finResponse != null && "000".equals(finResponse.get("status").asText())) {
                        JsonNode list = finResponse.get("list");
                        for (JsonNode item : list) {
                            String accountNm = item.get("account_nm").asText();
                            
                            // 1. 쉼표(,) 제거 및 양쪽 공백 제거
                            String amountStr = item.get("thstrm_amount").asText().replace(",", "").trim();

                            // 2. 빈칸이거나 하이픈(-)이 아닐 때만 계산하도록 안전장치 추가
                            if (!amountStr.isEmpty() && !amountStr.equals("-")) {
                                try {
                                    long amount = Long.parseLong(amountStr);
                                    String formattedAmount = (amount / 100000000) + "억 원";

                                    if (accountNm.contains("매출액")) {
                                        company.setRevenue(formattedAmount);
                                    } else if (accountNm.contains("영업이익")) {
                                        company.setOperatingProfit(formattedAmount);
                                    }
                                } catch (NumberFormatException e) {
                                    // 숫자로 변환할 수 없는 이상한 문자가 와도 무시하고 넘어감
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println("재무 데이터 수집 실패 (상장사가 아니거나 데이터가 없을 수 있습니다.)");
                }

                companyRepository.save(company);
                System.out.println("DB 저장 성공 (재무 데이터 포함): " + company.getCorpName());
            } else {
                System.out.println("DART API 호출 실패 또는 응답 오류");
            }
        } catch (Exception e) {
            System.out.println("에러 발생: " + e.getMessage());
            e.printStackTrace(); // 에러의 상세 원인을 콘솔에 출력
        }
    }
}