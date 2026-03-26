package com.dart.dashboard.controller;

import com.dart.dashboard.entity.Company;
import com.dart.dashboard.repository.CompanyRepository;
import com.dart.dashboard.service.DartApiService; 
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping; 
import org.springframework.web.bind.annotation.RequestParam; 

import java.util.List;

@Controller
public class CompanyController {

    private final CompanyRepository companyRepository;
    private final DartApiService dartApiService; // 서비스 계층 API 호출 연결

    public CompanyController(CompanyRepository companyRepository, DartApiService dartApiService) {
        this.companyRepository = companyRepository;
        this.dartApiService = dartApiService;
    }

    // 1. 메인 화면 띄워주기 
    @GetMapping("/")
    public String showDashboard(Model model) {
        List<Company> companyList = companyRepository.findAll();
        model.addAttribute("companies", companyList);
        return "dashboard";
    }

    // 2. 사용자가 검색창에 번호를 치고 엔터를 눌렀을 때 실행됨
    @PostMapping("/search")
    public String searchCompany(@RequestParam("corpCode") String corpCode) {
        // 사용자가 입력한 번호(corpCode)를 DartApiService로 넘겨서 데이터 수집 및 DB 저장 실행
        dartApiService.fetchAndSaveCompany(corpCode);
        
        // 작업이 다 끝나면 다시 메인 화면("/")으로 되돌아가서 새로고침 (방금 추가된 데이터가 표에 뜨게 됨)
        return "redirect:/";
    }

    @PostMapping("/delete/{corpCode}")
    public String deleteCompany(@PathVariable("corpCode") String corpCode) {
        // 주소창에 넘어온 번호(corpCode)를 뽑아서, Repository에게 삭제하라고 명령
        // (SQL의 DELETE FROM COMPANIES WHERE corp_code = ? 와 똑같은 역할)
        companyRepository.deleteById(corpCode);
        
        // 삭제가 끝나면 다시 메인 화면으로 돌아가서 새로고침
        return "redirect:/";
    }

    @GetMapping("/download/excel")
    public void downloadExcel(jakarta.servlet.http.HttpServletResponse response) throws Exception {
        // 1. 브라우저에게 웹페이지가 아니라 다운로드할 엑셀(CSV) 파일이라는 것을 알려줌
        response.setContentType("text/csv; charset=MS949"); // MS949: 엑셀에서 한글이 깨지지 않게 해주는 인코딩
        response.setHeader("Content-Disposition", "attachment; filename=\"dart_companies.csv\"");

        // 2. 파일에 쓸 준비
        java.io.PrintWriter writer = response.getWriter();
        
        // 3. 엑셀의 첫 번째 줄(헤더)을 작성
        writer.println("DART 고유번호,기업명,주식 종목코드,매출액,영업이익");

        // 4. DB에 있는 모든 기업 데이터를 가져와서 한 줄씩 쉼표(,)로 구분해서 적음
        List<Company> companies = companyRepository.findAll();
        for (Company company : companies) {
            writer.printf("%s,%s,%s,%s,%s\n",
                    company.getCorpCode(),
                    company.getCorpName(),
                    company.getStockCode() != null ? company.getStockCode() : "비상장", // 종목코드가 없으면 '비상장'으로 표시
                    company.getRevenue(),
                    company.getOperatingProfit()
            );
        }
        
        // 5. 작성이 끝나면 파일을 닫고 전송
        writer.flush();
    }

}