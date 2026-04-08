package com.dart.dashboard.controller;

import com.dart.dashboard.entity.Company;
import com.dart.dashboard.entity.DartMaster;
import com.dart.dashboard.repository.CompanyRepository;
import com.dart.dashboard.repository.DartMasterRepository;
import com.dart.dashboard.service.DartApiService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping; 
import org.springframework.web.bind.annotation.RequestParam; 
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

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
    public String dashboard(
            @RequestParam(name = "keyword", required = false, defaultValue = "") String keyword,
            @PageableDefault(size = 5) Pageable pageable, // 한 화면에 5개씩만 보여주도록 설정 (테스트용)
            org.springframework.ui.Model model) {

        Page<Company> companyPage;

        // 1. 검색어가 없으면 전체 목록을 5개씩 잘라서 가져오고, 
        //    검색어가 있으면 검색 결과만 5개씩 잘라서 가져옴
        if (keyword.isEmpty()) {
            companyPage = companyRepository.findAll(pageable);
        } else {
            companyPage = companyRepository.findByCorpNameContaining(keyword, pageable);
        }

        // 2. 기존 화면(표, 차트)이 에러 나지 않게 content만 꺼내서 예전 이름 그대로 뿌려줌
        model.addAttribute("companies", companyPage.getContent());
        
        // 3. 추후 화면에 [1] [2] [3] 버튼을 만들기 위해 페이지 정보 통째로 뿌려줌
        model.addAttribute("page", companyPage);
        model.addAttribute("keyword", keyword);

        return "dashboard";
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

    @PostMapping("/update-all")
    public String updateAllCompanies() {
        // 1. 창고(DB)에 있는 모든 기업의 명부를 가져옴
        List<Company> companies = companyRepository.findAll();
        
        // 2. 명부에 있는 기업들을 하나씩 꺼내서 API를 통해 저장합니다.
        for (Company company : companies) {
            // 이미 만들어둔 fetchAndSaveCompany 메서드를 재활용
            dartApiService.fetchAndSaveCompany(company.getCorpCode());
        }
        
        // 3. 모든 작업이 끝나면 대시보드 화면으로 새로고침
        return "redirect:/";
    }

    // 위쪽(클래스 상단)에 DartMasterService를 주입받는 코드 추가
    @Autowired
    private com.dart.dashboard.service.DartMasterService dartMasterService;

    // 이 주소로 접속하면 마스터 사전 다운로드가 시작됨
    @GetMapping("/init-master")
    @org.springframework.web.bind.annotation.ResponseBody
    public String initMasterData() {
        dartMasterService.downloadAndSaveMasterData();
        return "<h1>마스터 데이터(사전) 세팅이 완료되었습니다. 터미널 로그와 DB를 확인하세요.</h1><br><a href='/'>대시보드로 돌아가기</a>";
    }

    @Autowired
    private DartMasterRepository dartMasterRepository;

    // 종목코드 & 기업명으로 마스터 사전을 거쳐 기업을 추가
    @PostMapping("/add") 
    public String addCompany(@RequestParam("keyword") String keyword, 
                             org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        
        Optional<DartMaster> masterOpt;

        // 1. 사용자가 입력한 글자가 숫자 6자리(종목코드)인지 확인
        if (keyword.matches("\\d{6}")) {
            masterOpt = dartMasterRepository.findByStockCode(keyword);
        } else {
            // 숫자가 아니면 기업명으로 간주하고 검색
            masterOpt = dartMasterRepository.findByCorpName(keyword);
        }

        // 2. 사전에서 기업을 찾았다면
        if (masterOpt.isPresent()) {
            String realCorpCode = masterOpt.get().getCorpCode(); // DART 고유번호 획득
            dartApiService.fetchAndSaveCompany(realCorpCode); // API 호출 및 DB 저장
        } else {
            // 3. 사전에 없는 기업이면 에러 메시지를 챙겨서 돌려보냄
            redirectAttributes.addFlashAttribute("errorMessage", "상장사 마스터 사전에서 '" + keyword + "'을(를) 찾을 수 없습니다.");
        }

        return "redirect:/"; // 대시보드로 새로고침
    }

}