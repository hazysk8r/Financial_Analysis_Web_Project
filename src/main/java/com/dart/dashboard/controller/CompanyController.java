package com.dart.dashboard.controller;

import com.dart.dashboard.entity.Company;
import com.dart.dashboard.repository.CompanyRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller // "브라우저 주소창 입력을 받고 화면을 띄워줌
public class CompanyController {

    private final CompanyRepository companyRepository;

    public CompanyController(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    // 사용자가 인터넷 주소창에 'http://localhost:8080/' (기본 주소)를 치면 이 코드가 실행됨
    @GetMapping("/") 
    public String showDashboard(Model model) {
        // 1. Repository에 가서 DB에 있는 모든 기업 데이터를 가지고 옴
        List<Company> companyList = companyRepository.findAll();

        // 2. 가져온 데이터를 'companies'라는 이름표를 붙여서 HTML 화면으로 넘겨줌
        model.addAttribute("companies", companyList);

        // 3. "dashboard.html" 이라는 파일을 찾아서 브라우저에 띄움
        return "dashboard"; 
    }
}